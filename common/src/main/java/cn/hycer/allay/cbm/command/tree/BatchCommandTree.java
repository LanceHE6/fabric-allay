package cn.hycer.allay.cbm.command.tree;

import cn.hycer.allay.cbm.command.handler.BatchHandlers;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class BatchCommandTree {

    private BatchCommandTree() {}

    public static void addNodes(LiteralArgumentBuilder<CommandSourceStack> root) {
        var spawnAt = argument("x", DoubleArgumentType.doubleArg())
                .then(argument("y", DoubleArgumentType.doubleArg())
                        .then(argument("z", DoubleArgumentType.doubleArg())
                                .executes(BatchHandlers::batchSpawn)
                                .then(literal("in")
                                        .then(argument("dim", StringArgumentType.word())
                                                .executes(BatchHandlers::batchSpawn)))));

        var spawnIn = literal("in")
                .then(argument("dim", StringArgumentType.word())
                        .executes(BatchHandlers::batchSpawn)
                        .then(spawnAt));

        var spawnNode = literal("spawn")
                .executes(BatchHandlers::batchSpawn)
                .then(literal("at").then(spawnAt))
                .then(spawnIn);

        var useNode = literal("use")
                .executes(BatchHandlers::batchUse)
                .then(literal("continuous")
                        .executes(BatchHandlers::batchUseContinuous))
                .then(literal("interval")
                        .then(argument("ticks", IntegerArgumentType.integer(1))
                                .executes(BatchHandlers::batchUseInterval)));

        var attackNode = literal("attack")
                .executes(BatchHandlers::batchAttack)
                .then(literal("continuous")
                        .executes(BatchHandlers::batchAttackContinuous))
                .then(literal("interval")
                        .then(argument("ticks", IntegerArgumentType.integer(1))
                                .executes(BatchHandlers::batchAttackInterval)));

        root.then(literal("batch")
                .then(argument("prefix", StringArgumentType.word())
                        .then(argument("start", IntegerArgumentType.integer(1))
                                .then(argument("end", IntegerArgumentType.integer(1))
                                        .then(spawnNode)
                                        .then(literal("save").executes(BatchHandlers::batchSave))
                                        .then(literal("kill").executes(BatchHandlers::batchKill))
                                        .then(useNode)
                                        .then(attackNode)
                                        .then(literal("sneak").executes(BatchHandlers::batchSneak))))));
    }
}
