package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ResponseSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    @AfterEach
    void tearDown() {
        AstahApiLock.clearSuspension();
    }

    @Test
    void createToolDefinitions_ok_serializesConcurrentToolCalls() throws Exception {
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
    void createToolDefinitions_ng_returnsErrorWhenLockNotAcquiredWithinTimeout() throws Exception {
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
    void createToolDefinitions_ng_releasesLockWhenHandlerThrows() {
        ToolDefinition wrapped = wrap(definitionWith((exchange, request) -> {
            throw new IllegalStateException("handler failure");
        }), 30);

        McpSchema.CallToolResult result = wrapped.toolHandler().apply(null, null);

        assertTrue(result.isError(), "Result should be an error when the handler throws");
        assertTrue(firstContentText(result).contains("handler failure"), "Error message should include the handler's failure reason");
        assertFalse(AstahApiLock.LOCK.isLocked(), "Lock should be released even when the handler throws");
    }

    @Test
    void createToolDefinitions_ng_refusesToolCallsWhileAnAbandonedThreadIsAlive() throws Exception {
        // Stand in for a timed-out script that is still running and may be touching the Astah API
        CountDownLatch stopAbandoned = new CountDownLatch(1);
        Thread abandoned = new Thread(() -> {
            try {
                stopAbandoned.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "abandoned-thread");
        abandoned.start();
        AstahApiLock.suspend(abandoned, "a timed-out script did not stop");

        try {
            ToolDefinition wrapped = wrap(definitionWith((exchange, request) -> {
                throw new AssertionError("The tool handler must not run while Astah API access is suspended");
            }), 30);

            McpSchema.CallToolResult result = wrapped.toolHandler().apply(null, null);

            assertTrue(result.isError(), "Result should be an error while Astah API access is suspended");
            assertTrue(firstContentText(result).contains("a timed-out script did not stop"),
                    "Error message should carry the suspension reason");
            assertFalse(AstahApiLock.LOCK.isLocked(), "Lock should be released even when the call is refused");

        } finally {
            stopAbandoned.countDown();
            abandoned.join();
        }
    }

    @Test
    void createToolDefinitions_ng_tellsTheClientToRestartAstahWhenTheInterruptIsIgnored() throws Exception {
        // Stand in for a computation loop: it never reaches an interruptible point, so the interrupt flag stays set and the thread keeps running
        AtomicBoolean keepSpinning = new AtomicBoolean(true);
        Thread abandoned = new Thread(() -> {
            while (keepSpinning.get()) {
                Thread.onSpinWait();
            }
        }, "abandoned-thread");
        abandoned.start();
        abandoned.interrupt();
        AstahApiLock.suspend(abandoned, "a script that timed out after 20 seconds is still running");

        try {
            // Outlast the grace period that guards against reporting a verdict too early
            Thread.sleep(1200);

            ToolDefinition wrapped = wrap(definitionWith((exchange, request) -> {
                throw new AssertionError("The tool handler must not run while Astah API access is suspended");
            }), 30);

            McpSchema.CallToolResult result = wrapped.toolHandler().apply(null, null);
            String message = firstContentText(result);

            assertTrue(result.isError(), "Result should be an error while Astah API access is suspended");
            assertTrue(message.contains("ignored its interrupt"),
                    "Error message should report that the interrupt was ignored: " + message);
            assertTrue(message.contains("restart Astah"),
                    "Error message should tell the client that waiting will not help: " + message);

        } finally {
            keepSpinning.set(false);
            abandoned.join();
        }
    }

    @Test
    void createToolDefinitions_ok_resumesAfterTheAbandonedThreadTerminates() throws Exception {
        // A suspension is derived from the abandoned thread's liveness, so a thread that has already finished must not block tool calls
        Thread abandoned = new Thread(() -> {
        }, "abandoned-thread");
        abandoned.start();
        abandoned.join();
        AstahApiLock.suspend(abandoned, "a timed-out script that has since stopped");

        ToolDefinition wrapped = wrap(definitionWith((exchange, request) ->
                ResponseSupport.success(List.of(McpSchema.TextContent.builder("ok").build()))), 30);

        McpSchema.CallToolResult result = wrapped.toolHandler().apply(null, null);

        assertFalse(result.isError(), "Tool calls should resume once the abandoned thread has terminated");
        assertFalse(AstahApiLock.LOCK.isLocked(), "Lock should be released after the call");
    }

    @Test
    void createToolDefinitions_ok_preservesToolSchemaAndDelegateName() {
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
