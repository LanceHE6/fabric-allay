package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.DamageIndicator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class DamageIndicatorMixin {

    @Inject(method = "hurt", at = @At("HEAD"))
    private void onHurt(ServerLevel level, DamageSource source, float amount, CallbackInfo ci) {
        if (!(amount > 0)) return;
        var attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer player)) return;

        boolean crit = isCrit(player);
        DamageIndicator.onPlayerDamage((LivingEntity) (Object) this, player, amount, crit);
    }

    private static boolean isCrit(ServerPlayer player) {
        return !player.onGround()
                && player.fallDistance > 0.0F
                && !player.isInWater()
                && !player.isPassenger()
                && !player.onClimbable()
                && !player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                && player.getAttackStrengthScale(0.5F) > 0.848F;
    }
}
