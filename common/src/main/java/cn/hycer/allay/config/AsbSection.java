package cn.hycer.allay.config;


import cn.hycer.allay.asb.config.ScoreboardItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AsbSection {
    private static final Logger LOGGER = LoggerFactory.getLogger("allay-config");

    private String border = "===";
    private int switchInterval = 5;
    private int saveInterval = 5;
    private int maxDisplayNum = 15;
    private Set<String> hiddenScoreboards = new HashSet<>();
    private List<ScoreboardItem> scoreboards = new ArrayList<>();
    private boolean skipScore = false;
    private String skipPrefix = "bot_";

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

    public boolean isSkipScore() { return skipScore; }
    public void setSkipScore(boolean v) { this.skipScore = v; }

    public String getSkipPrefix() { return skipPrefix; }
    public void setSkipPrefix(String v) { this.skipPrefix = v != null && !v.isEmpty() ? v : "bot_"; }

    public boolean toggleScoreboardVisibility(String internalName) {
        if (hiddenScoreboards.contains(internalName)) {
            hiddenScoreboards.remove(internalName);
            return false;
        }
        hiddenScoreboards.add(internalName);
        return true;
    }

    public ScoreboardItem getScoreboardByInternalName(String internalName) {
        return scoreboards.stream()
                .filter(item -> internalName.equals(item.getInternalName()))
                .findFirst().orElse(null);
    }

    public String getFormattedDisplayName(ScoreboardItem item) {
        return border + item.getDisplayName() + border;
    }

    public void initDefaults() {
        String[][] defaults = {
                {AllayConfig.MINE_COUNT_INTERNAL_NAME, "挖掘量"},
                {AllayConfig.PLACE_COUNT_INTERNAL_NAME, "放置量"},
                {AllayConfig.ONLINE_TIME_INTERNAL_NAME, "在线时长(h)"},
                {AllayConfig.ELYTRA_DISTANCE_INTERNAL_NAME, "飞行距离(km)"},
                {AllayConfig.DAMAGE_TAKEN_INTERNAL_NAME, "受到伤害"},
                {AllayConfig.DEATHS_INTERNAL_NAME, "死亡次数"},
                {AllayConfig.MOB_KILLS_INTERNAL_NAME, "击杀生物数"},
                {AllayConfig.LATENCY_INTERNAL_NAME, "延迟(ms)"},
        };
        for (String[] def : defaults) {
            ScoreboardItem item = new ScoreboardItem();
            item.setInternalName(def[0]);
            item.setDisplayName(def[1]);
            scoreboards.add(item);
        }
    }

    public void addMissingDefaults() {
        String[][] defaults = {
                {AllayConfig.MINE_COUNT_INTERNAL_NAME, "挖掘量"},
                {AllayConfig.PLACE_COUNT_INTERNAL_NAME, "放置量"},
                {AllayConfig.ONLINE_TIME_INTERNAL_NAME, "在线时长(h)"},
                {AllayConfig.ELYTRA_DISTANCE_INTERNAL_NAME, "飞行距离(km)"},
                {AllayConfig.DAMAGE_TAKEN_INTERNAL_NAME, "受到伤害"},
                {AllayConfig.DEATHS_INTERNAL_NAME, "死亡次数"},
                {AllayConfig.MOB_KILLS_INTERNAL_NAME, "击杀生物数"},
                {AllayConfig.LATENCY_INTERNAL_NAME, "延迟(ms)"},
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
