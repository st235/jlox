package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Token(Type type, @NotNull String lexeme, @Nullable Object literal, int line) {

    public enum Type {
        // Single character operators.
        LEFT_PARENTHESIS, RIGHT_PARENTHESIS, LEFT_BRACE, RIGHT_BRACE,
        PLUS, MINUS, SLASH, STAR, COMA, DOT, SEMICOLON,

        // One or two characters operators.
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL,
        NOT, NOT_EQUAL,
        EQUAL, EQUAL_EQUAL,
        SINGLE_LINE_COMMENT,

        // Literals.
        IDENTIFIER, NUMBER, STRING,

        // Keywords.
        NIL, IF, ELSE, FOR, VAR, FUNCTION, RETURN, CLASS,
        TRUE, FALSE, PRINT, AND, OR, SUPER, THIS, WHILE,

        // Misc.
        EOF,
    }

    public static Token from(Type type, int line) {
        return from(type, "", null, line);
    }

    public static Token from(Type type, @NotNull String lexeme, @Nullable Object literal, int line) {
        return new Token(type, lexeme, literal, line);
    }
}
