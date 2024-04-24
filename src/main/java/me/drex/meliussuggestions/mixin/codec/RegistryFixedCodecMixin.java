package me.drex.meliussuggestions.mixin.codec;

import me.drex.meliussuggestions.util.access.RegistryFixedCodecAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RegistryFixedCodec.class)
public abstract class RegistryFixedCodecMixin implements RegistryFixedCodecAccess {
    @Shadow
    @Final
    private ResourceKey<? extends Registry<?>> registryKey;

    @Override
    public ResourceKey<? extends Registry<?>> registryKey() {
        return this.registryKey;
    }
}
