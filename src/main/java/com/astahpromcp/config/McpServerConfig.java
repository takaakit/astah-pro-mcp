package com.astahpromcp.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

// Configuration for the MCP server.
public final class McpServerConfig {

    private McpServerConfig() {
    }

    // Default host address
    public static final String DEFAULT_HOST = "127.0.0.1";

    // Default port
    public static final int DEFAULT_PORT = 8888;

    // Default allowlist of host addresses
    public static final Set<String> DEFAULT_ORIGIN_HOST_ALLOWLIST =
            Set.of("127.0.0.1", "::1");

    // Root directory where generated output is stored.
    public static final Path DEFAULT_OUTPUT_DIR =
            Paths.get(System.getProperty("user.home"), ".astah-pro-mcp");

    // Temporary workspace directory
    public static final Path DEFAULT_WORKSPACE_DIR =
            DEFAULT_OUTPUT_DIR.resolve("workspace");

    // Jetty server idle timeout
    public static final int JETTY_IDLE_TIMEOUT_MS = 300000; // 5min

    // Number of Jetty acceptor threads
    // Controls how many concurrent connections can be accepted.
    public static final int JETTY_ACCEPTOR_THREADS = 2;

    // Number of Jetty selector threads
    // Used to process non-blocking I/O operations.
    public static final int JETTY_SELECTOR_THREADS = 4;

    // Maximum number of Jetty worker threads
    // Controls how many requests can be processed in parallel.
    public static final int JETTY_MAX_THREADS = 200;

    // Minimum number of Jetty worker threads
    // Threads kept alive even when idle.
    public static final int JETTY_MIN_THREADS = 10;
}
