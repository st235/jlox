package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;

import java.util.List;

class LoxFunction implements LoxCallable {

    @NotNull
    private final Stmt.Function declaration;
    @NotNull
    private final Environment closure;

    LoxFunction(@NotNull Stmt.Function declaration, @NotNull Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < arguments.size(); i++) {
            Token identifier = declaration.params.get(i);
            Object value = arguments.get(i);
            environment.define(identifier.lexeme(), value);
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return ret) {
            return ret.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("<fn %s>", declaration.name.lexeme());
    }
}
