package cn.hycer.allay.asb.render;

import cn.hycer.allay.Allay;
import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.asb.AdvancedScoreboardModule;
import cn.hycer.allay.asb.config.ScoreboardItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.*;

public class CustomScoreboardRenderer {

    private static final String PREFIX = "asb_v_";
    private static final Map<UUID, List<String>> playerScoreHolders = new HashMap<>();

    public static String getObjectiveId(ServerPlayer player) {
        return PREFIX + player.getUUID().toString().replace("-", "");
    }

    private static Objective ensureObjective(ServerPlayer player) {
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        String objName = getObjectiveId(player);
        Objective obj = scoreboard.getObjective(objName);
        if (obj == null) {
            obj = scoreboard.addObjective(
                objName,
                ObjectiveCriteria.DUMMY,
                Component.empty(),
                ObjectiveCriteria.RenderType.INTEGER,
                true, null
            );
        }
        return obj;
    }

    public static void sendDisplay(ServerPlayer player, ScoreboardItem item) {
        try {
            var config = AllayConfig.getInstance();
            var scoreboard = AdvancedScoreboardModule.scoreboard;
            Objective obj = ensureObjective(player);
            UUID uuid = player.getUUID();

            obj.setDisplayName(Component.literal(config.getFormattedDisplayName(item)));
            player.connection.send(new ClientboundSetObjectivePacket(obj, ClientboundSetObjectivePacket.METHOD_ADD));

            List<String> oldHolders = playerScoreHolders.getOrDefault(uuid, List.of());
            for (String holderName : oldHolders) {
                player.connection.send(new ClientboundResetScorePacket(holderName, obj.getName()));
            }

            List<Map.Entry<String, Integer>> sorted = item.getData().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(config.getMaxDisplayNum())
                .toList();

            List<String> newHolders = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++) {
                var entry = sorted.get(i);
                int rank = i + 1;
                String holderName = "§" + String.format("%02x", rank) + entry.getKey();
                Component display = Component.literal("§e#" + rank + " §f" + entry.getKey() + " §7- §a" + entry.getValue());
                newHolders.add(holderName);
                player.connection.send(new ClientboundSetScorePacket(
                    holderName, obj.getName(), entry.getValue(),
                    Optional.of(display), Optional.empty()
                ));
            }
            playerScoreHolders.put(uuid, newHolders);

            player.connection.send(new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, obj));
        } catch (Exception e) {
            Allay.LOGGER.error("[ASB] failed to send custom display to player {}", player.getScoreboardName(), e);
        }
    }

    public static void clearDisplay(ServerPlayer player) {
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        UUID uuid = player.getUUID();
        Objective obj = scoreboard.getObjective(getObjectiveId(player));
        if (obj != null) {
            List<String> holders = playerScoreHolders.getOrDefault(uuid, List.of());
            for (String holderName : holders) {
                player.connection.send(new ClientboundResetScorePacket(holderName, obj.getName()));
            }
            player.connection.send(new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, (Objective) null));
        }
        playerScoreHolders.remove(uuid);
    }

    public static void onPlayerDisconnect(ServerPlayer player) {
        var scoreboard = AdvancedScoreboardModule.scoreboard;
        UUID uuid = player.getUUID();
        Objective obj = scoreboard.getObjective(getObjectiveId(player));
        if (obj != null) {
            scoreboard.removeObjective(obj);
        }
        playerScoreHolders.remove(uuid);
        cn.hycer.allay.asb.task.Task.removePlayer(uuid);
    }
}
