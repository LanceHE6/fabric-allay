package cn.hycer.allay.feature;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class DamageIndicator {

    private static final int DURATION_TICKS = 60;
    private static final Random RANDOM = new Random();

    private static final Map<Integer, ActiveDisplay> displays = new HashMap<>();
    private static ServerLevel overworld;

    private static class ActiveDisplay {
        final int displayId;
        int ticks;
        double totalDamage;
        boolean isCrit; // tracks if the display was created with a crit

        ActiveDisplay(int id, double dmg, boolean crit) {
            this.displayId = id;
            this.totalDamage = dmg;
            this.isCrit = crit;
            this.ticks = DURATION_TICKS;
        }
    }

    public static void init(MinecraftServer server) {
        overworld = server.overworld();
    }

    public static void onPlayerDamage(LivingEntity target, Player attacker, float damage, boolean crit) {
        if (!(attacker instanceof ServerPlayer sp)) return;
        if (!PlayerPrefs.isDamageIndicatorOn(sp.getUUID())) return;

        ServerLevel level = (ServerLevel) target.level();
        int entityId = target.getId();

        ActiveDisplay existing = displays.get(entityId);
        if (existing != null) {
            existing.totalDamage += damage;
            existing.isCrit = existing.isCrit || crit; // preserve crit if any hit was crit
            existing.ticks = DURATION_TICKS;
            updateDisplay(level, existing.displayId, formatDamage(existing.totalDamage, existing.isCrit));
        } else {
            Component text = formatDamage(damage, crit);
            Vec3 pos = target.position().add(
                    (RANDOM.nextDouble() - 0.5) * 1.2,
                    target.getBbHeight() + 0.5,
                    (RANDOM.nextDouble() - 0.5) * 1.2);

            Display.TextDisplay display = new Display.TextDisplay(
                    BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse("minecraft:text_display"))
                            .map(net.minecraft.core.Holder.Reference::value).orElseThrow(),
                    level);
            display.setPos(pos);
            display.setText(text);
            display.setBillboardConstraints(Display.BillboardConstraints.CENTER);

            level.addFreshEntity(display);
            displays.put(entityId, new ActiveDisplay(display.getId(), damage, crit));
        }
    }

    private static void updateDisplay(ServerLevel level, int displayId, Component text) {
        var entity = level.getEntity(displayId);
        if (entity instanceof Display.TextDisplay td) {
            td.setText(text);
        }
    }

    private static Component formatDamage(double damage, boolean crit) {
        String text = String.format(java.util.Locale.ENGLISH, "%.1f", damage);
        String color = crit ? "§4§l" : "§c";
        return Component.literal(color + text);
    }

    public static void tick() {
        Iterator<Map.Entry<Integer, ActiveDisplay>> it = displays.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            ActiveDisplay ad = e.getValue();
            ad.ticks--;
            if (ad.ticks <= 0) {
                if (overworld != null) {
                    var entity = overworld.getEntity(ad.displayId);
                    if (entity != null) entity.discard();
                }
                it.remove();
            }
        }
    }
}
