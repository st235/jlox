package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class Parser {

    private final List<Token> tokens;

    private int pointer;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pointer = 0;
    }

    public List<Stmt> parse() {
        List<Stmt> out = new ArrayList<>();

        while (!isAtEnd()) {
            out.add(declaration());
        }

        return out;
    }

    private Stmt declaration() {
        try {
            if (match(Token.Type.VAR)) return varStatement();
            if (match(Token.Type.FUNCTION)) return funStatement("function");
            if (match(Token.Type.CLASS)) return classStatement();
            return statement();
        } catch (RuntimeError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classStatement() {
        Token name = consume(Token.Type.IDENTIFIER, "Expect class name.");
        consume(Token.Type.LEFT_PARENTHESIS, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(Token.Type.RIGHT_PARENTHESIS) && !isAtEnd()) {
            methods.add(funStatement("method"));
        }
        consume(Token.Type.RIGHT_PARENTHESIS, "Expect '}' after class body.");

        return new Stmt.Class(name, methods);
    }

    private Stmt.Function funStatement(@NotNull String kind) {
        Token name = consume(Token.Type.IDENTIFIER, String.format("Expect %s name.", kind));

        consume(Token.Type.LEFT_BRACE,  String.format("Expect '(' after %s name.", kind));

        List<Token> parameters = new ArrayList<>();
        if (!check(Token.Type.RIGHT_BRACE)) {
            do {
                if (parameters.size() >= 255) {
                    Lox.error(peek().line(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(Token.Type.IDENTIFIER, "Expect parameter name."));
            } while (match(Token.Type.COMA));
        }

        consume(Token.Type.RIGHT_BRACE, "Expect ')' after parameters.");

        consume(Token.Type.LEFT_PARENTHESIS, String.format("Expect '{' before %s body.", kind));
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt varStatement() {
        Token name = consume(Token.Type.IDENTIFIER, "Expected variable name.");

        Expr expression = null;
        if (match(Token.Type.EQUAL)) {
            expression = expression();
        }

        consume(Token.Type.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, expression);
    }

    private Stmt statement() {
        if (match(Token.Type.PRINT)) return printStatement();
        if (match(Token.Type.LEFT_PARENTHESIS)) return new Stmt.Block(block());
        if (match(Token.Type.IF)) return ifStatement();
        if (match(Token.Type.WHILE)) return whileStatement();
        if (match(Token.Type.FOR)) return forStatement();
        if (match(Token.Type.RETURN)) return returnStatement();

        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr expr = null;
        if (!check(Token.Type.SEMICOLON)) {
            expr = expression();
        }
        consume(Token.Type.SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, expr);
    }

    private Stmt forStatement() {
        consume(Token.Type.LEFT_BRACE, "Expect '(' after for.");

        Stmt initialiser;
        if (match(Token.Type.SEMICOLON)) {
            initialiser = null;
        } else if (match(Token.Type.VAR)) {
            initialiser = varStatement();
        } else {
            initialiser = expressionStatement();
        }

        Expr condition = null;
        if (!check(Token.Type.SEMICOLON)) {
            condition = expression();
        }
        consume(Token.Type.SEMICOLON, "Expect ';' after for loop condition.");

        Expr increment = null;
        if (!check(Token.Type.RIGHT_BRACE)) {
            increment = expression();
        }
        consume(Token.Type.RIGHT_BRACE, "Expect ')' after for clauses.");

        Stmt body = statement();

        // Desugaring for loop into a while loop.
        if (increment != null) {
            body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initialiser != null) {
            body = new Stmt.Block(List.of(initialiser, body));
        }

        return body;
    }

    private Stmt whileStatement() {
        consume(Token.Type.LEFT_BRACE, "Expect '(' after while.");
        Expr condition = expression();
        consume(Token.Type.RIGHT_BRACE, "Expect ')' after condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(Token.Type.LEFT_BRACE, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(Token.Type.RIGHT_BRACE, "Expect ')' after if condition.");
        Stmt thenBranch = statement();

        Stmt elseBranch = null;
        if (match(Token.Type.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(Token.Type.SEMICOLON, "Expected ';' after value.");
        return new Stmt.Print(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(Token.Type.RIGHT_PARENTHESIS) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(Token.Type.RIGHT_PARENTHESIS, "Expect '} after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(Token.Type.SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(Token.Type.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            Lox.error(equals.line(), "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr left = and();

        while (match(Token.Type.OR)) {
            Token operator = previous();
            Expr right = and();
            left = new Expr.Logical(left, operator, right);
        }

        return left;
    }

    private Expr and() {
        Expr left = equality();

        while (match(Token.Type.AND)) {
            Token operator = previous();
            Expr right = equality();
            left = new Expr.Logical(left, operator, right);
        }

        return left;
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
            return call();
        }
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(Token.Type.LEFT_BRACE)) {
                expr = finishCall(expr);
            } else if (match(Token.Type.DOT)) {
              Token name = consume(Token.Type.IDENTIFIER, "Expect property name after '.'.");
              expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(@NotNull Expr callee) {
        List<Expr> arguments = new ArrayList<>();

        if (!check(Token.Type.RIGHT_BRACE)) {
            do {
                if (arguments.size() >= 255) {
                    Lox.error(peek().line(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(Token.Type.COMA));
        }

        Token paren = consume(Token.Type.RIGHT_BRACE, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
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

        if (match(Token.Type.THIS)) {
            return new Expr.This(previous());
        }

        if (match(Token.Type.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        throw new ParsingException("Expression expected but " + peek().type() + " found");
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type() == Token.Type.SEMICOLON) return;

            switch (peek().type()) {
                case CLASS:
                case FUNCTION:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
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
