package cn.hycer.allay.feature.damage;

import cn.hycer.allay.feature.PlayerPrefs;
import cn.hycer.allay.mixin.DisplayInvoker;
import cn.hycer.allay.mixin.TextDisplayAccessor;
import cn.hycer.allay.mixin.TextDisplayInvoker;
import com.mojang.math.Transformation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class DamageIndicator {

    private static final int DURATION_TICKS = 60;
    private static final int MERGE_WINDOW_TICKS = 12;

    private static final float SCALE_MIN = 1.2f;
    private static final float SCALE_MAX = 2.5f;
    private static final float SCALE_PER_DAMAGE = 0.08f;

    private static final String TAG = "allay_dmg";

    private static final Random RANDOM = new Random();
    private static final Quaternionf IDENTITY_ROTATION = new Quaternionf(0, 0, 0, 1);

    private static final List<TimedDisplay> displays = new ArrayList<>();
    private static ServerLevel overworld;
    private static final Map<Long, MergeState> mergeStates = new HashMap<>();

    private static final EntityDataAccessor<Byte> STYLE_FLAGS_ID = TextDisplayAccessor.getStyleFlagsId();

    private static class TimedDisplay {
        final int entityId;
        int ticks;
        TimedDisplay(int entityId) { this.entityId = entityId; this.ticks = DURATION_TICKS; }
    }

    private static class MergeState {
        float totalDamage;
        int displayEntityId;
        int ticksRemaining;
    }

    public static void init(MinecraftServer server) {
        overworld = server.overworld();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity.entityTags().contains(TAG)) entity.discard();
            }
        }
    }

    public static void onPlayerDamage(LivingEntity target, Player attacker, float dealt, boolean crit) {
        if (!(attacker instanceof ServerPlayer sp)) return;
        if (!PlayerPrefs.isDamageIndicatorOn(sp.getUUID())) return;

        ServerLevel level = (ServerLevel) target.level();
        long key = mergeKey(sp.getUUID(), target.getId());
        MergeState state = mergeStates.get(key);

        if (state != null && state.ticksRemaining > DURATION_TICKS - MERGE_WINDOW_TICKS) {
            state.totalDamage += dealt;
            state.ticksRemaining = DURATION_TICKS;
            updateDisplay(level, state.displayEntityId, state.totalDamage, crit);
        } else {
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
            display.addTag(TAG);
            applyStyle(display);
            applyScale(display, dealt);

            level.addFreshEntity(display);

            MergeState newState = new MergeState();
            newState.totalDamage = dealt;
            newState.displayEntityId = display.getId();
            newState.ticksRemaining = DURATION_TICKS;
            mergeStates.put(key, newState);
            displays.add(new TimedDisplay(display.getId()));
        }
    }

    private static void updateDisplay(ServerLevel level, int entityId, float damage, boolean crit) {
        if (overworld == null) return;
        var entity = overworld.getEntity(entityId);
        if (entity instanceof Display.TextDisplay display) {
            display.setText(formatDamage(damage, crit));
            applyScale(display, damage);
        }
    }

    private static void applyStyle(Display.TextDisplay display) {
        // Make background fully transparent (default is 25% black = 0x40000000)
        ((TextDisplayInvoker) display).invokeSetBackgroundColor(0);

        // Ensure text shadow flag is cleared
        byte flags = display.getEntityData().get(STYLE_FLAGS_ID);
        flags &= ~Display.TextDisplay.FLAG_SHADOW;
        display.getEntityData().set(STYLE_FLAGS_ID, flags);

        // Disable entity ground shadow
        DisplayInvoker di = (DisplayInvoker) display;
        di.invokeSetShadowRadius(0);
        di.invokeSetShadowStrength(0);
    }

    private static void applyScale(Display display, float damage) {
        float s = Math.min(SCALE_MAX, SCALE_MIN + damage * SCALE_PER_DAMAGE);
        Vector3f scale = new Vector3f(s, s, s);
        Transformation transform = new Transformation(
                new Vector3f(), IDENTITY_ROTATION, scale, IDENTITY_ROTATION);
        ((DisplayInvoker) display).invokeSetTransformation(transform);
    }

    private static Component formatDamage(double damage, boolean crit) {
        String text = String.format(Locale.ENGLISH, "%.1f", damage);
        String color = crit ? "§4§l*" : "§c";
        return Component.literal(color + text);
    }

    public static void tick() {
        Iterator<TimedDisplay> it = displays.iterator();
        while (it.hasNext()) {
            TimedDisplay td = it.next();
            td.ticks--;
            if (td.ticks <= 0) {
                if (overworld != null) {
                    var entity = overworld.getEntity(td.entityId);
                    if (entity != null) entity.discard();
                }
                it.remove();
            }
        }
        Iterator<Map.Entry<Long, MergeState>> mit = mergeStates.entrySet().iterator();
        while (mit.hasNext()) {
            if (mit.next().getValue().ticksRemaining-- <= 0) mit.remove();
        }
    }

    private static long mergeKey(UUID playerId, int targetId) {
        return ((long) playerId.hashCode() << 32) | (targetId & 0xFFFFFFFFL);
    }
}
