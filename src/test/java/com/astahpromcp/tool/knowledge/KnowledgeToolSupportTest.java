package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KnowledgeToolSupportTest {

    @Test
    void newHttpClient_ok_hasConnectTimeout() {
        HttpClient httpClient = KnowledgeToolSupport.newHttpClient();

        assertTrue(httpClient.connectTimeout().isPresent(), "Connect timeout should be configured");
        assertTrue(httpClient.connectTimeout().get().compareTo(Duration.ZERO) > 0,
                "Connect timeout should be a positive duration");
    }

    @Test
    void fetchAndParse_ok_setsRequestTimeout() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(200).when(response).statusCode();
        doReturn("<html><body>guide text</body></html>").when(response).body();
        doReturn(response).when(httpClient).send(any(), any());

        String content = KnowledgeToolSupport.fetchAndParse(httpClient, "http://example.com/guide").join();

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any());
        assertTrue(requestCaptor.getValue().timeout().isPresent(), "Request timeout should be configured");
        assertTrue(requestCaptor.getValue().timeout().get().compareTo(Duration.ZERO) > 0,
                "Request timeout should be a positive duration");

        assertEquals("guide text", content.trim(), "The page body should be converted to Markdown");
    }

    @Test
    void fetchAndParse_ng_returnsErrorPlaceholderOnTimeout() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        doThrow(new HttpTimeoutException("request timed out")).when(httpClient).send(any(), any());

        String content = KnowledgeToolSupport.fetchAndParse(httpClient, "http://example.com/guide").join();

        assertTrue(content.contains("HTTP timeout"), "The placeholder should tell the reason");
    }

    @Test
    void convertHtmlToMarkdown_ok_keepsMainContentAsMarkdown() {
        String html = "<html><body><nav><a href='/'>menu</a></nav>"
                + "<main><h2>Title</h2><p>Some <b>bold</b> text.</p>"
                + "<textarea>@startuml\nA -> B\n@enduml</textarea></main>"
                + "<footer>copyright</footer></body></html>";

        String markdown = KnowledgeToolSupport.convertHtmlToMarkdown(html);

        assertTrue(markdown.contains("## Title"), "Headings should be converted to Markdown");
        assertTrue(markdown.contains("**bold**"), "Emphasis should be converted to Markdown");
        assertTrue(markdown.contains("@startuml"), "Textarea content should be kept as a code block");
        assertFalse(markdown.contains("menu"), "Navigation should be removed");
        assertFalse(markdown.contains("copyright"), "Footer should be removed");
    }

    @Test
    void splitTextWithOverlap_ok_returnsSingleChunkUnchangedWhenTextFitsInOneChunk() {
        String text = "line1\nline2\nline3";

        List<String> chunks = KnowledgeToolSupport.splitTextWithOverlap(text, 100, 3);

        assertEquals(List.of(text), chunks);
    }

    @Test
    void splitTextWithOverlap_ok_prefixesEachChunkWithLastLinesOfPreviousChunk() {
        // 10 lines of 10 characters each (including the newline); chunk size 30 -> base chunks of exactly 3 lines
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(String.format("L%d_xxxxxx", i)).append('\n');
        }
        String text = sb.toString();

        List<String> chunks = KnowledgeToolSupport.splitTextWithOverlap(text, 30, 3);
        List<String> baseChunks = KnowledgeToolSupport.splitText(text, 30);

        assertEquals(baseChunks.size(), chunks.size(), "Overlap should not change the number of chunks");
        assertEquals(baseChunks.get(0), chunks.get(0), "The first chunk should have no overlap prefix");
        for (int i = 1; i < chunks.size(); i++) {
            // Each base chunk is exactly 3 lines, so the overlap is the whole previous base chunk
            assertEquals(baseChunks.get(i - 1) + baseChunks.get(i), chunks.get(i),
                    "Each chunk should start with the last 3 lines of the previous chunk");
        }
    }

    @Test
    void splitTextWithOverlap_ok_keepsTextContinuousAcrossMidLineSplit() {
        // Chunk size 25 cuts the 10-character lines mid-line
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(String.format("L%d_xxxxxx", i)).append('\n');
        }
        String text = sb.toString();

        List<String> chunks = KnowledgeToolSupport.splitTextWithOverlap(text, 25, 3);
        List<String> baseChunks = KnowledgeToolSupport.splitText(text, 25);

        for (int i = 1; i < chunks.size(); i++) {
            // The overlap ends exactly where the current base chunk begins, so each chunk
            // must appear verbatim in the original document even when a line was cut mid-way
            assertTrue(text.contains(chunks.get(i)), "Chunk " + i + " should be a continuous part of the document");
            assertTrue(chunks.get(i).endsWith(baseChunks.get(i)), "Chunk " + i + " should end with its own content");
        }
    }

    @Test
    void splitTextWithOverlap_ok_usesWholePreviousChunkWhenItHasFewerLinesThanOverlap() {
        String text = "0123456789abcdefghij"; // no newlines, chunk size 10 -> 2 chunks

        List<String> chunks = KnowledgeToolSupport.splitTextWithOverlap(text, 10, 3);

        assertEquals(2, chunks.size());
        assertEquals("0123456789", chunks.get(0));
        assertEquals("0123456789abcdefghij", chunks.get(1));
    }

    @Test
    void splitTextWithOverlap_ok_doesNotCountTrailingNewlineAsALine() {
        String text = "aa\nbb\ncc\ndd\n" // 12 characters -> the first chunk ends exactly at a line break
                + "ee\nff\n";

        List<String> chunks = KnowledgeToolSupport.splitTextWithOverlap(text, 12, 3);

        assertEquals(2, chunks.size());
        assertEquals("aa\nbb\ncc\ndd\n", chunks.get(0));
        assertEquals("bb\ncc\ndd\nee\nff\n", chunks.get(1));
    }

    @Test
    void chunkAndCache_ok_splitsTextAndReplacesContentCache() {
        List<String> contentCache = new ArrayList<>(List.of("stale chunk"));
        String text = "a".repeat(60000); // larger than the default 50KB chunk size, so it splits into two chunks

        DocumentDTO result = KnowledgeToolSupport.chunkAndCache(text, contentCache);

        assertEquals(2, contentCache.size(), "Stale content should be replaced by the new chunks");
        assertEquals(contentCache.size(), result.totalChunks());
        assertEquals(contentCache.get(0), result.firstChunk());
    }

    @Test
    void chunkAndCache_ok_storesASingleEmptyChunkForEmptyText() {
        List<String> contentCache = new ArrayList<>();

        DocumentDTO result = KnowledgeToolSupport.chunkAndCache("", contentCache);

        assertEquals(List.of(""), contentCache);
        assertEquals(1, result.totalChunks());
        assertEquals("", result.firstChunk());
    }

    @Test
    void fetchAndParse_ng_returnsErrorPlaceholderOnHttpErrorStatus() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(403).when(response).statusCode();
        doReturn(response).when(httpClient).send(any(), any());

        String content = KnowledgeToolSupport.fetchAndParse(httpClient, "http://example.com/guide").join();

        assertTrue(content.contains("403"), "The placeholder should tell the status code");
    }
}
