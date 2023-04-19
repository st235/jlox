package st235.com.github.lox;

public class SymbolsUtils {

    private SymbolsUtils() {
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
