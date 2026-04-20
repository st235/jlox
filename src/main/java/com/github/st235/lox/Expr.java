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

    public static class Variable extends Expr {

        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitVariable(this);
        }

    }

    public static class Assign extends Expr {

        final Token name;
        final Expr expression;

        Assign(Token name,  Expr expression) {
            this.name = name;
            this.expression = expression;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitAssign(this);
        }

    }

    public static class Logical extends Expr {

        final Expr left;
        final Token operator;
        final Expr right;

        Logical(Expr left,  Token operator,  Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitLogical(this);
        }

    }

    public interface Visitor<R> {
        R visitBinary(Binary node);
        R visitGrouping(Grouping node);
        R visitLiteral(Literal node);
        R visitUnary(Unary node);
        R visitVariable(Variable node);
        R visitAssign(Assign node);
        R visitLogical(Logical node);
    }

}
