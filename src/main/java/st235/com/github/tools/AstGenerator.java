package st235.com.github.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AstGenerator {

    private static final String DEFAULT_INDENT = "    ";

    private final String rootInterface;
    private final List<String> entriesDefinition;

    public AstGenerator(String rootInterface, List<String> entriesDefinition) {
        this.rootInterface = rootInterface.trim();
        this.entriesDefinition = entriesDefinition;
    }

    private void defineInnerClass(BufferedWriter writer,
                                  String indent,
                                  String type,
                                  String[] dependencies) throws IOException {
        writer.write(indent + String.format("public static class %s extends %s {", type, rootInterface));
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
            String argument = dependency.trim().split(" ")[1].trim();
            writer.write(methodIndent + String.format("this.%s = %s;", argument, argument));
            writer.newLine();
        }

        writer.write(innerIndent + "}");
        writer.newLine();
        writer.newLine();

        writer.write(innerIndent + "@Override");
        writer.newLine();
        writer.write(innerIndent + "<R> R visit(Visitor<R> visitor) {");
        writer.newLine();
        writer.write(innerIndent + indent + String.format("return visitor.visit%s(this);", type));
        writer.newLine();
        writer.write(innerIndent + "}");
        writer.newLine();
        writer.newLine();

        writer.write(indent + "}");
        writer.newLine();
    }

    private void defineVisitor(BufferedWriter writer,
                               String indent)  throws IOException {
        String innerIndent = indent + indent;

        writer.write(indent + "public interface Visitor<R> {");
        writer.newLine();
        writer.newLine();

        for (String entryDefinition: entriesDefinition) {
            String[] parts = entryDefinition.split(":");
            String type = parts[0].trim();

            writer.write(innerIndent + String.format("R visit%s(%s node);", type, type));
            writer.newLine();
            writer.newLine();
        }

        writer.write(indent + "}");
        writer.newLine();
        writer.newLine();
    }

    public void define(BufferedWriter writer)  throws IOException {
        writer.write("package st235.com.github.lox;");
        writer.newLine();
        writer.newLine();

        writer.write("import java.util.List;");
        writer.newLine();
        writer.newLine();

        writer.write(String.format("public abstract class %s {", rootInterface));
        writer.newLine();

        defineVisitor(writer, DEFAULT_INDENT);

        for (String entryDefinition: entriesDefinition) {
            String[] parts = entryDefinition.split(":");
            String type = parts[0].trim();
            String[] entries = parts[1].trim().split(",");

            defineInnerClass(writer, DEFAULT_INDENT, type, entries);
            writer.newLine();
        }

        String innerIndent = DEFAULT_INDENT;

        writer.write(innerIndent + "abstract <R> R visit(Visitor<R> visitor);");
        writer.newLine();
        writer.newLine();

        writer.write("}");
        writer.newLine();
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: generate_ast <output dir>");
            System.exit(64);
        }

        String rootInterface = "Expression";
        AstGenerator generator = new AstGenerator(rootInterface, Arrays.asList(
                "Binary: Expression left, Token operator, Expression right",
                "Grouping: Expression expression",
                "Literal: Object value",
                "Unary: Token operator, Expression right"
        ));

        if (args.length == 0) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8))) {
                generator.define(writer);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            String outputDir = args[0];
            Path outputPath = Paths.get(outputDir, String.format("%s.java", rootInterface));

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                generator.define(writer);
                System.out.println("File generated at: " + outputPath);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
