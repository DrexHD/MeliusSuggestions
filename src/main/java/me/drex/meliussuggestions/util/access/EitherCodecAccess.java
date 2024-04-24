package me.drex.meliussuggestions.util.access;

import com.mojang.serialization.Codec;

public interface EitherCodecAccess<F, S> {

    Codec<F> first();

    Codec<S> second();

}
