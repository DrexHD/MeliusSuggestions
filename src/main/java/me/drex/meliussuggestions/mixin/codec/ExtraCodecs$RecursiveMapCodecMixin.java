package me.drex.meliussuggestions.mixin.codec;

import com.mojang.serialization.MapCodec;
import me.drex.meliussuggestions.util.access.ExtraCodecs$RecursiveMapCodecAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(targets = "com.mojang.serialization.MapCodec$RecursiveMapCodec")
public abstract class ExtraCodecs$RecursiveMapCodecMixin implements ExtraCodecs$RecursiveMapCodecAccess {

    @Shadow
    @Final
    private Supplier<MapCodec<?>> wrapped;

    @Override
    public Supplier<MapCodec<?>> wrapped() {
        return this.wrapped;
    }

}
