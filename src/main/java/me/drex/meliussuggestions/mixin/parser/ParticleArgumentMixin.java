package me.drex.meliussuggestions.mixin.parser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.drex.meliussuggestions.CodecSyntaxParser;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.TagParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ParticleArgument.class)
public abstract class ParticleArgumentMixin {

    @Unique
    private static CompletableFuture<Suggestions> suggestions;

    @Inject(
        method = "readParticle(Lnet/minecraft/nbt/TagParser;Lcom/mojang/brigadier/StringReader;Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/core/particles/ParticleOptions;",
        at = @At("HEAD")
    )
    private static <T extends ParticleOptions, O> void addCustomParser(TagParser<O> tagParser, StringReader reader, ParticleType<T> particleType, HolderLookup.Provider provider, CallbackInfoReturnable<T> cir) {
        CodecSyntaxParser parser = new CodecSyntaxParser(reader);
        parser.parse(particleType.codec().codec());
        suggestions = parser.getSuggestions();
    }

    @Inject(method = "listSuggestions", at = @At("HEAD"), cancellable = true)
    public <S> void applySuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        if (suggestions != null) {
            cir.setReturnValue(suggestions);
        }
        suggestions = null;
    }

}
