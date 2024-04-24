package me.drex.meliussuggestions.mixin.codec.hint;

import me.drex.meliussuggestions.util.access.hint.RegistryHint;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Codec.mapResult
@Mixin(targets = "com.mojang.serialization.Codec$4")
public abstract class Codec$4Mixin<T> implements RegistryHint<T> {

    @Unique
    private Registry<T> registry;

    @Override
    public void setRegistryHint(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public Registry<T> getRegistryHint() {
        return this.registry;
    }
}
