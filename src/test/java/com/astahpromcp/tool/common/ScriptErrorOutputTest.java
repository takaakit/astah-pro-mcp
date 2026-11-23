package com.astahpromcp.tool.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptErrorOutputTest {
    
    @Test
    void of_ok_keepsTheNotesWhenTheScriptFilledItsErrorOutput() {
        BoundedWriter output = new BoundedWriter(8);
        BoundedWriter errorOutput = new BoundedWriter(8);
        output.write("0123456789");
        errorOutput.write("0123456789");
        
        String text = ScriptErrorOutput.of(output, errorOutput, 8, "test script", List.of("A transaction was aborted."));
        
        assertEquals("01234567\n"
                + "A transaction was aborted.\n"
                + "Output was truncated at 8 bytes; print less from the test script.\n"
                + "Error output was truncated at 8 bytes; write less to it from the test script.\n", text);
    }
    
    @Test
    void of_ok_leavesTheErrorOutputAsItIsWhenThereIsNothingToSay() {
        BoundedWriter errorOutput = new BoundedWriter(64);
        errorOutput.write("no line break at the end");
        
        String text = ScriptErrorOutput.of(new BoundedWriter(64), errorOutput, 64, "test script", List.of());
        
        assertEquals("no line break at the end", text);
    }
    
    @Test
    void of_ok_startsTheNotesOnALineOfTheirOwn() {
        BoundedWriter errorOutput = new BoundedWriter(64);
        errorOutput.write("no line break at the end");
        
        String text = ScriptErrorOutput.of(new BoundedWriter(64), errorOutput, 64, "test script", List.of("A note."));
        
        assertEquals("no line break at the end\nA note.\n", text);
    }
}
