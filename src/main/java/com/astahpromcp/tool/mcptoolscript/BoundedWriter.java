package com.astahpromcp.tool.mcptoolscript;

import java.io.Writer;
import java.nio.charset.StandardCharsets;

// A writer that keeps at most a fixed number of bytes and remembers that it dropped the rest.
//
// The astah api script executor collects output in an unbounded StringWriter, which is tolerable there because one
// run is one operation. An mcp tool script that drives tool functions in a loop can print on every iteration, so its output
// needs a ceiling; silently returning a truncated string would be worse than saying so, hence truncated().
final class BoundedWriter extends Writer {

    private final StringBuilder buffer = new StringBuilder();
    private final int maxBytes;
    private int bytesWritten;
    private boolean truncated;

    BoundedWriter(int maxBytes) {
        this.maxBytes = maxBytes;
    }

    @Override
    public void write(char[] chars, int offset, int length) {
        append(new String(chars, offset, length));
    }

    @Override
    public void write(String text) {
        append(text);
    }

    @Override
    public void write(String text, int offset, int length) {
        append(text.substring(offset, offset + length));
    }

    private synchronized void append(String text) {
        if (text == null || text.isEmpty() || bytesWritten >= maxBytes) {
            if (text != null && !text.isEmpty()) {
                truncated = true;
            }
            return;
        }

        int textBytes = text.getBytes(StandardCharsets.UTF_8).length;
        if (bytesWritten + textBytes <= maxBytes) {
            buffer.append(text);
            bytesWritten += textBytes;
            return;
        }

        // Append character by character up to the limit, so that a multi-byte character is never cut in half.
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int charBytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8).length;
            if (bytesWritten + charBytes > maxBytes) {
                break;
            }
            buffer.append(c);
            bytesWritten += charBytes;
        }
        truncated = true;
    }

    @Override
    public void flush() {
        // Nothing is buffered downstream
    }

    @Override
    public void close() {
        // Nothing to release
    }

    synchronized boolean truncated() {
        return truncated;
    }

    @Override
    public synchronized String toString() {
        return buffer.toString();
    }
}
