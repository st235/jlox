package com.github.st235.lox;

public abstract class Expr {

    abstract <R> R visit(Visitor<R> visitor);

    public static class Binary extends Expr {

        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left,  Token operator,  Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitBinary(this);
        }

    }

    public static class Grouping extends Expr {

        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitGrouping(this);
        }

    }

    public static class Literal extends Expr {

        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }

    }

    public static class Unary extends Expr {

        final Token operator;
        final Expr right;

        Unary(Token operator,  Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitUnary(this);
        }

    }

    public interface Visitor<R> {
        R visitBinary(Binary node);
        R visitGrouping(Grouping node);
        R visitLiteral(Literal node);
        R visitUnary(Unary node);
    }
}
