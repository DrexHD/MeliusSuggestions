package me.drex.meliussuggestions.util.access;

import com.mojang.serialization.Codec;

public interface ListCodecAccess<E> {

    Codec<E> elementCodec();

}
