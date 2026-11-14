package com.astahpromcp.tool.knowledge;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PdfConversionTest {
    
    @TempDir
    Path directory;
    
    @Test
    void convertPdfToMarkdown_ok_preservesPageOrderAndRequestedFileName() throws Exception {
        Path pdf = directory.resolve("input.pdf");
        Files.write(pdf, documentBytes());
        
        String markdown = KnowledgeToolSupport.convertPdfToMarkdown(pdf, directory, "guide");
        
        assertContent(markdown);
        assertEquals(markdown, Files.readString(directory.resolve("guide.md")));
        assertFalse(Files.exists(directory.resolve("input.md")));
        assertTrue(Files.exists(pdf), "The file overload must preserve the caller's PDF");
    }
    
    @Test
    void convertPdfToMarkdown_ok_convertsBytesAndRemovesTemporaryPdf() throws Exception {
        Path output = directory.resolve("nested");
        
        String markdown = KnowledgeToolSupport.convertPdfToMarkdown(documentBytes(), output, "guide");
        
        assertContent(markdown);
        assertEquals(markdown, Files.readString(output.resolve("guide.md")));
        try (Stream<Path> files = Files.list(output)) {
            assertEquals(List.of("guide.md"), files.map(file -> file.getFileName().toString()).toList(),
                    "Only the Markdown may be left: the temporary PDF and the file the converter named after it must be gone");
        }
    }
    
    @Test
    void convertPdfToMarkdown_ok_preservesOclOperators() throws Exception {
        String ocl = "self.employees->size() <> 0 and a <= b & c";
        
        String markdown = KnowledgeToolSupport.convertPdfToMarkdown(documentBytes(ocl), directory, "ocl");
        
        assertTrue(markdown.contains(ocl), () -> "The OCL expression was altered: " + markdown);
        assertFalse(Stream.of("&lt;", "&gt;", "&amp;").anyMatch(markdown::contains), () -> "HTML entities left in: " + markdown);
        assertEquals(markdown, Files.readString(directory.resolve("ocl.md")));
    }
    
    @Test
    void convertPdfToMarkdown_ok_convertsOnlyTheSelectedPages() throws Exception {
        byte[] pdf = documentBytes("Alpha page content", "Bravo page content", "Charlie page content");
        
        String markdown = KnowledgeToolSupport.convertPdfToMarkdown(pdf, directory, "excerpt", "1-1,3-3");
        
        int alpha = markdown.indexOf("Alpha page content");
        int charlie = markdown.indexOf("Charlie page content");
        assertTrue(alpha >= 0, () -> "Missing the first selected page: " + markdown);
        assertTrue(charlie > alpha, () -> "Missing or reordered the second selected page: " + markdown);
        assertFalse(markdown.contains("Bravo page content"), () -> "A page that was not selected was converted: " + markdown);
        assertEquals(markdown, Files.readString(directory.resolve("excerpt.md")));
    }
    
    @Test
    void convertPdfToMarkdown_ng_rejectsPagesBeyondTheLastPage() throws Exception {
        IOException e = assertThrows(IOException.class,
                () -> KnowledgeToolSupport.convertPdfToMarkdown(documentBytes(), directory, "excerpt", "3-4"));
        
        assertTrue(e.getMessage().contains("3-4"), e.getMessage());
    }
    
    private static void assertContent(String markdown) {
        int first = markdown.indexOf("First page content");
        int second = markdown.indexOf("Second page content");
        assertTrue(first >= 0, () -> "Missing first page: " + markdown);
        assertTrue(second > first, () -> "Missing or reordered second page: " + markdown);
    }
    
    private static byte[] documentBytes() throws Exception {
        return documentBytes("First page content", "Second page content");
    }
    
    // One page per text
    static byte[] documentBytes(String... pageTexts) throws Exception {
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(72, 700);
                    content.showText(text);
                    content.endText();
                }
            }
            document.save(bytes);
            return bytes.toByteArray();
        }
    }
}
