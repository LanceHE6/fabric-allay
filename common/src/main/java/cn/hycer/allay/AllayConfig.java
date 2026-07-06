package cn.hycer.allay;

import cn.hycer.allay.asb.config.ScoreboardItem;
import cn.hycer.allay.cbm.model.BotGroup;
import cn.hycer.allay.cbm.model.BotPreset;
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

    public static final String CONFIG_FILE_NAME = "allay.json";

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

    // ═══════════════════════════════════════════════════════════
    //  Inner section classes
    // ═══════════════════════════════════════════════════════════

    public static class AsbSection {
        private String border = "===";
        private int switchInterval = 5;
        private int saveInterval = 5;
        private int maxDisplayNum = 15;
        private Set<String> hiddenScoreboards = new HashSet<>();
        private List<ScoreboardItem> scoreboards = new ArrayList<>();

        public String getBorder() { return border; }
        public void setBorder(String v) { this.border = v != null ? v : "==="; }

        public int getSwitchInterval() { return switchInterval; }
        public void setSwitchInterval(int v) { this.switchInterval = Math.max(1, v); }

        public int getSaveInterval() { return saveInterval; }
        public void setSaveInterval(int v) { this.saveInterval = Math.max(1, v); }

        public int getMaxDisplayNum() { return maxDisplayNum; }
        public void setMaxDisplayNum(int v) { this.maxDisplayNum = Math.max(1, v); }

        public List<ScoreboardItem> getScoreboards() { return scoreboards; }
        public void setScoreboards(List<ScoreboardItem> v) { this.scoreboards = v != null ? v : new ArrayList<>(); }

        public Set<String> getHiddenScoreboards() { return hiddenScoreboards; }
        public void setHiddenScoreboards(Set<String> v) { this.hiddenScoreboards = v != null ? v : new HashSet<>(); }

        public boolean toggleScoreboardVisibility(String internalName) {
            if (hiddenScoreboards.contains(internalName)) {
                hiddenScoreboards.remove(internalName);
                return false;
            } else {
                hiddenScoreboards.add(internalName);
                return true;
            }
        }

        public ScoreboardItem getScoreboardByInternalName(String internalName) {
            return scoreboards.stream()
                    .filter(item -> internalName.equals(item.getInternalName()))
                    .findFirst().orElse(null);
        }

        public String getFormattedDisplayName(ScoreboardItem item) {
            return border + item.getDisplayName() + border;
        }

        private void initDefaults() {
            String[][] defaults = {
                    {MINE_COUNT_INTERNAL_NAME, "挖掘量"},
                    {PLACE_COUNT_INTERNAL_NAME, "放置量"},
                    {ONLINE_TIME_INTERNAL_NAME, "在线时长(h)"},
                    {ELYTRA_DISTANCE_INTERNAL_NAME, "飞行距离(km)"},
                    {DAMAGE_TAKEN_INTERNAL_NAME, "受到伤害"},
                    {DEATHS_INTERNAL_NAME, "死亡次数"},
                    {MOB_KILLS_INTERNAL_NAME, "击杀生物数"},
                    {LATENCY_INTERNAL_NAME, "延迟(ms)"},
            };
            for (String[] def : defaults) {
                ScoreboardItem item = new ScoreboardItem();
                item.setInternalName(def[0]);
                item.setDisplayName(def[1]);
                scoreboards.add(item);
            }
        }

        private void addMissingDefaults() {
            String[][] defaults = {
                    {MINE_COUNT_INTERNAL_NAME, "挖掘量"},
                    {PLACE_COUNT_INTERNAL_NAME, "放置量"},
                    {ONLINE_TIME_INTERNAL_NAME, "在线时长(h)"},
                    {ELYTRA_DISTANCE_INTERNAL_NAME, "飞行距离(km)"},
                    {DAMAGE_TAKEN_INTERNAL_NAME, "受到伤害"},
                    {DEATHS_INTERNAL_NAME, "死亡次数"},
                    {MOB_KILLS_INTERNAL_NAME, "击杀生物数"},
                    {LATENCY_INTERNAL_NAME, "延迟(ms)"},
            };
            for (String[] def : defaults) {
                if (getScoreboardByInternalName(def[0]) == null) {
                    ScoreboardItem item = new ScoreboardItem();
                    item.setInternalName(def[0]);
                    item.setDisplayName(def[1]);
                    scoreboards.add(item);
                    LOGGER.info("added missing default scoreboard: {} ({})", def[1], def[0]);
                }
            }
        }
    }

    public static class CbmSection {
        private int permissionLevel = 0;
        private String botNamePrefix = "bot_";
        private boolean requirePrefix = false;
        private List<String> autoLoadBots = new ArrayList<>();
        private List<String> autoLoadGroups = new ArrayList<>();
        private LinkedHashMap<String, BotPreset> bots = new LinkedHashMap<>();
        private LinkedHashMap<String, BotGroup> groups = new LinkedHashMap<>();

        public int getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(int v) { this.permissionLevel = v; }

        public String getBotNamePrefix() { return botNamePrefix; }
        public void setBotNamePrefix(String v) { this.botNamePrefix = v; }

        public boolean isRequirePrefix() { return requirePrefix; }
        public void setRequirePrefix(boolean v) { this.requirePrefix = v; }

        public List<String> getAutoLoadBots() { return autoLoadBots; }
        public void setAutoLoadBots(List<String> v) { this.autoLoadBots = v != null ? v : new ArrayList<>(); }

        public List<String> getAutoLoadGroups() { return autoLoadGroups; }
        public void setAutoLoadGroups(List<String> v) { this.autoLoadGroups = v != null ? v : new ArrayList<>(); }

        public LinkedHashMap<String, BotPreset> getBots() { return bots; }
        public void setBots(LinkedHashMap<String, BotPreset> v) { this.bots = v != null ? v : new LinkedHashMap<>(); }

        public LinkedHashMap<String, BotGroup> getGroups() { return groups; }
        public void setGroups(LinkedHashMap<String, BotGroup> v) { this.groups = v != null ? v : new LinkedHashMap<>(); }
    }

    public static class TkSection {
        /** Relative path from server directory, e.g. "config/trialkeeper_data.nbt" */
        private String dataFile = "config/allay_trialkeeper_data.nbt";

        public String getDataFile() { return dataFile; }
        public void setDataFile(String v) { this.dataFile = v != null && !v.isEmpty() ? v : "config/allay_trialkeeper_data.nbt"; }
    }

    // ═══════════════════════════════════════════════════════════
    //  Delegating accessors
    // ═══════════════════════════════════════════════════════════

    // ── ASB ───────────────────────────────────────────────────

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

    // ── CBM ───────────────────────────────────────────────────

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

    // ── TK ───────────────────────────────────────────────────

    public String getTkDataFile() { return trialKeeper.getDataFile(); }
    public void setTkDataFile(String v) { trialKeeper.setDataFile(v); }

    // ── Direct section access ─────────────────────────────────

    @JsonIgnore
    public AsbSection getAsb() { return advancedScoreboard; }

    @JsonIgnore
    public CbmSection getCbm() { return carpetBotManager; }

    @JsonIgnore
    public TkSection getTk() { return trialKeeper; }

    // ═══════════════════════════════════════════════════════════
    //  Singleton & persistence
    // ═══════════════════════════════════════════════════════════

    public static AllayConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
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

        // ── Migration from standalone mods ─────────────────
        AllayConfig cfg = new AllayConfig();
        cfg.configFile = file;

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

    // ── Migration helpers ──────────────────────────────────────

    private static boolean migrateFromAdvancedScoreboard(AllayConfig cfg, Path configDir) {
        Path oldFile = configDir.resolve("advanced_scoreboard.json");
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
            Files.move(oldFile, configDir.resolve("advanced_scoreboard.json.bak"), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Migrated advanced_scoreboard.json → allay.json");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to migrate advanced_scoreboard.json: {}", e.getMessage());
            return false;
        }
    }

    private static boolean migrateFromCarpetBotManager(AllayConfig cfg, Path configDir) {
        Path oldFile = configDir.resolve("carpetbotmanager.json");
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

            Files.move(oldFile, configDir.resolve("carpetbotmanager.json.bak"), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Migrated carpetbotmanager.json → allay.json");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to migrate carpetbotmanager.json: {}", e.getMessage());
            return false;
        }
    }

    private static boolean migrateCarpetBotData(AllayConfig cfg, Path configDir) {
        Path botsFile = configDir.resolve("carpetbotmanager_bots.json");
        Path groupsFile = configDir.resolve("carpetbotmanager_groups.json");
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
                Files.move(botsFile, configDir.resolve("carpetbotmanager_bots.json.bak"),
                        StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Migrated carpetbotmanager_bots.json → allay.json");
            }
            if (groupsExist) {
                try (Reader reader = Files.newBufferedReader(groupsFile, StandardCharsets.UTF_8)) {
                    LinkedHashMap<String, BotGroup> groups = gson.fromJson(reader,
                            new TypeToken<LinkedHashMap<String, BotGroup>>(){}.getType());
                    if (groups != null) cbm.setGroups(groups);
                }
                Files.move(groupsFile, configDir.resolve("carpetbotmanager_groups.json.bak"),
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
        Path oldFile = configDir.resolve("trialkeeper_data.nbt");
        if (!Files.exists(oldFile)) return false;

        try {
            Path newFile = configDir.resolve("allay_trialkeeper_data.nbt");
            Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Renamed trialkeeper_data.nbt → allay_trialkeeper_data.nbt");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to rename trialkeeper_data.nbt: {}", e.getMessage());
            return false;
        }
    }

    public void saveConfig() {
        if (configFile == null) {
            configFile = new File(
                    FabricLoader.getInstance().getConfigDir().toFile(),
                    CONFIG_FILE_NAME
            );
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
}
