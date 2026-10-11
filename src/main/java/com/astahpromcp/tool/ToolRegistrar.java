package com.astahpromcp.tool;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

@Slf4j
public final class ToolRegistrar {

    private ToolRegistrar() {
    }

    // Register all tools with the MCP server builder
    public static void registerAll(McpServer.SyncSpecification<?> builder,
                                   Collection<ToolProvider> providers) {

        int totalTools = 0;
        for (ToolProvider provider : providers) {
            log.info("Tool provider: {}", provider.name());
            List<ToolDefinition> definitions;
            try {
                definitions = provider.createToolDefinitions();
            } catch (Exception e) {
                throw new IllegalStateException("Tool provider " + provider.name() + " failed to build its tool definitions", e);
            }

            if (definitions == null || definitions.isEmpty()) {
                throw new IllegalStateException("Tool provider " + provider.name() + " returned no tool definitions");
            }

            for (ToolDefinition definition : definitions) {
                log.info("Registering tool: {}", definition.toolSchema().name());
                try {
                    builder.toolCall(definition.toolSchema(), withCallSummary(definition.toolSchema().name(), definition.toolHandler()));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to register tool " + definition.toolSchema().name() + " of provider " + provider.name(), e);
                }
                totalTools++;
            }
        }
        log.info("Total tool count: {}", totalTools);
    }

    static BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> withCallSummary(
            String toolName,
            BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler) {

        return (exchange, request) -> {
            long start = System.nanoTime();
            McpSchema.CallToolResult result = null;
            try {
                result = handler.apply(exchange, request);
                return result;
            } finally {
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                ToolCallStatistics.record(ToolCallStatistics.Route.DIRECT, toolName, result);
                log.info("Tool call {}: {} ({} ms)", toolName, outcomeOf(result), elapsedMs);
            }
        };
    }

    // How a call ended, for the summary line. A handler that threw leaves no result.
    static String outcomeOf(McpSchema.CallToolResult result) {
        if (result == null) {
            return "failed";
        } else if (Boolean.TRUE.equals(result.isError())) {
            return "error";
        } else if (result.structuredContent() instanceof Map<?, ?> content && Boolean.FALSE.equals(content.get("ok"))) {
            // The script tools report a failed script this way rather than as an error result
            return "ok=false";
        } else {
            return "ok";
        }
    }
}
