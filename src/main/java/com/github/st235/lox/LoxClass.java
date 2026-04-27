package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {

    @NotNull
    final String name;

    @NotNull
    final Map<String, LoxFunction> methods;

    public LoxClass(@NotNull String name,
                    @NotNull Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        LoxFunction initialiser = findMethod("init");
        if (initialiser != null) {
            return initialiser.arity();
        }
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initialiser = findMethod("init");
        if (initialiser != null) {
            initialiser.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Nullable
    LoxFunction findMethod(@NotNull String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        return null;
    }
}
