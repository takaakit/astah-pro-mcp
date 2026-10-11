package com.astahpromcp.tool.common;

import java.io.IOException;
import java.io.Writer;

// The writer a script prints through: once the runner thread has been interrupted, the next print stops the script.
public final class StopWhenInterruptedWriter extends Writer {

    private final Writer target;

    public StopWhenInterruptedWriter(Writer target) {
        this.target = target;
    }

    @Override
    public void write(char[] chars, int offset, int length) throws IOException {
        stopIfInterrupted();
        target.write(chars, offset, length);
    }

    @Override
    public void write(String text, int offset, int length) throws IOException {
        stopIfInterrupted();
        target.write(text, offset, length);
    }

    @Override
    public void flush() throws IOException {
        target.flush();
    }

    @Override
    public void close() throws IOException {
        target.close();
    }

    // Reads the flag without clearing it, so that every later print stops the script as well.
    private static void stopIfInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new IllegalStateException("The script was stopped: its run timed out or its caller stopped waiting.");
        }
    }
}
