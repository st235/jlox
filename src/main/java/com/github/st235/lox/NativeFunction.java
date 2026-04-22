package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;

public abstract class NativeFunction implements LoxCallable {

    @NotNull
    final String name;
    private final int arity;

    public NativeFunction(@NotNull String name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    @Override
    public int arity() {
        return arity;
    }

    @Override
    public String toString() {
        return String.format("<native fn %s>", name);
    }
}
