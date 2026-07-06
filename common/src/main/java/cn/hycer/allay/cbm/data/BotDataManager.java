package cn.hycer.allay.cbm.data;

import cn.hycer.allay.AllayConfig;
import cn.hycer.allay.cbm.model.BotGroup;
import cn.hycer.allay.cbm.model.BotPreset;

import java.util.*;

/**
 * Manages bot presets and groups, persisted via AllayConfig into allay.json.
 */
public class BotDataManager {

    private static final BotDataManager INSTANCE = new BotDataManager();

    private BotDataManager() {}

    public static BotDataManager getInstance() {
        return INSTANCE;
    }

    private AllayConfig.CbmSection cbm() {
        return AllayConfig.getInstance().getCbm();
    }

    // === Bot Presets ===

    public void addBotPreset(BotPreset preset) {
        cbm().getBots().put(preset.getName(), preset);
        AllayConfig.getInstance().saveConfig();
    }

    public void removeBotPreset(String name) {
        cbm().getBots().remove(name);
        for (BotGroup group : cbm().getGroups().values()) {
            group.getBots().remove(name);
        }
        AllayConfig.getInstance().saveConfig();
    }

    public Optional<BotPreset> getBotPreset(String name) {
        return Optional.ofNullable(cbm().getBots().get(name));
    }

    public Collection<BotPreset> getAllBotPresets() {
        return Collections.unmodifiableCollection(cbm().getBots().values());
    }

    public boolean hasBotPreset(String name) {
        return cbm().getBots().containsKey(name);
    }

    // === Bot Groups ===

    public void addBotGroup(BotGroup group) {
        cbm().getGroups().put(group.getName(), group);
        AllayConfig.getInstance().saveConfig();
    }

    public void removeBotGroup(String name) {
        cbm().getGroups().remove(name);
        AllayConfig.getInstance().saveConfig();
    }

    public Optional<BotGroup> getBotGroup(String name) {
        return Optional.ofNullable(cbm().getGroups().get(name));
    }

    public Collection<BotGroup> getAllBotGroups() {
        return Collections.unmodifiableCollection(cbm().getGroups().values());
    }

    public boolean hasBotGroup(String name) {
        return cbm().getGroups().containsKey(name);
    }
}
