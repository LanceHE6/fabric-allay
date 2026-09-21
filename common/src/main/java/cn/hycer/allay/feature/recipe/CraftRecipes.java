package cn.hycer.allay.feature.recipe;

import cn.hycer.allay.Allay;
import cn.hycer.allay.feature.FeatureManager;
import cn.hycer.allay.util.VersionCompat;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义工作台合成配方（鞘翅 / 海绵），由 FeatureManager 开关控制。
 *
 * <p>实现原理：
 * <ul>
 *   <li>服务器启动 / 数据包重载完成后，把自定义配方注入 RecipeManager
 *       （重建 RecipeMap 写回 recipes 字段，再调 finalizeRecipeLoading 重建显示缓存）；</li>
 *   <li>开关关闭时把配方从 RecipeManager 移除 —— 服务端合成匹配走
 *       getRecipeFor，配方不存在则摆对材料也不出预览、无法取出（彻底不可见）；</li>
 *   <li>通过 ServerRecipeBook.addRecipes / removeRecipes 向全体在线玩家广播
 *       配方书 add/remove 数据包并同步各自的上锁记录，配方书界面即时解锁 / 上锁。</li>
 * </ul>
 *
 * <p>时序要点：removeRecipes 解析 displayId 依赖 RecipeManager 显示缓存，
 * 因此「先广播移除、后清缓存」；addRecipes 相反，「先注缓存、后广播添加」。
 */
public final class CraftRecipes {

    /** 鞘翅配方 ID: allay:elytra_craft */
    public static final ResourceKey<Recipe<?>> ELYTRA_KEY =
            ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,
                    Identifier.fromNamespaceAndPath(Allay.MOD_ID, "elytra_craft"));
    /** 海绵配方 ID: allay:sponge_craft */
    public static final ResourceKey<Recipe<?>> SPONGE_KEY =
            ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,
                    Identifier.fromNamespaceAndPath(Allay.MOD_ID, "sponge_craft"));

    private static Field recipesField;

    /** 供命令层即时切换用的 holder 工厂。 */
    public static RecipeHolder<ShapedRecipe> buildElytraRecipeHolder() { return buildElytraRecipe(); }

    /** 供命令层即时切换用的 holder 工厂。 */
    public static RecipeHolder<ShapedRecipe> buildSpongeRecipeHolder() { return buildSpongeRecipe(); }

    // ── 配方定义 ────────────────────────────────────────────────

    /**
     * 鞘翅合成（刻意非对称，格 8 为空）：
     * <pre>
     * 膜 线 膜          膜=幻翼膜  线=线  锭=下界合金锭
     * 膜 锭 膜          → 鞘翅 ×1
     * 膜 _  膜
     * </pre>
     */
    private static RecipeHolder<ShapedRecipe> buildElytraRecipe() {
        Map<Character, Ingredient> key = new LinkedHashMap<>();
        key.put('M', Ingredient.of(Items.PHANTOM_MEMBRANE));
        key.put('S', Ingredient.of(Items.STRING));
        key.put('N', Ingredient.of(Items.NETHERITE_INGOT));
        return holder(ELYTRA_KEY, ShapedRecipePattern.of(key, "MSM", "MNM", "M M"),
                Items.ELYTRA, 1);
    }

    /**
     * 海绵合成（对称）：
     * <pre>
     * 沙 纸 沙          沙=沙子  纸=纸  羊=黄色羊毛
     * 纸 羊 纸          → 海绵 ×2
     * 沙 纸 沙
     * </pre>
     */
    private static RecipeHolder<ShapedRecipe> buildSpongeRecipe() {
        Map<Character, Ingredient> key = new LinkedHashMap<>();
        key.put('A', Ingredient.of(item("sand")));
        key.put('P', Ingredient.of(Items.PAPER));
        key.put('W', Ingredient.of(item("yellow_wool")));
        return holder(SPONGE_KEY, ShapedRecipePattern.of(key, "APA", "PWP", "APA"),
                Items.SPONGE, 2);
    }

    private static RecipeHolder<ShapedRecipe> holder(ResourceKey<Recipe<?>> id,
                                                     ShapedRecipePattern pattern,
                                                     Item result, int count) {
        var common = new Recipe.CommonInfo(true);                       // showNotification=true
        var book = new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "");
        return new RecipeHolder<>(id,
                new ShapedRecipe(common, book, pattern, new ItemStackTemplate(result, count)));
    }

    /** 按命名空间 ID 从物品注册表取物品，避免依赖跨版本有差异的常量字段。 */
    private static Item item(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path));
    }

    // ── 生命周期 ────────────────────────────────────────────────

    public static void init() {
        // 服务器启动完成后首次注入（此时 vanilla 已完成首轮 finalize）
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                syncFromFeatures(server);
                Allay.LOGGER.info("[CraftRecipes] synced on startup (elytra={}, sponge={})",
                        FeatureManager.getInstance().isElytraRecipe(),
                        FeatureManager.getInstance().isSpongeRecipe());
            } catch (Exception e) {
                Allay.LOGGER.error("[CraftRecipes] startup sync failed", e);
            }
        });

        // /reload 会用数据包内容整体重建 RecipeManager，结束后按当前开关重新注入
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (!success) return;
            try {
                syncFromFeatures(server);
                Allay.LOGGER.info("[CraftRecipes] re-synced after datapack reload");
            } catch (Exception e) {
                Allay.LOGGER.error("[CraftRecipes] post-reload sync failed", e);
            }
        });
    }

    /** 按当前 FeatureManager 开关状态同步两条配方（启动、reload、命令切换后调用）。 */
    public static void syncFromFeatures(MinecraftServer server) {
        setEnabled(server, ELYTRA_KEY, buildElytraRecipe(),
                FeatureManager.getInstance().isElytraRecipe());
        setEnabled(server, SPONGE_KEY, buildSpongeRecipe(),
                FeatureManager.getInstance().isSpongeRecipe());
    }

    /**
     * 单条配方即时切换（命令层调用）：注入或移除指定配方并广播配方书变更。
     * 与 setEnabled 相同语义，仅公开给同包外使用。
     */
    public static void applySingle(MinecraftServer server,
                                   ResourceKey<Recipe<?>> id,
                                   RecipeHolder<ShapedRecipe> holder,
                                   boolean enable) {
        setEnabled(server, id, holder, enable);
    }

    // ── 注入 / 移除 ─────────────────────────────────────────────

    private static void setEnabled(MinecraftServer server,
                                   ResourceKey<Recipe<?>> id,
                                   RecipeHolder<ShapedRecipe> holder,
                                   boolean enable) {
        try {
            RecipeManager mgr = server.getRecipeManager();
            List<RecipeHolder<?>> values =
                    new ArrayList<>(currentMap(mgr).values());
            boolean exists = values.stream().anyMatch(h -> h.id().equals(id));

            if (enable && exists) {
                return;                                     // 已处于开启态，无需动作
            }
            if (!enable && !exists) {
                return;                                     // 已处于关闭态
            }

            if (!enable) {
                // ① 先广播移除（此时显示缓存仍含本配方，才能解析出 displayId）
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.getRecipeBook()
                          .removeRecipes(List.of(holder), player);
                }
                // ② 再清缓存
                values.removeIf(h -> h.id().equals(id));
                writeAndFinalize(mgr, values, server);
                Allay.LOGGER.info("[CraftRecipes] disabled {}", id.identifier());
            } else {
                // reload 场景下残留同名项则先剔除
                values.removeIf(h -> h.id().equals(id));
                // ① 注入缓存
                values.add(holder);
                writeAndFinalize(mgr, values, server);
                // ② 广播添加（从新缓存解析 displayId）
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.getRecipeBook()
                          .addRecipes(List.of(holder), player);
                }
                Allay.LOGGER.info("[CraftRecipes] enabled {}", id.identifier());
            }
        } catch (Exception e) {
            Allay.LOGGER.error("[CraftRecipes] failed to toggle {} -> {}", id.identifier(), enable, e);
        }
    }

    private static RecipeMap currentMap(RecipeManager mgr) throws ReflectiveOperationException {
        synchronized (CraftRecipes.class) {
            if (recipesField == null) {
                recipesField = RecipeManager.class.getDeclaredField("recipes");
                recipesField.setAccessible(true);
            }
            return (RecipeMap) recipesField.get(mgr);
        }
    }

    private static void writeAndFinalize(RecipeManager mgr,
                                         List<RecipeHolder<?>> values,
                                         MinecraftServer server)
            throws ReflectiveOperationException {
        synchronized (CraftRecipes.class) {
            FeatureFlagSet flags = server.getWorldData().enabledFeatures();
            recipesField.set(mgr, VersionCompat.createRecipeMap(values));
            mgr.finalizeRecipeLoading(flags);
        }
    }

    private CraftRecipes() {}
}
