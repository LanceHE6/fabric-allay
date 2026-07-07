package cn.hycer.allay.config;

import cn.hycer.allay.asb.config.ScoreboardItem;
import cn.hycer.allay.cbm.model.BotGroup;
import cn.hycer.allay.cbm.model.BotPreset;
import cn.hycer.allay.config.AsbSection;
import cn.hycer.allay.config.CbmSection;
import cn.hycer.allay.config.TkSection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class AllayConfig {

    public static final String CONFIG_FILE_NAME = "allay/allay.json";

    public static final String MINE_COUNT_INTERNAL_NAME = "mine_count";
    public static final String PLACE_COUNT_INTERNAL_NAME = "place_count";
    public static final String ONLINE_TIME_INTERNAL_NAME = "online_time";
    public static final String ELYTRA_DISTANCE_INTERNAL_NAME = "elytra_dist";
    public static final String DAMAGE_TAKEN_INTERNAL_NAME = "damage_taken";
    public static final String DEATHS_INTERNAL_NAME = "deaths";
    public static final String MOB_KILLS_INTERNAL_NAME = "mob_kills";
    public static final String LATENCY_INTERNAL_NAME = "latency";

    @JsonIgnore
    private static final Logger LOGGER = LoggerFactory.getLogger("allay-config");

    @JsonIgnore
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @JsonIgnore
    private static AllayConfig INSTANCE;

    @JsonIgnore
    private File configFile;

    @JsonProperty("advancedScoreboard")
    private AsbSection advancedScoreboard = new AsbSection();

    @JsonProperty("carpetBotManager")
    private CbmSection carpetBotManager = new CbmSection();

    @JsonProperty("trialKeeper")
    private TkSection trialKeeper = new TkSection();

    @JsonProperty("featureDefaults")
    private Map<String, Boolean> featureDefaults = new LinkedHashMap<>();

    // ═══════════════════════════════════════════════════════════
    //  Delegating accessors
    // ═══════════════════════════════════════════════════════════

    public String getBorder() { return advancedScoreboard.getBorder(); }
    public void setBorder(String v) { advancedScoreboard.setBorder(v); }

    public int getSwitchInterval() { return advancedScoreboard.getSwitchInterval(); }
    public void setSwitchInterval(int v) { advancedScoreboard.setSwitchInterval(v); }

    public int getSaveInterval() { return advancedScoreboard.getSaveInterval(); }
    public void setSaveInterval(int v) { advancedScoreboard.setSaveInterval(v); }

    public int getMaxDisplayNum() { return advancedScoreboard.getMaxDisplayNum(); }
    public void setMaxDisplayNum(int v) { advancedScoreboard.setMaxDisplayNum(v); }

    public List<ScoreboardItem> getScoreboards() { return advancedScoreboard.getScoreboards(); }
    public void setScoreboards(List<ScoreboardItem> v) { advancedScoreboard.setScoreboards(v); }

    public Set<String> getHiddenScoreboards() { return advancedScoreboard.getHiddenScoreboards(); }
    public void setHiddenScoreboards(Set<String> v) { advancedScoreboard.setHiddenScoreboards(v); }

    public boolean toggleScoreboardVisibility(String name) { return advancedScoreboard.toggleScoreboardVisibility(name); }
    public ScoreboardItem getScoreboardByInternalName(String name) { return advancedScoreboard.getScoreboardByInternalName(name); }
    public String getFormattedDisplayName(ScoreboardItem item) { return advancedScoreboard.getFormattedDisplayName(item); }
    public void addMissingDefaultScoreboards() { advancedScoreboard.addMissingDefaults(); }

    public boolean isSkipScore() { return advancedScoreboard.isSkipScore(); }
    public void setSkipScore(boolean v) { advancedScoreboard.setSkipScore(v); }

    public String getSkipPrefix() { return advancedScoreboard.getSkipPrefix(); }
    public void setSkipPrefix(String v) { advancedScoreboard.setSkipPrefix(v); }

    public int getPermissionLevel() { return carpetBotManager.getPermissionLevel(); }
    public void setPermissionLevel(int v) { carpetBotManager.setPermissionLevel(v); }

    public String getBotNamePrefix() { return carpetBotManager.getBotNamePrefix(); }
    public void setBotNamePrefix(String v) { carpetBotManager.setBotNamePrefix(v); }

    public boolean isRequirePrefix() { return carpetBotManager.isRequirePrefix(); }
    public void setRequirePrefix(boolean v) { carpetBotManager.setRequirePrefix(v); }

    public List<String> getAutoLoadBots() { return carpetBotManager.getAutoLoadBots(); }
    public void setAutoLoadBots(List<String> v) { carpetBotManager.setAutoLoadBots(v); }

    public List<String> getAutoLoadGroups() { return carpetBotManager.getAutoLoadGroups(); }
    public void setAutoLoadGroups(List<String> v) { carpetBotManager.setAutoLoadGroups(v); }

    public String getTkDataFile() { return trialKeeper.getDataFile(); }
    public void setTkDataFile(String v) { trialKeeper.setDataFile(v); }

    @JsonIgnore
    public AsbSection getAsb() { return advancedScoreboard; }

    @JsonIgnore
    public CbmSection getCbm() { return carpetBotManager; }

    @JsonIgnore
    public TkSection getTk() { return trialKeeper; }

    // ── Feature defaults ──────────────────────────────────────

    public boolean hasFeatureDefault(String name) {
        return featureDefaults.containsKey(name);
    }

    public boolean getFeatureDefault(String name) {
        return featureDefaults.getOrDefault(name, false);
    }

    public void setFeatureDefault(String name, boolean value) {
        featureDefaults.put(name, value);
        saveConfig();
    }

    public void removeFeatureDefault(String name) {
        featureDefaults.remove(name);
        saveConfig();
    }

    // ═══════════════════════════════════════════════════════════
    //  Singleton & persistence
    // ═══════════════════════════════════════════════════════════

    public static AllayConfig getInstance() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    public static AllayConfig load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        File file = configDir.resolve(CONFIG_FILE_NAME).toFile();

        if (file.exists()) {
            try {
                AllayConfig cfg = OBJECT_MAPPER.readValue(file, AllayConfig.class);
                cfg.configFile = file;
                cfg.advancedScoreboard.addMissingDefaults();
                return cfg;
            } catch (IOException e) {
                LOGGER.error("Failed to load allay config, using defaults: {}", e.getMessage());
            }
        }

        AllayConfig cfg = new AllayConfig();
        cfg.configFile = file;

        // Migrate old config/allay.json → config/allay/allay.json
        Path oldAllayJson = configDir.resolve("allay.json");
        if (Files.exists(oldAllayJson)) {
            try {
                Files.createDirectories(file.getParentFile().toPath());
                Files.move(oldAllayJson, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Moved allay.json → allay/allay.json");
            } catch (IOException ignored) {}
        }

        boolean migrated = migrateFromAdvancedScoreboard(cfg, configDir)
                       | migrateFromCarpetBotManager(cfg, configDir)
                       | migrateCarpetBotData(cfg, configDir)
                       | migrateTrialKeeperData(configDir);

        if (migrated) {
            cfg.saveConfig();
            LOGGER.info("Migrated standalone mod configs to allay.json");
        } else {
            cfg.advancedScoreboard.initDefaults();
            cfg.saveConfig();
        }
        return cfg;
    }

    public void saveConfig() {
        if (configFile == null) {
            configFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME).toFile();
        }
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            OBJECT_MAPPER.writeValue(configFile, this);
        } catch (IOException e) {
            LOGGER.error("Failed to save allay config: {}", e.getMessage());
        }
    }

    @JsonIgnore
    public File getConfigFile() { return configFile; }
    public void setConfigFile(File f) { this.configFile = f; }

    // ═══════════════════════════════════════════════════════════
    //  Migration
    // ═══════════════════════════════════════════════════════════

    private static boolean migrateFromAdvancedScoreboard(AllayConfig cfg, Path configDir) {
        Path oldFile = configDir.getParent().resolve("advanced_scoreboard.json");
        if (!Files.exists(oldFile)) return false;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> old = OBJECT_MAPPER.readValue(oldFile.toFile(), Map.class);
            AsbSection asb = cfg.advancedScoreboard;

            if (old.containsKey("border")) asb.setBorder((String) old.get("border"));
            if (old.containsKey("switchInterval")) asb.setSwitchInterval(((Number) old.get("switchInterval")).intValue());
            if (old.containsKey("saveInterval")) asb.setSaveInterval(((Number) old.get("saveInterval")).intValue());
            if (old.containsKey("maxDisplayNum")) asb.setMaxDisplayNum(((Number) old.get("maxDisplayNum")).intValue());

            @SuppressWarnings("unchecked")
            List<String> hidden = (List<String>) old.get("hiddenScoreboards");
            if (hidden != null) asb.setHiddenScoreboards(new HashSet<>(hidden));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> boards = (List<Map<String, Object>>) old.get("scoreboards");
            if (boards != null) {
                List<ScoreboardItem> items = new ArrayList<>();
                for (var b : boards) {
                    ScoreboardItem item = new ScoreboardItem();
                    item.setInternalName((String) b.get("internalName"));
                    item.setDisplayName((String) b.get("displayName"));
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> data = (Map<String, Integer>) b.get("data");
                    if (data != null) item.setData(new LinkedHashMap<>(data));
                    items.add(item);
                }
                asb.setScoreboards(items);
            }

            asb.addMissingDefaults();
            Files.move(oldFile, configDir.getParent().resolve("advanced_scoreboard.json.bak"), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Migrated advanced_scoreboard.json → allay.json");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to migrate advanced_scoreboard.json: {}", e.getMessage());
            return false;
        }
    }

    private static boolean migrateFromCarpetBotManager(AllayConfig cfg, Path configDir) {
        Path oldFile = configDir.getParent().resolve("carpetbotmanager.json");
        if (!Files.exists(oldFile)) return false;

        try {
            Gson gson = new Gson();
            try (Reader reader = Files.newBufferedReader(oldFile, StandardCharsets.UTF_8)) {
                JsonObject old = gson.fromJson(reader, JsonObject.class);
                CbmSection cbm = cfg.carpetBotManager;

                if (old.has("permission_level")) cbm.setPermissionLevel(old.get("permission_level").getAsInt());
                if (old.has("bot_name_prefix")) cbm.setBotNamePrefix(old.get("bot_name_prefix").getAsString());
                if (old.has("require_prefix")) cbm.setRequirePrefix(old.get("require_prefix").getAsBoolean());

                if (old.has("auto_load_bots")) {
                    List<String> bots = gson.fromJson(old.get("auto_load_bots"),
                            new TypeToken<List<String>>(){}.getType());
                    if (bots != null) cbm.setAutoLoadBots(new ArrayList<>(bots));
                }
                if (old.has("auto_load_groups")) {
                    List<String> groups = gson.fromJson(old.get("auto_load_groups"),
                            new TypeToken<List<String>>(){}.getType());
                    if (groups != null) cbm.setAutoLoadGroups(new ArrayList<>(groups));
                }
            }

            Files.move(oldFile, configDir.getParent().resolve("carpetbotmanager.json.bak"), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Migrated carpetbotmanager.json → allay.json");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to migrate carpetbotmanager.json: {}", e.getMessage());
            return false;
        }
    }

    private static boolean migrateCarpetBotData(AllayConfig cfg, Path configDir) {
        Path botsFile = configDir.getParent().resolve("carpetbotmanager_bots.json");
        Path groupsFile = configDir.getParent().resolve("carpetbotmanager_groups.json");
        boolean botsExist = Files.exists(botsFile);
        boolean groupsExist = Files.exists(groupsFile);
        if (!botsExist && !groupsExist) return false;

        Gson gson = new Gson();
        CbmSection cbm = cfg.carpetBotManager;

        try {
            if (botsExist) {
                try (Reader reader = Files.newBufferedReader(botsFile, StandardCharsets.UTF_8)) {
                    LinkedHashMap<String, BotPreset> bots = gson.fromJson(reader,
                            new TypeToken<LinkedHashMap<String, BotPreset>>(){}.getType());
                    if (bots != null) cbm.setBots(bots);
                }
                Files.move(botsFile, configDir.getParent().resolve("carpetbotmanager_bots.json.bak"),
                        StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Migrated carpetbotmanager_bots.json → allay.json");
            }
            if (groupsExist) {
                try (Reader reader = Files.newBufferedReader(groupsFile, StandardCharsets.UTF_8)) {
                    LinkedHashMap<String, BotGroup> groups = gson.fromJson(reader,
                            new TypeToken<LinkedHashMap<String, BotGroup>>(){}.getType());
                    if (groups != null) cbm.setGroups(groups);
                }
                Files.move(groupsFile, configDir.getParent().resolve("carpetbotmanager_groups.json.bak"),
                        StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Migrated carpetbotmanager_groups.json → allay.json");
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to migrate bot data: {}", e.getMessage());
            return false;
        }
    }

    private static boolean migrateTrialKeeperData(Path configDir) {
        Path oldFile = configDir.getParent().resolve("trialkeeper_data.nbt");
        if (!Files.exists(oldFile)) return false;

        try {
            Path newDir = configDir.getParent().resolve("allay");
            Files.createDirectories(newDir);
            Files.move(oldFile, newDir.resolve("allay_trialkeeper_data.nbt"), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Moved trialkeeper_data.nbt → allay/allay_trialkeeper_data.nbt");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to move trialkeeper_data.nbt: {}", e.getMessage());
            return false;
        }
    }
}
