package com.github.st235.lox;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpreterTest {

    private static final List<NativeFunction> NATIVE_FUNCTIONS = new ArrayList<>();

    static {
        NATIVE_FUNCTIONS.add(new NativeFunction("floor", 1) {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.floor((double) arguments.get(0));
            }
        });

        NATIVE_FUNCTIONS.add(new NativeFunction("mod", 2) {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)(((Double)arguments.get(0)).intValue() % ((Double)arguments.get(1)).intValue());
            }
        });

        NATIVE_FUNCTIONS.add(new NativeFunction("div", 2) {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return ((Double)arguments.get(0)).intValue() / ((Double)arguments.get(1)).intValue();
            }
        });
    }

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
        Resolver resolver = new Resolver(interpreter);

        for (NativeFunction function: NATIVE_FUNCTIONS) {
            interpreter.addFunction(function);
        }

        resolver.resolve(statements);
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
                new Arguments("logical.lox", "logical.out"),
                new Arguments("for.lox", "for.out"),
                new Arguments("functions.lox", "functions.out"),
                new Arguments("closures.lox", "closures.out")
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
