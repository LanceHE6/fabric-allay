package cn.hycer.allay.asb.event;

import cn.hycer.allay.AllayConfig;
import cn.hycer.allay.asb.AdvancedScoreboardModule;
import cn.hycer.allay.asb.render.CustomScoreboardRenderer;
import cn.hycer.allay.asb.task.Task;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.ScoreHolder;
import cn.hycer.allay.Allay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerStartedEvent {

    public static void onServerStarted(MinecraftServer server) {
        AdvancedScoreboardModule.scoreboard = Objects.requireNonNull(server.overworld().getScoreboard());
        clearInGameScoreboardData(server);
        registerScoreboard(server);
        setLatencyToListDisplay();
        syncDataFromConfig();
        ServerTickEvents.END_SERVER_TICK.register(Task::onServerTick);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server1) -> {
            if (handler.player != null) {
                CustomScoreboardRenderer.onPlayerDisconnect(handler.player);
            }
        });
    }

    public static void registerScoreboard(MinecraftServer server) {
        var config = AllayConfig.getInstance();
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        for (var sb : config.getScoreboards()) {
            try {
                String formattedName = config.getFormattedDisplayName(sb);
                if (scoreboard.getObjective(sb.getInternalName()) != null) continue;
                var obj = scoreboard.addObjective(
                    sb.getInternalName(),
                    net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY,
                    net.minecraft.network.chat.Component.literal(formattedName),
                    net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER,
                    true, null
                );
                if (obj != null) {
                    Allay.LOGGER.info("[ASB] registered scoreboard: {}", sb.getInternalName());
                }
            } catch (Exception e) {
                Allay.LOGGER.error("[ASB] failed to register scoreboard: {}", sb.getInternalName(), e);
            }
        }
    }

    private static void setLatencyToListDisplay() {
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        if (scoreboard == null) return;
        var obj = scoreboard.getObjective(AllayConfig.LATENCY_INTERNAL_NAME);
        if (obj != null) {
            scoreboard.setDisplayObjective(net.minecraft.world.scores.DisplaySlot.LIST, obj);
        }
    }

    public static void refreshAllDisplayNames() {
        // placeholder — display names are refreshed through CustomScoreboardRenderer
    }

    public static void syncDataFromConfig() {
        try {
            var config = AllayConfig.getInstance();
            var scoreboard = AdvancedScoreboardModule.scoreboard;
            for (var item : config.getScoreboards()) {
                var obj = scoreboard.getObjective(item.getInternalName());
                if (obj == null) continue;
                Task.syncTopNToScoreboard(obj, item.getData(), config.getMaxDisplayNum());
            }
            Allay.LOGGER.info("[ASB] synced all player data from config to scoreboard");
        } catch (Exception e) {
            Allay.LOGGER.error("[ASB] sync data from config failed", e);
        }
    }

    public static void clearInGameScoreboardData(MinecraftServer server) {
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        var config = AllayConfig.getInstance();

        Set<String> currentNames = config.getScoreboards().stream()
                .map(cn.hycer.allay.asb.config.ScoreboardItem::getInternalName)
                .collect(Collectors.toSet());
        Set<String> obsoleteNames = Set.of("elytron_distance", "latency");

        try {
            List<Objective> toRemove = new ArrayList<>();
            for (var obj : scoreboard.getObjectives()) {
                String name = obj.getName();
                if (obsoleteNames.contains(name)) {
                    toRemove.add(obj);
                } else if (currentNames.contains(name)) {
                    for (var entry : scoreboard.listPlayerScores(obj)) {
                        scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(entry.owner()), obj);
                    }
                }
            }
            for (var obj : toRemove) {
                scoreboard.removeObjective(obj);
                Allay.LOGGER.info("[ASB] removed obsolete objective: {}", obj.getName());
            }
        } catch (Exception e) {
            Allay.LOGGER.error("[ASB] reset scoreboard data failed", e);
        }
    }
}
