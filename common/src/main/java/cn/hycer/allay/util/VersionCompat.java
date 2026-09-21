package cn.hycer.allay.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 跨 MC 版本的兼容适配层。
 *
 * <p>26.1~26.2 与 26.3 之间有两个 API 签名变化（其余代码路径完全一致）：
 * <ul>
 *   <li>{@code RecipeMap.create(Iterable<RecipeHolder<?>>)} → {@code RecipeMap.create(HolderLookup)}</li>
 *   <li>{@code ServerPlayer.drop(ItemStack, boolean, boolean)} → {@code ServerPlayer.drop(ItemStack, boolean, Prediction)}</li>
 * </ul>
 * 这里用反射绕开编译期签名差异，保持 common 源码可被所有版本共享编译。
 * 注意：26.3 的 {@code RecipeMap.create(HolderLookup)} 语义是从注册表构建，
 * 与旧版「直接给定集合」不同，因此这里统一反射调用私有构造器 {@code (Multimap, Map)}，
 * 该构造器在 26.1~26.3 签名与字段布局完全一致。
 */
public final class VersionCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("allay-version-compat");

    private static Constructor<RecipeMap> recipeMapCtor;
    private static Method dropMethod;

    private VersionCompat() {}

    /**
     * 根据给定配方集合构建 {@link RecipeMap}（跨版本，26.1~26.3）。
     * 与旧版 {@code RecipeMap.create(Iterable)} 语义一致：直接按集合构建。
     */
    public static RecipeMap createRecipeMap(Collection<RecipeHolder<?>> values) {
        try {
            if (recipeMapCtor == null) {
                recipeMapCtor = RecipeMap.class.getDeclaredConstructor(
                        Multimap.class, Map.class);
                recipeMapCtor.setAccessible(true);
            }
            Multimap<RecipeType<?>, RecipeHolder<?>> byType = ArrayListMultimap.create();
            Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = new HashMap<>();
            for (RecipeHolder<?> holder : values) {
                byType.put(holder.value().getType(), holder);
                byKey.put(holder.id(), holder);
            }
            return recipeMapCtor.newInstance(byType, byKey);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to construct RecipeMap reflectively", e);
        }
    }

    /**
     * 物品栏放不下时丢出物品（跨版本，26.1~26.3）。
     * 26.3 起 {@code ServerPlayer.drop(ItemStack, boolean)} 增加第三个参数
     * {@code Prediction}（枚举），旧版为 {@code boolean}。按方法签名动态匹配。
     */
    public static void dropItem(ServerPlayer player, ItemStack stack, boolean throwRandomly) {
        try {
            if (dropMethod == null) {
                Class<?> playerClass = ServerPlayer.class;
                Method fallback = null;
                for (Method m : playerClass.getMethods()) {
                    if (!m.getName().equals("drop")) continue;
                    Class<?>[] params = m.getParameterTypes();
                    if (params.length != 3) continue;
                    if (params[0] == ItemStack.class && params[1] == boolean.class) {
                        if (params[2] == boolean.class) {
                            dropMethod = m;               // 26.1 ~ 26.2
                            break;
                        }
                        fallback = m;                     // 26.3: (ItemStack, boolean, Prediction)
                    }
                }
                if (dropMethod == null) dropMethod = fallback;
                if (dropMethod == null) {
                    throw new NoSuchMethodException("ServerPlayer.drop(ItemStack, boolean, ...)");
                }
            }
            Class<?> third = dropMethod.getParameterTypes()[2];
            if (third == boolean.class) {
                dropMethod.invoke(player, stack, throwRandomly, true);
            } else {
                // Prediction enum: 客户端无预测，服务端直接掉落
                Object prediction = Enum.valueOf(
                        (Class<? extends Enum>) third, "SERVER_ONLY");
                dropMethod.invoke(player, stack, throwRandomly, prediction);
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[VersionCompat] dropItem failed", e);
        }
    }
}
