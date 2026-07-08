package cn.hycer.allay.feature.damage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Holds per-player critical-hit state between attack start and AFTER_DAMAGE event. */
public class CritTracker {
    private static final Map<UUID, Boolean> pending = new ConcurrentHashMap<>();

    public static void set(UUID playerId, boolean crit) { pending.put(playerId, crit); }
    public static boolean consume(UUID playerId) { return Boolean.TRUE.equals(pending.remove(playerId)); }
}
