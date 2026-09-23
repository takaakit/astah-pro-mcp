package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

        KnowledgeToolSupport.FetchResult result = KnowledgeToolSupport.fetchAndParse(httpClient, "http://example.com/guide").join();

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any());
        assertTrue(requestCaptor.getValue().timeout().isPresent(), "Request timeout should be configured");
        assertTrue(requestCaptor.getValue().timeout().get().compareTo(Duration.ZERO) > 0,
                "Request timeout should be a positive duration");

        assertFalse(result.isError(), "A page that was fetched is not a failure");
        assertEquals("guide text", result.text().trim(), "The page body should be converted to Markdown");
    }

    @Test
    void fetchAndParse_ng_reportsFailureWithoutTextOnTimeout() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        doThrow(new HttpTimeoutException("request timed out")).when(httpClient).send(any(), any());

        KnowledgeToolSupport.FetchResult result = KnowledgeToolSupport.fetchAndParse(httpClient, "http://example.com/guide").join();

        assertTrue(result.isError(), "A timed out page is a failure, not content");
        assertNull(result.text(), "A failed page carries no text that could be cached as the document");
        assertTrue(result.error().contains("HTTP timeout"), "The failure should tell the reason");
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
    void chunkAndCache_ok_keepsEveryChunkWithinTheResponseBudget() {
        KnowledgeToolSupport.ContentCache contentCache = new KnowledgeToolSupport.ContentCache();

        DocumentDTO result = KnowledgeToolSupport.chunkAndCache("a".repeat(300000), contentCache);

        assertTrue(result.totalChunks() > 1, "the document has to be large enough to split");
        for (int i = 0; i < result.totalChunks(); i++) {
            int length = contentCache.chunkAt(i).length();
            assertTrue(length <= KnowledgeToolSupport.MAX_CHUNK_CHARS,
                    "chunk " + i + " is " + length + " characters, over the " + KnowledgeToolSupport.MAX_CHUNK_CHARS + " budget");
        }
    }

    @Test
    void splitTextWithOverlap_ok_capsTheOverlapPrefixToItsTail() {
        // No line breaks, so the last 3 lines of the first chunk are the whole first chunk
        String text = "a".repeat(4000) + "b".repeat(4000);

        List<String> chunks = KnowledgeToolSupport.splitTextWithOverlap(text, 4000, 3);

        assertEquals(2, chunks.size());
        assertEquals(KnowledgeToolSupport.CHUNK_OVERLAP_MAX_CHARS + 4000, chunks.get(1).length(),
                "The overlap prefix should be capped");
        assertTrue(text.contains(chunks.get(1)),
                "The capped prefix should be the tail of the previous chunk, so the chunk stays a continuous part of the document");
    }

    @Test
    void chunkAndCache_ok_splitsTextAndReplacesContentCache() {
        KnowledgeToolSupport.ContentCache contentCache = new KnowledgeToolSupport.ContentCache();
        KnowledgeToolSupport.chunkAndCache("stale chunk", contentCache);
        String text = "a".repeat(60000); // larger than the default chunk size, so it splits into two chunks

        DocumentDTO result = KnowledgeToolSupport.chunkAndCache(text, contentCache);

        DocumentDTO cached = contentCache.describe();
        assertEquals(2, cached.totalChunks(), "Stale content should be replaced by the new chunks");
        assertEquals(cached.totalChunks(), result.totalChunks());
        assertEquals(cached.firstChunk(), result.firstChunk());
    }

    @Test
    void chunkAndCache_ok_storesASingleEmptyChunkForEmptyText() {
        KnowledgeToolSupport.ContentCache contentCache = new KnowledgeToolSupport.ContentCache();

        DocumentDTO result = KnowledgeToolSupport.chunkAndCache("", contentCache);

        assertEquals(1, result.totalChunks());
        assertEquals("", result.firstChunk());
        assertEquals("", contentCache.chunkAt(0));
    }

    @Test
    void contentCache_ok_describesNothingBeforeAnythingIsLoaded() {
        KnowledgeToolSupport.ContentCache contentCache = new KnowledgeToolSupport.ContentCache();

        assertFalse(contentCache.isLoaded());
        assertNull(contentCache.describe());
        assertThrows(IllegalArgumentException.class, () -> contentCache.chunkAt(0));
    }

    @Test
    void contentCache_ng_rejectsAChunkIndexOutsideTheDocument() {
        KnowledgeToolSupport.ContentCache contentCache = new KnowledgeToolSupport.ContentCache();
        KnowledgeToolSupport.chunkAndCache("only one chunk", contentCache);

        assertThrows(IllegalArgumentException.class, () -> contentCache.chunkAt(-1));
        assertThrows(IllegalArgumentException.class, () -> contentCache.chunkAt(1));
    }

    // The two profiles share one provider instance and these tools take no Astah lock, so a reload has to be
    // visible as a whole. Reading while another thread reloads must never see an empty or half-filled cache.
    @Test
    void contentCache_ok_staysReadableWhileAnotherThreadReloadsIt() throws Exception {
        KnowledgeToolSupport.ContentCache contentCache = new KnowledgeToolSupport.ContentCache();
        KnowledgeToolSupport.chunkAndCache("a".repeat(60000), contentCache);

        AtomicBoolean running = new AtomicBoolean(true);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread reader = new Thread(() -> {
            while (running.get()) {
                try {
                    // Each call reads one snapshot. Two calls may land on different reloads, so what has to hold is that neither ever sees the cache empty or half-filled, which is what clearing and refilling a shared list would expose.
                    DocumentDTO seen = contentCache.describe();
                    assertNotNull(seen, "the cache must never be observed empty once it has been loaded");
                    assertFalse(seen.firstChunk().isEmpty(), "a chunk must never be observed half-written");
                    assertEquals(1, seen.firstChunk().chars().distinct().count(),
                            "the first chunk must come from a single reload, not a mix of two");
                    assertFalse(contentCache.chunkAt(0).isEmpty());
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                    return;
                }
            }
        });
        reader.start();

        for (int i = 0; i < 200; i++) {
            KnowledgeToolSupport.chunkAndCache("b".repeat(60000), contentCache);
            KnowledgeToolSupport.chunkAndCache("c".repeat(120000), contentCache);
        }
        running.set(false);
        reader.join(10_000);

        assertNull(failure.get(), () -> "reader observed a broken cache: " + failure.get());
    }

    @Test
    void fetchAndParse_ng_reportsFailureWithoutTextOnHttpErrorStatus() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(403).when(response).statusCode();
        doReturn(response).when(httpClient).send(any(), any());

        KnowledgeToolSupport.FetchResult result = KnowledgeToolSupport.fetchAndParse(httpClient, "http://example.com/guide").join();

        assertTrue(result.isError(), "A rejected page is a failure, not content");
        assertNull(result.text(), "A failed page carries no text that could be cached as the document");
        assertTrue(result.error().contains("403"), "The failure should tell the status code");
    }

    @Test
    void fetchAllOrFail_ok_joinsEveryPage() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(200).when(response).statusCode();
        doReturn("<html><body>page text</body></html>").when(response).body();
        doReturn(response).when(httpClient).send(any(), any());

        String document = KnowledgeToolSupport.fetchAllOrFail(httpClient,
                List.of("http://example.com/a", "http://example.com/b"), "test guide");

        assertEquals(2, document.split("page text", -1).length - 1, "Every page should be part of the document");
    }

    @Test
    void fetchAllOrFail_ng_failsWithoutTextWhenOnePageFails() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> ok = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(200).when(ok).statusCode();
        doReturn("<html><body>page text</body></html>").when(ok).body();
        @SuppressWarnings("unchecked")
        HttpResponse<String> unavailable = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(503).when(unavailable).statusCode();
        // Answered by URL: the pages are fetched in parallel, so the order the stub is called in is not fixed.
        doAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            return request.uri().toString().endsWith("/b") ? unavailable : ok;
        }).when(httpClient).send(any(), any());

        IOException failure = assertThrows(IOException.class, () -> KnowledgeToolSupport.fetchAllOrFail(httpClient,
                List.of("http://example.com/a", "http://example.com/b"), "test guide"));

        assertTrue(failure.getMessage().contains("503"), "The failure should tell why the page could not be fetched");
        assertTrue(failure.getMessage().contains("http://example.com/b"), "The failure should name the failed page");
    }

    @Test
    void fetchAllOrFail_ng_failsWhenThereIsNoUrl() {
        HttpClient httpClient = mock(HttpClient.class);

        assertThrows(IOException.class, () -> KnowledgeToolSupport.fetchAllOrFail(httpClient, List.of(" "), "test guide"),
                "An empty URL list must not be loaded as an empty document");
    }
}
