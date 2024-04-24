package me.drex.meliussuggestions.mixin.codec.hint;

import com.mojang.serialization.Codec;
import me.drex.meliussuggestions.util.access.hint.CodecHint;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(UUIDUtil.class)
public abstract class UUIDUtilMixin {

    @Shadow
    @Final
    public static Codec<UUID> CODEC;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void addHint(CallbackInfo ci) {
        ((CodecHint<?>) CODEC).setUuidHint();
    }

}
