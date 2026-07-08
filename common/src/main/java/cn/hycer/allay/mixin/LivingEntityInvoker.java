package cn.hycer.allay.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes protected damage-reduction methods from {@link LivingEntity}
 * so they can be called from other mixins.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {

    @Invoker("getDamageAfterArmorAbsorb")
    float invokeGetDamageAfterArmorAbsorb(DamageSource source, float amount);

    @Invoker("getDamageAfterMagicAbsorb")
    float invokeGetDamageAfterMagicAbsorb(DamageSource source, float amount);
}
