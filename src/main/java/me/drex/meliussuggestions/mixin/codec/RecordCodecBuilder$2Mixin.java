package me.drex.meliussuggestions.mixin.codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.meliussuggestions.util.access.RecordCodecAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.mojang.serialization.codecs.RecordCodecBuilder$2")
public abstract class RecordCodecBuilder$2Mixin<O> implements RecordCodecAccess<O> {

    @Shadow
    @Final
    RecordCodecBuilder<O, O> val$builder;

    @Override
    public RecordCodecBuilder<O, O> builder() {
        return this.val$builder;
    }
}
