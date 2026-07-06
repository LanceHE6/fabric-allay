package cn.hycer.allay.config;

import cn.hycer.allay.cbm.model.BotGroup;
import cn.hycer.allay.cbm.model.BotPreset;

import java.util.*;

public class CbmSection {
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
