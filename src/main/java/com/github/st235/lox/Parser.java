package com.github.st235.lox;

import java.util.List;

public final class Parser {

    private final List<Token> tokens;

    private int pointer;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pointer = 0;
    }

    public Expr parse() {
        try {
            return expression();
        } catch (ParsingException exception) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr lhs = comparison();

        while (match(Token.Type.EQUAL_EQUAL, Token.Type.NOT_EQUAL)) {
            Token operand = previous();
            Expr rhs = comparison();

            lhs = new Expr.Binary(lhs, operand, rhs);
        }

        return lhs;
    }

    private Expr comparison() {
        Expr lhs = term();

        while (match(Token.Type.LESS_EQUAL, Token.Type.LESS, Token.Type.GREATER_EQUAL, Token.Type.GREATER)) {
            Token operand = previous();
            Expr rhs = term();

            lhs = new Expr.Binary(lhs, operand, rhs);
        }

        return lhs;
    }

    private Expr term() {
        Expr lhs = factor();

        while (match(Token.Type.PLUS, Token.Type.MINUS)) {
            Token operand = previous();
            Expr rhs = factor();

            lhs = new Expr.Binary(lhs, operand, rhs);
        }

        return lhs;
    }

    private Expr factor() {
        Expr lhs = unary();

        while (match(Token.Type.STAR, Token.Type.SLASH)) {
            Token operand = previous();
            Expr rhs = unary();

            lhs = new Expr.Binary(lhs, operand, rhs);
        }

        return lhs;
    }

    private Expr unary() {
        if (match(Token.Type.MINUS, Token.Type.NOT)) {
            return new Expr.Unary(previous(), unary());
        } else {
            return primary();
        }
    }

    private Expr primary() {
        if (match(Token.Type.FALSE)) {
            return new Expr.Literal(false);
        }

        if (match(Token.Type.TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(Token.Type.NIL)) {
            return new Expr.Literal(null);
        }

        if (match(Token.Type.NUMBER, Token.Type.STRING)) {
            return new Expr.Literal(previous().literal());
        }

        if (match(Token.Type.LEFT_BRACE)) {
            Expr expression = expression();
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
