package cn.hycer.allay.cbm;

import cn.hycer.allay.Allay;
import cn.hycer.allay.cbm.command.BotSpawner;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

/**
 * CarpetBotManager module initializer.
 */
public class CarpetBotModule {

    public static void init() {
        Allay.LOGGER.info("[Allay/CBM] Initializing CarpetBotManager module...");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Allay.LOGGER.info("[CBM] Auto-loading bots...");
            BotSpawner.autoLoad(server.createCommandSourceStack());
        });
    }
}
