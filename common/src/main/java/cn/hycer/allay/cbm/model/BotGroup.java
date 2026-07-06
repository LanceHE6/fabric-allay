package cn.hycer.allay.cbm.model;

import java.util.ArrayList;
import java.util.List;

public class BotGroup {

    private String name;
    private String description;
    private List<String> bots = new ArrayList<>();

    public BotGroup() {}
    public BotGroup(String name, String description, List<String> bots) {
        this.name = name; this.description = description; this.bots = bots;
    }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public List<String> getBots() { return bots; }
    public void setBots(List<String> v) { this.bots = v; }
}
