package cn.hycer.allay.feature.damage;

import cn.hycer.allay.feature.PlayerPrefs;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DamageIndicatorHook {

    /**
     * Tracks the effective health (health + absorption) of each entity
     * right before damage is applied, so we can compute the actual damage
     * for non-lethal hits (where AFTER_DAMAGE fires).
     * Also serves as a signal that a tracked player damaged this entity.
     */
    private static final Map<Integer, Float> healthBefore = new ConcurrentHashMap<>();

    public static void register() {
        // Capture effective health BEFORE damage, and verify the attacker is
        // a tracked player. This runs for all damage (lethal + non-lethal).
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(source.getEntity() instanceof ServerPlayer player)) return true;
            if (!PlayerPrefs.isDamageIndicatorOn(player.getUUID())) return true;
            healthBefore.put(entity.getId(), entity.getHealth() + entity.getAbsorptionAmount());
            return true;
        });

        // AFTER_DAMAGE: for non-lethal hits.
        // Fabric API skips this event when the entity dies, so killing blows
        // are handled separately in AFTER_DEATH.
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageDealt, dealt, blocked) -> {
            var attacker = source.getEntity();
            if (!(attacker instanceof ServerPlayer player)) return;
            if (!PlayerPrefs.isDamageIndicatorOn(player.getUUID())) return;

            Float before = healthBefore.remove(entity.getId());
            if (before == null) return;

            float effectiveAfter = entity.getHealth() + entity.getAbsorptionAmount();
            float actualDamage = before - effectiveAfter;

            if (actualDamage <= 0) return;

            // Consume the tracker value too (we don't need it for non-lethal)
            ActualDamageStorage.consume(entity.getId());

            boolean crit = CritTracker.consume(player.getUUID());
            DamageIndicator.onPlayerDamage(entity, player, actualDamage, crit);
        });

        // AFTER_DEATH: for killing blows.
        // Uses ActualDamageStorage to get the full weapon damage after
        // armor/enchantment reductions, NOT capped by the entity's remaining HP.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            var attacker = source.getEntity();
            if (!(attacker instanceof ServerPlayer player)) return;
            if (!PlayerPrefs.isDamageIndicatorOn(player.getUUID())) return;

            // Verify that our tracked player actually damaged this entity
            if (healthBefore.remove(entity.getId()) == null) return;

            // Get the uncapped damage from the mixin tracker
            Float actualDamage = ActualDamageStorage.consume(entity.getId());
            if (actualDamage == null || actualDamage <= 0) return;

            boolean crit = CritTracker.consume(player.getUUID());
            DamageIndicator.onPlayerDamage(entity, player, actualDamage, crit);
        });
    }
}
