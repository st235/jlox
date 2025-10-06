package com.github.st235.lox;

import java.util.List;

public final class Parser {

    private final List<Token> tokens;

    private int pointer;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pointer = 0;
    }

    public Expression parse() {
        try {
            return expression();
        } catch (ParsingException exception) {
            return null;
        }
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression lhs = comparison();

        while (match(Token.Type.EQUAL_EQUAL, Token.Type.NOT_EQUAL)) {
            Token operand = previous();
            Expression rhs = comparison();

            lhs = new Expression.Binary(lhs, operand, rhs);
        }

        return lhs;
    }

    private Expression comparison() {
        Expression lhs = term();

        while (match(Token.Type.LESS_EQUAL, Token.Type.LESS, Token.Type.GREATER_EQUAL, Token.Type.GREATER)) {
            Token operand = previous();
            Expression rhs = term();

            lhs = new Expression.Binary(lhs, operand, rhs);
        }

        return lhs;
    }

    private Expression term() {
        Expression lhs = factor();

        while (match(Token.Type.PLUS, Token.Type.MINUS)) {
            Token operand = previous();
            Expression rhs = factor();

            lhs = new Expression.Binary(lhs, operand, rhs);
        }

        return lhs;
    }

    private Expression factor() {
        Expression lhs = unary();

        while (match(Token.Type.STAR, Token.Type.SLASH)) {
            Token operand = previous();
            Expression rhs = unary();

            lhs = new Expression.Binary(lhs, operand, rhs);
        }

        return lhs;
    }

    private Expression unary() {
        if (match(Token.Type.MINUS, Token.Type.NOT)) {
            return new Expression.Unary(previous(), unary());
        } else {
            return primary();
        }
    }

    private Expression primary() {
        if (match(Token.Type.FALSE)) {
            return new Expression.Literal(false);
        }

        if (match(Token.Type.TRUE)) {
            return new Expression.Literal(true);
        }

        if (match(Token.Type.NIL)) {
            return new Expression.Literal(null);
        }

        if (match(Token.Type.NUMBER, Token.Type.STRING)) {
            return new Expression.Literal(previous().literal());
        }

        if (match(Token.Type.LEFT_BRACE)) {
            Expression expression = expression();
            consume(Token.Type.RIGHT_BRACE, "No matching )");
            return expression;
        }

        throw new ParsingException("Expression expected but " + peek().type() + " found");
    }

    private Token consume(Token.Type token, String message) {
        if (check(token)) {
            return advance();
        }

        // TODO(st235): implement errors propagation.
        throw new ParsingException(message);
    }

    private boolean match(Token.Type... types) {
        for (Token.Type type: types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(Token.Type type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type() == type;
    }

    private boolean isAtEnd() {
        return peek().type() == Token.Type.EOF;
    }

    private Token peek() {
        return tokens.get(pointer);
    }

    private Token previous() {
        return tokens.get(pointer - 1);
    }

    private Token advance() {
        if (isAtEnd()) {
            return peek();
        }

        Token token = peek();
        pointer += 1;
        return token;
    }

    final class ParsingException extends RuntimeException {

        public ParsingException(String message) {
            super(message);
        }

        public ParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
