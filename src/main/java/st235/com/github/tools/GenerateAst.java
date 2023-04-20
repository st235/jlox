package st235.com.github.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    private static final String DEFAULT_INDENT = "    ";

    private static void defineInnerClass(String indent,
                                  BufferedWriter writer,
                                  String entryDefinition) throws IOException {
        String[] parts = entryDefinition.split(":");
        String type = parts[0].trim();
        String[] dependencies = parts[1].trim().split(",");

        writer.write(indent + String.format("public static final class %s {", type));
        writer.newLine();
        writer.newLine();

        String innerIndent = indent + indent;

        for (String dependency: dependencies) {
            writer.write(innerIndent + String.format("final %s;", dependency.trim()));
            writer.newLine();
        }

        writer.newLine();

        String formattedDependencies = String.join(", ", dependencies);
        writer.write(innerIndent + String.format("%s(%s) {", type, formattedDependencies));
        writer.newLine();

        String methodIndent = innerIndent + indent;
        for (String dependency: dependencies) {
            String argument = dependency.split(" ")[1].trim();
            writer.write(methodIndent + String.format("this.%s = %s;", argument, argument));
            writer.newLine();
        }

        writer.write(innerIndent + "}");
        writer.newLine();
        writer.write(indent + "}");
        writer.newLine();
    }

    private static void defineAst(String outputDir,
                                  String rootInterface,
                                  List<String> entriesDefinition) {
        Path outputPath = Paths.get(outputDir, String.format("%s.java", rootInterface));

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("package st235.com.github.lox;");
            writer.newLine();
            writer.newLine();

            writer.write("import java.util.List;");
            writer.newLine();
            writer.newLine();

            writer.write(String.format("public abstract class %s {", rootInterface));
            writer.newLine();

            for (String entryDefinition: entriesDefinition) {
                defineInnerClass(DEFAULT_INDENT, writer, entryDefinition);
                writer.newLine();
            }

            writer.write("}");
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("File generated at: " + outputPath);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output dir>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expression", Arrays.asList(
                "Binary: Expression left, Token operator, Expression right",
                "Grouping: Expression expression",
                "Literal: Object value",
                "Unary: Token operator, Expression right"
        ));
    }
}
