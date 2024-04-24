package me.drex.meliussuggestions.mixin.codec.hint;

import me.drex.meliussuggestions.util.access.hint.CodecHint;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Codec.of
@Mixin(targets = "com.mojang.serialization.Codec$2")
public abstract class Codec$2Mixin<T> implements CodecHint<T> {

    @Unique
    private boolean uuidHint;

    @Unique
    private ResourceKey<? extends Registry<T>> resourceKeyHint;

    @Override
    public void setUuidHint() {
        this.uuidHint = true;
    }

    @Override
    public boolean isUuidHint() {
        return uuidHint;
    }

    @Override
    public void setResourceKeyHint(ResourceKey<? extends Registry<T>> resourceKey) {
        this.resourceKeyHint = resourceKey;
    }

    @Override
    public ResourceKey<? extends Registry<T>> getResourceKeyHint() {
        return resourceKeyHint;
    }
}
