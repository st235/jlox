package com.github.st235.lox;

public abstract class Expression {
    public interface Visitor<R> {

        R visitBinary(Binary node);

        R visitGrouping(Grouping node);

        R visitLiteral(Literal node);

        R visitUnary(Unary node);

    }

    public static class Binary extends Expression {

        final Expression left;
        final Token operator;
        final Expression right;

        Binary(Expression left,  Token operator,  Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitBinary(this);
        }

    }

    public static class Grouping extends Expression {

        final Expression expression;

        Grouping(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitGrouping(this);
        }

    }

    public static class Literal extends Expression {

        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }

    }

    public static class Unary extends Expression {

        final Token operator;
        final Expression right;

        Unary(Token operator,  Expression right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitUnary(this);
        }

    }

    abstract <R> R visit(Visitor<R> visitor);

}
