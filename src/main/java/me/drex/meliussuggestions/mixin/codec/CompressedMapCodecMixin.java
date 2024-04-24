package me.drex.meliussuggestions.mixin.codec;

import com.mojang.serialization.MapCodec;
import me.drex.meliussuggestions.util.access.CompressedMapCodecAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.util.ExtraCodecs$3")
public abstract class CompressedMapCodecMixin<E> implements CompressedMapCodecAccess<E> {

    @Shadow
    @Final
    MapCodec<E> val$normal;

    @Override
    public MapCodec<E> val$normal() {
        return this.val$normal;
    }

}
