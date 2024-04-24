package me.drex.meliussuggestions.util.access;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

import java.util.List;

public interface HolderSetCodecAccess<E> {

    Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec();

}
