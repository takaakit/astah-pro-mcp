package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

// The profiles start together or not at all
public class ProfileStartupTest {

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

    // Occupy the programmatic port so startup fails after the direct profile has started.
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
