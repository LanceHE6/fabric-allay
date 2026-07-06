package cn.hycer.allay.cbm.command.tree;

import cn.hycer.allay.cbm.command.handler.BotHandlers;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;

import static cn.hycer.allay.cbm.command.CommandSuggestions.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class BotCommandTree {

    private BotCommandTree() {}

    public static void addNodes(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("add")
                .then(argument("player", EntityArgument.player())
                        .executes(BotHandlers::addBot)
                        .then(argument("description", StringArgumentType.greedyString())
                                .executes(BotHandlers::addBot))))
            .then(literal("remove")
                .then(argument("name", StringArgumentType.word())
                        .suggests(BOT_NAME_SUGGESTIONS)
                        .executes(BotHandlers::removeBot)))
            .then(literal("load")
                .then(argument("name", StringArgumentType.word())
                        .suggests(BOT_NAME_SUGGESTIONS)
                        .executes(BotHandlers::loadBot)))
            .then(literal("help").executes(BotHandlers::showHelp))
            .then(literal("list").executes(BotHandlers::listBots));
    }
}
