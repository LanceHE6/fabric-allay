package cn.hycer.allay.feature;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public class DamageIndicatorHook {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageDealt, dealt, blocked) -> {
            var attacker = source.getEntity();
            if (!(attacker instanceof ServerPlayer player)) return;
            if (!PlayerPrefs.isDamageIndicatorOn(player.getUUID())) return;
            if (dealt <= 0) return;

            boolean crit = isCrit(player);
            DamageIndicator.onDamage(entity, attacker, dealt, crit);
        });
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
