package me.drex.meliussuggestions.util.access.field;

import com.mojang.serialization.Decoder;

public interface FieldDecoderAccess<A> {

    String name();

    Decoder<A> elementCodec();

}
