package cn.hycer.allay.mixin;

import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ServerCommonPacketListenerImpl.class, remap = false)
public interface ServerCommonPacketListenerImplAccessor {

    @Accessor(value = "latency", remap = false)
    int getLatency();
}
