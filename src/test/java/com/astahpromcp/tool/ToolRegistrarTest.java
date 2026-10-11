package com.astahpromcp.tool;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class ToolRegistrarTest {

    private ListAppender<ILoggingEvent> captured;
    private Level originalLevel;

    @BeforeEach
    void attachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(ToolRegistrar.class);
        originalLevel = logger.getLevel();
        logger.setLevel(Level.INFO);
        captured = new ListAppender<>();
        captured.start();
        logger.addAppender(captured);
    }

    @AfterEach
    void detachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(ToolRegistrar.class);
        logger.detachAppender(captured);
        logger.setLevel(originalLevel);
        captured.stop();
    }

    private static McpSchema.CallToolRequest request() {
        return new McpSchema.CallToolRequest("get_class_info", Map.of(), null);
    }

    @Test
    void withCallSummary_ok_returnsTheResultAndLogsOneInfoLine() {
        McpSchema.CallToolResult expected = ResponseSupport.success(Map.of("name", "Sample"));
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler =
                ToolRegistrar.withCallSummary("get_class_info", (exchange, request) -> expected);

        assertSame(expected, handler.apply(null, request()), "The wrapper must hand back the handler's own result");

        assertEquals(1, captured.list.size(), "One call must leave exactly one line");
        ILoggingEvent event = captured.list.get(0);
        assertEquals(Level.INFO, event.getLevel());
        assertTrue(event.getFormattedMessage().matches("Tool call get_class_info: ok \\(\\d+ ms\\)"),
                "Unexpected summary line: " + event.getFormattedMessage());
    }

    @Test
    void withCallSummary_ok_reportsAnErrorResultAsError() {
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler =
                ToolRegistrar.withCallSummary("get_class_info", (exchange, request) -> ResponseSupport.error("boom"));

        assertTrue(handler.apply(null, request()).isError());

        assertEquals(1, captured.list.size());
        assertTrue(captured.list.get(0).getFormattedMessage().startsWith("Tool call get_class_info: error ("),
                "Unexpected summary line: " + captured.list.get(0).getFormattedMessage());
    }

    @Test
    void withCallSummary_ok_reportsAResultWhoseOkIsFalseAsOkFalse() {
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler =
                ToolRegistrar.withCallSummary("run_mcp_tool_script", (exchange, request) -> ResponseSupport.success(Map.of("ok", false)));

        assertFalse(handler.apply(null, request()).isError());

        assertEquals(1, captured.list.size());
        assertTrue(captured.list.get(0).getFormattedMessage().startsWith("Tool call run_mcp_tool_script: ok=false ("),
                "Unexpected summary line: " + captured.list.get(0).getFormattedMessage());
    }

    @Test
    void withCallSummary_ng_letsAThrownExceptionThroughAndReportsTheCallAsFailed() {
        IllegalStateException thrown = new IllegalStateException("boom");
        BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler =
                ToolRegistrar.withCallSummary("get_class_info", (exchange, request) -> {
                    throw thrown;
                });

        assertSame(thrown, assertThrows(IllegalStateException.class, () -> handler.apply(null, request())));

        assertEquals(1, captured.list.size());
        assertTrue(captured.list.get(0).getFormattedMessage().startsWith("Tool call get_class_info: failed ("),
                "Unexpected summary line: " + captured.list.get(0).getFormattedMessage());
    }
}
