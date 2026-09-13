package com.astahpromcp.tool.common;

import org.openjdk.nashorn.api.scripting.NashornException;

// Reads the line of a script a failure happened on
public final class ScriptLine {

    // The 1-based line the failure happened on, or -1 when the throwable never passed through the named source.
    public static int of(Throwable error, String sourceName) {
        if (error == null) {
            return -1;
        }

        for (StackTraceElement frame : NashornException.getScriptFrames(error)) {
            if (sourceName.equals(frame.getFileName())) {
                return frame.getLineNumber();
            }
        }

        return -1;
    }

    private ScriptLine() {
    }
}
