package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private enum FunctionType {
        NONE,
        FUNCTION,
        INITIALISER,
        METHOD,
    }

    private enum ClassType {
        NONE,
        CLASS,
    }

    @NotNull
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    @NotNull
    private final Interpreter interpreter;

    @NotNull
    private FunctionType functionType = FunctionType.NONE;

    private ClassType classType = ClassType.NONE;

    public Resolver(@NotNull Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    void resolve(@NotNull List<Stmt> statements) {
        for (Stmt statement: statements) {
            statement.visit(this);
        }
    }

    private void resolve(@NotNull Stmt statement) {
        statement.visit(this);
    }

    private void resolve(@NotNull Expr expr) {
        expr.visit(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(@NotNull Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        if (scopes.peek().containsKey(name.lexeme())) {
            Lox.error(name.line(), String.format("Variable '%s' was already declared in the scope.", name.lexeme()));
            return;
        }

        scopes.peek().put(name.lexeme(), false);
    }

    private void define(@NotNull Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        scopes.peek().put(name.lexeme(), true);
    }

    @Override
    public Void visitBinary(Expr.Binary node) {
        resolve(node.left);
        resolve(node.right);
        return null;
    }

    @Override
    public Void visitGrouping(Expr.Grouping node) {
        resolve(node.expression);
        return null;
    }

    @Override
    public Void visitLiteral(Expr.Literal node) {
        return null;
    }

    @Override
    public Void visitUnary(Expr.Unary node) {
        resolve(node.right);
        return null;
    }

    @Override
    public Void visitVariable(Expr.Variable node) {
        if (!scopes.isEmpty() &&
            scopes.peek().get(node.name.lexeme()) == Boolean.FALSE) {
            Lox.error(node.name.line(), "Can't read local variable in its own initialisation.");
            return null;
        }

        resolveLocal(node, node.name);
        return null;
    }

    @Override
    public Void visitAssign(Expr.Assign node) {
        resolve(node.expression);
        resolveLocal(node, node.name);
        return null;
    }

    private void resolveLocal(@NotNull Expr expression, @NotNull Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                interpreter.resolve(expression, scopes.size() - i - 1);
                return;
            }
        }
    }

    @Override
    public Void visitLogical(Expr.Logical node) {
        resolve(node.left);
        resolve(node.right);
        return null;
    }

    @Override
    public Void visitCall(Expr.Call node) {
        resolve(node.callee);
        for (Expr argument: node.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGet(Expr.Get node) {
        resolve(node.object);
        return null;
    }

    @Override
    public Void visitSet(Expr.Set node) {
        resolve(node.object);
        resolve(node.value);
        return null;
    }

    @Override
    public Void visitExpression(Stmt.Expression node) {
        resolve(node.expression);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print node) {
        resolve(node.expression);
        return null;
    }

    @Override
    public Void visitVar(Stmt.Var node) {
        declare(node.name);
        if (node.initializer != null) {
            resolve(node.initializer);
        }
        define(node.name);
        return null;
    }

    @Override
    public Void visitBlock(Stmt.Block node) {
        beginScope();
        resolve(node.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIf(Stmt.If node) {
        resolve(node.condition);
        resolve(node.thenBranch);
        if (node.elseBranch != null) {
            resolve(node.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhile(Stmt.While node) {
        resolve(node.condition);
        resolve(node.body);
        return null;
    }

    @Override
    public Void visitFunction(Stmt.Function node) {
        declare(node.name);
        define(node.name);

        resolveFunction(node, FunctionType.FUNCTION);

        return null;
    }

    private void resolveFunction(@NotNull Stmt.Function function,
                                 @NotNull FunctionType newFunctionType) {
        FunctionType oldFunctionType = functionType;
        functionType = newFunctionType;
        beginScope();

        for (Token parameter: function.params) {
            declare(parameter);
            define(parameter);
        }

        resolve(function.body);

        endScope();
        functionType = oldFunctionType;
    }

    @Override
    public Void visitReturn(Stmt.Return node) {
        if (functionType == FunctionType.NONE) {
            Lox.error(node.keyword.line(), "Return is not allowed here.");
            return null;
        }

        if (node.value != null) {
            if (functionType == FunctionType.INITIALISER) {
                Lox.error(node.keyword.line(), "Can't return a value from an initialiser.");
            }
            resolve(node.value);
        }
        return null;
    }

    @Override
    public Void visitClass(Stmt.Class node) {
        ClassType oldClassType = classType;
        classType = ClassType.CLASS;

        declare(node.name);
        define(node.name);

        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method: node.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme().equals("init")) {
                declaration = FunctionType.INITIALISER;
            }
            resolveFunction(method, declaration);
        }

        endScope();

        classType = oldClassType;

        return null;
    }

    @Override
    public Void visitThis(Expr.This node) {
        if (classType != ClassType.CLASS) {
            Lox.error(node.keyword.line(), "Cannot use 'this' outside of a class.");
            return null;
        }

        resolveLocal(node, node.keyword);
        return null;
    }
}
