package me.drex.meliussuggestions.mixin.codec.hint;

import com.mojang.serialization.Codec;
import me.drex.meliussuggestions.util.access.hint.CodecHint;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceKey.class)
public abstract class ResourceKeyMixin {

    @Inject(method = "codec", at = @At("TAIL"))
    private static <T> void addHint(ResourceKey<? extends Registry<T>> resourceKey, CallbackInfoReturnable<Codec<ResourceKey<T>>> cir) {
        ((CodecHint<T>) cir.getReturnValue()).setResourceKeyHint(resourceKey);
    }

}
