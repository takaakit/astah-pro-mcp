package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// The profiles start together or not at all
public class ProfileStartupTest {

    private static final List<Integer> PROFILE_PORTS = List.of(
            McpServerConfig.PORT_FOR_DIRECT,
            McpServerConfig.PORT_FOR_PROGRAMMATIC);

    private McpServerApp app;

    @AfterEach
    void tearDown() throws Exception {
        if (app != null) {
            app.stop();
            app = null;
        }
    }

    private static boolean canConnect(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByName(McpServerConfig.HOST), port), 2_000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void awaitListening(int port) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (System.nanoTime() < deadline) {
            if (canConnect(port)) {
                return;
            }
            Thread.sleep(100);
        }
        fail("Nothing is listening on port " + port);
    }

    @Test
    void start_ok_listensOnEveryProfilePort() throws Exception {
        app = new McpServerApp();
        app.start();

        for (int port : PROFILE_PORTS) {
            awaitListening(port);
        }
    }

    // Occupying the programmatic port because it is the one the profiles were most recently extended with.
    @Test
    void start_ng_leavesNoProfileRunningWhenAPortIsTaken() throws Exception {
        try (ServerSocket occupied = new ServerSocket()) {
            occupied.setReuseAddress(false);
            occupied.bind(new InetSocketAddress(InetAddress.getByName(McpServerConfig.HOST),
                    McpServerConfig.PORT_FOR_PROGRAMMATIC), 1);

            app = new McpServerApp();

            assertThrows(Exception.class, () -> app.start(),
                    "A port that cannot be bound must disable the plugin rather than leave it half started");

            assertFalse(canConnect(McpServerConfig.PORT_FOR_DIRECT),
                    "The direct profile started before the failure and must have been stopped again");
        }
    }
}
