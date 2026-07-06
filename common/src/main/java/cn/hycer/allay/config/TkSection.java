package cn.hycer.allay.config;

public class TkSection {
    /** Relative path from server directory, e.g. "config/trialkeeper_data.nbt" */
    private String dataFile = "config/allay_trialkeeper_data.nbt";

    public String getDataFile() { return dataFile; }
    public void setDataFile(String v) { this.dataFile = v != null && !v.isEmpty() ? v : "config/allay_trialkeeper_data.nbt"; }
}
