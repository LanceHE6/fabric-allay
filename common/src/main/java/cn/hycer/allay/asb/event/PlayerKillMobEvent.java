package cn.hycer.allay.asb.event;

import cn.hycer.allay.AllayConfig;
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
        var item = config.getScoreboardByInternalName(AllayConfig.MOB_KILLS_INTERNAL_NAME);
        if (item == null) return;
        String playerName = player.getScoreboardName();
        int playerScore = item.getDataValue(playerName, 0);
        item.updateData(playerName, ++playerScore);

        var dataItem = config.getScoreboardByInternalName(AllayConfig.MOB_KILLS_INTERNAL_NAME);
        if (dataItem != null) {
            Task.refreshDisplayForItem(world.getServer(), dataItem);
        }
    }
}
