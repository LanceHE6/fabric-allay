package cn.hycer.allay.tk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TrialStorage {

    public static final class BlockEntry {
        public final BlockPos pos;
        public final BlockState state;
        public final CompoundTag blockEntityData;

        public BlockEntry(BlockPos pos, BlockState state, CompoundTag blockEntityData) {
            this.pos = pos;
            this.state = state;
            this.blockEntityData = blockEntityData;
        }
    }

    private static final Map<String, List<BlockEntry>> REGIONS = new LinkedHashMap<>();
    private static Path saveFile;

    public static void setSaveFile(Path path) { saveFile = path; }

    public static void store(String name, List<BlockEntry> entries) {
        REGIONS.put(name, new ArrayList<>(entries));
        saveToFile();
    }

    public static List<BlockEntry> getAndRemove(String name) {
        List<BlockEntry> result = REGIONS.remove(name);
        saveToFile();
        return result != null ? result : new ArrayList<>();
    }

    public static void removeRegion(String name) { REGIONS.remove(name); saveToFile(); }

    public static List<BlockEntry> get(String name) {
        return REGIONS.getOrDefault(name, new ArrayList<>());
    }

    public static Map<String, List<BlockEntry>> getAllRegions() {
        return new LinkedHashMap<>(REGIONS);
    }

    public static int size(String name) {
        List<BlockEntry> entries = REGIONS.get(name);
        return entries != null ? entries.size() : 0;
    }

    public static int totalStored() {
        int total = 0;
        for (List<BlockEntry> entries : REGIONS.values()) total += entries.size();
        return total;
    }

    public static void clearAll() { REGIONS.clear(); saveToFile(); }

    private static void saveToFile() {
        if (saveFile == null) return;
        CompoundTag root = new CompoundTag();
        ListTag regionsList = new ListTag();
        for (Map.Entry<String, List<BlockEntry>> region : REGIONS.entrySet()) {
            CompoundTag regionTag = new CompoundTag();
            regionTag.putString("name", region.getKey());
            ListTag blocksList = new ListTag();
            for (BlockEntry entry : region.getValue()) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("x", entry.pos.getX());
                tag.putInt("y", entry.pos.getY());
                tag.putInt("z", entry.pos.getZ());
                Identifier blockId = BuiltInRegistries.BLOCK.getKey(entry.state.getBlock());
                tag.putString("block", blockId != null ? blockId.toString() : "minecraft:air");
                if (entry.blockEntityData != null) tag.put("be", entry.blockEntityData);
                blocksList.add(tag);
            }
            regionTag.put("blocks", blocksList);
            regionsList.add(regionTag);
        }
        root.put("regions", regionsList);
        try {
            Files.createDirectories(saveFile.getParent());
            Path tmp = saveFile.resolveSibling(saveFile.getFileName() + ".tmp");
            NbtIo.writeCompressed(root, tmp);
            Files.move(tmp, saveFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {}
    }

    public static void loadFromFile() {
        if (saveFile == null || !Files.exists(saveFile)) return;
        try {
            CompoundTag root = NbtIo.readCompressed(saveFile, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            if (root == null) return;
            ListTag regionsList = root.getListOrEmpty("regions");
            for (int r = 0; r < regionsList.size(); r++) {
                CompoundTag regionTag = regionsList.getCompoundOrEmpty(r);
                String name = regionTag.getStringOr("name", "");
                if (name.isEmpty()) continue;
                ListTag blocksList = regionTag.getListOrEmpty("blocks");
                List<BlockEntry> entries = new ArrayList<>();
                for (int i = 0; i < blocksList.size(); i++) {
                    CompoundTag tag = blocksList.getCompoundOrEmpty(i);
                    int x = tag.getIntOr("x", 0);
                    int y = tag.getIntOr("y", 0);
                    int z = tag.getIntOr("z", 0);
                    BlockPos pos = new BlockPos(x, y, z);
                    String blockId = tag.getStringOr("block", "");
                    if (blockId.isEmpty()) continue;
                    Block block = BuiltInRegistries.BLOCK.get(Identifier.tryParse(blockId))
                        .map(net.minecraft.core.Holder.Reference::value).orElse(null);
                    if (block == null) continue;
                    BlockState state = block.defaultBlockState();
                    CompoundTag beData = tag.contains("be") ? tag.getCompoundOrEmpty("be") : null;
                    entries.add(new BlockEntry(pos, state, beData));
                }
                REGIONS.put(name, entries);
            }
        } catch (IOException ignored) {}
    }
}
