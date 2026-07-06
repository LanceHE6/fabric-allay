package cn.hycer.allay.cbm.command.tree;

import cn.hycer.allay.cbm.command.handler.AutoLoadHandlers;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

import static cn.hycer.allay.cbm.command.CommandSuggestions.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class AutoLoadCommandTree {

    private AutoLoadCommandTree() {}

    public static void addNodes(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("autoload")
                .then(literal("add")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(BOT_NAME_SUGGESTIONS)
                                .executes(AutoLoadHandlers::addAutoLoadBot)))
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(AUTOLOAD_BOT_SUGGESTIONS)
                                .executes(AutoLoadHandlers::removeAutoLoadBot)))
                .then(literal("list").executes(AutoLoadHandlers::listAutoLoad)));
    }
}
