package cn.hycer.allay.feature.damage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe storage for pending damage values captured by
 * {@code ActualDamageTracker} mixin. Kept separate from the mixin
 * package to avoid Mixin's restriction against non-mixin classes
 * in a declared mixin package.
 */
public final class ActualDamageStorage {

    private static final Map<Integer, Float> PENDING = new ConcurrentHashMap<>();

    private ActualDamageStorage() {}

    public static void put(int entityId, float damage) {
        PENDING.put(entityId, damage);
    }

    public static Float consume(int entityId) {
        return PENDING.remove(entityId);
    }
}
