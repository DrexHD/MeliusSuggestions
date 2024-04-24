package me.drex.meliussuggestions.mixin.parser;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.StringReader;
import me.drex.meliussuggestions.CodecSyntaxParser;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.commands.arguments.item.ItemParser$State")
public abstract class ItemSyntaxParser$StateMixin {

    @Shadow
    @Final
    private StringReader reader;

    @Shadow
    @Final
    private ItemParser.Visitor visitor;

    @Inject(method = "readComponents", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/item/ItemParser$State;readComponent(Lnet/minecraft/core/component/DataComponentType;)V"))
    private void addCustomParser(CallbackInfo ci, @Local DataComponentType<?> dataComponentType) {
        CodecSyntaxParser parser = new CodecSyntaxParser(reader);
        parser.parse(dataComponentType.codecOrThrow());
        visitor.visitSuggestions(ignored -> parser.getSuggestions());
    }

}
