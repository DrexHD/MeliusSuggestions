package me.drex.meliussuggestions.mixin.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.EitherCodec;
import me.drex.meliussuggestions.util.access.EitherCodecAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EitherCodec.class)
public abstract class EitherCodecMixin<F, S> implements EitherCodecAccess<F, S> {

    @Shadow
    @Final
    private Codec<F> first;

    @Shadow
    @Final
    private Codec<S> second;

    @Override
    public Codec<F> first() {
        return this.first;
    }

    @Override
    public Codec<S> second() {
        return this.second;
    }
}
