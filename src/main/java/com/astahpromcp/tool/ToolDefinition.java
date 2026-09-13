package com.astahpromcp.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.function.BiFunction;

// A record to encapsulate a tool's schema and its handler function
public record ToolDefinition(
        McpSchema.Tool toolSchema,
        ResultKind resultKind,
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> toolHandler
) {

    // What kind of result the handler answers with.
    public enum ResultKind {
        DTO,
        DTO_AND_CONTENTS,
        CONTENTS;

        // Whether a result of this kind carries structured content for an mcp tool script to receive
        public boolean carriesStructuredContent() {
            return this != CONTENTS;
        }
    }
}
