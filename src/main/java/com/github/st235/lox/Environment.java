package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

final class Environment {

    @NotNull
    private final Map<String, Object> lookup = new HashMap<>();

    @Nullable
    private final Environment parent;

    Environment() {
        this(null);
    }

    Environment(@Nullable Environment parent) {
        this.parent = parent;
    }

    @Nullable
    public Environment getParent() {
        return parent;
    }

    void define(@NotNull String name, @Nullable Object value) {
        lookup.put(name, value);
    }

    void assign(@NotNull Token name, @Nullable Object value) {
        if (lookup.containsKey(name.lexeme())) {
            lookup.put(name.lexeme(), value);
            return;
        }

        if (parent != null) {
            parent.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    void assignAt(int depth, @NotNull Token name, @Nullable Object value) {
        Map<String, Object> lookup = findAt(depth);
        lookup.put(name.lexeme(), value);
    }

    Object get(@NotNull Token name) {
        if (lookup.containsKey(name.lexeme())) {
            return lookup.get(name.lexeme());
        }

        if (parent != null) {
            return parent.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    Object getAt(int depth, @NotNull String name) {
        Map<String, Object> lookup = findAt(depth);
        return lookup.get(name);
    }

    @NotNull
    private Map<String, Object> findAt(int depth) {
        Environment environment = this;
        for (int i = 0; i < depth; i++) {
            environment = environment.parent;
        }
        return environment.lookup;
    }

}
