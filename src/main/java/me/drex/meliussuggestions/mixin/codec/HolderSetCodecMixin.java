package me.drex.meliussuggestions.mixin.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import me.drex.meliussuggestions.util.access.HolderSetCodecAccess;
import net.minecraft.core.Holder;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(HolderSetCodec.class)
public abstract class HolderSetCodecMixin<E> implements HolderSetCodecAccess<E> {

    @Shadow
    @Final
    private Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

    @Override
    public Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec() {
        return this.registryAwareCodec;
    }
}
