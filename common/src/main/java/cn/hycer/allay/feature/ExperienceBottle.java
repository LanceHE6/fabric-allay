package cn.hycer.allay.feature;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ExperienceBottle {

    private static final Random RANDOM = new Random();

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!FeatureManager.getInstance().isExperienceBottle()) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.GLASS_BOTTLE)) return InteractionResult.PASS;
            if (!player.isShiftKeyDown()) return InteractionResult.PASS;

            // Need at least 3 experience points
            int totalExp = sp.totalExperience;
            if (totalExp < 3) return InteractionResult.PASS;

            // Random 3-11 cost, capped at current exp
            int cost = 3 + RANDOM.nextInt(9);
            int actualCost = Math.min(cost, totalExp);

            // Consume bottle
            if (!sp.getAbilities().instabuild) stack.shrink(1);

            // Consume experience
            sp.giveExperiencePoints(-actualCost);

            // Give experience bottle
            ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
            if (!sp.getInventory().add(bottle)) {
                sp.drop(bottle, false);
            }

            // Sound
            world.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.PLAYERS,
                    0.5F, 0.8F + RANDOM.nextFloat() * 0.4F);

            return InteractionResult.SUCCESS;
        });
    }
}
