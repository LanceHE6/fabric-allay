package cn.hycer.allay.cbm.command.tree;

import cn.hycer.allay.cbm.command.handler.ChatInterface;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

import static cn.hycer.allay.cbm.command.CommandSuggestions.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class UiCommandTree {

    private UiCommandTree() {}

    public static void addNodes(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("ui")
                .executes(ChatInterface::showMainMenu)
                .then(literal("bots").executes(ChatInterface::showBotList))
                .then(literal("groups").executes(ChatInterface::showGroupList))
                .then(literal("autoload")
                        .executes(ChatInterface::showAutoLoad)
                        .then(literal("add")
                                .then(argument("name", StringArgumentType.word())
                                        .suggests(BOT_NAME_SUGGESTIONS)
                                        .executes(ctx -> ChatInterface.showAutoLoadAddBot(
                                                ctx, StringArgumentType.getString(ctx, "name")))))
                        .then(literal("group").then(literal("add")
                                .then(argument("name", StringArgumentType.word())
                                        .suggests(GROUP_NAME_SUGGESTIONS)
                                        .executes(ctx -> ChatInterface.showAutoLoadAddGroup(
                                                ctx, StringArgumentType.getString(ctx, "name")))))))
                .then(literal("add").executes(ChatInterface::showAddHelp))
                .then(literal("batch").executes(ChatInterface::showBatchMenu)));
    }
}
