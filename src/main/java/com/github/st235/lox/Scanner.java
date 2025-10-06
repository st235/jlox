package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    @NotNull
    private final String script;
    @NotNull
    private final ScannerStream stream;

    private int line = 1;
    private int start = 0;

    public Scanner(@NotNull String script) {
        this.script = script;
        this.stream = new ScannerStream(script);
    }

    @NotNull
    public List<Token> scan() {
        while (stream.hasNext()) {
            start = stream.position;
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
                if (Symbols.isDigit(c)) {
                    number();
                } else if (Symbols.isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unknown identifier.");
                }
        }
    }

    private void string() {
        StringBuilder literal = new StringBuilder();

        if (!stream.hasNext()) {
            Lox.error(line, "Cannot parse string");
        }

        // Lox allows multi-line strings by design.
        while (stream.hasNext() && stream.peek() != '"') {
           char c = stream.next();
           literal.append(c);
        }

        if (!stream.hasNext()) {
            Lox.error(line, "Unexpected EOF, string should start and finish with closing brackets \".");
        }

        // Reading \".
        stream.next();
        // Let's skip adding bracket to the literal.
        addToken(Token.Type.STRING, literal.toString());
    }

    private void identifier() {
        while (stream.hasNext() && Symbols.isAlphanumeric(stream.peek())) {
            stream.next();
        }

        String rawValue = script.substring(start, stream.position);
        Token.Type keywordType = RESERVED_KEYWORDS.get(rawValue);
        addToken(Objects.requireNonNullElse(keywordType, Token.Type.IDENTIFIER));
    }

    private void number() {
        while (stream.hasNext() && Symbols.isDigit(stream.peek())) {
            stream.next();
        }

        if (stream.hasNext() && stream.peek() == '.' && Symbols.isDigit(stream.peekNext())) {
            // Skipping dot symbol.
            stream.next();
            // Reading the rest of the number after a dot.
            while (stream.hasNext() && Symbols.isDigit(stream.peek())) {
                stream.next();
            }
        }

        addToken(Token.Type.NUMBER,
                Double.parseDouble(script.substring(start, stream.position)));
    }

    private void addToken(@NotNull Token.Type type) {
        addToken(type, null);
    }

    private void addToken(@NotNull Token.Type type, @Nullable Object literal) {
        String lexeme = script.substring(start, stream.position);
        tokens.add(Token.from(type, lexeme, literal, line));
    }

    private static final class ScannerStream {

        private int position;
        @NotNull private final String script;

        public ScannerStream(@NotNull String script) {
            this.script = script;
        }

        char next() {
            char next = peek();
            position += 1;
            return next;
        }

        boolean match(char c) {
            if (!hasNext() || peek() != c) {
                return false;
            }

            position += 1;
            return true;
        }

        char peek() {
            return script.charAt(position);
        }

        char peekNext() {
            if (position + 1 >= script.length()) {
                return '\0';
            }

            return script.charAt(position + 1);
        }

        boolean hasNext() {
            return position < script.length();
        }
    }
}
