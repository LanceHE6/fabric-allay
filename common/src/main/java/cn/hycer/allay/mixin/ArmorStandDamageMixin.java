package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.PlayerPrefs;
import cn.hycer.allay.feature.damage.CritTracker;
import cn.hycer.allay.feature.damage.DamageIndicator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link ArmorStand} overrides {@code hurtServer} without calling super,
 * so normal Fabric API damage events never fire.
 * This mixin triggers the damage indicator display, computing actual
 * damage after armor and enchantment reductions.
 */
@Mixin(ArmorStand.class)
public abstract class ArmorStandDamageMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void onHurt(ServerLevel level, DamageSource source, float amount,
                         CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        if (!PlayerPrefs.isDamageIndicatorOn(player.getUUID())) return;
        if (amount <= 0) return;

        // Compute actual damage after armor and enchantment/effect reductions
        LivingEntity self = (LivingEntity) (Object) this;
        float afterArmor = ((LivingEntityInvoker) this).invokeGetDamageAfterArmorAbsorb(source, amount);
        float afterMagic = ((LivingEntityInvoker) this).invokeGetDamageAfterMagicAbsorb(source, afterArmor);

        boolean crit = CritTracker.consume(player.getUUID());
        DamageIndicator.onPlayerDamage(self, player, afterMagic, crit);
    }
}
