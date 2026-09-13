package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ResponseSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import com.astahpromcp.tool.ToolProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class McpToolScriptDispatcherTest {

    private static McpSchema.Tool schema(String name) {
        return McpSchema.Tool.builder(name, JsonSupport.MCP_JSON_MAPPER, "{\"type\":\"object\"}")
                .description("Test tool " + name)
                .build();
    }

    private static ToolDefinition definition(
            String name,
            BiFunction<io.modelcontextprotocol.server.McpSyncServerExchange, McpSchema.CallToolRequest, McpSchema.CallToolResult> handler) {
        return new ToolDefinition(schema(name), ToolDefinition.ResultKind.DTO, handler);
    }

    // A tool that echoes the arguments it was given, so a test can check what actually reached the handler
    private static ToolDefinition echoing(String name) {
        return definition(name, (exchange, request) -> ResponseSupport.success(request.arguments()));
    }

    private static AstahToolRegistry registryOf(ToolDefinition... definitions) {
        return AstahToolRegistry.of(List.of(definitions), Map.of());
    }

    // A registry in which the named tool cannot be called from a script, for the given reason
    private static AstahToolRegistry registryExcluding(String name, NotMcpToolScriptCallableReason reason,
                                                       ToolDefinition... definitions) {
        return AstahToolRegistry.of(List.of(definitions), Map.of(name, reason));
    }

    private static Map<String, Object> parse(String json) {
        return JsonSupport.OBJ_MAPPER.readValue(json, Map.class);
    }

    @Test
    void invoke_ok_returnsTheStructuredContentAsJson() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        String json = dispatcher.invoke("get_class_info", "{\"id\":\"abc\"}");

        assertEquals("abc", parse(json).get("id"));
    }

    // The reason JSON is the boundary: handing Nashorn objects to Jackson turns an array into {"0":...,"1":...}.
    @Test
    void invoke_ok_preservesNestedObjectsAndArrays() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("set_points_of_link_prst")));

        String json = dispatcher.invoke("set_points_of_link_prst",
                "{\"targetLinkPresentationId\":\"id-1\",\"drawPoints\":[{\"x\":100,\"y\":100},{\"x\":200,\"y\":200}]}");

        Map<String, Object> echoed = parse(json);
        assertEquals("id-1", echoed.get("targetLinkPresentationId"));

        List<?> points = (List<?>) echoed.get("drawPoints");
        assertEquals(2, points.size(), "An array must stay an array, not become an object keyed by index");
        assertEquals(100, ((Map<?, ?>) points.get(0)).get("x"));
        assertEquals(200, ((Map<?, ?>) points.get(1)).get("y"));
    }

    @Test
    void invoke_ng_rejectsAnUnknownToolName() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> dispatcher.invoke("no_such_tool", "{}"));
        assertTrue(e.getMessage().contains("no_such_tool"), e.getMessage());
    }

    // Refusing after running the handler would apply the tool's effects and still report a failure.
    @Test
    void invoke_ng_refusesExcludedToolsWithoutRunningTheHandler() {
        for (NotMcpToolScriptCallableReason reason : NotMcpToolScriptCallableReason.values()) {
            String name = "excluded_tool";
            AtomicBoolean handlerRan = new AtomicBoolean(false);
            ToolDefinition definition = definition(name, (exchange, request) -> {
                handlerRan.set(true);
                return ResponseSupport.success(Map.of("ok", true));
            });

            McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryExcluding(name, reason, definition));

            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> dispatcher.invoke(name, "{}"), name);
            assertTrue(e.getMessage().contains(name), e.getMessage());
            assertFalse(handlerRan.get(), "The handler of " + name + " must never run");
        }
    }

    @Test
    void invoke_ng_reportsAHandlerErrorAsAnException() {
        ToolDefinition definition = definition("get_class_info",
                (exchange, request) -> ResponseSupport.error("Exception @tool=get_class_info: no such element"));
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(definition));

        RuntimeException e = assertThrows(RuntimeException.class, () -> dispatcher.invoke("get_class_info", "{}"));
        assertTrue(e.getMessage().contains("no such element"), e.getMessage());
    }

    // The runtime guard: a tool that starts answering with contents only must be refused even if its declared result kind was never updated.
    @Test
    void invoke_ng_refusesAHandlerThatReturnsNoStructuredContent() {
        ToolDefinition definition = definition("some_new_image_tool",
                (exchange, request) -> ResponseSupport.success(List.of(new McpSchema.TextContent("image"))));
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(definition));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> dispatcher.invoke("some_new_image_tool", "{}"));
        assertTrue(e.getMessage().contains("structured content"), e.getMessage());
        assertTrue(e.getMessage().contains("directly as an MCP tool"), e.getMessage());
    }

    @Test
    void invoke_ng_rejectsArgumentsAboveTheSizeLimit() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        String oversized = "{\"id\":\"" + "x".repeat(McpServerConfig.MCP_TOOL_SCRIPT_MAX_ARG_BYTES) + "\"}";

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> dispatcher.invoke("get_class_info", oversized));
        assertTrue(e.getMessage().contains("above the limit"), e.getMessage());
        assertFalse(e.getMessage().contains("com.astahpromcp"), "Limit errors must not leak internals: " + e.getMessage());
    }

    @Test
    void invoke_ng_countsEveryAttemptedCall() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryExcluding(
                "capture_dgm_img", NotMcpToolScriptCallableReason.RETURNS_BINARY_CONTENT,
                echoing("get_class_info"), echoing("capture_dgm_img")));

        dispatcher.invoke("get_class_info", "{}");
        dispatcher.invoke("get_class_info", "{}");

        assertThrows(IllegalStateException.class, () -> dispatcher.invoke("capture_dgm_img", "{}"));
        assertEquals(3, dispatcher.firstFailureCallIndex(),
                "Two calls succeeded, so the refused one is the third");

        assertThrows(IllegalArgumentException.class, () -> dispatcher.invoke("no_such_tool", "{}"));
        assertEquals(4, dispatcher.callCount(), "A name that resolves to no tool is still an attempted call");
    }

    @Test
    void invoke_ng_leadsWithTheReasonRatherThanTheOrdinal() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(
                echoing("get_class_info"),
                definition("delete_elem", (exchange, request) -> ResponseSupport.error("boom"))));

        dispatcher.invoke("get_class_info", "{}");

        RuntimeException e = assertThrows(RuntimeException.class, () -> dispatcher.invoke("delete_elem", "{}"));

        assertFalse(e.getMessage().contains("Tool call #"),
                "The ordinal belongs in the result, not in front of the message: " + e.getMessage());
        assertTrue(e.getMessage().startsWith("boom"),
                "The message has to open with what went wrong: " + e.getMessage());
    }

    // The one reason that is not about the tool itself. The message has to send the agent to the tool list rather
    // than leave it thinking the tool is broken, because the tool is right there and works.
    @Test
    void invoke_ng_sendsAToolPublishedDirectlyBackToTheToolList() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryExcluding(
                "zoom", NotMcpToolScriptCallableReason.PUBLISHED_DIRECTLY, echoing("zoom")));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> dispatcher.invoke("zoom", "{}"));

        assertTrue(e.getMessage().contains("publishes it directly"), e.getMessage());
        assertTrue(e.getMessage().contains("directly as an MCP tool"), e.getMessage());
    }

    @Test
    void invoke_ng_stopsAfterTheCallLimit() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        for (int i = 0; i < McpServerConfig.MCP_TOOL_SCRIPT_MAX_CALLS; i++) {
            dispatcher.invoke("get_class_info", "{}");
        }

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> dispatcher.invoke("get_class_info", "{}"));
        assertTrue(e.getMessage().contains("tool function calls"), e.getMessage());
    }

    @Test
    void invoke_ng_rejectsArgumentsNestedTooDeeply() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        int depth = McpServerConfig.MCP_TOOL_SCRIPT_MAX_JSON_DEPTH + 10;
        String json = "{\"a\":".repeat(depth) + "1" + "}".repeat(depth);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> dispatcher.invoke("get_class_info", json));
        assertTrue(e.getMessage().contains("nested"), e.getMessage());
    }

    @Test
    void invoke_ng_rejectsMalformedArguments() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        assertThrows(IllegalArgumentException.class, () -> dispatcher.invoke("get_class_info", "{not json"));
    }

    // Once a run has timed out, its thread must not begin further work: nobody is waiting for the answer any more.
    @Test
    void invoke_ng_refusesEveryCallAfterTheRunWasAbandoned() {
        AtomicInteger calls = new AtomicInteger();
        ToolDefinition definition = definition("get_class_info", (exchange, request) -> {
            calls.incrementAndGet();
            return ResponseSupport.success(Map.of("ok", true));
        });
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(definition));

        dispatcher.invoke("get_class_info", "{}");
        dispatcher.abandon();

        assertThrows(IllegalStateException.class, () -> dispatcher.invoke("get_class_info", "{}"));
        assertEquals(1, calls.get(), "No handler may run once the run was abandoned");
    }

    // Every name is bound, including the ones a script may not call. Leaving those out would make calling one fail with "tools.capture_dgm_img is not a function", which reads as "no such tool" instead of "call it directly".
    @Test
    void toolNamesJson_ok_bindsEveryToolIncludingTheOnesAScriptCannotCall() {
        List<String> excluded = List.of("capture_dgm_img", "run_astah_api_script", "get_info_of_ocl_spec");
        List<ToolDefinition> definitions = new ArrayList<>();
        definitions.add(echoing("get_class_info"));
        Map<String, NotMcpToolScriptCallableReason> reasons = new java.util.LinkedHashMap<>();
        reasons.put("capture_dgm_img", NotMcpToolScriptCallableReason.RETURNS_BINARY_CONTENT);
        reasons.put("run_astah_api_script", NotMcpToolScriptCallableReason.RUNS_AN_ASTAH_API_SCRIPT);
        reasons.put("get_info_of_ocl_spec", NotMcpToolScriptCallableReason.PERFORMS_BLOCKING_IO);
        for (String name : excluded) {
            definitions.add(echoing(name));
        }
        McpToolScriptDispatcher dispatcher =
                new McpToolScriptDispatcher(AstahToolRegistry.of(definitions, reasons));

        List<?> names = JsonSupport.OBJ_MAPPER.readValue(dispatcher.toolNamesJson(), List.class);

        assertTrue(names.contains("get_class_info"));
        for (String name : excluded) {
            assertTrue(names.contains(name),
                    "'" + name + "' must be bound so that calling it explains how to reach it");
        }
    }

    // On the direct profile the lock wrapper drains the EDT after every single tool call. Calling the unwrapped
    // handler skips that, so the dispatcher has to do it, or a script could read back a value before Astah had
    // finished applying the edit that produced it.
    @Test
    void invoke_ok_drainsTheEventDispatchThreadAfterEveryCall() {
        AtomicReference<AtomicBoolean> queuedWorkDone = new AtomicReference<>();

        ToolDefinition definition = definition("set_name_of_named_element", (exchange, request) -> {
            AtomicBoolean done = new AtomicBoolean(false);
            queuedWorkDone.set(done);
            // Stands in for the work Astah queues on the EDT while applying an edit
            SwingUtilities.invokeLater(() -> done.set(true));
            return ResponseSupport.success(Map.of("ok", true));
        });
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(definition));

        for (int i = 0; i < 3; i++) {
            dispatcher.invoke("set_name_of_named_element", "{}");
            assertTrue(queuedWorkDone.get().get(),
                    "Work queued on the EDT by call " + (i + 1) + " must have run before the call returns");
        }
    }

    @Test
    void invoke_ok_treatsMissingArgumentsAsAnEmptyObject() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_project_info")));

        assertEquals(Map.of(), parse(dispatcher.invoke("get_project_info", null)));
        assertEquals(Map.of(), parse(dispatcher.invoke("get_project_info", "{}")));
    }

    @Test
    void invoke_ng_rejectsAResultAboveTheSizeLimit() {
        ToolDefinition definition = definition("get_chunk_of_all_definitions", (exchange, request) -> {
            Map<String, Object> huge = new LinkedHashMap<>();
            huge.put("text", "x".repeat(McpServerConfig.MCP_TOOL_SCRIPT_MAX_RESULT_BYTES + 1));
            return ResponseSupport.success(huge);
        });
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(definition));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> dispatcher.invoke("get_chunk_of_all_definitions", "{}"));
        assertTrue(e.getMessage().contains("above the limit"), e.getMessage());
    }

    // The registry holds unwrapped definitions, so the handler must be reached with a null exchange.
    @Test
    void invoke_ok_passesANullExchangeToTheHandler() {
        AtomicReference<Object> seenExchange = new AtomicReference<>("not called");
        ToolDefinition definition = definition("get_class_info", (exchange, request) -> {
            seenExchange.set(exchange);
            return ResponseSupport.success(Map.of("ok", true));
        });
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(definition));

        dispatcher.invoke("get_class_info", "{}");

        assertNull(seenExchange.get());
    }

    @Test
    void failed_ok_staysUnsetWhenEveryCallSucceeds() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        dispatcher.invoke("get_class_info", "{}");

        assertFalse(dispatcher.failed(), "A run in which nothing failed has nothing to roll back");
    }

    @Test
    void failed_ng_staysSetOnceAToolFunctionHasFailed() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(
                echoing("get_class_info"),
                definition("delete_elem", (exchange, request) -> ResponseSupport.error("boom"))));

        assertThrows(RuntimeException.class, () -> dispatcher.invoke("delete_elem", "{}"));
        assertTrue(dispatcher.failed(), "The failure has to outlive the throw, which the script may catch");

        // The script caught that failure and carried on. The run is still going to be rolled back.
        dispatcher.invoke("get_class_info", "{}");

        assertTrue(dispatcher.failed(), "A later success must not clear the flag");
    }

    @Test
    void failed_ng_isSetWhenTheDispatcherItselfRefusesTheCall() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        assertThrows(IllegalArgumentException.class, () -> dispatcher.invoke("no_such_tool", "{}"));

        assertFalse(dispatcher.callCount() == 0, "The refused call is still counted");
        assertTrue(dispatcher.failed(), "A refused call is a failed call");
    }

    @Test
    void firstFailure_ok_staysNullWhenEveryCallSucceeds() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        dispatcher.invoke("get_class_info", "{}");

        assertNull(dispatcher.firstFailure(), "A run in which nothing failed has nothing to report");
    }

    @Test
    void firstFailure_ng_keepsTheFirstOneWithItsOrdinal() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(
                echoing("get_class_info"),
                definition("delete_elem", (exchange, request) -> ResponseSupport.error("boom")),
                definition("delete_prst", (exchange, request) -> ResponseSupport.error("later failure"))));

        dispatcher.invoke("get_class_info", "{}");
        assertThrows(RuntimeException.class, () -> dispatcher.invoke("delete_elem", "{}"));

        assertEquals(2, dispatcher.firstFailureCallIndex(),
                "The ordinal says how far the script got before it left the rails");
        assertTrue(dispatcher.firstFailure().contains("boom"), "The reason has to survive the catch too");

        // The script caught that failure and carried on into another one.
        assertThrows(RuntimeException.class, () -> dispatcher.invoke("delete_prst", "{}"));

        assertTrue(dispatcher.firstFailure().contains("boom"),
                "A later failure may be a consequence of the first, so the first is the one worth reporting");
        assertEquals(2, dispatcher.firstFailureCallIndex(),
                "The ordinal has to stay on the first failure alongside its message");
    }

    @Test
    void firstFailureCallIndex_ok_staysUnsetWhenEveryCallSucceeds() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        dispatcher.invoke("get_class_info", "{}");

        assertEquals(-1, dispatcher.firstFailureCallIndex(), "A run in which nothing failed has no ordinal to give");
    }

    // What the executor reads to decide that a run which outlived its timeout must not commit.
    @Test
    void abandoned_ok_reportsWhetherTheRequestThreadGaveUpOnTheRun() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        assertFalse(dispatcher.abandoned(), "A run nobody has given up on is not abandoned");

        dispatcher.abandon();

        assertTrue(dispatcher.abandoned(), "The run was abandoned, and it stays abandoned");
    }

    // A timed-out run whose script never calls a tool function again reaches its end with nothing failed. The flag below is then the only thing left that says the caller was already answered with a failure.
    @Test
    void abandoned_ng_isSetEvenWhenNoToolFunctionCallFailed() {
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registryOf(echoing("get_class_info")));

        dispatcher.invoke("get_class_info", "{}");
        dispatcher.abandon();

        assertFalse(dispatcher.failed(), "Nothing failed: the run was simply given up on");
        assertTrue(dispatcher.abandoned(), "Which is exactly why abandonment has to be readable on its own");
    }
}
