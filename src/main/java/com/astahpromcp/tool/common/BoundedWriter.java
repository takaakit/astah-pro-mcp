package com.astahpromcp.tool.common;

import java.io.Writer;

// A writer that keeps at most a fixed number of bytes and remembers that it dropped the rest.
//
// What a script prints stays in Astah's own heap until the run ends, so a script that prints in a loop -- an mcp tool
// script driving tool functions, or any script whose loop went wrong -- would exhaust it without a ceiling.
public final class BoundedWriter extends Writer {
    
    private final StringBuilder buffer = new StringBuilder();
    private final int maxBytes;
    private int bytesWritten;
    private boolean truncated;
    
    public BoundedWriter(int maxBytes) {
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
    
    // Appends the text up to the first character that does not fit, which is dropped with the rest of the text.
    // Everything written after that is dropped as well: the room the character left behind could still take a shorter one from a later write, which would then read as if it had come right after the text in front of the gap.
    private synchronized void append(String text) {
        if (text == null || truncated) {
            return;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!appendChar(text.charAt(i))) {
                truncated = true;
                return;
            }
        }
    }
    
    // Appends one UTF-16 char if the bytes it adds fit within the limit, and answers whether it did.
    private boolean appendChar(char c) {
        boolean completesPair = Character.isLowSurrogate(c)
            && !buffer.isEmpty()
            && Character.isHighSurrogate(buffer.charAt(buffer.length() - 1));
        int charBytes = completesPair ? 3 : utf8Length(c);
        
        if (bytesWritten + charBytes > maxBytes) {
            if (completesPair) {
                buffer.setLength(buffer.length() - 1);
                bytesWritten -= 1;
            }
            return false;
        }
        
        buffer.append(c);
        bytesWritten += charBytes;
        return true;
    }
    
    // The UTF-8 length of a char standing alone. A surrogate alone is not a character and encodes to the replacement '?'.
    private static int utf8Length(char c) {
        if (c < 0x80 || Character.isSurrogate(c)) {
            return 1;
        } else if (c < 0x800) {
            return 2;
        } else {
            return 3;
        }
    }
    
    @Override
    public void flush() {
        // Nothing is buffered downstream
    }
    
    @Override
    public void close() {
        // Nothing to release
    }
    
    public synchronized boolean truncated() {
        return truncated;
    }
    
    @Override
    public synchronized String toString() {
        return buffer.toString();
    }
}
