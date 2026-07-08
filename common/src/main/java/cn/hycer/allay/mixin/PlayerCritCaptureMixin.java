package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.damage.CritTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, remap = false)
public class PlayerCritCaptureMixin {

    @Shadow
    private native boolean canCriticalAttack(Entity target);

    @Inject(method = "attack", at = @At("HEAD"), remap = false)
    private void captureCrit(Entity target, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer sp) {
            CritTracker.set(sp.getUUID(), canCriticalAttack(target));
        }
    }
}
