package me.drex.meliussuggestions.mixin.codec;

import com.mojang.serialization.Codec;
import me.drex.meliussuggestions.util.access.RegistryFileCodecAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RegistryFileCodec.class)
public abstract class RegistryFileCodecMixin implements RegistryFileCodecAccess {

    @Shadow
    @Final
    private ResourceKey<? extends Registry<?>> registryKey;

    @Shadow
    @Final
    private Codec<?> elementCodec;

    @Shadow
    @Final
    private boolean allowInline;

    @Override
    public ResourceKey<? extends Registry<?>> registryKey() {
        return this.registryKey;
    }

    @Override
    public Codec<?> elementCodec() {
        return this.elementCodec;
    }

    @Override
    public boolean allowInline() {
        return this.allowInline;
    }
}
