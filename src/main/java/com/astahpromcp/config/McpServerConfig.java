package com.astahpromcp.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

// Configuration for the MCP server.
public final class McpServerConfig {

    private McpServerConfig() {
    }

    // Default host address
    public static final String HOST = "127.0.0.1";

    // Port for full tool access
    public static final int PORT_FOR_FULL_TOOL = 8888;

    // Port for query-only tool access
    public static final int PORT_FOR_QUERY_ONLY_TOOL = 8889;

    // Allowlist of host addresses
    public static final Set<String> ORIGIN_HOST_ALLOWLIST = Set.of("127.0.0.1", "::1");

    // Grace period after a user approval during which further initialize requests
    // from the same User-Agent are approved automatically without a dialog.
    public static final long APPROVAL_GRACE_PERIOD_MS = 30_000;

    // Maximum time a tool call waits to acquire exclusive access to the Astah API
    public static final long ASTAH_API_LOCK_TIMEOUT_SECONDS = 30;

    // Maximum time to wait for the EDT queue to drain after a tool execution.
    public static final long EDT_FLUSH_TIMEOUT_SECONDS = 20;

    // Maximum time a script run may execute.
    public static final long SCRIPT_EXECUTION_TIMEOUT_SECONDS = 60;

    // Maximum time to establish a connection when fetching knowledge documents from the web.
    public static final long KNOWLEDGE_FETCH_CONNECT_TIMEOUT_SECONDS = 10;

    // Maximum time to wait for a response when fetching knowledge documents from the web.
    public static final long KNOWLEDGE_FETCH_REQUEST_TIMEOUT_SECONDS = 30;

    // Root directory where generated output is stored.
    public static final Path ROOT_OUTPUT_DIR = Paths.get(System.getProperty("user.home"), ".astah-pro-mcp");

    // Temporary workspace directory
    public static final Path WORKSPACE_DIR = ROOT_OUTPUT_DIR.resolve("workspace");

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
