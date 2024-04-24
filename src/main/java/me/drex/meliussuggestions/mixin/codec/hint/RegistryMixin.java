package me.drex.meliussuggestions.mixin.codec.hint;

import com.mojang.serialization.Codec;
import me.drex.meliussuggestions.util.access.hint.RegistryHint;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Registry.class)
public interface RegistryMixin<T> {

    @Inject(method = "referenceHolderWithLifecycle", at = @At("TAIL"))
    private void addHint(CallbackInfoReturnable<Codec<Holder.Reference<?>>> cir) {
        //noinspection unchecked
        ((RegistryHint<T>) cir.getReturnValue()).setRegistryHint((Registry<T>) this);
    }

}
