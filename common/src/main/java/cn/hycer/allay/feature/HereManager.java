package cn.hycer.allay.feature;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class HereManager {

    private static final int DURATION_TICKS = 200;
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
    private static final Map<UUID, HereSession> sessions = new HashMap<>();

    private static class HereSession {
        final ServerPlayer source;
        final ResourceKey<Level> dimension;
        final UUID targetUuid;
        int ticks;

        HereSession(ServerPlayer source, UUID targetUuid) {
            this.source = source;
            this.dimension = source.level().dimension();
            this.targetUuid = targetUuid;
            this.ticks = DURATION_TICKS;
        }

        ServerPlayer getTarget() {
            if (targetUuid == null) return null;
            return ((ServerLevel) source.level()).getServer().getPlayerList().getPlayer(targetUuid);
        }
    }

    public static void execute(ServerPlayer player, ServerPlayer target) {
        sessions.remove(player.getUUID());
        var session = new HereSession(player, target != null ? target.getUUID() : null);
        sessions.put(player.getUUID(), session);

        // Glowing
        var opt = BuiltInRegistries.MOB_EFFECT.get(Identifier.tryParse("glowing"));
        if (opt.isEmpty()) return;
        player.addEffect(new MobEffectInstance(opt.get(), DURATION_TICKS, 0, false, false));

        // Broadcast chat
        String dim = dimName(player.level().dimension());
        String msg = "§e[" + player.getScoreboardName() + "] §f在 " + dim
                + " §7(" + (int)player.getX() + ", " + (int)player.getY() + ", " + (int)player.getZ() + ")";
        MinecraftServer srv = ((ServerLevel)player.level()).getServer();
        if (target != null) {
            target.sendSystemMessage(Component.literal(msg));
        } else {
            for (ServerPlayer p : srv.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal(msg));
            }
        }
    }

    public static void tick() {
        var it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            HereSession s = e.getValue();
            if (--s.ticks <= 0) { it.remove(); continue; }

            ServerPlayer src = s.source;
            Vec3 from = src.position();
            String label = "§e" + src.getScoreboardName() + "§f";
            int secs = s.ticks / 20;
            MinecraftServer srv = ((ServerLevel)src.level()).getServer();

            if (s.targetUuid != null) {
                // Only show direction to the specified target
                ServerPlayer target = srv.getPlayerList().getPlayer(s.targetUuid);
                if (target != null) {
                    sendArrow(target, from, label, secs);
                }
            } else {
                // Show direction to all same-dimension players (except self)
                for (ServerPlayer v : srv.getPlayerList().getPlayers()) {
                    if (v.getUUID().equals(src.getUUID())) continue;
                    if (!v.level().dimension().equals(s.dimension)) continue;
                    sendArrow(v, from, label, secs);
                }
            }
        }
    }

    private static void sendArrow(ServerPlayer viewer, Vec3 from, String label, int secs) {
        Vec3 d = from.subtract(viewer.position());
        double dist = d.length();
        if (dist < 0.5) return;

        double yaw = Math.toDegrees(Math.atan2(-d.x, d.z));
        double vYaw = viewer.getYRot() % 360;
        if (vYaw < 0) vYaw += 360;
        int idx = (int)Math.round(((yaw - vYaw + 360) % 360) / 45.0) % 8;
        if (idx < 0) idx += 8;

        String ds = dist < 10 ? String.format("%.0fm", dist) : String.format("%.0fm", dist);
        viewer.sendSystemMessage(Component.literal(
                "  " + ARROWS[idx] + " " + ds + "  " + label + "  §7(" + secs + "s)"), true);
    }

    private static String dimName(ResourceKey<Level> key) {
        String s = key.toString();
        if (s.contains("overworld")) return "主世界";
        if (s.contains("the_nether")) return "下界";
        if (s.contains("the_end")) return "末地";
        return s;
    }
}
