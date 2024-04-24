package me.drex.meliussuggestions.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Optional;

public interface OptionalParsingFunction<T, R> {

    Optional<R> apply(T t) throws CommandSyntaxException;

}
