package com.astahpromcp.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

// Configuration for the MCP server.
public final class McpServerConfig {

    private McpServerConfig() {
    }

    // Default host address
    public static final String HOST = "127.0.0.1";

    // Environment variable that overrides the port for full tool access
    public static final String ENV_PORT_FOR_FULL_TOOL = "ASTAH_PRO_MCP_PORT_FOR_FULL";

    // Environment variable that overrides the port for query-only tool access
    public static final String ENV_PORT_FOR_QUERY_ONLY_TOOL = "ASTAH_PRO_MCP_PORT_FOR_QUERY";

    // Port for full tool access
    public static final int DEFAULT_PORT_FOR_FULL_TOOL = 8888;

    // Port for query-only tool access
    public static final int DEFAULT_PORT_FOR_QUERY_ONLY_TOOL = 8889;

    private static final PortResolver.Result FULL_TOOL_PORT = PortResolver.resolve(
            ENV_PORT_FOR_FULL_TOOL,
            System.getenv(ENV_PORT_FOR_FULL_TOOL),
            DEFAULT_PORT_FOR_FULL_TOOL);

    private static final PortResolver.Result QUERY_ONLY_TOOL_PORT = PortResolver.resolve(
            ENV_PORT_FOR_QUERY_ONLY_TOOL,
            System.getenv(ENV_PORT_FOR_QUERY_ONLY_TOOL),
            DEFAULT_PORT_FOR_QUERY_ONLY_TOOL);

    // Port for full tool access, overridable via ASTAH_PRO_MCP_PORT_FOR_FULL
    public static final int PORT_FOR_FULL_TOOL = FULL_TOOL_PORT.port();

    // Port for query-only tool access, overridable via ASTAH_PRO_MCP_PORT_FOR_QUERY
    public static final int PORT_FOR_QUERY_ONLY_TOOL = QUERY_ONLY_TOOL_PORT.port();

    // Warnings produced while resolving the ports
    public static List<String> portResolutionWarnings() {
        return Stream.of(FULL_TOOL_PORT.warning(), QUERY_ONLY_TOOL_PORT.warning())
                .filter(Objects::nonNull)
                .toList();
    }

    // Allowlist of host addresses
    public static final Set<String> ORIGIN_HOST_ALLOWLIST = Set.of("127.0.0.1", "::1");

    // Grace period after a user approval during which further initialize requests from the same User-Agent are approved automatically without a dialog.
    public static final long APPROVAL_GRACE_PERIOD_MS = 30_000;

    // Maximum time an initialize request waits for the approval dialog of another initialize request on the same port to be answered.
    // Kept below JETTY_IDLE_TIMEOUT_MS so that the rejection still reaches a client whose connection has not been dropped yet.
    public static final long APPROVAL_DIALOG_WAIT_TIMEOUT_SECONDS = 180;

    // Maximum time a tool call waits to acquire exclusive access to the Astah API
    public static final long ASTAH_API_LOCK_TIMEOUT_SECONDS = 30;

    // Maximum time to wait for the EDT queue to drain after a tool execution.
    public static final long EDT_FLUSH_TIMEOUT_SECONDS = 20;

    // Maximum time a script run may execute.
    // Kept below the tool call timeout of typical MCP clients (around 30 seconds) so that the timeout response actually reaches the AI agent instead of the client giving up first.
    public static final long SCRIPT_EXECUTION_TIMEOUT_SECONDS = 20;

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

    // Interval at which the MCP transport pings each session over its listening SSE stream.
    // Kept well below JETTY_IDLE_TIMEOUT_MS.
    public static final long TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS = 30;

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
