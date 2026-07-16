package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerGameMode.class, remap = false)
public class FragileObsidianMixin {

    @Shadow @Final protected ServerPlayer player;
    @Shadow @Final protected ServerLevel level;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true, remap = false)
    private void instantBreak(BlockPos pos, ServerboundPlayerActionPacket.Action action,
                              Direction direction, int maxBuildHeight, int sequence, CallbackInfo ci) {
        if (action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return;
        if (player.getAbilities().instabuild) return;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        var fm = FeatureManager.getInstance();

        boolean fragileObsidian = fm.isFragileObsidian()
                && (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN);
        boolean fragileGlass = fm.isFragileGlass()
                && state.getSoundType() == SoundType.GLASS;

        if (!fragileObsidian && !fragileGlass) return;
        if (!player.hasCorrectToolForDrops(state)) return;

        level.destroyBlock(pos, true, player);
        ci.cancel();
    }
}
