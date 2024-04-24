package me.drex.meliussuggestions.mixin.codec;

import com.mojang.serialization.Codec;
import me.drex.meliussuggestions.util.access.Codec$RecursiveCodecAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(targets = "com.mojang.serialization.Codec$RecursiveCodec")
public abstract class Codec$RecursiveCodecMixin<T> implements Codec$RecursiveCodecAccess<T> {

    @Shadow
    @Final
    private Supplier<Codec<T>> wrapped;

    @Override
    public Supplier<Codec<T>> wrapped() {
        return this.wrapped;
    }
}
