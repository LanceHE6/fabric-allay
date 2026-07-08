package cn.hycer.allay.feature.damage;

import cn.hycer.allay.feature.PlayerPrefs;
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

/**
 * Floating damage numbers. Each hit creates an independent TextDisplay
 * that lasts exactly 3 seconds, no refresh on subsequent hits.
 */
public class DamageIndicator {

    private static final int DURATION_TICKS = 60; // 3s
    private static final Random RANDOM = new Random();

    /** Active displays to tick — list of (displayId, ticksRemaining) */
    private static final List<ActiveDisplay> displays = new ArrayList<>();
    private static ServerLevel overworld;

    private static class ActiveDisplay {
        final int displayId;
        int ticks;

        ActiveDisplay(int id) {
            this.displayId = id;
            this.ticks = DURATION_TICKS;
        }
    }

    public static void init(MinecraftServer server) {
        overworld = server.overworld();
    }

    /**
     * Called when a player damages an entity.  Each call creates a NEW display.
     */
    public static void onPlayerDamage(LivingEntity target, Player attacker, float dealt, boolean crit) {
        if (!(attacker instanceof ServerPlayer sp)) return;
        if (!PlayerPrefs.isDamageIndicatorOn(sp.getUUID())) return;

        ServerLevel level = (ServerLevel) target.level();
        Vec3 pos = target.position().add(
                (RANDOM.nextDouble() - 0.5) * 1.2,
                target.getBbHeight() + 0.5,
                (RANDOM.nextDouble() - 0.5) * 1.2);

        Component text = formatDamage(dealt, crit);
        Display.TextDisplay display = new Display.TextDisplay(
                BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse("minecraft:text_display"))
                        .map(net.minecraft.core.Holder.Reference::value).orElseThrow(),
                level);
        display.setPos(pos);
        display.setText(text);
        display.setBillboardConstraints(Display.BillboardConstraints.CENTER);

        level.addFreshEntity(display);
        displays.add(new ActiveDisplay(display.getId()));
    }

    private static Component formatDamage(double damage, boolean crit) {
        String text = String.format(java.util.Locale.ENGLISH, "%.1f", damage);
        String color = crit ? "§4§l*" : "§c";
        return Component.literal(color + text);
    }

    public static void tick() {
        Iterator<ActiveDisplay> it = displays.iterator();
        while (it.hasNext()) {
            ActiveDisplay ad = it.next();
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
