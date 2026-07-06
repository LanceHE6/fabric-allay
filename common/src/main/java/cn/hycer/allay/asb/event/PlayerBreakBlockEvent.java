package cn.hycer.allay.asb.event;

import cn.hycer.allay.AllayConfig;
import cn.hycer.allay.asb.task.Task;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class PlayerBreakBlockEvent {

    public static void onBreak(Player player) {
        var config = AllayConfig.getInstance();
        var item = config.getScoreboardByInternalName(AllayConfig.MINE_COUNT_INTERNAL_NAME);
        if (item == null) return;
        String playerName = player.getScoreboardName();
        int playerScore = item.getDataValue(playerName, 0);
        item.updateData(playerName, ++playerScore);

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        var dataItem = config.getScoreboardByInternalName(AllayConfig.MINE_COUNT_INTERNAL_NAME);
        if (server != null && dataItem != null) {
            Task.refreshDisplayForItem(server, dataItem);
        }
    }
}
