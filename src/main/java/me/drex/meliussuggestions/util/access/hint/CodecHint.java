package me.drex.meliussuggestions.util.access.hint;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface CodecHint<T> {

    void setUuidHint();

    boolean isUuidHint();

    void setResourceKeyHint(ResourceKey<? extends Registry<T>> resourceKey);

    ResourceKey<? extends Registry<T>> getResourceKeyHint();

}
