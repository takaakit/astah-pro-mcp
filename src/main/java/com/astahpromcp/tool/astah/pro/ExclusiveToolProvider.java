package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ResponseSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

// Decorator that wraps every tool handler of a delegate provider so that the handler runs
// while holding the process-wide Astah API lock.
@Slf4j
public final class ExclusiveToolProvider implements ToolProvider {

    private final ToolProvider delegate;
    private final long lockTimeoutSeconds;

    public ExclusiveToolProvider(ToolProvider delegate) {
        this(delegate, McpServerConfig.ASTAH_API_LOCK_TIMEOUT_SECONDS);
    }

    public ExclusiveToolProvider(ToolProvider delegate, long lockTimeoutSeconds) {
        this.delegate = delegate;
        this.lockTimeoutSeconds = lockTimeoutSeconds;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return delegate.createToolDefinitions().stream()
                .map(definition -> new ToolDefinition(
                        definition.toolSchema(),
                        wrapWithLock(definition)))
                .toList();
    }

    // Wrap a tool handler so that it acquires the Astah API lock before running.
    private BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> wrapWithLock(
            ToolDefinition definition) {

        String toolName = definition.toolSchema().name();

        return (exchange, request) -> {
            boolean acquired;
            try {
                acquired = AstahApiLock.LOCK.tryLock(lockTimeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                String msg = String.format("Interrupted while waiting for Astah API access @tool=%s", toolName);
                log.warn(msg);
                return ResponseSupport.error(msg);
            }

            if (!acquired) {
                String msg = String.format(
                        "Astah is busy processing another agent's request (waited %d seconds) @tool=%s. Please retry shortly.",
                        lockTimeoutSeconds, toolName);
                log.warn(msg);
                return ResponseSupport.error(msg);
            }

            try {
                return definition.toolHandler().apply(exchange, request);
            } finally {
                AstahApiLock.LOCK.unlock();
            }
        };
    }
}
