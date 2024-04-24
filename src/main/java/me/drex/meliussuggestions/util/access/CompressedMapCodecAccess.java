package me.drex.meliussuggestions.util.access;

import com.mojang.serialization.MapCodec;

public interface CompressedMapCodecAccess<E> {

    MapCodec<E> val$normal();

}
