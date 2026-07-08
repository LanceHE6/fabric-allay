package cn.hycer.allay.feature.damage;

import cn.hycer.allay.feature.PlayerPrefs;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class DamageIndicatorHook {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageDealt, dealt, blocked) -> {
            var attacker = source.getEntity();
            if (!(attacker instanceof ServerPlayer player)) return;
            if (!PlayerPrefs.isDamageIndicatorOn(player.getUUID())) return;
            if (dealt <= 0) return;

            boolean crit = CritTracker.consume(player.getUUID());
            DamageIndicator.onPlayerDamage(entity, player, dealt, crit);
        });
    }
}
