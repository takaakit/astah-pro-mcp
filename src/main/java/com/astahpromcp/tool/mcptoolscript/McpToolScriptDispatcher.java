package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.common.ScriptLine;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

import javax.swing.SwingUtilities;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// The only Java entry point an mcp tool script can reach, and therefore the only place the limits of one run are enforced.
//
// JSON strings are the boundary in both directions. Handing Nashorn's ScriptObjectMirror to Jackson instead would
// misconvert nested values -- a JavaScript array arrives as {"0":...,"1":...} -- and the failure would surface deep
// inside a DTO rather than at the call. Stringifying on the JavaScript side and parsing here removes that class of bug.
//
// One instance serves one mcp tool script run: the call counter, the first failure and the abandoned flag are per-run state.
@Slf4j
public final class McpToolScriptDispatcher {

    private final AstahToolRegistry registry;
    private final AtomicBoolean abandoned = new AtomicBoolean(false);
    private int callCount;
    private String firstFailure;
    private int firstFailureLine = -1;
    private int firstFailureCallIndex = -1;

    public McpToolScriptDispatcher(AstahToolRegistry registry) {
        this.registry = registry;
    }

    // Called from JavaScript. Both the arguments and the return value are JSON strings.
    // Every failure leaves as an exception so that the script stops at the failing call instead of carrying on with a value that looks like a result; the executor turns it into ScriptResultDTO(ok=false).
    //
    // The message is kept rather than a bare flag because a caught exception is gone by the time the run ends, and the run would otherwise have nothing to tell the caller but that something, somewhere, failed.
    //
    // An Error is deliberately not caught: it leaves engine.eval as the script's own failure, which the executor already rolls back.
    public String invoke(String toolName, String argsJson) {
        try {
            return doInvoke(toolName, argsJson);

        } catch (RuntimeException e) {
            // The first failure, not the last: it is where the script left the rails, and whatever failed after it may be no more than a consequence of carrying on past it.
            if (firstFailure == null) {
                String message = e.getMessage();
                firstFailure = message == null || message.isBlank() ? String.valueOf(e) : message;
                
                // Read while the exception is still here
                firstFailureLine = ScriptLine.of(e, McpServerConfig.MCP_TOOL_SCRIPT_SOURCE_NAME);

                // The ordinal answers what the line cannot: how far the run got
                firstFailureCallIndex = callCount;
            }
            throw e;
        }
    }

    private String doInvoke(String toolName, String argsJson) {
        // A run that has already timed out must not start further work: the request thread has given up on this
        // script, so anything begun now would edit the model with nobody waiting for the answer.
        if (abandoned.get()) {
            throw new IllegalStateException("The mcp tool script run was stopped, so '" + toolName + "' was not called.");
        }

        // Counted before anything can fail, so that a failure below is attributed to the call that caused it.
        callCount++;

        if (callCount > McpServerConfig.MCP_TOOL_SCRIPT_MAX_CALLS) {
            throw new IllegalStateException("The mcp tool script made more than " + McpServerConfig.MCP_TOOL_SCRIPT_MAX_CALLS + " tool function calls. Split the work across several mcp tool script runs.");
        }

        ToolDefinition definition = registry.find(toolName);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown tool function '" + toolName + "'.");
        }

        // Refuse before running anything. Refusing afterwards would apply the tool's effects and still report a failure.
        NotMcpToolScriptCallableReason reason = registry.notMcpToolScriptCallableReason(toolName);
        if (reason != null) {
            throw new IllegalStateException(reason.message(toolName));
        }

        String arguments = argsJson == null ? "{}" : argsJson;
        int argumentBytes = arguments.getBytes(StandardCharsets.UTF_8).length;
        if (argumentBytes > McpServerConfig.MCP_TOOL_SCRIPT_MAX_ARG_BYTES) {
            throw new IllegalArgumentException("The arguments of '" + toolName + "' are " + argumentBytes
                    + " bytes, above the limit of " + McpServerConfig.MCP_TOOL_SCRIPT_MAX_ARG_BYTES
                    + " bytes. Pass less data per call.");
        }

        Map<String, Object> parsedArguments = parseArguments(toolName, arguments);

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(toolName, parsedArguments, null);

        // The exchange is unused: every handler this server builds ignores it (the three factory methods in
        // ToolSupport all bind it as an ignored parameter), and ExclusiveToolProvider only passes it through.
        McpSchema.CallToolResult result = definition.toolHandler().apply(null, request);

        // The lock wrapper drains the EDT after every single tool call before it releases the lock, so on the direct
        // profile one call means one drain. Calling the unwrapped handler skips that, which would let a script read
        // back a value before Astah had finished applying the edit that produced it. Draining here keeps a tool
        // function behaving the same whether it is called directly or from an mcp tool script.
        drainEventDispatchThread(toolName);

        if (result == null) {
            throw new IllegalStateException("'" + toolName + "' returned no result.");
        }

        if (Boolean.TRUE.equals(result.isError())) {
            throw new RuntimeException(errorMessageOf(result, toolName));
        }

        Object structuredContent = result.structuredContent();
        if (structuredContent == null) {
            // Runtime guard for a tool that stopped returning structured content without its result kind changing.
            throw new IllegalStateException(NotMcpToolScriptCallableReason.missingStructuredContentMessage(toolName));
        }

        // Wrapped so that every failure leaving this method names the tool function it came from, which is what the run reports when the script catches the exception and the original is gone.
        String resultJson;
        try {
            resultJson = JsonSupport.OBJ_MAPPER.writeValueAsString(structuredContent);
        } catch (RuntimeException e) {
            throw new IllegalStateException("The result of '" + toolName + "' could not be converted to JSON: " + e);
        }

        int resultBytes = resultJson.getBytes(StandardCharsets.UTF_8).length;
        if (resultBytes > McpServerConfig.MCP_TOOL_SCRIPT_MAX_RESULT_BYTES) {
            throw new IllegalStateException("The result of '" + toolName + "' is " + resultBytes
                    + " bytes, above the limit of " + McpServerConfig.MCP_TOOL_SCRIPT_MAX_RESULT_BYTES
                    + " bytes. Request less data per call.");
        }

        return resultJson;
    }

    // Every tool function name, as a JSON array, for the prelude to build the tools object from.
    public String toolNamesJson() {
        return JsonSupport.OBJ_MAPPER.writeValueAsString(List.copyOf(registry.names()));
    }

    // Stop accepting calls. Called when the run times out and its thread is abandoned.
    public void abandon() {
        abandoned.set(true);
    }

    // Whether the request thread gave up on this run. Set before the timeout is reported to the caller, so a run that reads it as true has already been answered with a failure and must not commit anything.
    public boolean abandoned() {
        return abandoned.get();
    }

    public int callCount() {
        return callCount;
    }

    // Whether any tool function call of this run failed, including one whose failure the script caught.
    public boolean failed() {
        return firstFailure != null;
    }

    // The message of the first tool function call that failed, or null when none did. A caught exception is gone by the time the run ends, so this is what lets the run say what went wrong rather than merely that something did.
    public String firstFailure() {
        return firstFailure;
    }

    // The script line of the first tool function call that failed, or -1 when none did.
    public int firstFailureLine() {
        return firstFailureLine;
    }

    // The 1-based ordinal of the first tool function call that failed, or -1 when none did
    public int firstFailureCallIndex() {
        return firstFailureCallIndex;
    }

    private Map<String, Object> parseArguments(String toolName, String argsJson) {
        JsonNode node;
        try {
            node = JsonSupport.OBJ_MAPPER.readTree(argsJson);

        } catch (RuntimeException e) {
            throw new IllegalArgumentException("The arguments of '" + toolName + "' are not valid JSON.");
        }

        if (node == null || node.isNull()) {
            return Map.of();
        }
        if (!node.isObject()) {
            throw new IllegalArgumentException("The arguments of '" + toolName + "' must be an object.");
        }

        int depth = depthOf(node, 1);
        if (depth > McpServerConfig.MCP_TOOL_SCRIPT_MAX_JSON_DEPTH) {
            throw new IllegalArgumentException("The arguments of '" + toolName + "' are nested " + depth
                    + " levels deep, above the limit of " + McpServerConfig.MCP_TOOL_SCRIPT_MAX_JSON_DEPTH + ".");
        }

        Map<String, Object> arguments = new LinkedHashMap<>();
        node.propertyNames().forEach(name ->
                arguments.put(name, JsonSupport.OBJ_MAPPER.convertValue(node.get(name), Object.class)));

        return arguments;
    }

    // Nesting depth, computed iteratively-by-recursion but bounded: it stops as soon as the limit is passed, so a deliberately deep document cannot turn this check itself into a stack overflow.
    private static int depthOf(JsonNode node, int currentDepth) {
        if (currentDepth > McpServerConfig.MCP_TOOL_SCRIPT_MAX_JSON_DEPTH) {
            return currentDepth;
        }
        if (!node.isObject() && !node.isArray()) {
            return currentDepth;
        }

        int deepest = currentDepth;
        for (JsonNode child : node) {
            int childDepth = depthOf(child, currentDepth + 1);
            if (childDepth > deepest) {
                deepest = childDepth;
            }
            if (deepest > McpServerConfig.MCP_TOOL_SCRIPT_MAX_JSON_DEPTH) {
                return deepest;
            }
        }
        return deepest;
    }

    private static String errorMessageOf(McpSchema.CallToolResult result, String toolName) {
        if (result.content() == null || result.content().isEmpty()) {
            return "'" + toolName + "' failed.";
        }

        StringBuilder message = new StringBuilder();
        for (McpSchema.Content content : result.content()) {
            if (content instanceof McpSchema.TextContent text && text.text() != null) {
                if (!message.isEmpty()) {
                    message.append('\n');
                }
                message.append(text.text());
            }
        }

        return message.isEmpty() ? "'" + toolName + "' failed." : message.toString();
    }

    // Wait until everything Astah queued on the event dispatch thread has run.
    private static void drainEventDispatchThread(String toolName) {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(latch::countDown);
        try {
            if (!latch.await(McpServerConfig.EDT_FLUSH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("EDT did not drain within {} seconds; continuing the mcp tool script anyway @tool={}",
                        McpServerConfig.EDT_FLUSH_TIMEOUT_SECONDS, toolName);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while draining the EDT after a tool call from an mcp tool script @tool={}", toolName);
        }
    }
}
