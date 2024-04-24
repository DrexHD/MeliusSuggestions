package me.drex.meliussuggestions.util.access;

import com.mojang.serialization.MapDecoder;

public interface RecordCodecBuilderAccess<F> {

    MapDecoder<F> decoder();

}
