package cn.hycer.allay.asb;

import cn.hycer.allay.Allay;
import cn.hycer.allay.asb.event.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.scores.Scoreboard;

import java.util.UUID;

public class AdvancedScoreboardModule {

    public static Scoreboard scoreboard;

    public static void init() {
        Allay.LOGGER.info("[Allay/ASB] Initializing AdvancedScoreboard module...");

        PlayerScoreboardPrefs.load();

        ServerLifecycleEvents.SERVER_STARTED.register(ServerStartedEvent::onServerStarted);

        PlayerBlockBreakEvents.AFTER.register(((world, playerEntity, blockPos, blockState, blockEntity) ->
                PlayerBreakBlockEvent.onBreak(playerEntity)));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.getItemInHand(hand).getItem() instanceof BlockItem) {
                PlayerPlaceBlockEvent.onPlace(player);
            }
            return InteractionResult.PASS;
        });

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(
            (world, entity, killedEntity, damageSource) ->
                PlayerKillMobEvent.onKill(world, entity, killedEntity)
        );
    }

    public static boolean isPlayerHidden(UUID uuid, String internalName) {
        return PlayerScoreboardPrefs.isHidden(uuid, internalName);
    }

    public static boolean togglePlayerHidden(UUID uuid, String internalName) {
        return PlayerScoreboardPrefs.toggle(uuid, internalName);
    }
}
