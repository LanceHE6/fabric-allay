package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/world/entity/monster/Phantom$PhantomSpawnData", remap = false)
public class PhantomSuppressorMixin {

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true, remap = false)
    private static void suppressSpawning(ServerLevel level, CallbackInfoReturnable<Boolean> cir) {
        if (FeatureManager.getInstance().isPhantomSuppressor()) {
            cir.setReturnValue(false);
        }
    }
}
