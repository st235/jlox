package com.github.st235.lox;

import java.util.List;

public interface LoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);

}
