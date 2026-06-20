package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.*;

public class McpServerAppTest {

    private static final int[] SERVER_PORTS = {
            McpServerConfig.PORT_FOR_FULL_TOOL,
            McpServerConfig.PORT_FOR_QUERY_ONLY_TOOL
    };

    private McpServerApp app;

    @BeforeEach
    void setUp() {
        app = new McpServerApp();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (app != null) {
            app.stop();
        }
    }

    // Starts the Jetty server; only tests that exercise the network layer need this.
    private void startServer() throws Exception {
        app.start();
        waitForServerReady();
    }

    @Test
    void rejectsConnectionsFromNonLoopbackAddresses() throws Exception {
        startServer();

        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        InetAddress nonLoopback = findNonLoopbackAddress();
        for (int port : SERVER_PORTS) {
            int targetPort = port;
            assertDoesNotThrow(
                    () -> attemptConnection(loopback, targetPort),
                    () -> "Loopback connection should succeed on port " + targetPort);

            IOException exception = assertThrows(IOException.class,
                    () -> attemptConnection(nonLoopback, targetPort),
                    () -> "Expected connection to be rejected for " + nonLoopback + " on port " + targetPort);

            assertTrue(exception instanceof ConnectException ||
                       exception instanceof NoRouteToHostException ||
                       exception instanceof java.net.SocketTimeoutException,
                    "Expected the connection to be refused or timeout, but got: " + exception);
        }
    }

    @Test
    void jettyConnectorBindsOnlyToLoopbackHost() throws Exception {
        startServer();

        Server jettyServer = app.getJettyServer();
        assertNotNull(jettyServer, "Jetty server should be initialized after start");

        Connector[] connectors = jettyServer.getConnectors();
        assertTrue(connectors.length > 0, "Expected at least one connector to be configured");

        Connector connector = connectors[0];
        assertTrue(connector instanceof ServerConnector, "First connector should be a ServerConnector");

        ServerConnector serverConnector = (ServerConnector) connector;
        String boundHost = serverConnector.getHost();

        assertNotNull(boundHost, "ServerConnector host should be explicitly set");
        assertEquals("127.0.0.1", boundHost, "ServerConnector must bind to 127.0.0.1");
        assertNotEquals("0.0.0.0", boundHost, "ServerConnector must not bind to 0.0.0.0");
    }

    @Test
    void registersEachAgentSessionIndependently() {
        app.registerClientSession("session-A", "127.0.0.1:50001", "Agent-A");
        app.registerClientSession("session-B", "127.0.0.1:50002", "Agent-B");

        assertEquals(2, app.getActiveSessionCount(), "Both agent sessions should be registered");
    }

    @Test
    void terminatingOneSessionLeavesOtherSessionsActive() {
        app.registerClientSession("session-A", "127.0.0.1:50001", "Agent-A");
        app.registerClientSession("session-B", "127.0.0.1:50002", "Agent-B");

        app.terminateClientSession("session-A", "client_disconnect");

        assertEquals(1, app.getActiveSessionCount(), "Only the terminated session should be removed");
        assertTrue(app.getActiveSessions().stream()
                        .anyMatch(s -> s.getSessionId().equals("session-B")),
                "The other agent's session must remain active after terminating a different session");
        assertFalse(app.getActiveSessions().stream()
                        .anyMatch(s -> s.getSessionId().equals("session-A")),
                "The terminated session must no longer be active");
    }

    private void waitForServerReady() throws InterruptedException, UnknownHostException {
        for (int port : SERVER_PORTS) {
            waitForPortReady(port);
        }
    }

    private void waitForPortReady(int port) throws InterruptedException, UnknownHostException {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        while (System.nanoTime() < deadline) {
            try {
                attemptConnection(loopback, port);
                return;
            } catch (IOException ignored) {
                Thread.sleep(100);
            }
        }
        fail("Server did not start accepting loopback connections within timeout for port " + port);
    }

    private void attemptConnection(InetAddress address, int port) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, port), 5_000);
        }
    }

    private InetAddress findNonLoopbackAddress() throws SocketException {
        InetAddress ipv6Candidate = null;
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                continue;
            }
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
                    continue;
                }
                if (address instanceof Inet4Address inet4 && !inet4.isLinkLocalAddress()) {
                    return inet4;
                }
                if (ipv6Candidate == null && address instanceof Inet6Address inet6 && !inet6.isLinkLocalAddress()) {
                    ipv6Candidate = inet6;
                }
            }
        }
        if (ipv6Candidate != null) {
            return ipv6Candidate;
        }
        throw new IllegalStateException("No non-loopback network address found for the test");
    }
}
