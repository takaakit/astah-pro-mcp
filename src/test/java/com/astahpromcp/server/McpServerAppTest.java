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

    private McpServerApp app;

    @BeforeEach
    void setUp() throws Exception {
        app = new McpServerApp();
        app.start();
        waitForServerReady();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void rejectsConnectionsFromNonLoopbackAddresses() throws Exception {
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        assertDoesNotThrow(() -> attemptConnection(loopback), "Loopback connection should succeed");

        InetAddress nonLoopback = findNonLoopbackAddress();
        IOException exception = assertThrows(IOException.class,
                () -> attemptConnection(nonLoopback),
                () -> "Expected connection to be rejected for " + nonLoopback);

        assertTrue(exception instanceof ConnectException || 
                   exception instanceof NoRouteToHostException ||
                   exception instanceof java.net.SocketTimeoutException,
                "Expected the connection to be refused or timeout, but got: " + exception);
    }

    @Test
    void jettyConnectorBindsOnlyToLoopbackHost() {
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

    private void waitForServerReady() throws InterruptedException, UnknownHostException {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        while (System.nanoTime() < deadline) {
            try {
                attemptConnection(loopback);
                return;
            } catch (IOException ignored) {
                Thread.sleep(100);
            }
        }
        fail("Server did not start accepting loopback connections within timeout");
    }

    private void attemptConnection(InetAddress address) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, McpServerConfig.DEFAULT_PORT), 5_000);
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
