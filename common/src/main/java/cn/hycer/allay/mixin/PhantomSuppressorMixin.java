package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 幻翼抑制器：MC 26.x(1.21.4+) 重构后幻翼生成不再走 PhantomSpawnData.checkSpawnRules，
 * 而是全部内联在 PhantomSpawner.tick 里。直接在 tick 头部短路即可阻止幻翼自然生成。
 */
@Mixin(targets = "net/minecraft/world/level/levelgen/PhantomSpawner", remap = false)
public class PhantomSuppressorMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void suppressSpawning(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
        if (FeatureManager.getInstance().isPhantomSuppressor()) {
            ci.cancel();
        }
    }
}