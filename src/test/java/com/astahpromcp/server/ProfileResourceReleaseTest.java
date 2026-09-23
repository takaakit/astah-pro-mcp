package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.KeepAliveScheduler;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

// What a profile owns has to be released whether or not it finished starting.
//
// The transport starts a keep-alive scheduler the moment it is built, and that task holds on to the transport, so a
// profile that fails half way through cannot be cleaned up by dropping the local variables. The scheduler runs on the
// shared boundedElastic pool, so its state is read from the scheduler itself rather than from a thread count.
public class ProfileResourceReleaseTest {

    private final List<HttpServletStreamableServerTransportProvider> transports = new ArrayList<>();
    private McpServerApp app;

    @AfterEach
    void tearDown() throws Exception {
        if (app != null) {
            app.stop();
            app = null;
        }
        for (HttpServletStreamableServerTransportProvider transport : transports) {
            try {
                transport.closeGracefully().block();
            } catch (Exception ignored) {
                // The test is done with it either way.
            }
        }
        transports.clear();
    }

    // Built the way a profile builds it, so that the keep-alive scheduler this test watches is the one production starts.
    private HttpServletStreamableServerTransportProvider newTransport() {
        HttpServletStreamableServerTransportProvider transport = HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .jsonMapper(JsonSupport.MCP_JSON_MAPPER)
                .keepAliveInterval(Duration.ofSeconds(McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS))
                .build();
        transports.add(transport);
        return transport;
    }

    private static boolean keepAliveRunning(HttpServletStreamableServerTransportProvider transport) throws Exception {
        Field field = HttpServletStreamableServerTransportProvider.class.getDeclaredField("keepAliveScheduler");
        field.setAccessible(true);
        KeepAliveScheduler scheduler = (KeepAliveScheduler) field.get(transport);

        return scheduler != null && scheduler.isRunning();
    }

    private static McpSyncServer mcpServerOn(HttpServletStreamableServerTransportProvider transport) {
        return McpServer.sync(transport)
                .serverInfo(McpSchema.Implementation.builder("astah-pro-mcp-test", "0").title("test").build())
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .build();
    }

    // closeGracefully() only describes the shutdown: the keep-alive scheduler is stopped when the Mono completes, so a
    // Mono that is dropped instead of subscribed leaves the scheduler running and the leak in place.
    @Test
    void releaseProfileResources_ok_stopsTheKeepAliveOfATransportThatHasNoMcpServer() throws Exception {
        HttpServletStreamableServerTransportProvider transport = newTransport();
        assertTrue(keepAliveRunning(transport), "The transport should start its keep-alive when it is built");

        List<Exception> failures = McpServerApp.releaseProfileResources("test", null, transport, null);

        assertEquals(List.of(), failures, "Closing a freshly built transport should not fail");
        assertFalse(keepAliveRunning(transport), "The keep-alive of a released transport must be stopped");
    }

    // Closing the MCP server closes the transport with it, so the transport is not closed a second time.
    @Test
    void releaseProfileResources_ok_stopsTheKeepAliveThroughTheMcpServer() throws Exception {
        HttpServletStreamableServerTransportProvider transport = newTransport();
        McpSyncServer mcpServer = mcpServerOn(transport);
        assertTrue(keepAliveRunning(transport), "The transport should start its keep-alive when it is built");

        List<Exception> failures = McpServerApp.releaseProfileResources("test", mcpServer, transport, null);

        assertEquals(List.of(), failures, "Closing a freshly built MCP server should not fail");
        assertFalse(keepAliveRunning(transport), "Closing the MCP server must close the transport with it");
    }

    // A step that fails must not take the rest of the clean-up with it, and must be reported rather than thrown.
    @Test
    void releaseProfileResources_ng_releasesTheRestWhenAStepFails() throws Exception {
        HttpServletStreamableServerTransportProvider transport = newTransport();
        McpSyncServer failingMcpServer = mock(McpSyncServer.class);
        doThrow(new IllegalStateException("close failed")).when(failingMcpServer).closeGracefully();

        AtomicBoolean destroyed = new AtomicBoolean();
        Server jettyServer = new Server() {
            @Override
            public void destroy() {
                destroyed.set(true);
                super.destroy();
            }
        };
        jettyServer.start();

        List<Exception> failures =
                McpServerApp.releaseProfileResources("test", failingMcpServer, transport, jettyServer);

        assertEquals(1, failures.size(), "The failure should be reported: " + failures);
        assertEquals("close failed", failures.get(0).getMessage());
        assertTrue(jettyServer.isStopped(), "The Jetty server must be stopped even though an earlier step failed");
        assertTrue(destroyed.get(), "The Jetty server must be destroyed even though an earlier step failed");
    }

    // The profiles start together or not at all, and what the failed start built must be released with them.
    @Test
    void start_ng_closesEveryTransportItBuiltWhenAProfileCannotStart() throws Exception {
        try (ServerSocket occupied = new ServerSocket()) {
            occupied.setReuseAddress(false);
            occupied.bind(new InetSocketAddress(InetAddress.getByName(McpServerConfig.HOST),
                    McpServerConfig.PORT_FOR_PROGRAMMATIC), 1);

            app = new McpServerApp();
            app.transportFactory = this::newTransport;

            Exception failure = assertThrows(Exception.class, () -> app.start(),
                    "A port that cannot be bound must disable the plugin rather than leave it half started");

            assertFalse(transports.isEmpty(), "The start should have built at least one transport");
            for (HttpServletStreamableServerTransportProvider transport : transports) {
                assertFalse(keepAliveRunning(transport),
                        "Every transport the failed start built must have been closed, including the profile's that never finished");
            }

            assertNotNull(failure.getMessage(), "The reason the profile did not start must survive the clean-up");

            // Nothing was registered for the failed profile, so stopping again has nothing to do and must not fail.
            assertDoesNotThrow(() -> app.stop());
            app = null;
        }
    }
}
