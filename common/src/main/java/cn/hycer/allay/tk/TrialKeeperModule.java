package cn.hycer.allay.tk;

import cn.hycer.allay.config.AllayConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class TrialKeeperModule {

    public static void init() {
        cn.hycer.allay.Allay.LOGGER.info("[Allay/TK] Initializing TrialKeeper module...");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            String dataFile = AllayConfig.getInstance().getTkDataFile();
            TrialStorage.setSaveFile(server.getServerDirectory().resolve(dataFile));
            TrialStorage.loadFromFile();
        });
    }
}
