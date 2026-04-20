package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpreterTest {

    @ParameterizedTest
    @MethodSource("provideScriptsForInterpreter")
    void when_providesAValidLoxScript_producesAValidOutput(@NotNull Arguments arguments) {
        String scriptFile = readFile(arguments.inputScript);
        String expectedOutput = readFile(arguments.expectedOutputFile);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        Scanner scanner = new Scanner(scriptFile);

        List<Token> tokens = scanner.scan();
        Parser parser = new Parser(tokens);

        List<Stmt> statements = parser.parse();

        Interpreter interpreter = new Interpreter(outStream);
        interpreter.interpret(statements);

        String loxOutput = outStream.toString(StandardCharsets.UTF_8);

        assertEquals(expectedOutput, loxOutput);
    }

    private record Arguments(@NotNull String inputScript, @NotNull String expectedOutputFile) {}

    private static Stream<Arguments> provideScriptsForInterpreter() {
        return Stream.of(
                new Arguments("expressions.lox", "expressions.out"),
                new Arguments("variables.lox", "variables.out"),
                new Arguments("scopes.lox", "scopes.out"),
                new Arguments("if.lox", "if.out"),
                new Arguments("while.lox", "while.out"),
                new Arguments("logical.lox", "logical.out")
        );
    }

    @NotNull
    private static String readFile(@NotNull String filename) {
        ClassLoader classLoader = InterpreterTest.class.getClassLoader();
        try (InputStream fileStream = classLoader.getResourceAsStream(filename)) {
            return new String(fileStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
