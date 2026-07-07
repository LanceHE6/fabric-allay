package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class SuperWorldEaterMixin {

    @Inject(method = "getExplosionResistance", at = @At("HEAD"), cancellable = true)
    private void overrideExplosionResistance(CallbackInfoReturnable<Float> cir) {
        if (!FeatureManager.getInstance().isSuperTNT()) return;

        Block self = (Block) (Object) this;
        if (self == Blocks.OBSIDIAN || self == Blocks.CRYING_OBSIDIAN
                || self == Blocks.SPAWNER) {
            cir.setReturnValue(6.0F);
        }
    }
}
