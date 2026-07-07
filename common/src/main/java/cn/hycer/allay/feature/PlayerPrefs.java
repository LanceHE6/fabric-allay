package cn.hycer.allay.feature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Per-player preferences, persisted to config/allay/player_prefs.json
 */
public class PlayerPrefs {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "allay/player_prefs.json";
    private static Path filePath;

    // ── Fields ───────────────────────────────────────────────

    /** Per-player hidden scoreboards: uuid -> set of internalName */
    private static final Map<String, Set<String>> scoreboardHidden = new LinkedHashMap<>();

    /** Per-player damage indicator toggle: uuid -> boolean */
    private static final Map<String, Boolean> damageIndicator = new LinkedHashMap<>();

    // ── Load / Save ─────────────────────────────────────────

    public static void load() {
        filePath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.exists(filePath)) {
            try (Reader r = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                Map<String, Object> data = GSON.fromJson(r,
                        new TypeToken<Map<String, Object>>(){}.getType());
                if (data == null) return;

                @SuppressWarnings("unchecked")
                Map<String, Set<String>> sb = (Map<String, Set<String>>) (Object)
                        data.getOrDefault("scoreboardHidden", new LinkedHashMap<>());
                if (sb != null) scoreboardHidden.putAll(sb);

                @SuppressWarnings("unchecked")
                Map<String, Boolean> di = (Map<String, Boolean>) (Object)
                        data.getOrDefault("damageIndicator", new LinkedHashMap<>());
                if (di != null) damageIndicator.putAll(di);
            } catch (IOException ignored) {}
        }
    }

    private static void save() {
        try {
            Files.createDirectories(filePath.getParent());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("scoreboardHidden", scoreboardHidden);
            data.put("damageIndicator", damageIndicator);
            try (Writer w = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                GSON.toJson(data, w);
            }
        } catch (IOException ignored) {}
    }

    // ── Scoreboard hidden ───────────────────────────────────

    public static boolean isScoreboardHidden(UUID uuid, String internalName) {
        Set<String> set = scoreboardHidden.get(uuid.toString());
        return set != null && set.contains(internalName);
    }

    public static boolean toggleScoreboardHidden(UUID uuid, String internalName) {
        Set<String> set = scoreboardHidden.computeIfAbsent(uuid.toString(), k -> new LinkedHashSet<>());
        if (set.contains(internalName)) {
            set.remove(internalName);
            save();
            return false;
        }
        set.add(internalName);
        save();
        return true;
    }

    // ── Damage indicator ────────────────────────────────────

    public static boolean isDamageIndicatorOn(UUID uuid) {
        return damageIndicator.getOrDefault(uuid.toString(), false);
    }

    public static void setDamageIndicator(UUID uuid, boolean on) {
        damageIndicator.put(uuid.toString(), on);
        save();
    }
}
