package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

// A document that could not be fetched must not be kept as a loaded document: a tool that fails once has to fetch again
// on the next call, so that a transient network failure does not outlive it.
public class RemoteDocumentCacheTest {

    private static final String INFO_TOOL = "get_info_of_puml_guide";

    // An HttpClient whose status code can be switched between calls, counting the requests it was asked to make.
    private static final class SwitchableHttpClient {

        private final AtomicInteger requests = new AtomicInteger();
        private final AtomicReference<HttpResponse<String>> response = new AtomicReference<>();
        private final HttpClient httpClient;

        @SuppressWarnings("unchecked")
        private SwitchableHttpClient(int initialStatusCode) throws Exception {
            response.set(responseWith(initialStatusCode));

            httpClient = mock(HttpClient.class);
            // The responses are stubbed up front: the pages are fetched in parallel, so nothing may be stubbed from within the call.
            doAnswer(invocation -> {
                requests.incrementAndGet();
                return response.get();
            }).when(httpClient).send(any(), any());
        }

        @SuppressWarnings("unchecked")
        private static HttpResponse<String> responseWith(int statusCode) {
            HttpResponse<String> response = (HttpResponse<String>) mock(HttpResponse.class);
            doReturn(statusCode).when(response).statusCode();
            doReturn("<html><body>page text</body></html>").when(response).body();
            return response;
        }

        private void switchTo(int statusCode) {
            response.set(responseWith(statusCode));
        }
    }

    private static McpSchema.CallToolResult callInfoTool(PlantumlGuideTool tool) {
        ToolDefinition definition = tool.createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals(INFO_TOOL))
                .findFirst()
                .orElseThrow(() -> new AssertionError("this provider registers no tool function named " + INFO_TOOL));

        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest(INFO_TOOL, Map.of(), null));
    }

    private static List<Path> filesIn(Path directory) throws Exception {
        try (var entries = java.nio.file.Files.list(directory)) {
            return entries.toList();
        }
    }

    @Test
    void getInfo_ng_fetchesAgainAfterEveryPageFailed(@TempDir Path outputDirectory) throws Exception {
        SwitchableHttpClient client = new SwitchableHttpClient(503);
        PlantumlGuideTool tool = new PlantumlGuideTool(outputDirectory, client.httpClient);

        McpSchema.CallToolResult firstResult = callInfoTool(tool);
        int requestsOfFirstCall = client.requests.get();

        assertTrue(Boolean.TRUE.equals(firstResult.isError()),
                "A document whose pages all failed must be reported as a failure");
        assertTrue(requestsOfFirstCall > 0, "The first call should have tried to fetch the pages");
        assertEquals(List.of(), filesIn(outputDirectory),
                "A document that could not be fetched must not be written to the workspace");

        client.switchTo(200);
        McpSchema.CallToolResult secondResult = callInfoTool(tool);

        assertEquals(requestsOfFirstCall * 2, client.requests.get(),
                "The failed document must be fetched again, not served from a cache");
        assertFalse(Boolean.TRUE.equals(secondResult.isError()),
                "Once the pages are reachable again the document should load: " + secondResult.content());
        assertEquals(1, filesIn(outputDirectory).size(), "The loaded document should be written to the workspace");
    }

    @Test
    void getInfo_ok_doesNotFetchAgainOnceLoaded(@TempDir Path outputDirectory) throws Exception {
        SwitchableHttpClient client = new SwitchableHttpClient(200);
        PlantumlGuideTool tool = new PlantumlGuideTool(outputDirectory, client.httpClient);

        McpSchema.CallToolResult firstResult = callInfoTool(tool);
        int requestsOfFirstCall = client.requests.get();

        assertFalse(Boolean.TRUE.equals(firstResult.isError()), String.valueOf(firstResult.content()));

        McpSchema.CallToolResult secondResult = callInfoTool(tool);

        assertFalse(Boolean.TRUE.equals(secondResult.isError()), String.valueOf(secondResult.content()));
        assertEquals(requestsOfFirstCall, client.requests.get(),
                "A loaded document is served from the cache, so the pages must not be fetched twice");
    }

    @Test
    void getInfo_ng_fetchesAgainAfterASinglePageFailed(@TempDir Path outputDirectory) throws Exception {
        SwitchableHttpClient client = new SwitchableHttpClient(200);
        PlantumlGuideTool tool = new PlantumlGuideTool(outputDirectory, client.httpClient);

        // One page failing is enough to fail the whole load: half a guide must not be cached as the guide.
        AtomicInteger pageNumber = new AtomicInteger();
        HttpResponse<String> ok = SwitchableHttpClient.responseWith(200);
        HttpResponse<String> unavailable = SwitchableHttpClient.responseWith(503);
        doAnswer(invocation -> {
            client.requests.incrementAndGet();
            return pageNumber.getAndIncrement() == 0 ? unavailable : ok;
        }).when(client.httpClient).send(any(), any());

        McpSchema.CallToolResult firstResult = callInfoTool(tool);
        int requestsOfFirstCall = client.requests.get();

        assertTrue(Boolean.TRUE.equals(firstResult.isError()),
                "A partially fetched document must be reported as a failure");
        assertEquals(List.of(), filesIn(outputDirectory),
                "A partially fetched document must not be written to the workspace");

        doAnswer(invocation -> {
            client.requests.incrementAndGet();
            return ok;
        }).when(client.httpClient).send(any(), any());

        McpSchema.CallToolResult secondResult = callInfoTool(tool);

        assertEquals(requestsOfFirstCall * 2, client.requests.get(),
                "The page that was missing must be fetched again");
        assertFalse(Boolean.TRUE.equals(secondResult.isError()),
                "Once every page is reachable the document should load: " + secondResult.content());
    }
}
