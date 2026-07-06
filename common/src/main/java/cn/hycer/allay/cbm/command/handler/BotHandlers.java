package cn.hycer.allay.cbm.command.handler;

import cn.hycer.allay.AllayConfig;
import cn.hycer.allay.cbm.command.BotSpawner;
import cn.hycer.allay.cbm.data.BotDataManager;
import cn.hycer.allay.cbm.model.BotGroup;
import cn.hycer.allay.cbm.model.BotPreset;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Locale;

import static cn.hycer.allay.cbm.command.CommandExceptions.*;

public final class BotHandlers {

    private BotHandlers() {}

    public static int showHelp(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.help.header", "=== /allay cbot 指令用法 ==="));
        for (String line : new String[]{
                "/allay cbot add <player> [description]",
                "/allay cbot remove <name>", "/allay cbot load <name>",
                "/allay cbot list", "/allay cbot help", "/allay cbot ui",
                "/allay cbot autoload add <name>", "/allay cbot autoload remove <name>",
                "/allay cbot autoload list",
                "/allay cbot group add <name> <description> <bot1 bot2 ...>",
                "/allay cbot group remove <name>", "/allay cbot group load <name>",
                "/allay cbot group autoload add <name>",
                "/allay cbot group autoload remove <name>",
                "/allay cbot batch <prefix> <start> <end> spawn [at <x> <y> <z>] [in <dim>]",
                "/allay cbot batch <prefix> <start> <end> save",
                "/allay cbot batch <prefix> <start> <end> kill",
                "/allay cbot batch <prefix> <start> <end> use [continuous|interval <ticks>]",
                "/allay cbot batch <prefix> <start> <end> attack [continuous|interval <ticks>]",
                "/allay cbot batch <prefix> <start> <end> sneak",
        }) { src.sendSystemMessage(Component.literal("  " + line)); }
        return 1;
    }

    public static int addBot(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String botName = player.getGameProfile().name();
        String prefix = AllayConfig.getInstance().getBotNamePrefix();

        if (AllayConfig.getInstance().isRequirePrefix()
                && !botName.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
            throw NOT_BOT_PREFIX.create();

        BotDataManager dm = BotDataManager.getInstance();
        if (dm.hasBotPreset(botName)) throw BOT_ALREADY_EXISTS.create();

        String desc;
        try { desc = StringArgumentType.getString(ctx, "description"); }
        catch (IllegalArgumentException e) { desc = ""; }

        Vec3 look = player.getLookAngle();
        BotPreset preset = new BotPreset(botName, desc,
                player.level().dimension().identifier().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot(),
                player.getX() + look.x, player.getEyeY() + look.y, player.getZ() + look.z);
        dm.addBotPreset(preset);
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.add.success", "Bot 预设 '%s' 已保存。", botName));
        return 1;
    }

    public static int removeBot(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (!BotDataManager.getInstance().hasBotPreset(name)) throw BOT_NOT_FOUND.create();
        BotDataManager.getInstance().removeBotPreset(name);
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.remove.success", "Bot 预设 '%s' 已移除。", name));
        return 1;
    }

    public static int loadBot(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        BotPreset preset = BotDataManager.getInstance().getBotPreset(name)
                .orElseThrow(BOT_NOT_FOUND::create);
        BotSpawner.spawn(src, preset);
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.load.success", "Bot '%s' 已召唤。", name));
        return 1;
    }

    public static int listBots(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        BotDataManager dm = BotDataManager.getInstance();
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.list.header", "=== Allay Carpet Bot Manager ==="));
        Collection<BotPreset> bots = dm.getAllBotPresets();
        if (bots.isEmpty()) {
            src.sendSystemMessage(Component.translatableWithFallback(
                    "carpetbotmanager.command.list.no_bots", "没有已保存的 bot 预设。"));
        } else {
            src.sendSystemMessage(Component.translatableWithFallback(
                    "carpetbotmanager.command.list.bots_title", "已保存的 Bot："));
            for (BotPreset b : bots) {
                String d = b.getDescription() != null && !b.getDescription().isEmpty()
                        ? " - " + b.getDescription() : "";
                src.sendSystemMessage(Component.literal(String.format(
                        "  - %s%s (%.0f, %.0f, %.0f in %s)",
                        b.getName(), d, b.getX(), b.getY(), b.getZ(), b.getDimension())));
            }
        }
        Collection<BotGroup> groups = dm.getAllBotGroups();
        if (groups.isEmpty()) {
            src.sendSystemMessage(Component.translatableWithFallback(
                    "carpetbotmanager.command.list.no_groups", "没有已保存的 bot 组。"));
        } else {
            src.sendSystemMessage(Component.translatableWithFallback(
                    "carpetbotmanager.command.list.groups_title", "已保存的组："));
            for (BotGroup g : groups) {
                String d = g.getDescription() != null && !g.getDescription().isEmpty()
                        ? " - " + g.getDescription() : "";
                src.sendSystemMessage(Component.literal(String.format(
                        "  - %s%s [%s]", g.getName(), d, String.join(", ", g.getBots()))));
            }
        }
        return 1;
    }
}
