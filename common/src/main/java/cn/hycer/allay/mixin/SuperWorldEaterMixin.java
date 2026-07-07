package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class SuperWorldEaterMixin {

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "getExplosionResistance", at = @At("HEAD"), cancellable = true)
    private void overrideExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion,
                                             CallbackInfoReturnable<Float> cir) {
        if (!FeatureManager.getInstance().isSuperTNT()) return;

        Block block = getBlock();
        if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN
                || block == Blocks.SPAWNER) {
            cir.setReturnValue(6.0F); // stone-level resistance
        }
    }
}
