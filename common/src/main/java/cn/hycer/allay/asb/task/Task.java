package cn.hycer.allay.asb.task;

import cn.hycer.allay.Allay;
import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.asb.AdvancedScoreboardModule;
import cn.hycer.allay.asb.config.ScoreboardItem;
import cn.hycer.allay.feature.damage.DamageIndicator;
import cn.hycer.allay.mixin.ServerCommonPacketListenerImplAccessor;
import cn.hycer.allay.asb.render.CustomScoreboardRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.*;

import java.util.*;
import java.util.stream.Collectors;

public class Task {

    private static final Map<UUID, Integer> playerRotationIndex = new HashMap<>();
    private static final Map<UUID, ScoreboardItem> playerCurrentItem = new HashMap<>();
    private static int tickCounter = 0;

    public static void onServerTick(MinecraftServer server) {
        tickCounter++;
        var config = AllayConfig.getInstance();
        int switchIntervalTicks = config.getSwitchInterval() * 20;
        int saveIntervalTicks = config.getSaveInterval() * 20;

        DamageIndicator.tick();
        if (tickCounter % 20 == 0) syncLatency(server);
        if (tickCounter % switchIntervalTicks == 0) rotateDisplay(server);
        if (tickCounter % saveIntervalTicks == 0) {
            syncDataToScoreboard(server);
            config.saveConfig();
        }
    }

    private static void rotateDisplay(MinecraftServer server) {
        try {
            var config = AllayConfig.getInstance();
            var scoreboard = AdvancedScoreboardModule.scoreboard;
            List<ScoreboardItem> allScoreboards = config.getScoreboards();
            if (allScoreboards == null || allScoreboards.isEmpty()) return;

            Set<String> hidden = config.getHiddenScoreboards();
            List<ScoreboardItem> visible = allScoreboards.stream()
                .filter(sb -> !hidden.contains(sb.getInternalName()))
                .filter(sb -> !AllayConfig.LATENCY_INTERNAL_NAME.equals(sb.getInternalName()))
                .toList();
            if (visible.isEmpty()) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                // Per-player visible: global visible minus per-player hidden
                List<ScoreboardItem> playerVisible = visible.stream()
                        .filter(sb -> !AdvancedScoreboardModule.isPlayerHidden(uuid, sb.getInternalName()))
                        .toList();
                if (playerVisible.isEmpty()) {
                    CustomScoreboardRenderer.clearDisplay(player);
                    continue;
                }

                int index = playerRotationIndex.getOrDefault(uuid, -1);
                index = (index + 1) % playerVisible.size();
                playerRotationIndex.put(uuid, index);

                ScoreboardItem currentItem = playerVisible.get(index);
                playerCurrentItem.put(uuid, currentItem);
                CustomScoreboardRenderer.sendDisplay(player, currentItem);
            }
        } catch (Exception e) {
            Allay.LOGGER.error("[ASB] scoreboard rotation error", e);
        }
    }

    private static void syncDataToScoreboard(MinecraftServer server) {
        var config = AllayConfig.getInstance();
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        for (ScoreboardItem item : config.getScoreboards()) {
            String internalName = item.getInternalName();
            if (AllayConfig.LATENCY_INTERNAL_NAME.equals(internalName)) continue;
            Objective objective = scoreboard.getObjective(internalName);
            if (objective == null) continue;

            switch (internalName) {
                case AllayConfig.ONLINE_TIME_INTERNAL_NAME -> {
                    for (var player : server.getPlayerList().getPlayers()) {
                        if (config.isSkipScore() && player.getScoreboardName().startsWith(config.getSkipPrefix())) continue;
                        int totalPlayTicks = player.getStats().getValue(Stats.CUSTOM, Stats.PLAY_TIME);
                        if (totalPlayTicks == 0) continue;
                        int totalHours = totalPlayTicks / 20 / 3600;
                        item.updateData(player.getScoreboardName(), totalHours);
                    }
                }
                case AllayConfig.ELYTRA_DISTANCE_INTERNAL_NAME -> {
                    for (var player : server.getPlayerList().getPlayers()) {
                        if (config.isSkipScore() && player.getScoreboardName().startsWith(config.getSkipPrefix())) continue;
                        int aviateOneCM = player.getStats().getValue(Stats.CUSTOM, Stats.AVIATE_ONE_CM);
                        if (aviateOneCM == 0) continue;
                        int aviateOneKM = aviateOneCM / 100 / 1000;
                        item.updateData(player.getScoreboardName(), aviateOneKM);
                    }
                }
                case AllayConfig.DAMAGE_TAKEN_INTERNAL_NAME -> {
                    for (var player : server.getPlayerList().getPlayers()) {
                        if (config.isSkipScore() && player.getScoreboardName().startsWith(config.getSkipPrefix())) continue;
                        int damageTaken = player.getStats().getValue(Stats.CUSTOM, Stats.DAMAGE_TAKEN) / 10;
                        if (damageTaken == 0) continue;
                        item.updateData(player.getScoreboardName(), damageTaken);
                    }
                }
                case AllayConfig.EXPERIENCE_LEVEL_INTERNAL_NAME -> {
                    for (var player : server.getPlayerList().getPlayers()) {
                        if (config.isSkipScore() && player.getScoreboardName().startsWith(config.getSkipPrefix())) continue;
                        int level = player.experienceLevel;
                        if (level == 0) continue;
                        item.updateData(player.getScoreboardName(), level);
                    }
                }
                case AllayConfig.DEATHS_INTERNAL_NAME -> {
                    for (var player : server.getPlayerList().getPlayers()) {
                        if (config.isSkipScore() && player.getScoreboardName().startsWith(config.getSkipPrefix())) continue;
                        int deaths = player.getStats().getValue(Stats.CUSTOM, Stats.DEATHS);
                        if (deaths == 0) continue;
                        item.updateData(player.getScoreboardName(), deaths);
                    }
                }
            }
            syncTopNToScoreboard(objective, item.getData(), config.getMaxDisplayNum());
        }
    }

    public static void syncTopNToScoreboard(Objective objective, Map<String, Integer> data, int maxDisplay) {
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        List<Map.Entry<String, Integer>> topEntries = data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(maxDisplay)
                .toList();

        Set<String> topPlayerNames = topEntries.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        for (var entry : scoreboard.listPlayerScores(objective)) {
            if (!topPlayerNames.contains(entry.owner())) {
                scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(entry.owner()), objective);
            }
        }

        for (var entry : topEntries) {
            ScoreHolder scoreHolder = ScoreHolder.forNameOnly(entry.getKey());
            ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(scoreHolder, objective);
            scoreAccess.set(entry.getValue());
        }
    }

    private static void syncLatency(MinecraftServer server) {
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        Objective objective = scoreboard.getObjective(AllayConfig.LATENCY_INTERNAL_NAME);
        if (objective == null) return;

        if (scoreboard.getDisplayObjective(DisplaySlot.LIST) != objective) {
            scoreboard.setDisplayObjective(DisplaySlot.LIST, objective);
        }

        for (var entry : scoreboard.listPlayerScores(objective)) {
            String owner = entry.owner();
            if (server.getPlayerList().getPlayerByName(owner) == null) {
                scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(owner), objective);
            }
        }

        for (var player : server.getPlayerList().getPlayers()) {
            int pingMs = ((ServerCommonPacketListenerImplAccessor) player.connection).getLatency();
            ScoreHolder scoreHolder = ScoreHolder.forNameOnly(player.getScoreboardName());
            ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(scoreHolder, objective);
            scoreAccess.set(pingMs);
            Component display = Component.literal(pingMs + "ms")
                    .withStyle(Style.EMPTY.withColor(latencyColor(pingMs)));
            scoreAccess.numberFormatOverride(new FixedFormat(display));
        }
    }

    private static int latencyColor(int pingMs) {
        if (pingMs < 50)  return 0x55FF55;
        if (pingMs < 100) return 0xFFFF55;
        if (pingMs < 200) return 0xFFAA00;
        return 0xFF5555;
    }

    public static void removePlayer(UUID uuid) {
        playerRotationIndex.remove(uuid);
        playerCurrentItem.remove(uuid);
    }

    public static void refreshDisplayForItem(MinecraftServer server, ScoreboardItem item) {
        for (var player : server.getPlayerList().getPlayers()) {
            ScoreboardItem current = playerCurrentItem.get(player.getUUID());
            if (current != null && current.getInternalName().equals(item.getInternalName())) {
                CustomScoreboardRenderer.sendDisplay(player, item);
            }
        }
    }
}
