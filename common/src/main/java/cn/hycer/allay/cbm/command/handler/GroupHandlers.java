package cn.hycer.allay.cbm.command.handler;

import cn.hycer.allay.cbm.command.BotSpawner;
import cn.hycer.allay.cbm.data.BotDataManager;
import cn.hycer.allay.cbm.model.BotGroup;
import cn.hycer.allay.cbm.model.BotPreset;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import static cn.hycer.allay.cbm.command.CommandExceptions.*;

public final class GroupHandlers {

    private GroupHandlers() {}

    public static int addGroup(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String groupName = StringArgumentType.getString(ctx, "groupName");
        String desc = StringArgumentType.getString(ctx, "description");
        String botsStr = StringArgumentType.getString(ctx, "bots");
        BotDataManager dm = BotDataManager.getInstance();
        if (dm.hasBotGroup(groupName)) throw GROUP_ALREADY_EXISTS.create();
        List<String> v = new ArrayList<>(), nf = new ArrayList<>();
        for (String n : botsStr.split("\\s+")) {
            if (dm.hasBotPreset(n)) {
                v.add(n);
            } else {
                ServerPlayer p = src.getServer().getPlayerList().getPlayerByName(n);
                if (p != null) {
                    dm.addBotPreset(new BotPreset(n, "",
                            p.level().dimension().identifier().toString(),
                            p.getX(), p.getY(), p.getZ(),
                            p.getYRot(), p.getXRot(),
                            p.getX(), p.getEyeY(), p.getZ()));
                    v.add(n);
                } else { nf.add(n); }
            }
        }
        if (v.isEmpty()) throw BOTS_NOT_FOUND_FOR_GROUP.create();
        dm.addBotGroup(new BotGroup(groupName, desc, v));
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.group.add.success", "组 '%s' 已创建，包含 %d 个 bot。", groupName, v.size()));
        if (!nf.isEmpty()) src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.group.add.partial", "警告：部分 bot 未找到，已跳过：%s", String.join(", ", nf)));
        return 1;
    }

    public static int removeGroup(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "groupName");
        if (!BotDataManager.getInstance().hasBotGroup(name)) throw GROUP_NOT_FOUND.create();
        BotDataManager.getInstance().removeBotGroup(name);
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.group.remove.success", "组 '%s' 已移除。", name));
        return 1;
    }

    public static int loadGroup(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "groupName");
        BotDataManager dm = BotDataManager.getInstance();
        BotGroup g = dm.getBotGroup(name).orElseThrow(GROUP_NOT_FOUND::create);
        int ok = 0, fail = 0;
        for (String bn : g.getBots()) {
            BotPreset p = dm.getBotPreset(bn).orElse(null);
            if (p != null) {
                try { BotSpawner.spawn(src, p); ok++; } catch (Exception e) { fail++;
                    src.sendSystemMessage(Component.translatableWithFallback(
                            "carpetbotmanager.command.group.load.failed_item", "加载 bot '%s' 失败：%s", bn, e.getMessage())); }
            } else { fail++; src.sendSystemMessage(Component.translatableWithFallback(
                    "carpetbotmanager.error.bot_not_found_item", "在保存的预设中未找到 bot '%s'。", bn)); }
        }
        final int finalOk = ok, finalFail = fail;
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.group.load.success", "组 '%s' 加载完成：%d 成功，%d 失败。", name, finalOk, finalFail));
        return 1;
    }
}
