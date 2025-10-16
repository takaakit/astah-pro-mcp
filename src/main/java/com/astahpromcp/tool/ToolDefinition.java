package com.astahpromcp.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.function.BiFunction;

// A record to encapsulate a tool's schema and its handler function
public record ToolDefinition(
        McpSchema.Tool toolSchema,
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> toolHandler
) {
}
