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

    void assign(@NotNull Token name, @Nullable Object value) {
        if (lookup.containsKey(name.lexeme())) {
            lookup.put(name.lexeme(), value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    Object get(@NotNull Token name) {
        if (lookup.containsKey(name.lexeme())) {
            return lookup.get(name.lexeme());
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

}
