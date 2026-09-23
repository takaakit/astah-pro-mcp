package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.ProtocolVersions;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// End-to-end checks against a real Jetty server running the real SDK transport behind the approval servlet.
class McpStreamableHttpCompatibilityTest {

    // Small enough that an oversized body costs nothing to build. McpClientApprovalServletTest pins the production
    // value, and McpServerApp states that same constant on the transport builder.
    private static final int TEST_MAX_REQUEST_SIZE_BYTES = 4096;

    private static final String ACCEPT_BOTH = "application/json, text/event-stream";

    // Every exchange below has to finish on its own; a stream that never ends must fail the test rather than hang it
    private static final Duration EXCHANGE_TIMEOUT = Duration.ofSeconds(15);

    private Server jettyServer;
    private McpSyncServer mcpServer;
    private HttpServletStreamableServerTransportProvider transport;
    private HttpClient httpClient;
    private String endpoint;
    private String rawEndpoint;

    @BeforeEach
    void startServer() throws Exception {
        transport = HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .jsonMapper(JsonSupport.MCP_JSON_MAPPER)
                // No keep-alive: these tests never open a listening GET stream, and a ping scheduler would only add noise
                .maxRequestSize(TEST_MAX_REQUEST_SIZE_BYTES)
                .build();

        mcpServer = McpServer.sync(transport)
                .serverInfo(McpSchema.Implementation.builder("astah-pro-mcp-test", "0.0.0").build())
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .toolCall(McpSchema.Tool.builder("echo", JsonSupport.MCP_JSON_MAPPER,
                                "{\"type\":\"object\",\"properties\":{\"text\":{\"type\":\"string\"}},\"required\":[\"text\"]}")
                                .description("Echo the given text back")
                                .build(),
                        (exchange, request) -> McpSchema.CallToolResult.builder()
                                .content(List.of(new McpSchema.TextContent(String.valueOf(request.arguments().get("text")))))
                                .isError(false)
                                .build())
                .build();

        // The dialog is the only production behaviour replaced here
        McpClientApprovalServlet approvalServlet = new McpClientApprovalServlet(
                transport, McpServerConfig.ORIGIN_HOST_ALLOWLIST, TEST_MAX_REQUEST_SIZE_BYTES) {

            @Override
            boolean promptUserForApproval(RequestContext context) {
                return true;
            }
        };

        jettyServer = new Server();
        ServerConnector connector = new ServerConnector(jettyServer);
        connector.setHost(McpServerConfig.HOST);
        // Port 0 so the test never collides with a running Astah on 18888 or 8888
        connector.setPort(0);
        try {
            connector.setReusePort(false);
        } catch (UnsupportedOperationException e) {
            // Not supported on this platform; the port is ephemeral anyway
        }
        jettyServer.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        ServletHolder approvalHolder = new ServletHolder(approvalServlet);
        approvalHolder.setAsyncSupported(true);
        context.addServlet(approvalHolder, "/mcp");

        // The same transport mounted without the servlet in front, so the SDK's own limit can be observed directly.
        // The transport matches on the endpoint suffix, so this path reaches the same handler.
        ServletHolder rawHolder = new ServletHolder(transport);
        rawHolder.setAsyncSupported(true);
        context.addServlet(rawHolder, "/raw/mcp");

        jettyServer.setHandler(context);
        jettyServer.start();

        int port = connector.getLocalPort();
        endpoint = "http://" + McpServerConfig.HOST + ":" + port + "/mcp";
        rawEndpoint = "http://" + McpServerConfig.HOST + ":" + port + "/raw/mcp";
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (mcpServer != null) {
            mcpServer.closeGracefully();
        }
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }

    @Test
    @Timeout(60)
    void session_ok_runsTheWholeHandshakeAndClosesCleanly() throws Exception {
        String sessionId = initialize();

        HttpResponse<String> initialized = post(endpoint,
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}", sessionId);
        assertEquals(202, initialized.statusCode(), "The initialized notification must be accepted");

        HttpResponse<String> toolsList = post(endpoint,
                "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}", sessionId);
        assertEquals(200, toolsList.statusCode());
        JsonNode listed = sseResult(toolsList.body());
        assertTrue(listed.at("/result/tools").toString().contains("\"echo\""),
                "tools/list must carry the registered tool: " + toolsList.body());

        HttpResponse<String> toolsCall = post(endpoint,
                "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\","
                        + "\"params\":{\"name\":\"echo\",\"arguments\":{\"text\":\"クラス図\"}}}", sessionId);
        assertEquals(200, toolsCall.statusCode());
        JsonNode called = sseResult(toolsCall.body());
        assertFalse(called.at("/result/isError").asBoolean(true), "The tool call must succeed: " + toolsCall.body());
        assertTrue(toolsCall.body().contains("クラス図"),
                "A Japanese argument must survive the round trip: " + toolsCall.body());

        HttpRequest delete = HttpRequest.newBuilder(URI.create(endpoint))
                .header("Mcp-Session-Id", sessionId)
                .timeout(EXCHANGE_TIMEOUT)
                .DELETE()
                .build();
        assertEquals(200, httpClient.send(delete, HttpResponse.BodyHandlers.ofString()).statusCode(),
                "Closing the session must succeed");
    }

    @Test
    @Timeout(60)
    void unknownMethod_ok_answersMethodNotFoundAndEndsThatResponseStream() throws Exception {
        // java-sdk#1041. Before the fix the error was written but the SSE stream stayed open, so the exchange below
        // would run into its timeout instead of completing. The session itself must survive.
        String sessionId = initialize();
        post(endpoint, "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}", sessionId);

        HttpResponse<String> unknown = post(endpoint,
                "{\"jsonrpc\":\"2.0\",\"id\":9,\"method\":\"no/such/method\"}", sessionId);

        assertEquals(200, unknown.statusCode());
        assertTrue(unknown.body().contains("-32601"),
                "An unknown method on an established session must be answered with -32601: " + unknown.body());

        // The body only arrives complete once the server ends the stream, so reaching here is the end-of-stream assertion
        HttpResponse<String> afterwards = post(endpoint,
                "{\"jsonrpc\":\"2.0\",\"id\":10,\"method\":\"tools/list\"}", sessionId);
        assertEquals(200, afterwards.statusCode(), "The session must stay usable after an unknown method");
        assertTrue(afterwards.body().contains("\"echo\""), "Response after the error: " + afterwards.body());
    }

    @Test
    @Timeout(60)
    void oversizedBody_ng_isRejectedByTheTransportItself() throws Exception {
        // Straight to the transport, with the approval servlet out of the way
        HttpResponse<String> response = post(rawEndpoint, oversizedInitialize(), null);

        assertEquals(413, response.statusCode(),
                "The SDK transport must refuse a body above its own limit");
    }

    @Test
    @Timeout(60)
    void oversizedBody_ng_isRejectedByTheServletBeforeItReachesTheTransport() throws Exception {
        // Through the approval servlet, which buffers the body before the transport sees it
        HttpResponse<String> response = post(endpoint, oversizedInitialize(), null);

        assertEquals(413, response.statusCode(),
                "The approval servlet must refuse the body it would otherwise cache whole");
    }

    @Test
    @Timeout(60)
    void serverDiscover_ok_isAnsweredByTheServletBeforeInitialize() throws Exception {
        // The pre-initialize fallback is this servlet's own; #1041 does not replace it
        HttpResponse<String> probe = post(endpoint,
                "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"server/discover\",\"params\":{}}", null);

        assertEquals(200, probe.statusCode());
        assertTrue(probe.body().contains("-32601"), "The probe must get a JSON-RPC error envelope: " + probe.body());
        assertTrue(probe.body().contains("\"id\":7"), probe.body());

        // And the client can then fall back to the ordinary handshake
        assertNotNull(initialize(), "initialize must still succeed after the probe");
    }

    // Run the initialize handshake and return the session ID the transport issued
    private String initialize() throws Exception {
        HttpResponse<String> response = post(endpoint,
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{"
                        + "\"protocolVersion\":\"" + ProtocolVersions.MCP_2025_11_25 + "\","
                        + "\"capabilities\":{},"
                        + "\"clientInfo\":{\"name\":\"compatibility-test\",\"version\":\"0.0.0\"}}}", null);

        assertEquals(200, response.statusCode(), "initialize failed: " + response.body());
        Optional<String> sessionId = response.headers().firstValue("Mcp-Session-Id");
        assertTrue(sessionId.isPresent(), "initialize must issue a session ID");
        return sessionId.get();
    }

    // An otherwise valid initialize padded past the limit
    private static String oversizedInitialize() {
        String padding = "x".repeat(TEST_MAX_REQUEST_SIZE_BYTES * 2);
        return "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{"
                + "\"protocolVersion\":\"" + ProtocolVersions.MCP_2025_11_25 + "\","
                + "\"capabilities\":{},"
                + "\"clientInfo\":{\"name\":\"" + padding + "\",\"version\":\"0.0.0\"}}}";
    }

    private HttpResponse<String> post(String url, String body, String sessionId) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", ACCEPT_BOTH)
                .timeout(EXCHANGE_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        if (sessionId != null) {
            request.header("Mcp-Session-Id", sessionId);
        }
        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    // Pull the single JSON-RPC message out of an SSE response body
    private static JsonNode sseResult(String sseBody) {
        for (String line : sseBody.split("\\R")) {
            if (line.startsWith("data:")) {
                return JsonSupport.OBJ_MAPPER.readTree(line.substring("data:".length()).trim());
            }
        }
        throw new AssertionError("No SSE data frame in the response body: " + sseBody);
    }
}
