package cn.hycer.allay.feature;

public class FeatureManager {

    private static final FeatureManager INSTANCE = new FeatureManager();

    private boolean fragileObsidian;
    private boolean superTNT;
    private boolean fragileGlass;
    private boolean experienceBottle;

    private FeatureManager() {
        reloadDefaults();
    }

    public static FeatureManager getInstance() { return INSTANCE; }

    public void reloadDefaults() {
        var cfg = cn.hycer.allay.config.AllayConfig.getInstance();
        fragileObsidian = cfg.hasFeatureDefault("fragileObsidian") && cfg.getFeatureDefault("fragileObsidian");
        superTNT      = cfg.hasFeatureDefault("superTNT")      && cfg.getFeatureDefault("superTNT");
        fragileGlass  = cfg.hasFeatureDefault("fragileGlass")  && cfg.getFeatureDefault("fragileGlass");
        experienceBottle = cfg.hasFeatureDefault("experienceBottle") && cfg.getFeatureDefault("experienceBottle");
    }

    public boolean isFragileObsidian() { return fragileObsidian; }
    public void setFragileObsidian(boolean v) { this.fragileObsidian = v; }

    public boolean isSuperTNT() { return superTNT; }
    public void setSuperTNT(boolean v) { this.superTNT = v; }

    public boolean isFragileGlass() { return fragileGlass; }
    public void setFragileGlass(boolean v) { this.fragileGlass = v; }

    public boolean isExperienceBottle() { return experienceBottle; }
    public void setExperienceBottle(boolean v) { this.experienceBottle = v; }
}
