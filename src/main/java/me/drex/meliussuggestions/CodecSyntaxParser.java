package me.drex.meliussuggestions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import me.drex.meliussuggestions.util.OptionalParsingFunction;
import me.drex.meliussuggestions.util.access.*;
import me.drex.meliussuggestions.util.access.field.FieldDecoderAccess;
import me.drex.meliussuggestions.util.access.field.OptionalFieldCodecAccess;
import me.drex.meliussuggestions.util.access.hint.RegistryHint;
import me.drex.meliussuggestions.util.access.hint.UnitHint;
import me.drex.meliussuggestions.util.access.hint.CodecHint;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.minecraft.nbt.TagParser.ERROR_EXPECTED_KEY;
import static net.minecraft.nbt.TagParser.ERROR_EXPECTED_VALUE;

// TODO Performance benchmarks
public class CodecSyntaxParser {

    // Copied from TagParser
    public static final char ELEMENT_SEPARATOR = ',';
    public static final char NAME_VALUE_SEPARATOR = ':';
    private static final char LIST_OPEN = '[';
    private static final char LIST_CLOSE = ']';
    private static final char STRUCT_CLOSE = '}';
    private static final char STRUCT_OPEN = '{';
    // TODO Use these for better validation / suggestions
//    private static final Pattern DOUBLE_PATTERN_NOSUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", Pattern.CASE_INSENSITIVE);
//    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", Pattern.CASE_INSENSITIVE);
//    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", Pattern.CASE_INSENSITIVE);
//    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", Pattern.CASE_INSENSITIVE);
//    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", Pattern.CASE_INSENSITIVE);
//    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");

    private final StringReader reader;
    private final List<Suggestion> suggestions = new ArrayList<>();
    private static final Object DUMMY = new Object();
    private static final Optional<Object> CONSUME = Optional.of(DUMMY);

    public CodecSyntaxParser(StringReader reader) {
        this.reader = reader;
    }

    private void suggest(String value, String hint) {
        suggest(reader.getCursor(), value, hint);
    }

    private void suggest(int cursor, String value, String hint) {
        if (!value.equals(reader.getString().substring(cursor))) { // text.equals(remaining)
            suggestions.add(new Suggestion(StringRange.between(cursor, reader.getString().length()), value, new LiteralMessage(hint)));
        }
    }

    private void suggestChar(Character value, String hint) {
        reader.skipWhitespace();
        if (reader.getRemaining().isEmpty()) {
            suggest(value.toString(), hint);
        }
    }

    private void expectChar(Character value, String hint) throws CommandSyntaxException {
        reader.skipWhitespace();
        if (reader.getRemaining().isEmpty()) {
            suggest(value.toString(), hint);
        }
        reader.expect(value);
        reader.skipWhitespace();
    }

    private void suggestList(List<String> list, String hint) {
        String listContent = String.join(", ", list);
        suggestString(LIST_OPEN + listContent + LIST_CLOSE, hint);
    }

    private void suggestString(String value, String hint) {
        if (reader.getRemaining().isEmpty()) {
            suggest(value, hint);
        }
    }

    private void expectString(String value, String hint) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String string = reader.readString();
        if (SharedSuggestionProvider.matchesSubStr(reader.getString().substring(cursor), value)) {
            suggest(cursor, value, hint);
        }
        if (!value.equals(string)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().createWithContext(this.reader, value);
        }
    }

    private void expectStrings(Set<String> values, String hint) throws CommandSyntaxException {
        String remaining = reader.getRemaining().toLowerCase(Locale.ROOT);
        for (String value : values) {
            for (char c : value.toCharArray()) {
                if (!StringReader.isAllowedInUnquotedString(c)) {
                    value = "'" + value + "'";
                    break;
                }
            }
            if (value.contains(remaining)) {
                suggest(value, hint);
            }
        }
        int cursor = reader.getCursor();

        String string = reader.readString();
        if (!values.contains(string)) {
            reader.setCursor(cursor);
            throw new SimpleCommandExceptionType(Component.literal("Unexpected value")).createWithContext(this.reader);
        }
    }

    public void parse(Codec<?> codec) {
        int cursor = reader.getCursor();
        try {
            visitCodec(codec);
        } catch (CommandSyntaxException ignored) {
        }
        reader.setCursor(cursor);

    }

    public CompletableFuture<Suggestions> getSuggestions() {
        return CompletableFuture.completedFuture(Suggestions.create(reader.getString(), suggestions));
    }

    private Optional<Object> parseCodec(Codec<?> codec) throws CommandSyntaxException {
        // Primitive types
        int cursor = reader.getCursor();
        boolean pass = false;
        if (codec == Codec.BOOL) {
            expectStrings(Set.of("true", "false"), "Codec.BOOL");
        } else if (codec == Codec.BYTE) {
            int aByte = reader.readInt();
        } else if (codec == Codec.SHORT) {
            int aShort = reader.readInt();
        } else if (codec == Codec.INT) {
            int anInt = reader.readInt();
        } else if (codec == Codec.LONG) {
            long aLong = reader.readLong();
        } else if (codec == Codec.FLOAT) {
            float aFloat = reader.readFloat();
        } else if (codec == Codec.DOUBLE) {
            double aDouble = reader.readDouble();
        } else if (codec == Codec.STRING) {
            reader.readString();
        } else if (codec == Codec.INT_STREAM) {
            parseList(Codec.INT);
        } else if (codec == Codec.LONG_STREAM) {
            parseList(Codec.LONG);
        } else {
            switch (codec) {
                case MapCodec.MapCodecCodec<?> mapCodecCodec -> {
                    expectChar(STRUCT_OPEN, "MapCodecCodec STRUCT_OPEN");
                    visitMapDecoder(mapCodecCodec.codec());
                    expectChar(STRUCT_CLOSE, "MapCodecCodec STRUCT_CLOSE");
                }
                case ListCodecAccess<?> listCodec -> parseList(listCodec.elementCodec());
                case EitherCodecAccess<?, ?> eitherCodec -> {
                    try {
                        visitCodec(eitherCodec.second());
                    } catch (CommandSyntaxException ignored) {
                    }
                    reader.setCursor(cursor);
                    visitCodec(eitherCodec.first());
                }
                case RegistryHint<?> registryHint -> {
                    if (registryHint.getRegistryHint() != null) {
                        expectStrings(registryHint.getRegistryHint().keySet().stream().map(ResourceLocation::toString).collect(Collectors.toSet()), "Codec$4Access");
                    } else {
                        pass = true;
                    }
                }
                case CodecHint<?> codecHint -> {
                    ResourceKey<? extends Registry<?>> resourceKey = codecHint.getResourceKeyHint();
                    if (codecHint.isUuidHint()) {
                        UUID playerUUID = Minecraft.getInstance().player.getUUID();
                        UUID randomUUID = UUID.randomUUID();
                        List<String> playerValues = Arrays.stream(UUIDUtil.uuidToIntArray(playerUUID)).mapToObj(String::valueOf).toList();
                        List<String> randomValues = Arrays.stream(UUIDUtil.uuidToIntArray(randomUUID)).mapToObj(String::valueOf).toList();
                        suggestList(playerValues, "Your Uuid");
                        suggestList(randomValues, "Random Uuid");
                        parseList(Codec.INT, 4);
                    } else if (resourceKey != null) {
                        Optional<HolderLookup.RegistryLookup<Object>> optional = Minecraft.getInstance().level.registryAccess().lookup(resourceKey);
                        Set<String> tagIds = optional.get().listTagIds().map(TagKey::location).map(ResourceLocation::toString).map(s -> '#' + s).collect(Collectors.toSet());
                        expectStrings(tagIds, "ResourceKey hint");
                    } else {
                        pass = true;
                    }
                }
                case StringRepresentable$EnumCodecAccess stringEnumCodec -> {
                    Set<String> candidates = new HashSet<>();
                    for (Enum<?> anEnum : stringEnumCodec.enums()) {
                        candidates.add(((StringRepresentable) anEnum).getSerializedName());
                    }
                    expectStrings(candidates, "StringRepresentable$EnumCodecAccess");
                }
                case StringRepresentable$StringRepresentableCodecAccess<?> stringRepresentableCodec -> {
                    Set<String> candidates = new HashSet<>();
                    for (StringRepresentable stringRepresentable : stringRepresentableCodec.stringRepresentables()) {
                        candidates.add(stringRepresentable.getSerializedName());
                    }
                    expectStrings(candidates, "StringRepresentable$EnumCodecAccess");
                }
                case RegistryFixedCodecAccess registryFixedCodec -> {
                    Optional<HolderLookup.RegistryLookup<Object>> optional = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).lookup(registryFixedCodec.registryKey());

                    if (optional.isPresent()) {
                        Set<String> entries = optional.get().listElementIds().map(ResourceKey::location).map(ResourceLocation::toString).collect(Collectors.toSet());
                        expectStrings(entries, "RegistryFixedCodecAccess");
                    }
                }
                case RegistryFileCodecAccess registryFileCodec -> {

                    // TODO Allow inline
                    Optional<HolderLookup.RegistryLookup<Object>> optional = Minecraft.getInstance().level.registryAccess().lookup(registryFileCodec.registryKey());
                    if (optional.isPresent()) {
                        expectStrings(optional.get().listElementIds().map(objectResourceKey -> objectResourceKey.location().toString()).collect(Collectors.toSet()), "RegistryFileCodecAccess");
                    }
                }
                case UnboundedMapCodec<?, ?> unboundedMapCodec -> parseUnboundedMap(unboundedMapCodec);
                default -> pass = true;
            }
        }
        if (pass) {
            return Optional.empty();
        } else {
            return CONSUME;
        }
    }

    private Optional<Object> parseDecoder(Decoder<?> decoder) throws CommandSyntaxException {
        return switch (decoder) {
            case UnboundedMapCodec<?, ?> unboundedMap -> {
                parseUnboundedMap(unboundedMap);
                yield CONSUME;
            }
            default -> Optional.empty();
        };
    }

    private Optional<Object> parseMapDecoder(MapDecoder<?> mapDecoder) throws CommandSyntaxException {
        boolean pass = false;
        switch (mapDecoder) {
            case FieldDecoderAccess<?> fieldDecoder -> {
                expectString(fieldDecoder.name(), "FieldDecoderAccess field");
                expectChar(NAME_VALUE_SEPARATOR, "FieldDecoderAccess separator");
                visitDecoder(fieldDecoder.elementCodec());
            }
            case OptionalFieldCodecAccess optionalFieldCodec -> {
                expectString(optionalFieldCodec.name(), "OptionalFieldCodecAccess field");
                expectChar(NAME_VALUE_SEPARATOR, "OptionalFieldCodecAccess separator");
                visitCodec(optionalFieldCodec.elementCodec());
            }
            case RecordCodecAccess<?> access -> {
                RecordCodecBuilderAccess<?> builderAccess = (RecordCodecBuilderAccess<?>) (Object) access.builder();
                parseRecordCodec(collectMapDecoders(builderAccess.decoder()));
            }
            default -> pass = true;
        }
        if (pass) {
            return Optional.empty();
        } else {
            return CONSUME;
        }
    }

    private Set<MapDecoder<?>> collectMapDecoders(MapDecoder<?> mapDecoder) {
        Set<MapDecoder<?>> result = new HashSet<>();
        switch (mapDecoder) {
            case MapDecoder.Implementation<?> ignored -> {
                for (Field declaredField : mapDecoder.getClass().getDeclaredFields()) {
                    try {
                        if (RecordCodecBuilder.class.isAssignableFrom(declaredField.getType())) {
                            declaredField.setAccessible(true);
                            RecordCodecBuilderAccess<?> builder = (RecordCodecBuilderAccess<?>) declaredField.get(mapDecoder);
                            result.addAll(collectMapDecoders(builder.decoder()));
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            case ExtraCodecs$RecursiveMapCodecAccess access -> result.addAll(collectMapDecoders(access.wrapped().get()));
            case RecordCodecAccess<?> access -> {
                RecordCodecBuilderAccess<?> builderAccess = (RecordCodecBuilderAccess<?>) (Object) access.builder();
                result.addAll(collectMapDecoders(builderAccess.decoder()));
            }
            default -> result.add(mapDecoder);
        }
        return result;
    }

    private void visitCodec(Codec<?> codec) throws CommandSyntaxException {
        visitCodec(codec, DUMMY, this::parseCodec, this::parseDecoder, this::parseMapDecoder);
    }

    private void visitDecoder(Decoder<?> decoder) throws CommandSyntaxException {
        visitDecoder(decoder, DUMMY, this::parseCodec, this::parseDecoder, this::parseMapDecoder);
    }

    private void visitMapDecoder(MapDecoder<?> mapDecoder) throws CommandSyntaxException {
        visitMapDecoder(mapDecoder, DUMMY, this::parseCodec, this::parseDecoder, this::parseMapDecoder);
    }

    private <R> R visitCodec(Codec<?> codec, R defaultReturnValue, OptionalParsingFunction<Codec<?>, R> codecResolver, OptionalParsingFunction<Decoder<?>, R> decoderResolver, OptionalParsingFunction<MapDecoder<?>, R> mapDecoderResolver) throws CommandSyntaxException {
        Optional<R> customResolver = codecResolver.apply(codec);
        if (customResolver.isPresent()) {
            return customResolver.get();
        } else {
            return switch (codec) {
                case Codec$RecursiveCodecAccess<?> recursiveCodec ->
                    visitCodec(recursiveCodec.wrapped().get(), defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
                case HolderSetCodecAccess<?> holderSetCodec ->
                    visitCodec(holderSetCodec.registryAwareCodec(), defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
                default -> visitUnknown(codec, defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
            };
        }
    }

    private <R> R visitDecoder(Decoder<?> decoder, R defaultReturnValue, OptionalParsingFunction<Codec<?>, R> codecResolver, OptionalParsingFunction<Decoder<?>, R> decoderResolver, OptionalParsingFunction<MapDecoder<?>, R> mapDecoderResolver) throws CommandSyntaxException {
        Optional<R> customResolver;
        if (decoder instanceof Codec<?> codec) {
            customResolver = codecResolver.apply(codec);
        } else {
            customResolver = decoderResolver.apply(decoder);
        }
        if (customResolver.isPresent()) {
            return customResolver.get();
        } else {
            return visitUnknown(decoder, defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
        }
    }

    private <R> R visitMapDecoder(MapDecoder<?> mapDecoder, R defaultReturnValue, OptionalParsingFunction<Codec<?>, R> codecResolver, OptionalParsingFunction<Decoder<?>, R> decoderResolver, OptionalParsingFunction<MapDecoder<?>, R> mapDecoderResolver) throws CommandSyntaxException {
        Optional<R> customResolver = mapDecoderResolver.apply(mapDecoder);
        if (customResolver.isPresent()) {
            return customResolver.get();
        } else {
            return switch (mapDecoder) {
                case ExtraCodecs$RecursiveMapCodecAccess access ->
                    visitMapDecoder(access.wrapped().get(), defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
                case CompressedMapCodecAccess<?> access ->
                    visitMapDecoder(access.val$normal(), defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
                default ->
                    visitUnknown(mapDecoder, defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
            };
        }
    }

    // This is used for tracking "mapping" codecs
    private <R> R visitUnknown(Object object, R defaultReturnValue, OptionalParsingFunction<Codec<?>, R> codecResolver, OptionalParsingFunction<Decoder<?>, R> decoderResolver, OptionalParsingFunction<MapDecoder<?>, R> mapDecoderResolver) throws CommandSyntaxException {
        Class<?> clazz = object.getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {
            try {
                if (Codec.class.isAssignableFrom(declaredField.getType())) {
                    declaredField.setAccessible(true);
                    return visitCodec((Codec<?>) declaredField.get(object), defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
                }
                if (Decoder.class.isAssignableFrom(declaredField.getType())) {
                    declaredField.setAccessible(true);
                    return visitDecoder((Decoder<?>) declaredField.get(object), defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
                }
                if (MapDecoder.class.isAssignableFrom(declaredField.getType())) {
                    declaredField.setAccessible(true);
                    return visitMapDecoder((MapDecoder<?>) declaredField.get(object), defaultReturnValue, codecResolver, decoderResolver, mapDecoderResolver);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return defaultReturnValue;
    }

    private boolean isRequired(MapDecoder<?> mapDecoder) {
        try {
            return visitMapDecoder(mapDecoder, false, codec -> Optional.empty(), decoder -> Optional.empty(), visitedMapDecoder -> switch (visitedMapDecoder) {
                case FieldDecoderAccess<?> ignored -> Optional.of(true);
                default -> Optional.empty();
            });
        } catch (CommandSyntaxException ignored) {
            return false;
        }
    }

    private boolean shouldIgnore(MapDecoder<?> mapDecoder) {
        try {
            return visitMapDecoder(mapDecoder, false, codec -> Optional.empty(), decoder -> Optional.empty(), visitedMapDecoder -> switch (visitedMapDecoder) {
                case UnitHint ignored -> Optional.of(true);
                default -> Optional.empty();
            });
        } catch (CommandSyntaxException ignored) {
            return false;
        }
    }

    private void parseRecordCodec(Set<MapDecoder<?>> mapDecoders) throws CommandSyntaxException {
        mapDecoders.removeIf(this::shouldIgnore);
        Set<MapDecoder<?>> required = new HashSet<>(mapDecoders);
        required.removeIf(mapDecoder -> !isRequired(mapDecoder));

        boolean changed = true;
        while (changed) {
            changed = false;
            Iterator<MapDecoder<?>> iterator = mapDecoders.iterator();
            while (iterator.hasNext()) {
                MapDecoder<?> mapDecoder = iterator.next();
                int cursor = reader.getCursor();
                try {
                    visitMapDecoder(mapDecoder);
                    changed = true;
                    iterator.remove();
                    break;
                } catch (CommandSyntaxException e) {
                    reader.setCursor(cursor);
                }
            }
            if (changed) {
                if (!Collections.disjoint(mapDecoders, required)) {
                    expectChar(ELEMENT_SEPARATOR, "parseRecordCodec ELEMENT_SEPARATOR");
                } else if (!mapDecoders.isEmpty()) {
                    suggestChar(ELEMENT_SEPARATOR, "parseRecordCodec ELEMENT_SEPARATOR (suggest)");
                    if (!this.hasElementSeparator()) {
                        break;
                    }
                }
            }
        }
        // Not all required decoders have been parsed
        if (!Collections.disjoint(mapDecoders, required)) {
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        }
    }

    private boolean hasElementSeparator() {
        reader.skipWhitespace();
        if (this.reader.canRead() && this.reader.peek() == ',') {
            reader.skip();

            reader.skipWhitespace();
            return true;
        }
        return false;
    }

    private void parseList(Codec<?> elementCodec) throws CommandSyntaxException {
        parseList(elementCodec, Integer.MAX_VALUE);
    }

    private void parseList(Codec<?> elementCodec, int maxElementCount) throws CommandSyntaxException {
        expectChar(LIST_OPEN, "readList LIST_OPEN");
        if (!this.reader.canRead()) {
            suggestChar(LIST_CLOSE, "parseList LIST_CLOSE0 (suggest)");
            visitCodec(elementCodec);
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        } else {
            int elementCount = 0;
            while (this.reader.peek() != LIST_CLOSE) {
                suggestChar(LIST_CLOSE, "parseList LIST_CLOSE1 (suggest)");

                visitCodec(elementCodec);
                elementCount++;

                suggestChar(ELEMENT_SEPARATOR, "parseList ELEMENT_SEPARATOR (suggest)");
                if (!this.hasElementSeparator()) {
                    break;
                }

                if (this.reader.canRead()) continue;
                suggestChar(LIST_CLOSE, "parseList LIST_CLOSE2 (suggest)");
                visitCodec(elementCodec);
                throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
            }
            if (elementCount > maxElementCount) {
                throw new SimpleCommandExceptionType(Component.literal("Input is not a list of " + maxElementCount + " elements")).createWithContext(this.reader);
            }
        }
        expectChar(LIST_CLOSE, "readList LIST_CLOSE");
    }

    private void parseUnboundedMap(UnboundedMapCodec<?, ?> access) throws CommandSyntaxException {
        expectChar(STRUCT_OPEN, "readMap");

        if (!this.reader.canRead()) {
            suggestChar(STRUCT_CLOSE, "readMap STRUCT_CLOSE (suggest)");
            visitCodec(access.keyCodec());
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        } else {
            while (this.reader.peek() != STRUCT_CLOSE) {
                visitCodec(access.keyCodec());
                expectChar(NAME_VALUE_SEPARATOR, "readMap split key:value");
                visitCodec(access.elementCodec());
                suggestChar(ELEMENT_SEPARATOR, "readMap ELEMENT_SEPARATOR (suggest)");
                if (!this.hasElementSeparator()) break;
                this.reader.skipWhitespace();
                if (this.reader.canRead()) continue;
                suggestChar(STRUCT_CLOSE, "readMap STRUCT_CLOSE (suggest)");
                visitCodec(access.keyCodec());
                throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
            }
        }
        expectChar(STRUCT_CLOSE, "readMap");
    }

}
