package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ResponseSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class ExclusiveToolProviderTest {

    private static final McpSchema.Tool DUMMY_SCHEMA = McpSchema.Tool.builder(
                    "dummy_tool",
                    JsonSupport.MCP_JSON_MAPPER,
                    "{\"type\":\"object\"}")
            .description("Dummy tool for testing")
            .build();

    private static ToolDefinition definitionWith(
            BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler) {
        return new ToolDefinition(DUMMY_SCHEMA, handler);
    }

    private static ToolDefinition wrap(ToolDefinition definition, long lockTimeoutSeconds) {
        ToolProvider delegate = () -> List.of(definition);
        return new ExclusiveToolProvider(delegate, lockTimeoutSeconds).createToolDefinitions().get(0);
    }

    private static String firstContentText(McpSchema.CallToolResult result) {
        return ((McpSchema.TextContent) result.content().get(0)).text();
    }

    @Test
    void serializesConcurrentToolCalls() throws Exception {
        int threadCount = 8;
        AtomicInteger activeCalls = new AtomicInteger();
        AtomicInteger maxObservedConcurrency = new AtomicInteger();
        AtomicInteger completedCalls = new AtomicInteger();

        ToolDefinition wrapped = wrap(definitionWith((exchange, request) -> {
            int active = activeCalls.incrementAndGet();
            maxObservedConcurrency.accumulateAndGet(active, Math::max);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            activeCalls.decrementAndGet();
            completedCalls.incrementAndGet();
            return ResponseSupport.success(List.of(McpSchema.TextContent.builder("ok").build()));
        }), 30);

        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    startSignal.await();
                    return wrapped.toolHandler().apply(null, null);
                });
            }
            startSignal.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS), "Tool calls did not finish in time");
        } finally {
            executor.shutdownNow();
        }

        assertEquals(threadCount, completedCalls.get(), "All tool calls should complete");
        assertEquals(1, maxObservedConcurrency.get(), "Tool calls should never run concurrently");
        assertFalse(AstahApiLock.LOCK.isLocked(), "Lock should be released after all calls");
    }

    @Test
    void returnsErrorWhenLockNotAcquiredWithinTimeout() throws Exception {
        ToolDefinition wrapped = wrap(definitionWith((exchange, request) ->
                ResponseSupport.error("should not be reached")), 1);

        // Hold the lock on this thread, then invoke the handler on another thread
        // so that its tryLock cannot succeed via reentrancy.
        AstahApiLock.LOCK.lock();
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                McpSchema.CallToolResult result = executor
                        .submit(() -> wrapped.toolHandler().apply(null, null))
                        .get(30, TimeUnit.SECONDS);

                assertTrue(result.isError(), "Result should be an error when the lock is unavailable");
                assertTrue(firstContentText(result).contains("busy"),
                        "Error message should tell the caller that Astah is busy");
            } finally {
                executor.shutdownNow();
            }
        } finally {
            AstahApiLock.LOCK.unlock();
        }
    }

    @Test
    void releasesLockWhenHandlerThrows() {
        ToolDefinition wrapped = wrap(definitionWith((exchange, request) -> {
            throw new IllegalStateException("handler failure");
        }), 30);

        McpSchema.CallToolResult result = wrapped.toolHandler().apply(null, null);

        assertTrue(result.isError(), "Result should be an error when the handler throws");
        assertTrue(firstContentText(result).contains("handler failure"), "Error message should include the handler's failure reason");
        assertFalse(AstahApiLock.LOCK.isLocked(), "Lock should be released even when the handler throws");
    }

    @Test
    void preservesToolSchemaAndDelegateName() {
        ToolDefinition definition = definitionWith((exchange, request) -> ResponseSupport.error("dummy"));
        ToolProvider delegate = new ToolProvider() {
            @Override
            public List<ToolDefinition> createToolDefinitions() {
                return List.of(definition);
            }
        };
        ExclusiveToolProvider provider = new ExclusiveToolProvider(delegate);

        List<ToolDefinition> wrapped = provider.createToolDefinitions();
        assertEquals(1, wrapped.size());
        assertSame(definition.toolSchema(), wrapped.get(0).toolSchema(), "Tool schema should be preserved as is");
        assertNotSame(definition.toolHandler(), wrapped.get(0).toolHandler(), "Tool handler should be wrapped");
        assertEquals(delegate.name(), provider.name(), "Provider name should be delegated for logging");
    }
}
