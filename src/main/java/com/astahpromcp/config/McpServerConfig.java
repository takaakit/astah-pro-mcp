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

    // ----------
    // Connection and ports

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

    // ----------
    // Transport and client approval

    // Jetty server idle timeout
    public static final int JETTY_IDLE_TIMEOUT_MS = 300000; // 5min

    // Interval at which the MCP transport pings each session over its listening SSE stream.
    // Kept well below JETTY_IDLE_TIMEOUT_MS.
    public static final long TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS = 30;

    // Maximum time an initialize request waits for the approval dialog of another initialize request on the same port to be answered.
    // Kept below JETTY_IDLE_TIMEOUT_MS so that the rejection still reaches a client whose connection has not been dropped yet.
    public static final long APPROVAL_DIALOG_WAIT_TIMEOUT_SECONDS = 180;

    // Grace period after a user approval during which further initialize requests from the same User-Agent are approved automatically without a dialog.
    public static final long APPROVAL_GRACE_PERIOD_MS = 30_000;

    // Maximum size of a single inbound MCP request body, in bytes.
    public static final int MCP_MAX_REQUEST_SIZE_BYTES = 16 * 1024 * 1024;

    // Number of Jetty acceptor threads
    // Controls how many concurrent connections can be accepted.
    public static final int JETTY_ACCEPTOR_THREADS = 2;

    // Number of Jetty selector threads
    // Used to process non-blocking I/O operations.
    public static final int JETTY_SELECTOR_THREADS = 4;

    // Minimum number of Jetty worker threads
    // Threads kept alive even when idle.
    public static final int JETTY_MIN_THREADS = 10;

    // Maximum number of Jetty worker threads
    // Controls how many requests can be processed in parallel.
    public static final int JETTY_MAX_THREADS = 200;

    // ----------
    // Astah execution and image fetching

    // Maximum time a tool call waits to acquire exclusive access to the Astah API
    public static final long ASTAH_API_LOCK_TIMEOUT_SECONDS = 30;

    // Maximum time to wait for the EDT queue to drain after a tool execution.
    public static final long EDT_FLUSH_TIMEOUT_SECONDS = 20;

    // Maximum time one run_astah_api_script (astah api script) run may execute.
    // Kept below the tool call timeout of typical MCP clients (around 30 seconds) so that the timeout response actually reaches the AI agent instead of the client giving up first.
    public static final long ASTAH_API_SCRIPT_TIMEOUT_SECONDS = 20;

    // Maximum amount of print() output, and of error output, kept from one astah api script run.
    public static final int ASTAH_API_SCRIPT_MAX_STDOUT_BYTES = 65_536;

    // Maximum time one run_mcp_tool_script (mcp tool script) run may execute
    public static final long MCP_TOOL_SCRIPT_TIMEOUT_SECONDS = 20;

    // Maximum time to fetch an image from a URL, as a whole.
    // Kept below ASTAH_API_LOCK_TIMEOUT_SECONDS, because the fetch runs while holding the Astah API lock.
    // Also kept below MCP_TOOL_SCRIPT_TIMEOUT_SECONDS in case the tools that fetch images are made callable from an mcp tool script again.
    public static final long IMAGE_URL_FETCH_TIMEOUT_SECONDS = 10;

    // Maximum number of pixels of an image fetched from a URL (about 200 MB once decoded as ARGB).
    public static final long IMAGE_URL_MAX_PIXELS = 50_000_000L;

    // Maximum number of pixels an SVG is rasterized to (64 MB as ARGB).
    public static final long SVG_MAX_RASTER_PIXELS = 4096L * 4096L;

    // Maximum width and height of an SVG inserted into the project.
    public static final int SVG_MAX_DISPLAY_DIMENSION = 4096;

    // ----------
    // MCP tool script limits and callable-tool discovery

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

    // Maximum size of the tool function entries in one callable-tool chunk
    public static final int CALLABLE_TOOL_CHUNK_MAX_BYTES = 24_576;

    // Maximum size of a single get_info_of_tools_callable_from_mcp_tool_script response
    public static final int CALLABLE_TOOL_INFO_MAX_RESULT_BYTES = 57_344;

    // Maximum number of tool functions one get_info_of_tools_callable_from_mcp_tool_script call may ask about
    public static final int CALLABLE_TOOL_INFO_MAX_NAMES = 10;

    // ----------
    // Knowledge document fetching

    // Maximum time to establish a connection when fetching knowledge documents from the web.
    public static final long KNOWLEDGE_FETCH_CONNECT_TIMEOUT_SECONDS = 10;

    // Maximum time to wait for a response when fetching knowledge documents from the web.
    public static final long KNOWLEDGE_FETCH_REQUEST_TIMEOUT_SECONDS = 30;

    // ----------
    // Logging

    // Environment variable that sets the lowest level the log panel of the mcp tab shows, such as DEBUG.
    public static final String ENV_LOG_PANEL_LEVEL = "ASTAH_PRO_MCP_LOG_PANEL_LEVEL";

    // ----------
    // Project comparison

    // Maximum number of comparisons being prepared or running at once
    public static final int PROJECT_DIFF_MAX_JOBS_IN_PROGRESS = 2;

    // Maximum number of comparisons kept, including finished ones whose results are still kept
    public static final int PROJECT_DIFF_MAX_STORED_JOBS = 100;

    // Number of comparisons the background runs at the same time
    public static final int PROJECT_DIFF_CONCURRENT_JOBS = 1;

    // Maximum size of a project file to compare
    public static final long PROJECT_DIFF_MAX_PROJECT_FILE_BYTES = 64L * 1024 * 1024;

    // Maximum time to copy one project file
    public static final long PROJECT_DIFF_COPY_TIMEOUT_SECONDS = 10;

    // Maximum time to wait, after writing the open project's XMI, for the EDT to finish what was queued meanwhile
    public static final long PROJECT_DIFF_CAPTURE_EDT_WAIT_SECONDS = 10;

    // Maximum size of an XMI to compare
    public static final long PROJECT_DIFF_MAX_XMI_BYTES = 128L * 1024 * 1024;

    // Maximum time a separate process may take to write the XMI of a project file
    public static final long PROJECT_DIFF_EXPORT_TIMEOUT_SECONDS = 180;

    // Maximum heap of a separate process that writes an XMI
    public static final String PROJECT_DIFF_CHILD_MAX_HEAP = "1024m";

    // Maximum size of the log of a separate process
    public static final long PROJECT_DIFF_CHILD_LOG_MAX_BYTES = 1024L * 1024;

    // Maximum time get_proj_diff_result waits for a comparison to finish
    public static final long PROJECT_DIFF_RESULT_WAIT_SECONDS = 20;

    // Maximum size, as UTF-8 JSON, of one page of the result get_proj_diff_result answers with. A change too large for a page is split across pages by its properties.
    public static final int PROJECT_DIFF_MAX_RESPONSE_BYTES = 32 * 1024;

    // Maximum length of one value in the answer of get_proj_diff_result
    public static final int PROJECT_DIFF_MAX_RESPONSE_VALUE_CHARS = 2_000;

    // Maximum length of one value in a part of a change in a page of get_proj_diff_result when not even one of its properties fits in the page with its values cut to PROJECT_DIFF_MAX_RESPONSE_VALUE_CHARS
    public static final int PROJECT_DIFF_LAST_RESORT_VALUE_CHARS = 100;

    // Disk space one comparison may use
    public static final long PROJECT_DIFF_MAX_JOB_BYTES = 512L * 1024 * 1024;

    // Disk space all comparisons together may use
    public static final long PROJECT_DIFF_MAX_TOTAL_BYTES = 2L * 1024 * 1024 * 1024;

    // How long a result is kept at least after the comparison ends
    public static final long PROJECT_DIFF_MINIMUM_RETENTION_MINUTES = 60;

    // How long a result is kept at least after it was last returned, so that the file it names can still be read
    public static final long PROJECT_DIFF_READ_GRACE_MINUTES = 10;

    // Maximum time stopping the server waits for comparisons to stop
    public static final long PROJECT_DIFF_STOP_WAIT_SECONDS = 15;

    // The XMI versions ("exporterVersion/currentModelVersion") the normalization rules of the comparison were checked against
    public static final Set<String> PROJECT_DIFF_VERIFIED_XMI_VERSIONS = Set.of("1.2.0.44/44");

    // ----------
    // Output directories

    // Root directory where generated output is stored.
    public static final Path ROOT_OUTPUT_DIR = Paths.get(System.getProperty("user.home"), ".astah-pro-mcp");

    public static final String WORKSPACE_DIR_NAME_PREFIX = "workspace-";

    // Temporary workspace directory
    public static final Path WORKSPACE_DIR = ROOT_OUTPUT_DIR.resolve(WORKSPACE_DIR_NAME_PREFIX + ProcessHandle.current().pid());
}
