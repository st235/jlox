package com.github.st235.lox;

public final class Symbols {

    private Symbols() {
        // Private on purpose.
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    public static boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

}
