package com.github.st235.lox;

import java.io.PrintStream;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<Void> {

    private static final String SPACE = " ";

    public void print(Expr expression, PrintStream printStream) {
        if (expression == null) {
            printStream.println("error: expression is null");
            return;
        }

        String expressionAsString = expression.visit(this);
        printStream.println(expressionAsString);
    }

    @Override
    public String visitBinary(Expr.Binary node) {
        return prettyPrint(node.operator.lexeme(), node.left, node.right);
    }

    @Override
    public String visitGrouping(Expr.Grouping node) {
        return prettyPrint("grouping", node.expression);
    }

    @Override
    public String visitLiteral(Expr.Literal node) {
        Object value = node.value == null ? "nil" : node.value;
        return String.valueOf(value);
    }

    @Override
    public String visitUnary(Expr.Unary node) {
        return prettyPrint(node.operator.lexeme(), node.right);
    }

    @Override
    public String visitVariable(Expr.Variable node) {
        return node.name.lexeme();
    }

    @Override
    public Void visitExpression(Stmt.Expression node) {
        prettyPrint("expression", node.expression);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print node) {
        prettyPrint("print", node.expression);
        return null;
    }

    @Override
    public Void visitVar(Stmt.Var node) {
        prettyPrint("var", node.initializer);
        return null;
    }

    private String prettyPrint(String expression, Expr... children) {
        StringBuilder builder = new StringBuilder();

        builder.append("(")
                .append(expression);

        for (Expr child: children) {
            builder
                    .append(SPACE)
                    .append(child.visit(this));
        }

        builder.append(")");
        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(Token.Type.MINUS, "-", null, 1),
                        new Expr.Literal(123)
                ),
                new Token(Token.Type.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)
                )
        );

        AstPrinter printer = new AstPrinter();
        System.out.println(expression.visit(printer));
    }
}
