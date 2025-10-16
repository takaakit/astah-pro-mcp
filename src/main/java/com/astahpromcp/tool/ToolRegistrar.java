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
            List<ToolDefinition> definitions = provider.createToolDefinitions();
            
            for (ToolDefinition definition : definitions) {
                log.info("Tool name: {}", definition.toolSchema().name());
                builder.toolCall(definition.toolSchema(), definition.toolHandler());
                totalTools++;
            }
        }
        log.info("Total tool count: {}", totalTools);
    }
}
