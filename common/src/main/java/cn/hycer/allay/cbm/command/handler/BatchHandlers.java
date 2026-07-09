package cn.hycer.allay.cbm.command.handler;

import cn.hycer.allay.cbm.data.BotDataManager;
import cn.hycer.allay.cbm.model.BotGroup;
import cn.hycer.allay.cbm.model.BotPreset;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BatchHandlers {

    private static final SimpleCommandExceptionType RANGE_INVALID =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.batch_range", "起始值必须小于等于结束值。"));

    private BatchHandlers() {}

    public static int batchSpawn(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String prefix = StringArgumentType.getString(ctx, "prefix");
        int start = IntegerArgumentType.getInteger(ctx, "start");
        int end = IntegerArgumentType.getInteger(ctx, "end");
        if (start > end) throw RANGE_INVALID.create();

        double x, y, z;
        float yaw, pitch;
        try {
            x = DoubleArgumentType.getDouble(ctx, "x");
            y = DoubleArgumentType.getDouble(ctx, "y");
            z = DoubleArgumentType.getDouble(ctx, "z");
            yaw = 0; pitch = 0;
        } catch (IllegalArgumentException e) {
            ServerPlayer player = src.getPlayerOrException();
            x = player.getX(); y = player.getY(); z = player.getZ();
            yaw = player.getYRot(); pitch = player.getXRot();
        }
        String dim;
        try {
            dim = StringArgumentType.getString(ctx, "dim");
        } catch (IllegalArgumentException e) {
            try { dim = src.getPlayerOrException().level().dimension().identifier().toString(); }
            catch (CommandSyntaxException ex) { dim = "minecraft:overworld"; }
        }

        for (int i = start; i <= end; i++) {
            String name = prefix + "_" + i;
            String cmd = String.format(Locale.ROOT,
                    "player %s spawn at %.2f %.2f %.2f facing %.2f %.2f in %s",
                    name, x, y, z, yaw, pitch, dim);
            // Use summoner's CommandSourceStack so bot inherits game mode
            src.getServer().getCommands().performPrefixedCommand(src, cmd);
        }
        int count = end - start + 1;
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.batch.spawn.success",
                "已召唤 %s 个 Bot：%s_%s ~ %s_%s。", count, prefix, start, prefix, end));
        return 1;
    }

    public static int batchSave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String prefix = StringArgumentType.getString(ctx, "prefix");
        int start = IntegerArgumentType.getInteger(ctx, "start");
        int end = IntegerArgumentType.getInteger(ctx, "end");
        if (start > end) throw RANGE_INVALID.create();

        List<String> missing = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String name = prefix + "_" + i;
            if (src.getServer().getPlayerList().getPlayerByName(name) == null) missing.add(name);
        }
        if (!missing.isEmpty()) {
            throw new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.batch_not_all_online", "所有 Bot 必须在线。缺失: %s",
                    String.join(", ", missing))).create();
        }
        BotDataManager dm = BotDataManager.getInstance();
        List<String> names = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String name = prefix + "_" + i;
            ServerPlayer bot = src.getServer().getPlayerList().getPlayerByName(name);
            dm.addBotPreset(new BotPreset(name, "",
                    bot.level().dimension().identifier().toString(),
                    bot.getX(), bot.getY(), bot.getZ(),
                    bot.getYRot(), bot.getXRot(),
                    bot.getX(), bot.getEyeY(), bot.getZ()));
            names.add(name);
        }
        dm.addBotGroup(new BotGroup(prefix, "", names));
        int count = names.size();
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.batch.save.success",
                "已将 %s 个 Bot 预设保存到分组 '%s'：%s_%s ~ %s_%s。",
                count, prefix, prefix, start, prefix, end));
        return 1;
    }

    private static int runBatchAction(CommandContext<CommandSourceStack> ctx,
                                      String playerAction, String desc) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String prefix = StringArgumentType.getString(ctx, "prefix");
        int start = IntegerArgumentType.getInteger(ctx, "start");
        int end = IntegerArgumentType.getInteger(ctx, "end");
        if (start > end) throw RANGE_INVALID.create();

        int done = 0, miss = 0;
        for (int i = start; i <= end; i++) {
            String name = prefix + "_" + i;
            if (src.getServer().getPlayerList().getPlayerByName(name) != null) {
                String cmd = "player " + name + " " + playerAction;
                src.getServer().getCommands().performPrefixedCommand(
                        src.getServer().createCommandSourceStack(), cmd);
                done++;
            } else { miss++; }
        }
        int count = end - start + 1;
        src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.batch.action.success",
                "%s完成：%s/%s 个 Bot %s。", desc, done, count, desc));
        if (miss > 0) src.sendSystemMessage(Component.translatableWithFallback(
                "carpetbotmanager.command.batch.action.missing", "注意：%s 个 Bot 不在线，已跳过。", miss));
        return 1;
    }

    public static int batchKill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return runBatchAction(ctx, "kill", "下线");
    }
    public static int batchUse(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return runBatchAction(ctx, "use once", "使用");
    }
    public static int batchUseContinuous(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return runBatchAction(ctx, "use continuous", "持续使用");
    }
    public static int batchUseInterval(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
        return runBatchAction(ctx, "use interval " + ticks, "间隔" + ticks + "tick使用");
    }
    public static int batchAttack(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return runBatchAction(ctx, "attack once", "攻击");
    }
    public static int batchAttackContinuous(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return runBatchAction(ctx, "attack continuous", "持续攻击");
    }
    public static int batchAttackInterval(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
        return runBatchAction(ctx, "attack interval " + ticks, "间隔" + ticks + "tick攻击");
    }
    public static int batchSneak(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return runBatchAction(ctx, "sneak", "潜行切换");
    }
}
