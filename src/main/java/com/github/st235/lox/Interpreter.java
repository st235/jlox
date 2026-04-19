package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    @NotNull
    private final Environment environment = new Environment();

    private static String stringify(Object value) {
        if (value == null) return "nil";

        if (value instanceof Double) {
            String text = value.toString();

            if (text.endsWith(".0")) {
                text = text.replace(".0", "");
            }

            return text;
        }

        return String.valueOf(value);
    }

    public void interpret(@NotNull List<Stmt> statements) {
        try {
            for (Stmt statement: statements) {
                statement.visit(this);
            }
        } catch (RuntimeError error) {
            Lox.error(error.token.line(), error.getMessage());
        }
    }

    private Object eval(Expr expression) {
        return expression.visit(this);
    }

    @Override
    public Object visitUnary(Expr.Unary node) {
        Object right = eval(node.right);
        Token operator = node.operator;

        return switch (operator.type()) {
            case NOT -> !isTruthy(right);
            case MINUS -> {
                checkIfNumberOperand(operator, right);
                yield -(double) right;
            }
            default -> null;
        };
    }

    @Override
    public Object visitBinary(Expr.Binary node) {
        Object left = eval(node.left);
        Object right = eval(node.right);
        Token operator = node.operator;

        return switch (operator.type()) {
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }

                if (left instanceof String || right instanceof String) {
                    yield String.valueOf(left) + String.valueOf(right);
                }

                throw new RuntimeError(operator, "Operand supports only strings or doubles.");
            }
            case MINUS -> {
                checkIfNumberOperand(operator, left, right);
                yield (double) left - (double) right;
            }
            case SLASH -> {
                checkIfNumberOperand(operator, left, right);

                if ((double) right == 0) {
                    throw new RuntimeError(operator, "Divide by 0");
                }

                yield (double) left / (double) right;
            }
            case STAR -> {
                checkIfNumberOperand(operator, left, right);
                yield (double) left * (double) right;
            }
            case GREATER -> {
                checkIfNumberOperand(operator, left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkIfNumberOperand(operator, left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkIfNumberOperand(operator, left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkIfNumberOperand(operator, left, right);
                yield (double) left <= (double) right;
            }
            case NOT_EQUAL -> !isEqual(left, right);
            case EQUAL_EQUAL -> isEqual(left, right);
            default -> null;
        };
    }

    @Override
    public Object visitGrouping(Expr.Grouping node) {
        Expr innerExpression = node.expression;
        return eval(innerExpression);
    }

    @Override
    public Object visitVariable(Expr.Variable node) {
        return environment.get(node.name);
    }

    @Override
    public Object visitLiteral(Expr.Literal node) {
        return node.value;
    }

    @Override
    public Void visitExpression(Stmt.Expression node) {
        eval(node.expression);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print node) {
        System.out.println(stringify(eval(node.expression)));
        return null;
    }

    @Override
    public Void visitVar(Stmt.Var node) {
        Object value = null;
        if (node.initializer != null) {
            value = eval(node.initializer);
        }
        environment.define(node.name.lexeme(), value);
        return null;
    }

    @Override
    public Object visitAssign(Expr.Assign node) {
        Object value = eval(node.expression);
        environment.assign(node.name, value);
        return null;
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object one, Object another) {
        if (one == null && another == null) return true;
        if (one == null) return false;
        return one.equals(another);
    }

    private void checkIfNumberOperand(Token operator, Object left, Object right) {
        checkIfNumberOperand(operator, left);
        checkIfNumberOperand(operator, right);
    }

    private void checkIfNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
}
