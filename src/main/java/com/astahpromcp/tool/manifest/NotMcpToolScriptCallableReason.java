package com.astahpromcp.tool.manifest;

// Why a tool function cannot be called from 'run_mcp_tool_script'.
public enum NotMcpToolScriptCallableReason {

    RETURNS_BINARY_CONTENT,
    RUNS_AN_ASTAH_API_SCRIPT,
    PERFORMS_BLOCKING_IO,
    PUBLISHED_DIRECTLY;

    public String message(String toolName) {
        return switch (this) {
            case RETURNS_BINARY_CONTENT -> "'" + toolName + "' cannot be called from an mcp tool script because it returns binary content such as an image, which a script has no way to receive. Call '" + toolName + "' directly as an MCP tool instead.";
            case RUNS_AN_ASTAH_API_SCRIPT -> "'" + toolName + "' cannot be called from an mcp tool script. If you need the raw Astah API, call 'run_astah_api_script' directly as an MCP tool instead.";
            case PERFORMS_BLOCKING_IO -> "'" + toolName + "' cannot be called from an mcp tool script because it may fetch over the network or convert a PDF, which would hold Astah for a long time. Call '" + toolName + "' directly as an MCP tool instead.";
            case PUBLISHED_DIRECTLY -> "'" + toolName + "' cannot be called from an mcp tool script because this server publishes it directly, and a tool function is never reachable both ways. Call '" + toolName + "' directly as an MCP tool instead.";
        };
    }

    // The message an mcp tool script receives when a handler answers without structured content.
    public static String missingStructuredContentMessage(String toolName) {
        return "'" + toolName + "' returned no structured content, so it cannot be called from an mcp tool script. Call '" + toolName + "' directly as an MCP tool instead.";
    }
}
