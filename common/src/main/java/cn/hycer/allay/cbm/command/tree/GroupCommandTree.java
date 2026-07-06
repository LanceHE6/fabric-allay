package cn.hycer.allay.cbm.command.tree;

import cn.hycer.allay.cbm.command.handler.AutoLoadHandlers;
import cn.hycer.allay.cbm.command.handler.GroupHandlers;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

import static cn.hycer.allay.cbm.command.CommandSuggestions.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class GroupCommandTree {

    private GroupCommandTree() {}

    public static void addNodes(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("group")
                .then(literal("add")
                        .then(argument("groupName", StringArgumentType.word())
                                .then(argument("description", StringArgumentType.string())
                                        .then(argument("bots", StringArgumentType.greedyString())
                                                .suggests(BOT_NAME_SUGGESTIONS)
                                                .executes(GroupHandlers::addGroup)))))
                .then(literal("remove")
                        .then(argument("groupName", StringArgumentType.word())
                                .suggests(GROUP_NAME_SUGGESTIONS)
                                .executes(GroupHandlers::removeGroup)))
                .then(literal("load")
                        .then(argument("groupName", StringArgumentType.word())
                                .suggests(GROUP_NAME_SUGGESTIONS)
                                .executes(GroupHandlers::loadGroup)))
                .then(literal("autoload")
                        .then(literal("add")
                                .then(argument("groupName", StringArgumentType.word())
                                        .suggests(GROUP_NAME_SUGGESTIONS)
                                        .executes(AutoLoadHandlers::addAutoLoadGroup)))
                        .then(literal("remove")
                                .then(argument("groupName", StringArgumentType.word())
                                        .suggests(AUTOLOAD_GROUP_SUGGESTIONS)
                                        .executes(AutoLoadHandlers::removeAutoLoadGroup)))));
    }
}
