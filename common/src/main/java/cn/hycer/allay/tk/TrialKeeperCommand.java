package cn.hycer.allay.tk;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TrialKeeperCommand {

    private static final Block OMINOUS_VAULT;

    static {
        OMINOUS_VAULT = BuiltInRegistries.BLOCK.get(Identifier.tryParse("minecraft:ominous_vault"))
            .map(net.minecraft.core.Holder.Reference::value)
            .orElse(null);
    }

    private static boolean isTargetBlock(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.VAULT
            || block == Blocks.TRIAL_SPAWNER
            || block == OMINOUS_VAULT;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildRemove() {
        return literal("remove")
                .then(argument("name", StringArgumentType.word())
                        .then(argument("from", BlockPosArgument.blockPos())
                                .then(argument("to", BlockPosArgument.blockPos())
                                        .executes(TrialKeeperCommand::removeBlocks))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildRestore() {
        return literal("restore")
                .then(argument("name", StringArgumentType.word())
                        .executes(TrialKeeperCommand::restoreBlocks));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildList() {
        return literal("list")
                .executes(TrialKeeperCommand::listRegions)
                .then(argument("name", StringArgumentType.word())
                        .executes(TrialKeeperCommand::listRegionDetail));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildClear() {
        return literal("clear")
                .executes(TrialKeeperCommand::clearAllRegions)
                .then(argument("name", StringArgumentType.word())
                        .executes(TrialKeeperCommand::clearRegion));
    }

    private static int removeBlocks(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        String name = StringArgumentType.getString(context, "name");
        BlockPos from = BlockPosArgument.getBlockPos(context, "from");
        BlockPos to = BlockPosArgument.getBlockPos(context, "to");

        BlockPos min = new BlockPos(
            Math.min(from.getX(), to.getX()),
            Math.min(from.getY(), to.getY()),
            Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(
            Math.max(from.getX(), to.getX()),
            Math.max(from.getY(), to.getY()),
            Math.max(from.getZ(), to.getZ()));

        List<TrialStorage.BlockEntry> entries = new ArrayList<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!isTargetBlock(state)) continue;
                    CompoundTag beData = null;
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be != null) beData = be.saveWithFullMetadata(level.registryAccess());
                    entries.add(new TrialStorage.BlockEntry(pos, state, beData));
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        TrialStorage.store(name, entries);
        int count = entries.size();
        String regionName = name;
        context.getSource().sendSuccess(() -> Component.literal("Removed " + count + " target block(s) from "
            + from.toShortString() + " to " + to.toShortString() + " [region: " + regionName + "]"), true);
        return 1;
    }

    private static int restoreBlocks(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        String name = StringArgumentType.getString(context, "name");
        List<TrialStorage.BlockEntry> entries = TrialStorage.getAndRemove(name);
        if (entries.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No blocks stored for region '" + name + "'"));
            return 0;
        }
        int restored = 0;
        for (TrialStorage.BlockEntry entry : entries) {
            level.setBlock(entry.pos, entry.state, 3);
            if (entry.blockEntityData != null) {
                BlockEntity be = level.getBlockEntity(entry.pos);
                if (be != null) {
                    be.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                        net.minecraft.util.ProblemReporter.DISCARDING,
                        level.registryAccess(),
                        entry.blockEntityData));
                }
            }
            restored++;
        }
        int finalRestored = restored;
        context.getSource().sendSuccess(() -> Component.literal("Restored " + finalRestored + " block(s) for region '" + name + "'"), true);
        return 1;
    }

    private static int listRegions(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        Map<String, List<TrialStorage.BlockEntry>> regions = TrialStorage.getAllRegions();
        if (regions.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No regions stored"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Stored regions (" + TrialStorage.totalStored() + " blocks total):"), false);
            for (Map.Entry<String, List<TrialStorage.BlockEntry>> entry : regions.entrySet()) {
                String line = "  " + entry.getKey() + " - " + entry.getValue().size() + " block(s)";
                context.getSource().sendSuccess(() -> Component.literal(line), false);
            }
        }
        return 1;
    }

    private static int listRegionDetail(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        List<TrialStorage.BlockEntry> entries = TrialStorage.get(name);
        if (entries.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("Region '" + name + "' not found"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Region '" + name + "' - " + entries.size() + " block(s):"), false);
            for (TrialStorage.BlockEntry entry : entries) {
                String blockName = BuiltInRegistries.BLOCK.getKey(entry.state.getBlock()).toString();
                String line = "  [" + entry.pos.getX() + ", " + entry.pos.getY() + ", " + entry.pos.getZ() + "] " + blockName;
                context.getSource().sendSuccess(() -> Component.literal(line), false);
                if (entry.blockEntityData != null) {
                    String nbt = entry.blockEntityData.toString();
                    for (String nbtLine : nbt.split("\n"))
                        context.getSource().sendSuccess(() -> Component.literal("    " + nbtLine), false);
                }
            }
        }
        return 1;
    }

    private static int clearAllRegions(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        int count = TrialStorage.totalStored();
        TrialStorage.clearAll();
        int finalCount = count;
        context.getSource().sendSuccess(() -> Component.literal("Cleared all regions (" + finalCount + " blocks)"), true);
        return 1;
    }

    private static int clearRegion(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        int count = TrialStorage.size(name);
        TrialStorage.removeRegion(name);
        int finalCount = count;
        context.getSource().sendSuccess(() -> Component.literal("Cleared region '" + name + "' (" + finalCount + " blocks)"), true);
        return 1;
    }
}
