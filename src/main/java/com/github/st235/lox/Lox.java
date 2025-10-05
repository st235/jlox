package com.github.st235.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Lox {

    private static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.printf("[line %d] Error %s: %s\n", line, where, message);
    }

    private static void run(String rawScript) {
        Scanner scanner = new Scanner(rawScript);

        try {
            List<Token> tokens = scanner.scan();
            Parser parser = new Parser(tokens);

            Expression abstractSyntaxTree = parser.parse();

            Interpreter interpreter = new Interpreter();
            interpreter.interpret(abstractSyntaxTree);
        } catch (Scanner.ScanningException scanningException) {
            error(scanningException.line, scanningException.getMessage());
        }
    }

    private static void runFromFile(String file) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(file));
        run(new String(bytes, Charset.defaultCharset()));
    }

    /**
     * REPL
     * (print (eval (read)))
     */
    private static void runPrompt() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            // script file
            runFromFile(args[0]);
        } else {
            runPrompt();
        }
    }
}