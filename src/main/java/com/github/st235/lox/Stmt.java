package com.github.st235.lox;

import java.util.List;

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

    public static class Block extends Stmt {

        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitBlock(this);
        }

    }

    public static class If extends Stmt {

        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        If(Expr condition,  Stmt thenBranch,  Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitIf(this);
        }

    }

    public interface Visitor<R> {
        R visitExpression(Expression node);
        R visitPrint(Print node);
        R visitVar(Var node);
        R visitBlock(Block node);
        R visitIf(If node);
    }

}
