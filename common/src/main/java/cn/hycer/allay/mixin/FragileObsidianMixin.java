package cn.hycer.allay.mixin;

import cn.hycer.allay.Allay;
import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class FragileObsidianMixin {

    private static boolean debugLogged;

    @Shadow
    public abstract Block getBlock();

    private boolean isTargetBlock() {
        Block block = getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN;
    }

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void overrideDestroySpeed(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (!debugLogged) {
            Allay.LOGGER.info("[FragileObsidian] Mixin active, fragileObsidian={}",
                    FeatureManager.getInstance().isFragileObsidian());
            debugLogged = true;
        }
        if (!FeatureManager.getInstance().isFragileObsidian()) return;
        if (isTargetBlock()) {
            cir.setReturnValue(Blocks.STONE.defaultBlockState().getDestroySpeed(level, pos));
        }
    }

    @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
    private void overrideDestroyProgress(Player player, BlockGetter level, BlockPos pos,
                                         CallbackInfoReturnable<Float> cir) {
        if (!FeatureManager.getInstance().isFragileObsidian()) return;
        if (isTargetBlock()) {
            float progress = Blocks.STONE.defaultBlockState().getDestroyProgress(player, level, pos);
            cir.setReturnValue(progress);
        }
    }
}
