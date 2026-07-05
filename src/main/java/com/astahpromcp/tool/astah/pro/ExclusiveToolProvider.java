package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ResponseSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
                McpSchema.CallToolResult result = definition.toolHandler().apply(exchange, request);
                flushEdt(toolName);  // Drain the EDT queue
                return result;
            
            } catch (Throwable t) {
                String msg = String.format("Unexpected failure @tool=%s: %s", toolName, t.getMessage());
                log.error(msg, t);
                return ResponseSupport.error(msg);
            
            } finally {
                AstahApiLock.LOCK.unlock();
            }
        };
    }

    private static void flushEdt(String toolName) {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }

        // Wait with a bounded timeout instead of invokeAndWait: if the EDT is blocked
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(latch::countDown);  // no-op: drain the EDT queue before releasing the Astah API lock
        try {
            if (!latch.await(McpServerConfig.EDT_FLUSH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("EDT did not drain within {} seconds; releasing the Astah API lock anyway @tool={}",
                        McpServerConfig.EDT_FLUSH_TIMEOUT_SECONDS, toolName);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while flushing EDT after tool execution @tool={}", toolName);
        }
    }
}
