package com.astahpromcp.tool.common;

import java.util.ArrayList;
import java.util.List;

// The error output a script run returns: what the script wrote there, within its limit, followed by what the run has to say about itself.
// The notes go in after the limit has been applied, so that a script that used up its error output cannot crowd out the news that its output was cut, or what became of a transaction it left open.
public final class ScriptErrorOutput {
    
    private ScriptErrorOutput() {
    }
    
    // scriptName names the kind of script the notes speak of, such as "mcp tool script"
    public static String of(BoundedWriter output, BoundedWriter errorOutput, int maxBytes, String scriptName, List<String> notes) {
        List<String> allNotes = new ArrayList<>(notes);
        if (output.truncated()) {
            allNotes.add(String.format("Output was truncated at %d bytes; print less from the %s.", maxBytes, scriptName));
        }
        if (errorOutput.truncated()) {
            allNotes.add(String.format("Error output was truncated at %d bytes; write less to it from the %s.", maxBytes, scriptName));
        }
        
        StringBuilder text = new StringBuilder(errorOutput.toString());
        if (allNotes.isEmpty()) {
            return text.toString();
        }
        
        // A note starts a line of its own, even when the error output ends in the middle of one, as a truncated one does
        if (!text.isEmpty() && text.charAt(text.length() - 1) != '\n') {
            text.append('\n');
        }
        for (String note : allNotes) {
            text.append(note).append('\n');
        }
        return text.toString();
    }
}
