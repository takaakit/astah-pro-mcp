package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.knowledge.document.DocumentSource;
import com.astahpromcp.tool.knowledge.document.DocumentType;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

// A reference document read from a PDF is fetched and converted once per build, and once at a time however many calls ask for it
public class DocumentLoadingTest {
    
    private static final Map<String, Object> OCL_SPEC = Map.of("documentType", "ocl_spec");
    
    private static final String PAGE_TEXT = "Object Constraint Language page";
    
    private static final long WAIT_SECONDS = 30;
    
    @TempDir
    Path directory;
    
    // Counts its fetches and holds each one until released, so that a test can call the tool while a document is loading
    private static final class PdfSource implements DocumentTool.PdfFetcher {
        
        private final AtomicInteger fetches = new AtomicInteger();
        private final CountDownLatch fetching = new CountDownLatch(1);
        private final CountDownLatch released;
        private volatile IOException failure;
        
        private PdfSource(boolean held) {
            released = new CountDownLatch(held ? 1 : 0);
        }
        
        @Override
        public byte[] fetch(String url) throws IOException {
            fetches.incrementAndGet();
            fetching.countDown();
            try {
                if (!released.await(WAIT_SECONDS, TimeUnit.SECONDS)) {
                    throw new IOException("The test never released the fetch");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
            
            if (failure != null) {
                throw failure;
            }
            try {
                return PdfConversionTest.documentBytes(PAGE_TEXT);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        
        private void release() {
            released.countDown();
        }
    }
    
    private DocumentTool tool(String session, ConvertedDocumentCache cache, PdfSource pdfSource) throws IOException {
        return new DocumentTool(Files.createDirectories(directory.resolve(session)), mock(HttpClient.class), cache, pdfSource);
    }
    
    private ConvertedDocumentCache cacheOf(String buildId) {
        List<DocumentSource.Pdf> documents = Arrays.stream(DocumentType.values())
                .map(DocumentType::source)
                .filter(DocumentSource.Pdf.class::isInstance)
                .map(DocumentSource.Pdf.class::cast)
                .toList();
        return ConvertedDocumentCache.open(directory.resolve("cache"), Optional.of(buildId), documents, pid -> false);
    }
    
    private static McpSchema.CallToolResult callInfoTool(DocumentTool tool) {
        ToolDefinition definition = tool.createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals("get_info_of_document"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("this provider registers no tool function named get_info_of_document"));
        
        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest("get_info_of_document", OCL_SPEC, null));
    }
    
    private static boolean failed(McpSchema.CallToolResult result) {
        return Boolean.TRUE.equals(result.isError());
    }
    
    // Calls the tool on a thread of its own
    private static Thread callInfoToolOnAnotherThread(DocumentTool tool, AtomicReference<McpSchema.CallToolResult> result) {
        Thread thread = new Thread(() -> result.set(callInfoTool(tool)));
        thread.start();
        return thread;
    }
    
    // Waits until the call on the given thread is either waiting for the load under way or fetching on its own, so that the load is released only once the call can no longer find the document already loaded
    private static void awaitWaitingOrFetching(Thread call) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(WAIT_SECONDS);
        while (System.nanoTime() < deadline) {
            for (StackTraceElement frame : call.getStackTrace()) {
                boolean waiting = frame.getClassName().equals(DocumentTool.class.getName()) && frame.getMethodName().equals("await");
                boolean fetching = frame.getClassName().equals(PdfSource.class.getName()) && frame.getMethodName().equals("fetch");
                if (waiting || fetching) {
                    return;
                }
            }
            Thread.sleep(10);
        }
        fail("The second call neither waited for the load under way nor fetched the document");
    }
    
    @Test
    void getInfo_ok_readsTheDocumentFromTheCacheInALaterSession() throws Exception {
        PdfSource pdfSource = new PdfSource(false);
        
        McpSchema.CallToolResult first = callInfoTool(tool("session-1", cacheOf("0.3.6+1"), pdfSource));
        McpSchema.CallToolResult later = callInfoTool(tool("session-2", cacheOf("0.3.6+1"), pdfSource));
        
        assertFalse(failed(first), () -> String.valueOf(first.content()));
        assertFalse(failed(later), () -> String.valueOf(later.content()));
        assertEquals(1, pdfSource.fetches.get(), "A later session of the same build must read the converted document from the cache");
        assertEquals(first.structuredContent(), later.structuredContent());
        assertTrue(String.valueOf(later.structuredContent()).contains(PAGE_TEXT), () -> String.valueOf(later.structuredContent()));
    }
    
    @Test
    void getInfo_ok_convertsAgainInANewBuild() throws Exception {
        PdfSource pdfSource = new PdfSource(false);
        
        callInfoTool(tool("session-1", cacheOf("0.3.6+1"), pdfSource));
        McpSchema.CallToolResult newBuild = callInfoTool(tool("session-2", cacheOf("0.3.6+2"), pdfSource));
        
        assertFalse(failed(newBuild), () -> String.valueOf(newBuild.content()));
        assertEquals(2, pdfSource.fetches.get(), "A new build must not read what an old build converted");
    }
    
    @Test
    void getInfo_ok_aCallThatComesWhileTheDocumentLoadsWaitsForThatLoad() throws Exception {
        PdfSource pdfSource = new PdfSource(true);
        DocumentTool tool = tool("session", ConvertedDocumentCache.disabled(), pdfSource);
        AtomicReference<McpSchema.CallToolResult> first = new AtomicReference<>();
        AtomicReference<McpSchema.CallToolResult> second = new AtomicReference<>();
        
        Thread firstCall = callInfoToolOnAnotherThread(tool, first);
        assertTrue(pdfSource.fetching.await(WAIT_SECONDS, TimeUnit.SECONDS), "The first call never started to fetch");
        Thread secondCall = callInfoToolOnAnotherThread(tool, second);
        awaitWaitingOrFetching(secondCall);
        pdfSource.release();
        firstCall.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
        secondCall.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
        
        assertEquals(1, pdfSource.fetches.get(), "The second call must wait for the load under way, not fetch and convert the document again");
        assertFalse(failed(first.get()), () -> String.valueOf(first.get().content()));
        assertFalse(failed(second.get()), () -> String.valueOf(second.get().content()));
        assertEquals(first.get().structuredContent(), second.get().structuredContent());
    }
    
    @Test
    void getInfo_ng_aFailedLoadFailsTheWaitingCallTooAndTheNextCallLoadsAgain() throws Exception {
        PdfSource pdfSource = new PdfSource(true);
        pdfSource.failure = new IOException("omg.org could not be reached");
        DocumentTool tool = tool("session", ConvertedDocumentCache.disabled(), pdfSource);
        AtomicReference<McpSchema.CallToolResult> first = new AtomicReference<>();
        AtomicReference<McpSchema.CallToolResult> second = new AtomicReference<>();
        
        Thread firstCall = callInfoToolOnAnotherThread(tool, first);
        assertTrue(pdfSource.fetching.await(WAIT_SECONDS, TimeUnit.SECONDS), "The first call never started to fetch");
        Thread secondCall = callInfoToolOnAnotherThread(tool, second);
        awaitWaitingOrFetching(secondCall);
        pdfSource.release();
        firstCall.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
        secondCall.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
        
        assertEquals(1, pdfSource.fetches.get());
        assertTrue(failed(first.get()), () -> String.valueOf(first.get().content()));
        assertTrue(failed(second.get()), () -> String.valueOf(second.get().content()));
        assertTrue(String.valueOf(second.get().content()).contains("omg.org could not be reached"),
                () -> "The waiting call must report why the load failed: " + second.get().content());
        
        pdfSource.failure = null;
        McpSchema.CallToolResult next = callInfoTool(tool);
        
        assertFalse(failed(next), () -> String.valueOf(next.content()));
        assertEquals(2, pdfSource.fetches.get(), "A failed load must not be kept, so the next call fetches again");
    }
}
