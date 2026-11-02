package cn.hycer.allay.mixin;

import cn.hycer.allay.feature.FeatureManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 原版丛林树叶掉落树苗基础概率仅为 2.5%（其他树叶均为 5.0%）。
 * 开启此特性后，若使用非剪刀且非精准采集工具（或自然腐烂），
 * 掉落结算若未产生丛林树苗，则进行一次额外判定补投，使其总体爆率提升至与普通树叶一致（基础 5.0%，时运相应提升）。
 */
@Mixin(BlockBehaviour.class)
public abstract class JungleSaplingDropRateMixin {

    @Inject(method = "getDrops", at = @At("RETURN"), cancellable = true)
    private void adjustJungleSaplingDrop(BlockState state, LootParams.Builder builder,
                                        CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!FeatureManager.getInstance().isJungleSaplingDropRate()) {
            return;
        }

        if (!state.is(Blocks.JUNGLE_LEAVES)) {
            return;
        }

        var toolInstance = builder.getOptionalParameter(LootContextParams.TOOL);
        if (toolInstance instanceof ItemStack toolStack) {
            if (toolStack.is(Items.SHEARS)) {
                return;
            }
            var level = builder.getLevel();
            Holder<Enchantment> silkTouch = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.SILK_TOUCH);
            if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, toolStack) > 0) {
                return;
            }
        }

        List<ItemStack> drops = cir.getReturnValue();
        boolean hasSapling = false;
        for (ItemStack stack : drops) {
            if (stack.is(Items.JUNGLE_SAPLING)) {
                hasSapling = true;
                break;
            }
        }

        if (!hasSapling) {
            var level = builder.getLevel();
            int fortuneLevel = 0;
            if (toolInstance instanceof ItemStack toolStack && !toolStack.isEmpty()) {
                Holder<Enchantment> fortune = level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.FORTUNE);
                fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(fortune, toolStack);
            }

            // 原版概率 vs 目标普通树叶概率：
            // Fortune 0: 2.5% -> 5.0% (补掷 ~2.564%)
            // Fortune 1: 2.78% -> 6.25% (补掷 ~3.57%)
            // Fortune 2: 3.125% -> 8.33% (补掷 ~5.38%)
            // Fortune 3: 4.167% -> 10.0% (补掷 ~6.09%)
            // 高时运(>=4): 10.0% -> 10.0% (补掷 0%)
            float extraChance = switch (fortuneLevel) {
                case 0 -> (0.05f - 0.025f) / (1.0f - 0.025f);
                case 1 -> (0.0625f - 0.027777778f) / (1.0f - 0.027777778f);
                case 2 -> (0.083333336f - 0.03125f) / (1.0f - 0.03125f);
                case 3 -> (0.10f - 0.041666668f) / (1.0f - 0.041666668f);
                default -> 0.0f;
            };

            if (extraChance > 0.0f && level.getRandom().nextFloat() < extraChance) {
                List<ItemStack> newDrops = new ArrayList<>(drops);
                newDrops.add(new ItemStack(Items.JUNGLE_SAPLING));
                cir.setReturnValue(newDrops);
            }
        }
    }
}
