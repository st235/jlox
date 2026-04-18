package com.github.st235.tools;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Slightly hacky solution to hide file and {@link System#out}
 * under the same interface, though avoid non-file writers.
 */
final class NonClosingWriter extends BufferedWriter {

    public NonClosingWriter(@NotNull Writer out) {
        super(out);
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}
