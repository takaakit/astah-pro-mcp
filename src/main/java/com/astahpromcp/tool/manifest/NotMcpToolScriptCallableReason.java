package com.astahpromcp.tool.manifest;

import java.util.ArrayList;
import java.util.List;

// Why a tool function cannot be called from 'run_mcp_tool_script'.
public enum NotMcpToolScriptCallableReason {

    RETURNS_NO_STRUCTURED_CONTENT("no_structured_content"),
    PERFORMS_BLOCKING_IO("network"),
    MANAGES_A_PROJECT_FILE("project_file"),
    DRIVES_THE_VIEW("view"),
    ANSWERS_A_STANDALONE_QUESTION("standalone"),
    STARTS_OR_AWAITS_A_BACKGROUND_JOB("background_job"),
    RUNS_AN_ASTAH_API_SCRIPT("astah_api_script"),
    PUBLISHED_DIRECTLY("direct");

    // What the manifest writes in its 'reason' column
    private final String code;

    NotMcpToolScriptCallableReason(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    // The reason whose code is exactly the given text, or null when no reason has it
    static NotMcpToolScriptCallableReason ofCode(String code) {
        for (NotMcpToolScriptCallableReason reason : values()) {
            if (reason.code.equals(code)) {
                return reason;
            }
        }
        return null;
    }

    static List<String> codes() {
        List<String> codes = new ArrayList<>();
        for (NotMcpToolScriptCallableReason reason : values()) {
            codes.add(reason.code);
        }
        return codes;
    }

    public String message(String toolName) {
        return switch (this) {
            case RETURNS_NO_STRUCTURED_CONTENT -> "'" + toolName + "' cannot be called from an mcp tool script because it answers with content such as an image or text instead of structured content, and a script receives structured content only. Call '" + toolName + "' directly as an MCP tool instead.";
            case PERFORMS_BLOCKING_IO -> "'" + toolName + "' cannot be called from an mcp tool script because it may fetch over the network or convert a PDF, which would hold Astah for a long time. Call '" + toolName + "' directly as an MCP tool instead.";
            case MANAGES_A_PROJECT_FILE -> "'" + toolName + "' cannot be called from an mcp tool script because it creates, opens or saves a project, which the transaction of the run cannot roll back. Call '" + toolName + "' directly as an MCP tool instead.";
            case DRIVES_THE_VIEW -> "'" + toolName + "' cannot be called from an mcp tool script because it drives the Astah view (opening a diagram, selection, zoom), which the transaction of the run cannot roll back. Call '" + toolName + "' directly as an MCP tool instead.";
            case ANSWERS_A_STANDALONE_QUESTION -> "'" + toolName + "' cannot be called from an mcp tool script because it answers a standalone question about the project, the Astah view, the MCP server or the logs, which is why this server publishes it directly, and a tool function is never reachable both ways. Call '" + toolName + "' directly as an MCP tool instead.";
            case STARTS_OR_AWAITS_A_BACKGROUND_JOB -> "'" + toolName + "' cannot be called from an mcp tool script because it starts work in a separate process that outlives the run, or waits for that work to finish, which the transaction of the run cannot roll back. Call '" + toolName + "' directly as an MCP tool instead.";
            case RUNS_AN_ASTAH_API_SCRIPT -> "'" + toolName + "' cannot be called from an mcp tool script. If you need the raw Astah API, call '" + toolName + "' directly as an MCP tool instead.";
            case PUBLISHED_DIRECTLY -> "'" + toolName + "' cannot be called from an mcp tool script because this server publishes it directly, and a tool function is never reachable both ways. Call '" + toolName + "' directly as an MCP tool instead.";
        };
    }

    // How the programmatic profile's instructions name the given tools that have this reason, or null for the one reason the instructions do not single out
    public String instructionPhrase(List<String> toolNames) {
        return switch (this) {
            case RETURNS_NO_STRUCTURED_CONTENT -> "that return an image or other content instead of structured content";
            case PERFORMS_BLOCKING_IO -> "that fetch over the network or convert a PDF";
            case MANAGES_A_PROJECT_FILE -> "that create, open or save a project";
            case DRIVES_THE_VIEW -> "that drive the diagram view (opening and closing a diagram, selection, highlighting, z-order)";
            case ANSWERS_A_STANDALONE_QUESTION -> "that answer a standalone question about the project, the Astah view, the MCP server or the logs";
            case STARTS_OR_AWAITS_A_BACKGROUND_JOB -> "that start a background job or wait for its result";
            case RUNS_AN_ASTAH_API_SCRIPT -> quoted(toolNames) + (toolNames.size() == 1 ? " itself" : " themselves");
            case PUBLISHED_DIRECTLY -> null;
        };
    }

    private static String quoted(List<String> toolNames) {
        List<String> quoted = new ArrayList<>();
        for (String toolName : toolNames) {
            quoted.add("'" + toolName + "'");
        }
        return String.join(" and ", quoted);
    }
}
