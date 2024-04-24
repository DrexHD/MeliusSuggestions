package me.drex.meliussuggestions.mixin.codec;

import me.drex.meliussuggestions.util.access.StringRepresentable$StringRepresentableCodecAccess;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.ToIntFunction;

@Mixin(StringRepresentable.StringRepresentableCodec.class)
public abstract class StringRepresentable$StringRepresentableCodecMixin implements StringRepresentable$StringRepresentableCodecAccess {

    private StringRepresentable[] stringRepresentables;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(StringRepresentable[] stringRepresentables, Function function, ToIntFunction toIntFunction, CallbackInfo ci) {
        this.stringRepresentables = stringRepresentables;
    }

    @Override
    public StringRepresentable[] stringRepresentables() {
        return this.stringRepresentables;
    }
}
