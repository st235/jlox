package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

final class Environment {

    private final Map<String, Object> lookup = new HashMap<>();

    void define(@NotNull String name, @Nullable Object value) {
        lookup.put(name, value);
    }

    Object get(@NotNull Token name) {
        if (lookup.containsKey(name.lexeme())) {
            return lookup.get(name.lexeme());
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

}
