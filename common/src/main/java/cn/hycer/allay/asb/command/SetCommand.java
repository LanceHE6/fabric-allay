package cn.hycer.allay.asb.command;

import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.asb.config.ScoreboardItem;
import cn.hycer.allay.asb.event.ServerStartedEvent;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SetCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return literal("set")
            .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
            .then(literal("switchInterval")
                .then(argument("value", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        int value = IntegerArgumentType.getInteger(context, "value");
                        AllayConfig.getInstance().setSwitchInterval(value);
                        AllayConfig.getInstance().saveConfig();
                        context.getSource().sendSuccess(
                            () -> Component.literal("轮播间隔已设置为 " + value + " 秒"), false);
                        return 1;
                    })))
            .then(literal("saveInterval")
                .then(argument("value", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        int value = IntegerArgumentType.getInteger(context, "value");
                        AllayConfig.getInstance().setSaveInterval(value);
                        AllayConfig.getInstance().saveConfig();
                        context.getSource().sendSuccess(
                            () -> Component.literal("保存间隔已设置为 " + value + " 秒"), false);
                        return 1;
                    })))
            .then(literal("maxDisplayNum")
                .then(argument("value", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        int value = IntegerArgumentType.getInteger(context, "value");
                        AllayConfig.getInstance().setMaxDisplayNum(value);
                        AllayConfig.getInstance().saveConfig();
                        context.getSource().sendSuccess(
                            () -> Component.literal("最大显示数量已设置为 " + value), false);
                        return 1;
                    })))
            .then(literal("border")
                .then(argument("value", StringArgumentType.word())
                    .executes(context -> {
                        String value = StringArgumentType.getString(context, "value");
                        AllayConfig.getInstance().setBorder(value);
                        AllayConfig.getInstance().saveConfig();
                        ServerStartedEvent.refreshAllDisplayNames();
                        context.getSource().sendSuccess(
                            () -> Component.literal("边框已设置为 " + value), false);
                        return 1;
                    })))
            .then(literal("skipScore")
                .then(argument("value", BoolArgumentType.bool())
                    .executes(context -> {
                        boolean value = BoolArgumentType.getBool(context, "value");
                        AllayConfig.getInstance().setSkipScore(value);
                        AllayConfig.getInstance().saveConfig();
                        context.getSource().sendSuccess(
                            () -> Component.literal("积分跳过已" + (value ? "开启" : "关闭")), false);
                        return 1;
                    })))
            .then(literal("skipPrefix")
                .then(argument("value", StringArgumentType.word())
                    .executes(context -> {
                        String value = StringArgumentType.getString(context, "value");
                        AllayConfig.getInstance().setSkipPrefix(value);
                        AllayConfig.getInstance().saveConfig();
                        context.getSource().sendSuccess(
                            () -> Component.literal("跳过前缀已设置为 " + value), false);
                        return 1;
                    })))
            // Global hide (OP-only, moved under set)
            .then(literal("notDisplay")
                .then(argument("displayName", StringArgumentType.greedyString())
                    .suggests(ScoreboardCommand.DISPLAY_NAME_SUGGESTIONS)
                    .executes(context -> {
                        String displayName = StringArgumentType.getString(context, "displayName");
                        ScoreboardItem item = AllayConfig.getInstance().getScoreboards().stream()
                            .filter(sb -> displayName.equals(sb.getDisplayName()))
                            .findFirst().orElse(null);
                        if (item == null) {
                            context.getSource().sendFailure(Component.literal("未找到榜单: " + displayName));
                            return 0;
                        }
                        boolean hidden = AllayConfig.getInstance().toggleScoreboardVisibility(item.getInternalName());
                        AllayConfig.getInstance().saveConfig();
                        String msg = hidden ? "已全局隐藏榜单: " + item.getDisplayName()
                                           : "已全局显示榜单: " + item.getDisplayName();
                        context.getSource().sendSuccess(() -> Component.literal(msg), false);
                        return 1;
                    })));
    }
}
