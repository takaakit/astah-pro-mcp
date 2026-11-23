package com.astahpromcp.tool.common;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class BoundedWriterTest {
    
    // U+1F600, a character outside the BMP: two UTF-16 chars, four UTF-8 bytes
    private static final String EMOJI = "😀";
    
    @Test
    void write_ok_keepsEverythingWithinTheLimit() {
        BoundedWriter writer = new BoundedWriter(16);
        
        writer.write("abc");
        writer.write("あい");
        writer.write(EMOJI);
        
        assertEquals("abcあい" + EMOJI, writer.toString());
        assertEquals(13, utf8Length(writer.toString()));
        assertFalse(writer.truncated());
    }
    
    @Test
    void write_ok_truncatesMultiByteCharactersWithinTheLimit() {
        BoundedWriter writer = new BoundedWriter(10);
        
        writer.write("あいうえお");
        
        // A fourth character would make 12 bytes
        assertEquals("あいう", writer.toString());
        assertTrue(writer.truncated());
    }
    
    // A surrogate counted on its own makes 2 bytes of a pair that encodes to 4, which let the output grow to twice the limit
    @Test
    void write_ok_countsCharactersOutsideTheBmpAsTheirFourBytes() {
        BoundedWriter writer = new BoundedWriter(65_536);
        
        writer.write(EMOJI.repeat(32_768));
        
        assertEquals(65_536, utf8Length(writer.toString()));
        assertEquals(EMOJI.repeat(16_384), writer.toString());
        assertTrue(writer.truncated());
    }
    
    @Test
    void write_ok_neverKeepsHalfOfASurrogatePairAtTheLimit() {
        BoundedWriter writer = new BoundedWriter(5);
        
        writer.write("a" + EMOJI + EMOJI);
        
        assertEquals("a" + EMOJI, writer.toString());
        assertTrue(writer.truncated());
        
        BoundedWriter tooSmallForOne = new BoundedWriter(1);
        tooSmallForOne.write(EMOJI);
        
        assertEquals("", tooSmallForOne.toString());
        assertTrue(tooSmallForOne.truncated());
    }
    
    // A PrintWriter passes through whatever it is given, so a pair may arrive split between two writes
    @Test
    void write_ok_countsASurrogatePairSplitBetweenTwoWrites() {
        BoundedWriter writer = new BoundedWriter(4);
        
        writer.write(EMOJI.substring(0, 1));
        writer.write(EMOJI.substring(1));
        
        assertEquals(EMOJI, writer.toString());
        assertEquals(4, utf8Length(writer.toString()));
        assertFalse(writer.truncated());
        
        writer.write("a");
        
        assertEquals(EMOJI, writer.toString());
        assertTrue(writer.truncated());
    }
    
    @Test
    void write_ok_takesBackTheHighSurrogateWhenItsLowSurrogateComesInALaterWriteThatDoesNotFit() {
        BoundedWriter writer = new BoundedWriter(2);
        
        writer.write("a" + EMOJI.substring(0, 1));
        writer.write(EMOJI.substring(1));
        
        assertEquals("a", writer.toString());
        assertTrue(writer.truncated());
    }
    
    // A lone surrogate is not a character: it encodes to the 1-byte replacement '?', and that is what it counts as
    @Test
    void write_ok_countsALoneSurrogateAsWhatItEncodesTo() {
        BoundedWriter writer = new BoundedWriter(3);
        
        writer.write(EMOJI.substring(1) + "ab");
        
        assertEquals(3, utf8Length(writer.toString()));
        assertFalse(writer.truncated());
    }
    
    @Test
    void write_ok_dropsEverythingOnceTheLimitIsReached() {
        BoundedWriter writer = new BoundedWriter(3);
        
        writer.write("abc");
        writer.write("d");
        
        assertEquals("abc", writer.toString());
        assertTrue(writer.truncated());
    }
    
    // A character that does not fit can leave room for a shorter one, which must not slip in after the gap
    @Test
    void write_ok_dropsALaterWriteThatWouldFitTheRoomLeftBehind() {
        BoundedWriter writer = new BoundedWriter(4);
        
        writer.write("ab");
        writer.write("界");
        writer.write("c");
        
        assertEquals("ab", writer.toString());
        assertTrue(writer.truncated());
        
        // Taking back half of a pair frees a byte, which leaves room for three
        BoundedWriter pairWriter = new BoundedWriter(4);
        
        pairWriter.write("a" + EMOJI.substring(0, 1));
        pairWriter.write(EMOJI.substring(1));
        pairWriter.write("xyz");
        
        assertEquals("a", pairWriter.toString());
        assertTrue(pairWriter.truncated());
    }
    
    @Test
    void write_ok_writingNothingIsNotATruncation() {
        BoundedWriter writer = new BoundedWriter(0);
        
        writer.write("");
        
        assertEquals("", writer.toString());
        assertFalse(writer.truncated());
    }
    
    private static int utf8Length(String text) {
        return text.getBytes(StandardCharsets.UTF_8).length;
    }
}
