package me.drex.meliussuggestions.util.access;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface RegistryFileCodecAccess {

    ResourceKey<? extends Registry<?>> registryKey();

    Codec<?> elementCodec();

    boolean allowInline();

}
