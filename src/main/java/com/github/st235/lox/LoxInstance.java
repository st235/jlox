package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {

    @NotNull
    private final LoxClass klass;

    @NotNull
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(@NotNull LoxClass klass) {
        this.klass = klass;
    }

    @Nullable
    public Object get(@NotNull Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }

        LoxFunction function = klass.findMethod(name.lexeme());
        if (function != null) {
            return function.bind(this);
        }

        throw new RuntimeError(name, String.format("Undefined property '%s'.", name.lexeme()));
    }

    public void set(@NotNull Token name, @Nullable Object object) {
        fields.put(name.lexeme(), object);
    }

    @Override
    public String toString() {
        return String.format("%s instance", klass.name);
    }
}
