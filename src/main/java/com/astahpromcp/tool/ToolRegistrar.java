package com.astahpromcp.tool;

import io.modelcontextprotocol.server.McpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

@Slf4j
public final class ToolRegistrar {

    private ToolRegistrar() {
    }

    // Register all tools with the MCP server builder
    public static void registerAll(McpServer.SyncSpecification<?> builder,
                                   Collection<ToolProvider> providers) {
        
        int totalTools = 0;
        for (ToolProvider provider : providers) {
            log.info("Tool provider: {}", provider.getClass().getSimpleName());
            try {
                List<ToolDefinition> definitions = provider.createToolDefinitions();
                
                if (definitions == null || definitions.isEmpty()) {
                    log.warn("Tool provider {} returned no tool definitions", provider.getClass().getSimpleName());
                    continue;
                }
                
                for (ToolDefinition definition : definitions) {
                    log.info("Registering tool: {}", definition.toolSchema().name());
                    builder.toolCall(definition.toolSchema(), definition.toolHandler());
                    totalTools++;
                }
            } catch (Exception e) {
                log.error("Error registering tools from provider: {}", provider.getClass().getSimpleName(), e);
            }
        }
        log.info("Total tool count: {}", totalTools);
    }
}
