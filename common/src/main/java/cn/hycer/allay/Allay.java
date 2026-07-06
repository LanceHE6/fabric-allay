package cn.hycer.allay;

import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.asb.AdvancedScoreboardModule;
import cn.hycer.allay.cbm.CarpetBotModule;
import cn.hycer.allay.command.AllayCommand;
import cn.hycer.allay.tk.TrialKeeperModule;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Allay implements ModInitializer {

    public static final String MOD_ID = "allay";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Load merged config
        AllayConfig config = AllayConfig.getInstance();
        if (config == null) {
            LOGGER.error("Failed to load config, mod will not initialize");
            return;
        }

        // Register root command
        AllayCommand.register();

        // Initialize sub-modules
        AdvancedScoreboardModule.init();
        CarpetBotModule.init();
        TrialKeeperModule.init();

        LOGGER.info("[Allay] All modules loaded successfully!");
    }
}
