package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes obsidian / crying obsidian mine at stone-equivalent speed when the
 * fragile-obsidian feature is enabled.  Still requires the correct tool
 * tier (diamond pickaxe or better) to harvest.
 */
@Mixin(ServerPlayerGameMode.class)
public class FragileObsidianMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    @Final
    protected ServerLevel level;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
    private void instantBreakObsidian(BlockPos pos, ServerboundPlayerActionPacket.Action action,
                                       net.minecraft.core.Direction direction, int maxBuildHeight,
                                       int sequence, CallbackInfo ci) {
        if (!FeatureManager.getInstance().isFragileObsidian()) return;
        if (action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block != Blocks.OBSIDIAN && block != Blocks.CRYING_OBSIDIAN) return;

        // Let creative mode use its own instant-break logic
        if (player.getAbilities().instabuild) return;

        // Still require the correct tool tier (diamond pickaxe+)
        if (!player.hasCorrectToolForDrops(state)) return;

        level.destroyBlock(pos, true, player);
        ci.cancel();
    }
}
