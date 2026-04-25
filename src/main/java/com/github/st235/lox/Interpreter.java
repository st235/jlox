package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    @NotNull
    private final PrintWriter outputWriter;

    @NotNull
    private final Environment global = new Environment();

    @NotNull
    private Environment environment = global;

    @NotNull
    private Map<Expr, Integer> localsDepthLookup = new HashMap<>();

    Interpreter() {
        this(System.out);
    }

    Interpreter(@NotNull OutputStream outputStream) {
        this.outputWriter = new PrintWriter(new OutputStreamWriter(outputStream));

        addFunction(new NativeFunction("clock", 0) {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return System.currentTimeMillis() / 1000.0;
            }
        });
    }

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

    void addFunction(@NotNull NativeFunction function) {
        global.define(function.name, function);
    }

    void resolve(@NotNull Expr expression, int depth) {
        localsDepthLookup.put(expression, depth);
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
        return lookupVariable(node);
    }

    private Object lookupVariable(@NotNull Expr.Variable expression) {
        Integer localDepth = localsDepthLookup.get(expression);
        if (localDepth == null) {
            return global.get(expression.name);
        }
        return environment.getAt(localDepth, expression.name);
    }

    @Override
    public Object visitLiteral(Expr.Literal node) {
        return node.value;
    }

    @Override
    public Object visitLogical(Expr.Logical node) {
        Object left = eval(node.left);

        if (node.operator.type() == Token.Type.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else {
            if (!isTruthy(left)) {
                return left;
            }
        }

        return eval(node.right);
    }

    @Override
    public Void visitExpression(Stmt.Expression node) {
        eval(node.expression);
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print node) {
        outputWriter.println(stringify(eval(node.expression)));
        outputWriter.flush();
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

        Integer localDepth = localsDepthLookup.get(node);
        if (localDepth == null) {
            global.assign(node.name, value);
        } else {
            environment.assignAt(localDepth, node.name, value);
        }

        return value;
    }

    @Override
    public Void visitBlock(Stmt.Block node) {
        executeBlock(node.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitWhile(Stmt.While node) {
        while (isTruthy(eval(node.condition))) {
            node.body.visit(this);
        }
        return null;
    }

    @Override
    public Void visitIf(Stmt.If node) {
        if (isTruthy(eval(node.condition))) {
            node.thenBranch.visit(this);
            return null;
        }

        if (node.elseBranch != null) {
            node.elseBranch.visit(this);
            return null;
        }

        return null;
    }

    @Override
    public Object visitCall(Expr.Call node) {
        Object callee = eval(node.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr expr: node.arguments) {
            arguments.add(eval(expr));
        }

        if (!(callee instanceof LoxCallable function)) {
            throw new RuntimeError(node.paren, "Can only call functions and classes.");
        }

        if (function.arity() != arguments.size()) {
            throw new RuntimeError(node.paren,
                    String.format("Expected %d arguments but got %d.", function.arity(), arguments.size()));
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visitFunction(Stmt.Function node) {
        environment.define(node.name.lexeme(), new LoxFunction(node, environment));
        return null;
    }

    @Override
    public Void visitReturn(Stmt.Return node) {
        Object value = null;
        if (node.value != null) value = eval(node.value);
        throw new Return(value);
    }

    void executeBlock(@NotNull List<Stmt> statements, @NotNull Environment currentEnvironment) {
        Environment previous = environment;

        try {
            this.environment = currentEnvironment;

            for (Stmt statement: statements) {
                statement.visit(this);
            }
        } finally {
            this.environment = previous;
        }
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
