package cn.hycer.allay.asb;

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
 * Per-player scoreboard visibility preferences.
 * Stored in config/allay/scoreboard_prefs.json
 */
public class PlayerScoreboardPrefs {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "allay/scoreboard_prefs.json";
    private static final Map<String, Set<String>> prefs = new LinkedHashMap<>();
    private static Path filePath;

    public static void load() {
        filePath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.exists(filePath)) {
            try (Reader r = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                Map<String, Set<String>> loaded = GSON.fromJson(r,
                        new TypeToken<Map<String, Set<String>>>(){}.getType());
                if (loaded != null) prefs.putAll(loaded);
            } catch (IOException ignored) {}
        }
    }

    private static void save() {
        try {
            Files.createDirectories(filePath.getParent());
            try (Writer w = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                GSON.toJson(prefs, w);
            }
        } catch (IOException ignored) {}
    }

    public static boolean isHidden(UUID uuid, String internalName) {
        Set<String> set = prefs.get(uuid.toString());
        return set != null && set.contains(internalName);
    }

    /** Toggle and persist. Returns true if now hidden. */
    public static boolean toggle(UUID uuid, String internalName) {
        Set<String> set = prefs.computeIfAbsent(uuid.toString(), k -> new LinkedHashSet<>());
        if (set.contains(internalName)) {
            set.remove(internalName);
            save();
            return false;
        }
        set.add(internalName);
        save();
        return true;
    }
}
