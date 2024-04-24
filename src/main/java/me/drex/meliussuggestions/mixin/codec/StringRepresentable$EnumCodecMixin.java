package me.drex.meliussuggestions.mixin.codec;

import me.drex.meliussuggestions.util.access.StringRepresentable$EnumCodecAccess;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(StringRepresentable.EnumCodec.class)
public abstract class StringRepresentable$EnumCodecMixin implements StringRepresentable$EnumCodecAccess {

    private Enum[] enums;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void onInit(Enum[] enums, Function function, CallbackInfo ci) {
        this.enums = enums;
    }

    @Override
    public Enum[] enums() {
        return this.enums;
    }
}
