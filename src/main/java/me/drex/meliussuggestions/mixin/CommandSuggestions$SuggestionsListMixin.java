package me.drex.meliussuggestions.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public abstract class CommandSuggestions$SuggestionsListMixin {

    @Shadow
    @Final
    CommandSuggestions field_21615;

    @Shadow
    @Final
    private List<Suggestion> suggestionList;

    @Inject(method = "useSuggestion", at = @At(value = "TAIL"))
    private void showSuggestionsAfterComplete(CallbackInfo ci) {
        if (this.suggestionList.size() <= 1) {
            this.field_21615.updateCommandInfo();
        }
    }

}
