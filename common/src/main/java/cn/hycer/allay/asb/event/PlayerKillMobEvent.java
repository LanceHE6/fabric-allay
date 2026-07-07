package cn.hycer.allay.asb.event;

import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.asb.task.Task;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PlayerKillMobEvent {

    public static void onKill(net.minecraft.server.level.ServerLevel world, Entity entity, LivingEntity killedEntity) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (killedEntity instanceof Player) return;

        var config = AllayConfig.getInstance();
        String playerName = player.getScoreboardName();
        if (config.isSkipScore() && playerName.startsWith(config.getSkipPrefix())) return;

        var item = config.getScoreboardByInternalName(AllayConfig.MOB_KILLS_INTERNAL_NAME);
        if (item == null) return;
        int playerScore = item.getDataValue(playerName, 0);
        item.updateData(playerName, ++playerScore);

        var dataItem = config.getScoreboardByInternalName(AllayConfig.MOB_KILLS_INTERNAL_NAME);
        if (dataItem != null) {
            Task.refreshDisplayForItem(world.getServer(), dataItem);
        }
    }
}
