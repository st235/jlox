package com.github.st235.tools;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AstGenerator {

    private static final String DEFAULT_INDENT = "    ";

    @NotNull
    private final String packageName;
    @NotNull
    private final List<String> imports;

    public AstGenerator(@NotNull String packageName,
                        @NotNull List<String> imports) {
        this.packageName = packageName.trim();
        this.imports = imports;
        Collections.sort(this.imports);
    }

    public void define(@NotNull String rootInterface,
                       @NotNull List<String> entriesDefinition,
                       @NotNull BufferedWriter writer) throws IOException {
        definePackage(writer);
        defineImports(writer);

        writer.write(String.format("public abstract class %s {", rootInterface));
        writer.newLine();
        writer.newLine();

        writer.write(DEFAULT_INDENT + "abstract <R> R visit(Visitor<R> visitor);");
        writer.newLine();
        writer.newLine();

        for (String entryDefinition: entriesDefinition) {
            String[] parts = entryDefinition.split(":");
            String type = parts[0].trim();
            String[] entries = parts[1].trim().split(",");

            defineInnerClass(rootInterface, type, entries, DEFAULT_INDENT, writer);
            writer.newLine();
        }

        defineVisitor(entriesDefinition, DEFAULT_INDENT, writer);

        writer.write("}");
        writer.newLine();
    }

    private void definePackage(@NotNull BufferedWriter writer) throws IOException {
        writer.write(String.format("package %s;",  packageName));
        writer.newLine();
        writer.newLine();
    }

    private void defineImports(@NotNull BufferedWriter writer) throws IOException {
        if (!imports.isEmpty()) {
            for (String imprt : imports) {
                writer.write(String.format("import %s;", imprt));
                writer.newLine();
            }
            writer.newLine();
        }
    }

    private void defineInnerClass(@NotNull String rootInterface,
                                  @NotNull String type,
                                  @NotNull String[] entries,
                                  @NotNull String indent,
                                  @NotNull BufferedWriter writer) throws IOException {
        writer.write(indent + String.format("public static class %s extends %s {", type, rootInterface));
        writer.newLine();
        writer.newLine();

        String innerIndent = indent + indent;

        for (String entry: entries) {
            writer.write(innerIndent + String.format("final %s;", entry.trim()));
            writer.newLine();
        }

        writer.newLine();

        String formattedDependencies = String.join(", ", entries);
        writer.write(innerIndent + String.format("%s(%s) {", type, formattedDependencies));
        writer.newLine();

        String methodIndent = innerIndent + indent;
        for (String entry: entries) {
            String argument = entry.trim().split(" ")[1].trim();
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

    private void defineVisitor(@NotNull List<String> entriesDefinition,
                               @NotNull String indent,
                               @NotNull BufferedWriter writer)  throws IOException {
        writer.write(indent + "public interface Visitor<R> {");
        writer.newLine();

        for (String entryDefinition: entriesDefinition) {
            String[] parts = entryDefinition.split(":");
            String type = parts[0].trim();

            writer.write(indent + indent + String.format("R visit%s(%s node);", type, type));
            writer.newLine();
        }

        writer.write(indent + "}");
        writer.newLine();
        writer.newLine();
    }

    public static void main(@NotNull String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: generate_ast <output dir>");
            System.exit(64);
            return;
        }

        String rootInterface = "Expr";
        AstGenerator generator = new AstGenerator("com.github.st235.lox",
                Arrays.asList());

        try(BufferedWriter writer = createWriter(args, rootInterface)) {
            generator.define(rootInterface, Arrays.asList(
                    "Binary   : Expr left, Token operator, Expr right",
                    "Grouping : Expr expression",
                    "Literal  : Object value",
                    "Unary    : Token operator, Expr right"
            ), writer);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static BufferedWriter createWriter(@NotNull String[] args,
                                               @NotNull String rootInterface) throws IOException {
        if (args.length == 0) {
            return new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        } else {
            String outputDir = args[0];
            Path outputPath = Paths.get(outputDir, String.format("%s.java", rootInterface));
            return Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
        }
    }
}
