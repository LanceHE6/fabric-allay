package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 灾厄巡逻队抑制器：
 * 在 PatrolSpawner.tick 头部判断特性开关，若开启则直接拦截，阻止灾厄巡逻队自然生成。
 */
@Mixin(targets = "net/minecraft/world/level/levelgen/PatrolSpawner", remap = false)
public class PatrolSuppressorMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void suppressSpawning(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
        if (FeatureManager.getInstance().isPatrolSuppressor()) {
            ci.cancel();
        }
    }
}
