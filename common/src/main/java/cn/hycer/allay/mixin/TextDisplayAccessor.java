package cn.hycer.allay.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {

    @Accessor("DATA_STYLE_FLAGS_ID")
    static EntityDataAccessor<Byte> getStyleFlagsId() {
        throw new AssertionError();
    }
}
