package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.astah.pro.TestSupport;
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
    
    // Built the way a profile builds it, so that the schedulers this test watches are the ones production starts.
    private HttpServletStreamableServerTransportProvider newTransport() {
        return track(ProfileServer.transportBuilder().build());
    }
    
    private HttpServletStreamableServerTransportProvider track(HttpServletStreamableServerTransportProvider transport) {
        transports.add(transport);
        return transport;
    }
    
    private static boolean keepAliveRunning(HttpServletStreamableServerTransportProvider transport) throws Exception {
        Field field = TestSupport.getAccessibleField(
                HttpServletStreamableServerTransportProvider.class,
                "keepAliveScheduler");
        KeepAliveScheduler scheduler = (KeepAliveScheduler) field.get(transport);
        
        return scheduler != null && scheduler.isRunning();
    }
    
    // The thread name the SDK gives the scheduler its session sweeper runs on
    private static final String SESSION_SWEEPER_THREAD_PREFIX = "streamable-http-server-transport-session-sweeper";
    
    private static List<Thread> sessionSweeperThreads() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().startsWith(SESSION_SWEEPER_THREAD_PREFIX))
                .toList();
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
    void release_ok_stopsTheKeepAliveOfATransportThatHasNoMcpServer() throws Exception {
        HttpServletStreamableServerTransportProvider transport = newTransport();
        assertTrue(keepAliveRunning(transport), "The transport should start its keep-alive when it is built");
        
        List<Exception> failures = ProfileServer.release("test", null, transport, null);
        
        assertEquals(List.of(), failures, "Closing a freshly built transport should not fail");
        assertFalse(keepAliveRunning(transport), "The keep-alive of a released transport must be stopped");
    }
    
    // Closing the MCP server closes the transport with it, so the transport is not closed a second time.
    @Test
    void release_ok_stopsTheKeepAliveThroughTheMcpServer() throws Exception {
        HttpServletStreamableServerTransportProvider transport = newTransport();
        McpSyncServer mcpServer = mcpServerOn(transport);
        assertTrue(keepAliveRunning(transport), "The transport should start its keep-alive when it is built");
        
        List<Exception> failures = ProfileServer.release("test", mcpServer, transport, null);
        
        assertEquals(List.of(), failures, "Closing a freshly built MCP server should not fail");
        assertFalse(keepAliveRunning(transport), "Closing the MCP server must close the transport with it");
    }
    
    // Why production leaves the SDK's session sweeper off (ProfileServer.transportBuilder): closing the transport does not
    // stop the thread the sweeper runs on. Pinned so that the SDK upgrade which fixes it fails here, which is the signal
    // to turn the sweeper on. The thread this test starts stays behind in the test JVM, as it would in Astah; it is idle.
    @Test
    void sessionSweeper_ng_theSdkLeavesItsThreadRunningAfterTheTransportCloses() throws Exception {
        List<Thread> before = sessionSweeperThreads();
        HttpServletStreamableServerTransportProvider transport = track(ProfileServer.transportBuilder()
                .sessionSweepInterval(Duration.ofHours(1))
                .build());
        
        List<Thread> started = new ArrayList<>(sessionSweeperThreads());
        started.removeAll(before);
        assertEquals(1, started.size(), "The SDK no longer runs its session sweeper on a thread named "
                + SESSION_SWEEPER_THREAD_PREFIX + "*, so check again whether closing the transport stops it: " + started);
        Thread sweeper = started.get(0);
        
        transport.closeGracefully().block();
        sweeper.join(Duration.ofSeconds(2));
        
        assertTrue(sweeper.isAlive(), "The SDK now stops its session sweeper when the transport closes. Turn the sweeper on"
                + " in ProfileServer.transportBuilder() at 30 minutes, and make this test require the thread to stop.");
        assertFalse(sweeper.isDaemon(), "The SDK now runs its session sweeper on a daemon thread. It still stays behind, but"
                + " no longer keeps a JVM from exiting, so weigh again whether to turn the sweeper on.");
    }
    
    // A step that fails must not take the rest of the clean-up with it, and must be reported rather than thrown.
    @Test
    void release_ng_releasesTheRestWhenAStepFails() throws Exception {
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
                ProfileServer.release("test", failingMcpServer, transport, jettyServer);
        
        assertEquals(1, failures.size(), "The failure should be reported: " + failures);
        assertEquals("close failed", failures.get(0).getMessage());
        assertTrue(jettyServer.isStopped(), "The Jetty server must be stopped even though an earlier step failed");
        assertTrue(destroyed.get(), "The Jetty server must be destroyed even though an earlier step failed");
    }
    
    // A profile releases what it owns when it is stopped, and lets go of it, so that stopping it again releases nothing twice.
    @Test
    void stop_ok_releasesWhatTheProfileOwnsOnlyOnce() throws Exception {
        // Port 0, so that the test takes whatever port is free rather than one a profile of a running Astah may hold.
        ProfileServer profile = ProfileServer.start("test", 0, List.of(), "", this::newTransport);
        HttpServletStreamableServerTransportProvider transport = transports.get(0);
        Server jettyServer = profile.jettyServer();
        assertTrue(jettyServer.isStarted(), "The profile should have started its Jetty server");
        assertTrue(keepAliveRunning(transport), "The profile's transport should be running its keep-alive");
        
        assertEquals(List.of(), profile.stop(), "Stopping a started profile should not fail");
        assertFalse(keepAliveRunning(transport), "Stopping the profile must close its transport");
        assertTrue(jettyServer.isStopped(), "Stopping the profile must stop its Jetty server");
        assertNull(profile.jettyServer(), "A stopped profile must let go of what it released");
        
        assertEquals(List.of(), profile.stop(), "Stopping again should find nothing left to release");
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
