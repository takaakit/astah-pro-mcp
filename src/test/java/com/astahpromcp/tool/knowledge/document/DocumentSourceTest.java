package com.astahpromcp.tool.knowledge.document;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentSourceTest {
    
    @Test
    void pageSelection_ok_isEmptyForTheWholeDocument() {
        DocumentSource.Pdf pdf = new DocumentSource.Pdf("https://example.com/document.pdf", "document");
        
        assertTrue(pdf.pages().isEmpty());
        assertEquals("", pdf.pageSelection());
    }
    
    @Test
    void pageSelection_ok_joinsTheRangesInTheFormOpenDataLoaderPdfReads() {
        DocumentSource.Pdf pdf = new DocumentSource.Pdf("https://example.com/document.pdf", "document",
                List.of(new DocumentSource.PageRange(145, 226), new DocumentSource.PageRange(307, 354)));
        
        assertEquals("145-226,307-354", pdf.pageSelection());
    }
    
    @Test
    void pageSelection_ok_writesASinglePageAsARange() {
        DocumentSource.Pdf pdf = new DocumentSource.Pdf("https://example.com/document.pdf", "document",
                List.of(new DocumentSource.PageRange(5, 5)));
        
        assertEquals("5-5", pdf.pageSelection());
    }
    
    @Test
    void pageRange_ng_rejectsARangeThatNamesNoPage() {
        assertThrows(IllegalArgumentException.class, () -> new DocumentSource.PageRange(0, 3));
        assertThrows(IllegalArgumentException.class, () -> new DocumentSource.PageRange(5, 4));
    }
}
