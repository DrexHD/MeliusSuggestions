package me.drex.meliussuggestions.mixin.codec.hint;

import com.mojang.serialization.Codec;
import me.drex.meliussuggestions.util.access.hint.CodecHint;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TagKey.class)
public abstract class TagKeyMixin {

    @Inject(method = "hashedCodec", at = @At("TAIL"))
    private static <T> void addResourceKeyHint(ResourceKey<? extends Registry<T>> resourceKey, CallbackInfoReturnable<Codec<TagKey<T>>> cir) {
        //noinspection unchecked
        ((CodecHint<T>) cir.getReturnValue()).setResourceKeyHint(resourceKey);
    }

}
