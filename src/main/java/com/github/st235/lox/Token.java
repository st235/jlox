package com.github.st235.lox;

public final class Token {

    public enum Type {
        // Single character operators
        LEFT_PARENTHESIS, RIGHT_PARENTHESIS, LEFT_BRACE, RIGHT_BRACE,
        PLUS, MINUS, SLASH, STAR, COMA, DOT, SEMICOLON,

        // One or two characters operators
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL,
        NOT, NOT_EQUAL,
        EQUAL, EQUAL_EQUAL,
        SINGLE_LINE_COMMENT,

        // Literals
        IDENTIFIER, NUMBER, STRING,

        // Keywords
        NIL, IF, ELSE, FOR, VAR, FUNCTION, RETURN, CLASS,
        TRUE, FALSE, PRINT, AND, OR, SUPER, THIS, WHILE,

        EOF
    }

    final Type type;
    final String lexeme;
    final Object literal;
    final int line;

    public static Token from(Type type, int line) {
        return from(type, "", null, line);
    }


    public static Token from(Type type, String lexeme, int line) {
        return from(type, lexeme, null, line);
    }

    public static Token from(Type type, String lexeme, Object literal, int line) {
        return new Token(type, lexeme, literal, line);
    }

    public Token(Type type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", lexeme='" + lexeme + '\'' +
                ", literal=" + literal +
                ", line=" + line +
                '}';
    }
}
