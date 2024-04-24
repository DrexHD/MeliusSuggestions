package me.drex.meliussuggestions.util.access;

import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface RecordCodecAccess<O> {

    RecordCodecBuilder<O, O> builder();

}
