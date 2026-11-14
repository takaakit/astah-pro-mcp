package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.ProtocolVersions;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tools.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

// End-to-end checks against a real Jetty server running the real SDK transport behind the approval servlet.
public class McpStreamableHttpCompatibilityTest {
    
    // Small enough that an oversized body costs nothing to build. McpClientApprovalServletTest pins the production
    // value, and McpServerApp states that same constant on the transport builder.
    private static final int TEST_MAX_REQUEST_SIZE_BYTES = 4096;
    
    private static final String ACCEPT_BOTH = "application/json, text/event-stream";
    
    // Every exchange below has to finish on its own; a stream that never ends must fail the test rather than hang it
    private static final Duration EXCHANGE_TIMEOUT = Duration.ofSeconds(15);
    
    private static final String INITIALIZE = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{"
            + "\"protocolVersion\":\"" + ProtocolVersions.MCP_2025_11_25 + "\","
            + "\"capabilities\":{},"
            + "\"clientInfo\":{\"name\":\"compatibility-test\",\"version\":\"0.0.0\"}}}";
    
    private static final String SERVER_DISCOVER = "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"server/discover\",\"params\":{}}";
    
    private Server jettyServer;
    private McpSyncServer mcpServer;
    private HttpServletStreamableServerTransportProvider transport;
    private HttpClient httpClient;
    private int port;
    private String endpoint;
    private String rawEndpoint;
    
    // How many times the approval dialog was asked for
    private final AtomicInteger prompts = new AtomicInteger();
    
    // The time the initialize rate limit reads, which only the tests move
    private final AtomicLong rateLimitClock = new AtomicLong();
    
    @BeforeEach
    void setUp() throws Exception {
        // The production settings, so that what these tests see is what a client sees. Replaced are only the keep-alive and the size limit.
        transport = ProfileServer.transportBuilder()
                .keepAliveInterval(null)
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
                                .content(List.of(McpSchema.TextContent.builder(String.valueOf(request.arguments().get("text"))).build()))
                                .isError(false)
                                .build())
                .build();
        
        // The dialog is the only production behaviour replaced here
        ApprovalPrompt approvingPrompt = request -> {
            prompts.incrementAndGet();
            return true;
        };
        // The production budget, on a clock the tests move
        InitializeRateLimit rateLimit = new InitializeRateLimit(rateLimitClock::get,
                McpServerConfig.MCP_INITIALIZE_RATE_PER_MINUTE,
                McpServerConfig.MCP_INITIALIZE_BURST);
        McpClientApprovalServlet approvalServlet = new McpClientApprovalServlet(transport, new ConnectionApproval(approvingPrompt), rateLimit, TEST_MAX_REQUEST_SIZE_BYTES);
        
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
        
        port = connector.getLocalPort();
        endpoint = "http://" + McpServerConfig.HOST + ":" + port + "/mcp";
        rawEndpoint = "http://" + McpServerConfig.HOST + ":" + port + "/raw/mcp";
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }
    
    @AfterEach
    void tearDown() throws Exception {
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
        HttpResponse<String> probe = post(endpoint, SERVER_DISCOVER, null);
        
        assertEquals(200, probe.statusCode());
        assertTrue(probe.body().contains("-32601"), "The probe must get a JSON-RPC error envelope: " + probe.body());
        assertTrue(probe.body().contains("\"id\":7"), probe.body());
        
        // And the client can then fall back to the ordinary handshake
        assertNotNull(initialize(), "initialize must still succeed after the probe");
    }
    
    @Test
    @Timeout(60)
    void hostHeader_ok_acceptsEitherNameOfThisServer() throws Exception {
        assertEquals(200, statusOfRawPost("POST /mcp HTTP/1.1\r\nHost: 127.0.0.1:" + port + "\r\n", SERVER_DISCOVER));
        assertEquals(200, statusOfRawPost("POST /mcp HTTP/1.1\r\nHost: localhost:" + port + "\r\n", SERVER_DISCOVER));
    }
    
    @Test
    @Timeout(60)
    void hostHeader_ng_refusesAHostThatDoesNotNameThisServer() throws Exception {
        int otherPort = port == 9 ? 10 : 9;
        
        assertEquals(403, statusOfRawPost("POST /mcp HTTP/1.1\r\nHost: evil.example:" + port + "\r\n", SERVER_DISCOVER),
                "A host name of another site, as a page reached through DNS rebinding sends");
        assertEquals(403, statusOfRawPost("POST /mcp HTTP/1.1\r\nHost: 127.0.0.1:" + otherPort + "\r\n", SERVER_DISCOVER),
                "Another port");
        assertEquals(403, statusOfRawPost("POST /mcp HTTP/1.0\r\n", SERVER_DISCOVER),
                "No Host header, which HTTP/1.0 allows");
    }
    
    @Test
    @Timeout(60)
    void hostHeader_ng_twoHostHeadersAreRefusedByJetty() throws Exception {
        // Jetty refuses these itself, before the servlet would; pinned so that the behaviour seen on the running server stays as observed
        assertEquals(400, statusOfRawPost(
                "POST /mcp HTTP/1.1\r\nHost: 127.0.0.1:" + port + "\r\nHost: evil.example\r\n", SERVER_DISCOVER));
    }
    
    @Test
    @Timeout(60)
    void originHeader_ng_refusesARequestThatCarriesOne() throws Exception {
        String host = "Host: 127.0.0.1:" + port + "\r\n";
        
        assertEquals(403, statusOfRawPost("POST /mcp HTTP/1.1\r\n" + host + "Origin: http://127.0.0.1:3000\r\n", SERVER_DISCOVER),
                "A page on another loopback port");
        assertEquals(403, statusOfRawPost("POST /mcp HTTP/1.1\r\n" + host + "Origin: http://evil.example\r\n", SERVER_DISCOVER),
                "A page of another site");
    }
    
    @Test
    @Timeout(60)
    void initialize_ng_isRefusedWithoutAJsonBodyAndNeverReachesTheDialog() throws Exception {
        // text/plain is what a browser may send to another origin without asking it first
        HttpRequest plainText = HttpRequest.newBuilder(URI.create(endpoint))
                .header("Content-Type", "text/plain;charset=UTF-8")
                .header("Accept", ACCEPT_BOTH)
                .timeout(EXCHANGE_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE, StandardCharsets.UTF_8))
                .build();
        
        HttpResponse<String> response = httpClient.send(plainText, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        
        assertEquals(415, response.statusCode(), response.body());
        assertEquals(0, prompts.get(), "The approval dialog must not be asked for");
        
        // The same initialize as JSON is approved and served, so it was the Content-Type that was refused
        assertNotNull(initialize());
        assertEquals(1, prompts.get());
    }
    
    @Test
    @Timeout(60)
    void initialize_ng_isRefusedWith429AndRetryAfterOnceTheBudgetIsUsedUp() throws Exception {
        for (int i = 0; i < McpServerConfig.MCP_INITIALIZE_BURST; i++) {
            initialize();
        }
        
        HttpResponse<String> refused = post(endpoint, INITIALIZE, null);
        
        assertEquals(429, refused.statusCode(), refused.body());
        // Set before sendError, so it must survive the error page Jetty writes
        Optional<String> retryAfter = refused.headers().firstValue("Retry-After");
        assertTrue(retryAfter.isPresent(), "A refusal must tell when to try again: " + refused.headers().map());
        assertTrue(Long.parseLong(retryAfter.get()) >= 1, "Retry-After: " + retryAfter.get());
        
        // One interval later the budget lets the next client in
        rateLimitClock.addAndGet(TimeUnit.MINUTES.toNanos(1) / McpServerConfig.MCP_INITIALIZE_RATE_PER_MINUTE);
        assertNotNull(initialize());
    }
    
    // Run the initialize handshake and return the session ID the transport issued
    private String initialize() throws Exception {
        HttpResponse<String> response = post(endpoint, INITIALIZE, null);
        
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
    
    // Send a JSON POST whose request line and headers are written out by the caller, and return the status code.
    private int statusOfRawPost(String requestLineAndHeaders, String body) throws Exception {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String head = requestLineAndHeaders
                + "Content-Type: application/json\r\n"
                + "Accept: " + ACCEPT_BOTH + "\r\n"
                + "Content-Length: " + bodyBytes.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
        
        try (Socket socket = new Socket(McpServerConfig.HOST, port)) {
            socket.setSoTimeout((int) EXCHANGE_TIMEOUT.toMillis());
            OutputStream out = socket.getOutputStream();
            out.write(head.getBytes(StandardCharsets.ISO_8859_1));
            out.write(bodyBytes);
            out.flush();
            
            String statusLine = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1)).readLine();
            assertNotNull(statusLine, "The server closed the connection without a response");
            return Integer.parseInt(statusLine.split(" ")[1]);
        }
    }
    
    // Pull the single JSON-RPC message out of an SSE response body
    private static JsonNode sseResult(String sseBody) {
        for (String line : sseBody.split("\\R")) {
            if (line.startsWith("data:")) {
                return JsonSupport.OBJ_MAPPER.readTree(line.substring("data:".length()).trim());
            }
        }
        return fail("No SSE data frame in the response body: " + sseBody);
    }
}
