package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Lox {

    private static boolean shouldExitWithErrorCode = false;

    static void error(int line,
                      @NotNull String message) {
        report(line, "", message);
        // Error has happened, we should signal that the process should be terminated
        // with an exit code.
        shouldExitWithErrorCode = true;
    }

    private static void report(int line,
                               @NotNull String where,
                               @NotNull String message) {
        System.err.printf("[line %d] Error %s: %s\n", line, where, message);
    }

    /**
     * Lox entry point.
     * Supports to modes: running a supplied file or evaluating commands in the interactive mode.
     *
     * @param args command line arguments, supplied by JVM.
     * @throws IOException if the file is not found.
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            // Running a script file.
            runFromFile(args[0]);
        } else {
            // Running interactive mode.
            runPrompt();
        }
    }

    private static void runFromFile(@NotNull String file) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(file));
        run(new String(bytes, Charset.defaultCharset()));

        if (shouldExitWithErrorCode) {
            // File run has finished, though there were errors
            // while evaluating it. Finishing with an error code.
            System.exit(65);
        }
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
            // Error may happen, though it does not mean we should terminate the session.
            shouldExitWithErrorCode = false;
        }
    }

    private static void run(@NotNull String rawScript) {
        Scanner scanner = new Scanner(rawScript);

        List<Token> tokens = scanner.scan();
        Parser parser = new Parser(tokens);

        Expr abstractSyntaxTree = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(abstractSyntaxTree);
    }
}