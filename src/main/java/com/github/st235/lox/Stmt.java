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

    public static class While extends Stmt {

        final Expr condition;
        final Stmt body;

        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitWhile(this);
        }

    }

    public static class Function extends Stmt {

        final Token name;
        final List<Token> params;
        final List<Stmt> body;

        Function(Token name,  List<Token> params,  List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitFunction(this);
        }

    }

    public static class Return extends Stmt {

        final Token keyword;
        final Expr value;

        Return(Token keyword,  Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitReturn(this);
        }

    }

    public static class Class extends Stmt {

        final Token name;
        final Expr.Variable superclass;
        final List<Stmt.Function> methods;

        Class(Token name,  Expr.Variable superclass,  List<Stmt.Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }

        @Override
        <R> R visit(Visitor<R> visitor) {
            return visitor.visitClass(this);
        }

    }

    public interface Visitor<R> {
        R visitExpression(Expression node);
        R visitPrint(Print node);
        R visitVar(Var node);
        R visitBlock(Block node);
        R visitIf(If node);
        R visitWhile(While node);
        R visitFunction(Function node);
        R visitReturn(Return node);
        R visitClass(Class node);
    }
}
