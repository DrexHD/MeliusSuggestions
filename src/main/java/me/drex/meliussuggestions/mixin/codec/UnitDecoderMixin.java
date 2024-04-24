package me.drex.meliussuggestions.mixin.codec;

import me.drex.meliussuggestions.util.access.hint.UnitHint;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.mojang.serialization.Decoder$5")
public abstract class UnitDecoderMixin implements UnitHint {

}
