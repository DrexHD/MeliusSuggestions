package me.drex.meliussuggestions.mixin.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import me.drex.meliussuggestions.util.access.ListCodecAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ListCodec.class)
public abstract class ListCodecMixin<E> implements ListCodecAccess<E> {
    @Shadow
    @Final
    private Codec<E> elementCodec;

    @Override
    public Codec<E> elementCodec() {
        return this.elementCodec;
    }
}
