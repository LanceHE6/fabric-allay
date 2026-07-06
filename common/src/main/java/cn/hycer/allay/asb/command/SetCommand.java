package cn.hycer.allay.asb.command;

import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.asb.config.ScoreboardItem;
import cn.hycer.allay.asb.event.ServerStartedEvent;
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
                    })));
    }
}
