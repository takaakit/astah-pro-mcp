package com.astahpromcp.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Check relationships, not individual default values.
public class McpServerConfigTest {

    @Test
    void approvalDialogWaitTimeout_ok_shorterThanJettyIdleTimeout() {
        Duration approvalWait = Duration.ofSeconds(McpServerConfig.APPROVAL_DIALOG_WAIT_TIMEOUT_SECONDS);
        Duration idleTimeout = Duration.ofMillis(McpServerConfig.JETTY_IDLE_TIMEOUT_MS);

        assertTrue(approvalWait.compareTo(idleTimeout) < 0,
                () -> "APPROVAL_DIALOG_WAIT_TIMEOUT_SECONDS (%d s) must be shorter than JETTY_IDLE_TIMEOUT_MS (%d ms) so a queued initialize request can be rejected before its connection becomes idle."
                        .formatted(McpServerConfig.APPROVAL_DIALOG_WAIT_TIMEOUT_SECONDS, McpServerConfig.JETTY_IDLE_TIMEOUT_MS));
    }

    @Test
    void transportKeepAliveInterval_ok_shorterThanJettyIdleTimeout() {
        Duration keepAliveInterval = Duration.ofSeconds(McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS);
        Duration idleTimeout = Duration.ofMillis(McpServerConfig.JETTY_IDLE_TIMEOUT_MS);

        // This checks the ordering; it does not prescribe a ratio or guarantee scheduling headroom.
        assertTrue(keepAliveInterval.compareTo(idleTimeout) < 0,
                () -> "TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS (%d s) must be shorter than JETTY_IDLE_TIMEOUT_MS (%d ms) so a listening SSE stream can be pinged before its connection becomes idle."
                        .formatted(McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS, McpServerConfig.JETTY_IDLE_TIMEOUT_MS));
    }

    @Test
    void imageUrlFetchTimeout_ok_shorterThanMcpToolScriptTimeout() {
        Duration imageFetchTimeout = Duration.ofSeconds(McpServerConfig.IMAGE_URL_FETCH_TIMEOUT_SECONDS);
        Duration scriptTimeout = Duration.ofSeconds(McpServerConfig.MCP_TOOL_SCRIPT_TIMEOUT_SECONDS);

        // No mcp tool script can call the tools that fetch images today, because the tool manifest (tools.tsv) publishes them directly.
        // This guards the case where the manifest makes them callable from a script again, a one-cell change that nothing else ties to this timeout.
        assertTrue(imageFetchTimeout.compareTo(scriptTimeout) < 0,
                () -> "IMAGE_URL_FETCH_TIMEOUT_SECONDS (%d s) must be shorter than MCP_TOOL_SCRIPT_TIMEOUT_SECONDS (%d s) so that, if the tools that fetch images are made callable from an mcp tool script, the script still has time to report a failed fetch."
                        .formatted(McpServerConfig.IMAGE_URL_FETCH_TIMEOUT_SECONDS, McpServerConfig.MCP_TOOL_SCRIPT_TIMEOUT_SECONDS));
    }

    @Test
    void imageUrlFetchTimeout_ok_shorterThanAstahApiLockTimeout() {
        Duration imageFetchTimeout = Duration.ofSeconds(McpServerConfig.IMAGE_URL_FETCH_TIMEOUT_SECONDS);
        Duration lockWaitTimeout = Duration.ofSeconds(McpServerConfig.ASTAH_API_LOCK_TIMEOUT_SECONDS);

        // The lock timeout bounds acquisition waits, not lock holding. This is a contention policy,
        // not a guarantee about total holding time, which also includes other work and EDT flushing.
        assertTrue(imageFetchTimeout.compareTo(lockWaitTimeout) < 0,
                () -> "IMAGE_URL_FETCH_TIMEOUT_SECONDS (%d s) must be shorter than ASTAH_API_LOCK_TIMEOUT_SECONDS (%d s) to limit the impact of image fetching on callers waiting for the Astah API lock."
                        .formatted(McpServerConfig.IMAGE_URL_FETCH_TIMEOUT_SECONDS, McpServerConfig.ASTAH_API_LOCK_TIMEOUT_SECONDS));
    }

    @Test
    void jettyMinThreads_ok_notAboveJettyMaxThreads() {
        assertTrue(McpServerConfig.JETTY_MIN_THREADS <= McpServerConfig.JETTY_MAX_THREADS,
                () -> "JETTY_MIN_THREADS (%d) must not exceed JETTY_MAX_THREADS (%d) so the worker thread pool has a consistent range."
                        .formatted(McpServerConfig.JETTY_MIN_THREADS, McpServerConfig.JETTY_MAX_THREADS));
    }
}
