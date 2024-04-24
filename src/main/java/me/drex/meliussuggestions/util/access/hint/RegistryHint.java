package me.drex.meliussuggestions.util.access.hint;

import net.minecraft.core.Registry;

public interface RegistryHint<T> {

    void setRegistryHint(Registry<T> registry);

    Registry<T> getRegistryHint();

}
