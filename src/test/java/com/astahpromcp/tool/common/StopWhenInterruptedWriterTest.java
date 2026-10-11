package com.astahpromcp.tool.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class StopWhenInterruptedWriterTest {

    @AfterEach
    void tearDown() {
        // A test that interrupts its own thread must not leave the flag set for the next test that runs on it
        Thread.interrupted();
    }

    @Test
    void write_ok_passesWhatIsPrintedThroughWhileTheThreadIsNotInterrupted() {
        StringWriter target = new StringWriter();
        PrintWriter printer = new PrintWriter(new StopWhenInterruptedWriter(target), true);

        printer.print("hello");

        assertEquals("hello", target.toString());
    }

    // Scripts print through a PrintWriter, which swallows an IOException: only an unchecked exception gets through it to stop the script.
    @Test
    void write_ng_stopsThroughAPrintWriterOnceTheThreadIsInterrupted() {
        StringWriter target = new StringWriter();
        PrintWriter printer = new PrintWriter(new StopWhenInterruptedWriter(target), true);

        Thread.currentThread().interrupt();

        assertThrows(IllegalStateException.class, () -> printer.print("lost"));
        assertEquals("", target.toString(), "Nothing may be written once the run is abandoned");
        assertTrue(Thread.currentThread().isInterrupted(), "The flag must stay set, so that every later print stops the script too");
    }
}
