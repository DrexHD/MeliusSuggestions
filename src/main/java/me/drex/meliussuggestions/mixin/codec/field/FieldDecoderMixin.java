package me.drex.meliussuggestions.mixin.codec.field;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.codecs.FieldDecoder;
import me.drex.meliussuggestions.util.access.field.FieldDecoderAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FieldDecoder.class)
public abstract class FieldDecoderMixin<A> implements FieldDecoderAccess<A> {

    @Shadow
    @Final
    protected String name;

    @Shadow
    @Final
    private Decoder<A> elementCodec;

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Decoder<A> elementCodec() {
        return this.elementCodec;
    }
}
