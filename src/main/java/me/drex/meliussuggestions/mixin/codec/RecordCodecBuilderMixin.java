package me.drex.meliussuggestions.mixin.codec;

import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.meliussuggestions.util.access.RecordCodecBuilderAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecordCodecBuilder.class)
public abstract class RecordCodecBuilderMixin<F> implements RecordCodecBuilderAccess<F> {

    @Shadow
    @Final
    private MapDecoder<F> decoder;

    @Override
    public MapDecoder<F> decoder() {
        return this.decoder;
    }
}
