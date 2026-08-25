package cn.hycer.allay.feature.recipe;

import cn.hycer.allay.Allay;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * 配方匹配自验：在真实 runServer 环境（Knot 类加载器 + 完整注册表）中
 * 对两条自定义配方执行形状/产量断言，结果写入日志。
 *
 * <p>挂载于 SERVER_STARTED；由 fabric.mod.json 无关的纯服务端路径驱动。
 * 断言失败抛 AssertionError —— 日志会出现 FAIL 标记，全部通过输出 PASS。
 */
public final class RecipeMatchTest {

    /** 注册到服务器生命周期；由 Allay.onInitialize 调用。 */
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                run();
                Allay.LOGGER.info("[RecipeMatchTest] PASS");
            } catch (Throwable t) {
                Allay.LOGGER.error("[RecipeMatchTest] FAIL", t);
            }
        });
    }

    public static void run() {
        testElytra();
        testSponge();
    }

    /** 鞘翅: MSM / MNM / M M → 鞘翅×1（格8刻意为空的非对称） */
    private static void testElytra() {
        RecipeHolder<ShapedRecipe> h = CraftRecipes.buildElytraRecipeHolder();

        CraftingInput ok = input(new ItemStack[][]{
                {mem(), string(), mem()},
                {mem(), ingot(), mem()},
                {mem(), null,     mem()},
        });
        check("elytra 正确摆放应匹配", h.value().matches(ok, null));
        check("elytra 产出=1", h.value().assemble(ok).getCount() == 1);
        check("elytra 产出物=elytra", h.value().assemble(ok).is(Items.ELYTRA));

        CraftingInput sym = input(new ItemStack[][]{          // 错误：格8放线
                {mem(), string(), mem()},
                {mem(), ingot(), mem()},
                {mem(), string(), mem()},
        });
        check("elytra 对称摆法(8=线)不应匹配", !h.value().matches(sym, null));

        CraftingInput miss = input(new ItemStack[][]{          // 错误：缺一个膜
                {mem(), string(), mem()},
                {null,  ingot(), mem()},
                {mem(), null,     mem()},
        });
        check("elytra 缺料不应匹配", !h.value().matches(miss, null));
        // MC 26.2 ShapedRecipePattern.matches 要求 CraftingInput 尺寸与 pattern 完全一致
        // （仅支持镜像，不支持网格内平移；真实工作台恒为 3x3 不受影响）。
        // 镜像对称：MSM/MNM/M M 左右翻转后仍等于自身 → 应匹配
        CraftingInput mirrored = input(new ItemStack[][]{
                {mem(), string(), mem()},
                {mem(), ingot(), mem()},
                {mem(), null,     mem()},
        });
        check("elytra 镜像(本就左右对称)应匹配", h.value().matches(mirrored, null));
    }

    /** 海绵: APA / PWP / APA → 海绵×2 */
    private static void testSponge() {
        RecipeHolder<ShapedRecipe> h = CraftRecipes.buildSpongeRecipeHolder();

        CraftingInput ok = input(new ItemStack[][]{
                {sand(), paper(), sand()},
                {paper(), wool(), paper()},
                {sand(), paper(), sand()},
        });
        check("sponge 正确摆放应匹配", h.value().matches(ok, null));
        check("sponge 产出=2", h.value().assemble(ok).getCount() == 2);
        check("sponge 产出物=sponge", h.value().assemble(ok).is(Items.SPONGE));

        CraftingInput wrongWool = input(new ItemStack[][]{     // 错误：白羊毛
                {sand(), paper(), sand()},
                {paper(), whiteWool(), paper()},
                {sand(), paper(), sand()},
        });
        check("sponge 白羊毛不应匹配", !h.value().matches(wrongWool, null));

        CraftingInput swapped = input(new ItemStack[][]{       // 错误：角边互换
                {paper(), sand(), paper()},
                {sand(), wool(), sand()},
                {paper(), sand(), paper()},
        });
        check("sponge 角边互换不应匹配", !h.value().matches(swapped, null));
    }

    // ── helpers ──────────────────────────────────────────────

    private static ItemStack mem() { return new ItemStack(Items.PHANTOM_MEMBRANE); }
    private static ItemStack string() { return new ItemStack(Items.STRING); }
    private static ItemStack ingot() { return new ItemStack(Items.NETHERITE_INGOT); }
    private static ItemStack sand() { return item("sand"); }
    private static ItemStack paper() { return new ItemStack(Items.PAPER); }
    private static ItemStack wool() { return item("yellow_wool"); }
    private static ItemStack whiteWool() { return item("white_wool"); }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path)));
    }

    /** 3x3 摆放转 CraftingInput；null 表示空格。 */
    private static CraftingInput input(ItemStack[][] grid) {
        var flat = new java.util.ArrayList<ItemStack>();
        for (ItemStack[] row : grid)
            for (ItemStack st : row)
                flat.add(st != null ? st : ItemStack.EMPTY);
        return CraftingInput.of(grid[0].length, grid.length, flat);
    }

    private static void check(String name, boolean cond) {
        if (!cond) throw new AssertionError("FAIL: " + name);
        Allay.LOGGER.info("[RecipeMatchTest]   ok - {}", name);
    }

    private RecipeMatchTest() {}
}
