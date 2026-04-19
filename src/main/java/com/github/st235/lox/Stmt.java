package com.github.st235.lox;

public abstract class Stmt {

    abstract <R> R visit(Visitor<R> visitor);

    public static class Expression extends Stmt {

        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitExpression(this);
        }

    }

    public static class Print extends Stmt {

        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitPrint(this);
        }

    }

    public static class Var extends Stmt {

        final Token name;
        final Expr initializer;

        Var(Token name,  Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitVar(this);
        }

    }

    public interface Visitor<R> {
        R visitExpression(Expression node);
        R visitPrint(Print node);
        R visitVar(Var node);
    }

}
