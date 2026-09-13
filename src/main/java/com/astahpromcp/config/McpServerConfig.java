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

    // Environment variable that overrides the port for the direct tool calling mode
    public static final String ENV_PORT_FOR_DIRECT = "ASTAH_PRO_MCP_PORT_FOR_DIRECT";

    // Environment variable that overrides the port for the programmatic tool calling mode
    public static final String ENV_PORT_FOR_PROGRAMMATIC = "ASTAH_PRO_MCP_PORT_FOR_PROGRAMMATIC";

    // Port for the direct tool calling mode
    public static final int DEFAULT_PORT_FOR_DIRECT = 18888;

    // Port for the programmatic tool calling mode
    public static final int DEFAULT_PORT_FOR_PROGRAMMATIC = 8888;

    private static final PortResolver.Result DIRECT_PORT = PortResolver.resolve(
            ENV_PORT_FOR_DIRECT,
            System.getenv(ENV_PORT_FOR_DIRECT),
            DEFAULT_PORT_FOR_DIRECT);

    private static final PortResolver.Result PROGRAMMATIC_PORT = PortResolver.resolve(
            ENV_PORT_FOR_PROGRAMMATIC,
            System.getenv(ENV_PORT_FOR_PROGRAMMATIC),
            DEFAULT_PORT_FOR_PROGRAMMATIC);

    // Port for the direct tool calling mode, overridable via ASTAH_PRO_MCP_PORT_FOR_DIRECT
    public static final int PORT_FOR_DIRECT = DIRECT_PORT.port();

    // Port for the programmatic tool calling mode, overridable via ASTAH_PRO_MCP_PORT_FOR_PROGRAMMATIC
    public static final int PORT_FOR_PROGRAMMATIC = PROGRAMMATIC_PORT.port();

    // Warnings produced while resolving the ports
    public static List<String> portResolutionWarnings() {
        return Stream.of(DIRECT_PORT.warning(), PROGRAMMATIC_PORT.warning())
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

    // Maximum time one run_astah_api_script (astah api script) run may execute.
    // Kept below the tool call timeout of typical MCP clients (around 30 seconds) so that the timeout response actually reaches the AI agent instead of the client giving up first.
    public static final long ASTAH_API_SCRIPT_TIMEOUT_SECONDS = 20;

    // Maximum time one run_mcp_tool_script (mcp tool script) run may execute
    public static final long MCP_TOOL_SCRIPT_TIMEOUT_SECONDS = 20;

    // Maximum size of the mcp tool script source
    public static final int MCP_TOOL_SCRIPT_MAX_SOURCE_BYTES = 32_768;

    // Name the engine is given for the mcp tool script itself, which is what tells its frames from those of the preludes evaluated before it
    public static final String MCP_TOOL_SCRIPT_SOURCE_NAME = "mcp-tool-script";

    // Maximum amount of print() output kept from one mcp tool script run
    public static final int MCP_TOOL_SCRIPT_MAX_STDOUT_BYTES = 65_536;

    // Maximum size of the JSON arguments of a single tool call made from an mcp tool script
    public static final int MCP_TOOL_SCRIPT_MAX_ARG_BYTES = 262_144;

    // Maximum size of the JSON result of a single tool call made from an mcp tool script
    public static final int MCP_TOOL_SCRIPT_MAX_RESULT_BYTES = 1_048_576;

    // Maximum number of tool calls one mcp tool script run may make
    public static final int MCP_TOOL_SCRIPT_MAX_CALLS = 500;

    // Maximum nesting depth of the JSON arguments of a tool call made from an mcp tool script
    public static final int MCP_TOOL_SCRIPT_MAX_JSON_DEPTH = 64;

    // Maximum size of a single get_all_tools_callable_from_mcp_tool_script response
    public static final int CALLABLE_TOOL_LIST_MAX_RESULT_BYTES = 131_072;

    // Maximum size of a single get_info_of_tools_callable_from_mcp_tool_script response
    public static final int CALLABLE_TOOL_INFO_MAX_RESULT_BYTES = 57_344;

    // Maximum number of tool functions one get_info_of_tools_callable_from_mcp_tool_script call may ask about
    public static final int CALLABLE_TOOL_INFO_MAX_NAMES = 10;

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
