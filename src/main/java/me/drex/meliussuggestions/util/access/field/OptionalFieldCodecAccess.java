package me.drex.meliussuggestions.util.access.field;

import com.mojang.serialization.Codec;

public interface OptionalFieldCodecAccess {

    String name();

    Codec<?> elementCodec();

}
