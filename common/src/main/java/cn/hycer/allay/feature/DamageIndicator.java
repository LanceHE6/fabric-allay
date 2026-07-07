package cn.hycer.allay.feature;

import cn.hycer.allay.Allay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class DamageIndicator {

    private static final int DURATION_TICKS = 60;
    private static final Random RANDOM = new Random();

    private static final List<ActiveDisplay> active = new ArrayList<>();

    private static class ActiveDisplay {
        final int displayId;
        final ServerLevel level;
        int ticks;

        ActiveDisplay(int displayId, ServerLevel level) {
            this.displayId = displayId;
            this.level = level;
            this.ticks = DURATION_TICKS;
        }
    }

    public static void init(MinecraftServer server) {
    }

    public static void onDamage(LivingEntity target, Entity attacker, float damage, boolean crit) {
        if (damage <= 0) return;

        ServerLevel level = (ServerLevel) target.level();
        double x = target.getX() + (RANDOM.nextDouble() - 0.5) * 1.2;
        double y = target.getY() + target.getBbHeight() + 0.5;
        double z = target.getZ() + (RANDOM.nextDouble() - 0.5) * 1.2;

        try {
            Display.TextDisplay display = new Display.TextDisplay(
                    BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse("minecraft:text_display"))
                            .map(net.minecraft.core.Holder.Reference::value).orElseThrow(),
                    level);
            display.setPos(x, y, z);
            display.setText(formatDamage(damage, crit));
            display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
            display.setTransformation(crit ? CRIT_TRANSFORM : NORMAL_TRANSFORM);

            level.addFreshEntity(display);
            active.add(new ActiveDisplay(display.getId(), level));
        } catch (Exception e) {
            Allay.LOGGER.error("[DamageIndicator] failed to spawn display", e);
        }
    }

    private static Component formatDamage(double damage, boolean crit) {
        String text = String.format(Locale.ENGLISH, "%.1f", damage);
        return Component.literal(crit ? "§4§l" + text : "§c" + text);
    }

    private static final Transformation NORMAL_TRANSFORM = new Transformation(
            new Vector3f(), new Quaternionf(), new Vector3f(1f, 1f, 1f), new Quaternionf());
    private static final Transformation CRIT_TRANSFORM = new Transformation(
            new Vector3f(), new Quaternionf(), new Vector3f(1.5f, 1.5f, 1f), new Quaternionf());

    public static void tick() {
        Iterator<ActiveDisplay> it = active.iterator();
        while (it.hasNext()) {
            ActiveDisplay ad = it.next();
            ad.ticks--;
            if (ad.ticks <= 0) {
                var entity = ad.level.getEntity(ad.displayId);
                if (entity != null) entity.discard();
                it.remove();
            }
        }
    }

    public static void clearAll() {
        for (var ad : active) {
            var entity = ad.level.getEntity(ad.displayId);
            if (entity != null) entity.discard();
        }
        active.clear();
    }
}
