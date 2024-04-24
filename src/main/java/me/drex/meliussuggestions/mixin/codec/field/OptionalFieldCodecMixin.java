package me.drex.meliussuggestions.mixin.codec.field;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import me.drex.meliussuggestions.util.access.field.OptionalFieldCodecAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OptionalFieldCodec.class)
public abstract class OptionalFieldCodecMixin implements OptionalFieldCodecAccess {

    @Shadow
    @Final
    private Codec<?> elementCodec;

    @Shadow
    @Final
    private String name;

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Codec<?> elementCodec() {
        return this.elementCodec;
    }
}
