package cn.hycer.allay.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FeatureNotifier {

    private static int tickCounter = 0;

    public static void init() {
        // 进服提示：当玩家登录时，若服务器启用了全局特性，发送一次提醒
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            if (player != null) {
                sendJoinNotification(player);
            }
        });

        // 准星 Action Bar 提示：每 5 tick（0.25 秒）检测一次手持镐子看易碎方块的玩家
        ServerTickEvents.END_SERVER_TICK.register(FeatureNotifier::onServerTick);
    }

    /**
     * 全局特性变更广播：
     * 开启：§6[Allay] §f玩家 §e{玩家名} §f已将【§c{特性名}§f】设置为：§c开启
     * 关闭：§6[Allay] §f玩家 §e{玩家名} §f已将【§7{特性名}§f】设置为：§c关闭
     */
    public static void broadcastFeatureChange(MinecraftServer server, String playerName, String displayName, boolean enabled) {
        if (server == null) return;
        MutableComponent msg;
        if (enabled) {
            msg = Component.literal("§6[Allay] §f玩家 §e" + playerName + " §f已将【§c" + displayName + "§f】设置为：§c开启");
        } else {
            msg = Component.literal("§6[Allay] §f玩家 §e" + playerName + " §f已将【§7" + displayName + "§f】设置为：§c关闭");
        }
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    /**
     * 永久设置广播（setDefault）
     */
    public static void broadcastDefaultChange(MinecraftServer server, String playerName, String ruleName, String displayName, boolean enabled) {
        if (server == null) return;
        String valStr = enabled ? "§c开启" : "§c关闭";
        MutableComponent msg = Component.literal("§6[Allay] §f玩家 §e" + playerName + " §f已将【§e" + displayName + "§f】永久设置为：" + valStr);
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    /**
     * 玩家进服提醒：
     * §6[Allay] §7当前服务器已启用特性：§e[易碎玻璃] §e[超级TNT] §7(输入 /allay features 可查看详情)
     */
    public static void sendJoinNotification(ServerPlayer player) {
        FeatureManager fm = FeatureManager.getInstance();
        List<String> enabledList = new ArrayList<>();

        if (fm.isFragileGlass()) enabledList.add("易碎玻璃");
        if (fm.isFragileObsidian()) enabledList.add("易碎黑曜石");
        if (fm.isSuperTNT()) enabledList.add("超级TNT");
        if (fm.isExperienceBottle()) enabledList.add("经验玻璃瓶");
        if (fm.isPhantomSuppressor()) enabledList.add("幻翼抑制器");
        if (fm.isJungleSaplingDropRate()) enabledList.add("丛林树苗爆率");
        if (fm.isElytraRecipe()) enabledList.add("鞘翅合成");
        if (fm.isSpongeRecipe()) enabledList.add("海绵合成");

        if (enabledList.isEmpty()) {
            return;
        }

        MutableComponent msg = Component.literal("§6[Allay] §7当前服务器已启用特性：");
        for (String feat : enabledList) {
            msg.append(Component.literal("§e[" + feat + "] "));
        }
        msg.append(Component.literal("§7(输入 /allay features 可查看详情)"));

        player.sendSystemMessage(msg, false);
    }

    /**
     * Action Bar 提示检测：
     * 当易碎玻璃开启，手持镐子且准星看玻璃时显示：§c⚠【易碎玻璃】已开启
     * 当易碎黑曜石开启，手持镐子且准星看黑曜石时显示：§c⚠【易碎黑曜石】已开启
     */
    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter % 5 != 0) {
            return;
        }

        FeatureManager fm = FeatureManager.getInstance();
        boolean fragileGlass = fm.isFragileGlass();
        boolean fragileObsidian = fm.isFragileObsidian();

        if (!fragileGlass && !fragileObsidian) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.is(ItemTags.PICKAXES)) {
                continue;
            }

            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getViewVector(1.0f);
            Vec3 traceEnd = eyePos.add(lookVec.scale(6.0)); // 挖掘触及距离约 5-6 格

            BlockHitResult hit = player.level().clip(new ClipContext(
                    eyePos,
                    traceEnd,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (hit.getType() != HitResult.Type.BLOCK) {
                continue;
            }

            BlockPos pos = hit.getBlockPos();
            BlockState state = player.level().getBlockState(pos);

            if (fragileGlass && state.getSoundType() == SoundType.GLASS) {
                // sendSystemMessage(..., true) 会发送到 Action Bar (overlay)
                player.sendSystemMessage(Component.literal("§c⚠【易碎玻璃】已开启"), true);
            } else if (fragileObsidian && (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN))) {
                player.sendSystemMessage(Component.literal("§c⚠【易碎黑曜石】已开启"), true);
            }
        }
    }

    public static String getFeatureDisplayName(String ruleName) {
        return switch (ruleName) {
            case "fragileGlass" -> "易碎玻璃";
            case "fragileObsidian" -> "易碎黑曜石";
            case "superTNT" -> "超级TNT";
            case "experienceBottle" -> "经验玻璃瓶";
            case "phantomSuppressor" -> "幻翼抑制器";
            case "jungleSaplingDropRate" -> "丛林树苗爆率";
            case "elytraRecipe" -> "鞘翅合成";
            case "spongeRecipe" -> "海绵合成";
            default -> ruleName;
        };
    }
}
