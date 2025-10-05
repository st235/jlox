package com.github.st235.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Scanner {

    private static final Map<String, Token.Type> RESERVED_KEYWORDS = new HashMap<>();

    static {
        RESERVED_KEYWORDS.put("nil", Token.Type.NIL);
        RESERVED_KEYWORDS.put("if", Token.Type.IF);
        RESERVED_KEYWORDS.put("else", Token.Type.ELSE);
        RESERVED_KEYWORDS.put("for", Token.Type.FALSE);
        RESERVED_KEYWORDS.put("var", Token.Type.VAR);
        RESERVED_KEYWORDS.put("fun", Token.Type.FUNCTION);
        RESERVED_KEYWORDS.put("return", Token.Type.RETURN);
        RESERVED_KEYWORDS.put("class", Token.Type.CLASS);
        RESERVED_KEYWORDS.put("true", Token.Type.TRUE);
        RESERVED_KEYWORDS.put("false", Token.Type.FALSE);
        RESERVED_KEYWORDS.put("print", Token.Type.PRINT);
        RESERVED_KEYWORDS.put("and", Token.Type.AND);
        RESERVED_KEYWORDS.put("or", Token.Type.OR);
        RESERVED_KEYWORDS.put("super", Token.Type.SUPER);
        RESERVED_KEYWORDS.put("this", Token.Type.THIS);
        RESERVED_KEYWORDS.put("while", Token.Type.WHILE);
    }

    private final List<Token> tokens = new ArrayList<>();
    private final String script;
    private final ScannerStream stream;

    private int line = 1;
    private int start = 0;

    public Scanner(String script) {
        this.script = script;
        stream = new ScannerStream(script);
    }

    public List<Token> scan() {
        while (stream.hasNext()) {
            start = stream.pointer;
            scanToken();
        }

        tokens.add(Token.from(Token.Type.EOF, line));
        return new ArrayList<>(tokens);
    }

    private void scanToken() {
        char c = stream.next();

        switch (c) {
            case '{': addToken(Token.Type.LEFT_PARENTHESIS); break;
            case '}': addToken(Token.Type.RIGHT_PARENTHESIS); break;
            case '(': addToken(Token.Type.LEFT_BRACE); break;
            case ')': addToken(Token.Type.RIGHT_BRACE); break;
            case '+': addToken(Token.Type.PLUS); break;
            case '-': addToken(Token.Type.MINUS); break;
            case '*': addToken(Token.Type.STAR); break;
            case ',': addToken(Token.Type.COMA); break;
            case '.': addToken(Token.Type.DOT); break;
            case ';': addToken(Token.Type.SEMICOLON); break;
            case '<': addToken(stream.match('=') ? Token.Type.LESS_EQUAL : Token.Type.LESS); break;
            case '>': addToken(stream.match('=') ? Token.Type.GREATER_EQUAL : Token.Type.GREATER); break;
            case '!': addToken(stream.match('=') ? Token.Type.NOT_EQUAL : Token.Type.NOT); break;
            case '=': addToken(stream.match('=') ? Token.Type.EQUAL_EQUAL : Token.Type.EQUAL); break;
            case '/':
                if (stream.peek() == '/') {
                    while (stream.hasNext() && stream.peek() != '\n') {
                        stream.next();
                    }
                } else {
                    addToken(Token.Type.SLASH);
                }
                break;
            case '"': string(); break;
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                line += 1;
                break;
            default:
                if (SymbolsUtils.isDigit(c)) {
                    number();
                } else if (SymbolsUtils.isAlpha(c)) {
                    identifier();
                } else {
                    throw new ScanningException("Unknown identifier", line);
                }
        }
    }

    private void string() {
        StringBuilder literal = new StringBuilder();

        if (!stream.hasNext()) {
            throw new ScanningException("Cannot parse string", line);
        }

        while (stream.hasNext() && stream.peek() != '"') {
           char c = stream.next();

           if (c == '\n') {
               throw new ScanningException("No new lines in a string", line);
           }

           literal.append(c);
        }

        if (!stream.hasNext()) {
            throw new ScanningException("Unexpected EOF", line);
        }

        // "
        stream.next();
        // let's trim " from literal
        addToken(Token.Type.STRING, literal.toString());
    }

    private void identifier() {
        while (stream.hasNext() && SymbolsUtils.isAlphanumeric(stream.peek())) {
            stream.next();
        }

        String rawValue = script.substring(start, stream.pointer);
        Token.Type keywordType = RESERVED_KEYWORDS.get(rawValue);
        if (keywordType == null) {
            addToken(Token.Type.IDENTIFIER);
        } else {
            addToken(keywordType);
        }
    }

    private void number() {
        while (stream.hasNext() && SymbolsUtils.isDigit(stream.peek())) {
            stream.next();
        }

        if (stream.hasNext() && stream.peek() == '.' && SymbolsUtils.isDigit(stream.peekNext())) {
            stream.next();
            while (stream.hasNext() && SymbolsUtils.isDigit(stream.peek())) {
                stream.next();
            }
        }

        addToken(Token.Type.NUMBER,
                Double.parseDouble(script.substring(start, stream.pointer)));
    }

    private void addToken(Token.Type type) {
        addToken(type, null);
    }

    private void addToken(Token.Type type, Object literal) {
        String lexeme = script.substring(start, stream.pointer);
        tokens.add(Token.from(type, lexeme, literal, line));
    }

    private static final class ScannerStream {

        private final String script;
        private int pointer;

        public ScannerStream(String script) {
            this.script = script;
        }

        char next() {
            char next = peek();
            pointer += 1;
            return next;
        }

        char peek() {
            return script.charAt(pointer);
        }

        boolean match(char c) {
            if (!hasNext() || peek() != c) {
                return false;
            }

            pointer += 1;
            return true;
        }

        char peekNext() {
            if (pointer + 1 >= script.length()) {
                return '\0';
            }

            return script.charAt(pointer + 1);
        }

        boolean hasNext() {
            return pointer < script.length();
        }
    }

    final class ScanningException extends RuntimeException {

        final int line;

        public ScanningException(String message, int line) {
            super(message);
            this.line = line;
        }

        public ScanningException(String message, int line, Throwable cause) {
            super(message, cause);
            this.line = line;
        }
    }
}
