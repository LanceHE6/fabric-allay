package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.damage.ActualDamageStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin that captures the actual damage amount inside
 * {@link LivingEntity#actuallyHurt} after armor and enchantment reductions
 * but BEFORE absorption and health cap.
 */
@Mixin(LivingEntity.class)
public abstract class ActualDamageTracker {

    /**
     * Inject after {@code getDamageAfterMagicAbsorb} to capture the damage
     * value after both armor and enchantment/effect reductions.
     */
    @Inject(
        method = "actuallyHurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F",
            shift = At.Shift.AFTER
        )
    )
    private void captureActualDamage(ServerLevel level, DamageSource source, float amount, CallbackInfo ci) {
        ActualDamageStorage.put(((LivingEntity) (Object) this).getId(), amount);
    }
}
