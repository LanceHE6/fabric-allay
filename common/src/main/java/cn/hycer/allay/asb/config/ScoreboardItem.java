package cn.hycer.allay.asb.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single scoreboard item config (元素 for scoreboards array in allay.json)
 */
public class ScoreboardItem {
    private String internalName;
    private String displayName;
    private Map<String, Integer> data = new LinkedHashMap<>();

    public String getInternalName() { return internalName; }
    public void setInternalName(String v) { this.internalName = v; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String v) { this.displayName = v; }

    public Map<String, Integer> getData() { return data; }
    public void setData(Map<String, Integer> v) { this.data = v != null ? v : new LinkedHashMap<>(); }

    public void updateData(String playerName, int value) { data.put(playerName, value); }
    public int getDataValue(String playerName, int defaultValue) { return data.getOrDefault(playerName, defaultValue); }
}
