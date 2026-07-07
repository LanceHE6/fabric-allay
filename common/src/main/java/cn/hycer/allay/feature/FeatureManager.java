package cn.hycer.allay.feature;

/**
 * Singleton holder for runtime feature state. Reads persistent defaults
 * from AllayConfig on startup, provides volatile runtime toggles.
 */
public class FeatureManager {

    private static final FeatureManager INSTANCE = new FeatureManager();

    private boolean fragileObsidian;
    private boolean superTNT;

    private FeatureManager() {
        reloadDefaults();
    }

    public static FeatureManager getInstance() {
        return INSTANCE;
    }

    /** Re-read defaults from config (called on startup / removeDefault). */
    public void reloadDefaults() {
        var cfg = cn.hycer.allay.config.AllayConfig.getInstance();
        fragileObsidian = cfg.hasFeatureDefault("fragileObsidian") && cfg.getFeatureDefault("fragileObsidian");
        superTNT = cfg.hasFeatureDefault("superTNT") && cfg.getFeatureDefault("superTNT");
    }

    public boolean isFragileObsidian() { return fragileObsidian; }
    public void setFragileObsidian(boolean v) { this.fragileObsidian = v; }

    public boolean isSuperTNT() { return superTNT; }
    public void setSuperTNT(boolean v) { this.superTNT = v; }
}
