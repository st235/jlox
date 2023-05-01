package st235.com.github.lox;

import java.io.PrintStream;

public class AstPrinter implements Expression.Visitor<String> {

    private static final String SPACE = " ";

    public void print(Expression expression, PrintStream printStream) {
        if (expression == null) {
            printStream.println("error: expression is null");
            return;
        }

        String expressionAsString = expression.visit(this);
        printStream.println(expressionAsString);
    }

    @Override
    public String visitBinary(Expression.Binary node) {
        return prettyPrint(node.operator.lexeme, node.left, node.right);
    }

    @Override
    public String visitGrouping(Expression.Grouping node) {
        return prettyPrint("grouping", node.expression);
    }

    @Override
    public String visitLiteral(Expression.Literal node) {
        Object value = node.value == null ? "nil" : node.value;
        return String.valueOf(value);
    }

    @Override
    public String visitUnary(Expression.Unary node) {
        return prettyPrint(node.operator.lexeme, node.right);
    }

    private String prettyPrint(String expression, Expression... children) {
        StringBuilder builder = new StringBuilder();

        builder.append("(")
                .append(expression);

        for (Expression child: children) {
            builder
                    .append(SPACE)
                    .append(child.visit(this));
        }

        builder.append(")");
        return builder.toString();
    }

    public static void main(String[] args) {
        Expression expression = new Expression.Binary(
                new Expression.Unary(
                        new Token(Token.Type.MINUS, "-", null, 1),
                        new Expression.Literal(123)
                ),
                new Token(Token.Type.STAR, "*", null, 1),
                new Expression.Grouping(
                        new Expression.Literal(45.67)
                )
        );

        AstPrinter printer = new AstPrinter();
        System.out.println(expression.visit(printer));
    }
}
