package me.drex.meliussuggestions.util.access;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface RegistryFixedCodecAccess {

    ResourceKey<? extends Registry<?>> registryKey();

}
