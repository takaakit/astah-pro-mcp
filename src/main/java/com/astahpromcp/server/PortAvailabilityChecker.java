package com.astahpromcp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

// Checks whether the TCP ports required by the MCP server are available.
public final class PortAvailabilityChecker {

    private PortAvailabilityChecker() {
    }

    // Return the subset of the given ports that are already in use on the given host.
    public static List<Integer> findPortsInUse(String host, List<Integer> ports) {
        List<Integer> inUse = new ArrayList<>();
        for (int port : ports) {
            try (ServerSocket socket = new ServerSocket()) {
                socket.setReuseAddress(false);
                socket.bind(new InetSocketAddress(InetAddress.getByName(host), port), 1);
            } catch (IOException e) {
                inUse.add(port);
            }
        }
        return inUse;
    }
}
