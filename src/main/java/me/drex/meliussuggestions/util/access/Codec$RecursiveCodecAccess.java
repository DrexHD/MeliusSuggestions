package me.drex.meliussuggestions.util.access;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

public interface Codec$RecursiveCodecAccess<T> {

    Supplier<Codec<T>> wrapped();


}
