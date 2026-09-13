package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ResponseSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.AstahApiLock;
import com.astahpromcp.tool.astah.pro.FakeTransactionBoundary;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.JsonSupport;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

// Behaviour of the mcp tool script execution boundary.
//
// The neutralization tests here are a regression suite: passing them means the holes that are known today are shut,
// not that untrusted code can be run safely. Whether a new escape route exists is what the canary tests below answer,
// because they watch for the effect rather than for a name.
public class McpToolScriptExecutorTest {

    private static McpSchema.Tool schema(String name) {
        return McpSchema.Tool.builder(name, JsonSupport.MCP_JSON_MAPPER, "{\"type\":\"object\"}")
                .description("Test tool " + name)
                .build();
    }

    private static class NoopTransactionManager implements ITransactionManager {
        @Override public void beginTransaction() { }
        @Override public void endTransaction() { }
        @Override public void abortTransaction() { }
        @Override public boolean isInTransaction() { return false; }
    }

    private static AstahToolRegistry registry() {
        ToolProvider provider = () -> List.of(
                new ToolDefinition(schema("get_class_info"), ToolDefinition.ResultKind.DTO,
                        (exchange, request) -> ResponseSupport.success(Map.of("name", "Sample", "args", request.arguments()))),
                // Stands in for a real editing *Tool class: it funnels through TransactionSupport exactly as one would.
                new ToolDefinition(schema("edit_class_info"), ToolDefinition.ResultKind.DTO,
                        (exchange, request) -> {
                            try {
                                new TransactionSupport(new NoopTransactionManager()).run(() -> { });
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            return ResponseSupport.success(Map.of("edited", true));
                        }),
                new ToolDefinition(schema("capture_dgm_img"), ToolDefinition.ResultKind.CONTENTS,
                        (exchange, request) -> ResponseSupport.success(List.of(new McpSchema.TextContent("image")))),
                new ToolDefinition(schema("run_astah_api_script"), ToolDefinition.ResultKind.DTO,
                        (exchange, request) -> ResponseSupport.success(Map.of("ok", true))),
                new ToolDefinition(schema("get_info_of_ocl_spec"), ToolDefinition.ResultKind.DTO,
                        (exchange, request) -> ResponseSupport.success(Map.of("ok", true))));

        // The reasons the catalog derives for these three, stated here because this registry has no catalog behind it
        return AstahToolRegistry.of(provider.createToolDefinitions(), Map.of(
                "capture_dgm_img", NotMcpToolScriptCallableReason.RETURNS_BINARY_CONTENT,
                "run_astah_api_script", NotMcpToolScriptCallableReason.RUNS_AN_ASTAH_API_SCRIPT,
                "get_info_of_ocl_spec", NotMcpToolScriptCallableReason.PERFORMS_BLOCKING_IO));
    }

    private static McpToolScriptExecutor executor() {
        return new McpToolScriptExecutor(registry(), 30, new FakeTransactionBoundary());
    }

    private static String evaluate(String expression) {
        McpToolScriptExecutor.Result result = executor().execute(expression);
        assertTrue(result.ok(), "Script should have run: " + result.errorMessage());
        return result.value();
    }

    @AfterEach
    void tearDown() {
        AstahApiLock.clearSuspension();
    }

    @Test
    void execute_ok_callsAToolFunction() {
        McpToolScriptExecutor.Result result =
                executor().execute("var c = tools.get_class_info({ id: 'x' }); c.name;");

        assertTrue(result.ok(), result.errorMessage());
        assertEquals("Sample", result.value());
    }

    @Test
    void execute_ok_passesNestedArgumentsThrough() {
        McpToolScriptExecutor.Result result = executor().execute(
                "var r = tools.get_class_info({ id: 'x', points: [{ x: 1 }, { x: 2 }] });"
                        + " '' + r.args.points.length + ':' + r.args.points[1].x;");

        assertTrue(result.ok(), result.errorMessage());
        assertEquals("2:2", result.value());
    }

    @Test
    void execute_ok_letsAScriptLoopOverManyCalls() {
        McpToolScriptExecutor.Result result = executor().execute(
                "var n = 0; for (var i = 0; i < 10; i++) { n += tools.get_class_info({ id: '' + i }).name.length; } '' + n;");

        assertTrue(result.ok(), result.errorMessage());
        assertEquals("60", result.value());
    }

    // The script can catch a tool failure and keep going -- the print below proves it did -- but the run it belongs to is over: one run is one transaction, so a failure anywhere in it means nothing the script did is kept.
    @Test
    void execute_ng_letsTheScriptCatchAToolFailureButStillFailsTheRun() {
        McpToolScriptExecutor.Result result = executor().execute(
                "try { tools.capture_dgm_img({}); print('not reached'); } catch (e) { print('caught'); }");

        assertTrue(result.output().contains("caught"), result.output());
        assertFalse(result.ok(), "A caught failure is still a failure of the run");
        assertTrue(result.errorMessage().contains("capture_dgm_img"),
                "The run has to say which call failed, because the script swallowed the exception: "
                        + result.errorMessage());
        assertTrue(result.errorMessage().contains("rolled back"), result.errorMessage());
    }

    @Test
    void execute_ng_failsTheRunWhenAToolFailureIsNotCaught() {
        McpToolScriptExecutor.Result result = executor().execute("tools.capture_dgm_img({});");

        assertFalse(result.ok());
        assertTrue(result.errorMessage().contains("capture_dgm_img"), result.errorMessage());
    }

    // The refusal has to reach the script as the sentence that explains it. Binding only the callable tools would
    // make this fail with "tools.capture_dgm_img is not a function", which reads as "no such tool" and sends the
    // agent looking for a different name instead of calling this one directly.
    @Test
    void execute_ng_refusesAnExcludedToolWithAMessageThatSaysHowToReachIt() {
        for (String name : List.of("capture_dgm_img", "get_info_of_ocl_spec", "run_astah_api_script")) {
            McpToolScriptExecutor.Result result = executor().execute("tools." + name + "({});");

            assertFalse(result.ok(), name);
            assertFalse(result.errorMessage().contains("is not a function"),
                    "'" + name + "' should be refused with an explanation, not reported as missing: "
                            + result.errorMessage());
            assertTrue(result.errorMessage().contains("directly as an MCP tool"),
                    "The refusal should say how to reach it: " + result.errorMessage());
        }
    }

    // A refusal arrives as a Java exception, and String.valueOf would prefix it with the exception class name.
    @Test
    void execute_ng_reportsFailuresWithoutTheJavaExceptionClassName() {
        McpToolScriptExecutor.Result result = executor().execute("tools.capture_dgm_img({});");

        assertFalse(result.ok());
        assertFalse(result.errorMessage().startsWith("java."),
                "The message should not lead with the exception class: " + result.errorMessage());
        assertFalse(result.errorMessage().contains("java.lang.IllegalStateException"), result.errorMessage());
    }

    // The ordinal answers a question the line number does not: how far the script got before it stopped.
    @Test
    void execute_ng_saysWhichToolCallFailed() {
        McpToolScriptExecutor.Result result = executor().execute(
                "for (var i = 0; i < 5; i++) { tools.get_class_info({ id: '' + i }); }\n"
                        + "tools.capture_dgm_img({});");

        assertFalse(result.ok());
        assertEquals(6, result.errorToolCallIndex(),
                "Five calls succeeded, so the failing one is the sixth: " + result.errorMessage());
    }

    @Test
    void execute_ng_saysWhichPassOfALoopTheFailingCallWasOn() {
        McpToolScriptExecutor.Result result = executor().execute(
                "for (var i = 0; i < 5; i++) {\n"
                        + "  tools.get_class_info({ id: '' + i });\n"
                        + "  if (i === 2) { tools.capture_dgm_img({}); }\n"
                        + "}\n");

        assertFalse(result.ok());
        assertEquals(3, result.errorLine(), "The line is where the failing call is written");
        assertEquals(4, result.errorToolCallIndex(),
                "Three get_class_info calls preceded it, so the refused one is the fourth: " + result.errorMessage());
    }

    @Test
    void execute_ng_leadsWithTheReasonRatherThanTheOrdinal() {
        McpToolScriptExecutor.Result result = executor().execute("tools.capture_dgm_img({});");

        assertFalse(result.ok());
        assertFalse(result.errorMessage().contains("Tool call #"),
                "The ordinal belongs in errorToolCallIndex, not in front of the sentence to act on: " + result.errorMessage());
        assertTrue(result.errorMessage().startsWith("'capture_dgm_img'"),
                "The message has to open with what went wrong: " + result.errorMessage());
    }

    @Test
    void execute_ng_reportsNoOrdinalForAFailureOutsideAToolCall() {
        McpToolScriptExecutor.Result result = executor().execute("var first = 1;\nvar broken = ;\n");

        assertFalse(result.ok());
        assertEquals(-1, result.errorToolCallIndex(),
                "A script-level error belongs to no tool function call: " + result.errorMessage());
    }

    // A tool failure arrives as a Java exception, which knows no line of its own.
    @Test
    void execute_ng_reportsTheLineOfAnUncaughtToolFailure() {
        McpToolScriptExecutor.Result result = executor().execute(
                "var a = 1;\n"
                        + "var b = 2;\n"
                        + "tools.capture_dgm_img({});\n");

        assertFalse(result.ok());
        assertEquals(3, result.errorLine(),
                "The failure has to point at the line the call was made on: " + result.errorMessage());
    }

    // The exception is gone by the time the run ends, so this line can only come from what the dispatcher kept while it still had it.
    @Test
    void execute_ng_reportsTheLineOfAToolFailureTheScriptCaught() {
        McpToolScriptExecutor.Result result = executor().execute(
                "var first = 1;\n"
                        + "try {\n"
                        + "  tools.capture_dgm_img({});\n"
                        + "} catch (e) {\n"
                        + "  print('caught');\n"
                        + "}\n");

        assertFalse(result.ok(), "A caught failure is still a failure of the run");
        assertEquals(3, result.errorLine(),
                "A caught failure has to point at the line the call was made on: " + result.errorMessage());
        assertEquals(1, result.errorToolCallIndex(),
                "The ordinal has to survive the catch just as the line does: " + result.errorMessage());
    }

    // The ordinal counts the first failure, so the message and the line have to belong to that call too: fields describing two different calls read as though the wrong one had been blamed.
    @Test
    void execute_ng_reportsTheFirstFailureWhenTheScriptCaughtItAndThenFailedAgain() {
        McpToolScriptExecutor.Result result = executor().execute(
                "try {\n"
                        + "  tools.capture_dgm_img({});\n"
                        + "} catch (e) {\n"
                        + "  print('caught');\n"
                        + "}\n"
                        + "tools.run_astah_api_script({});\n");

        assertFalse(result.ok());
        assertTrue(result.errorMessage().startsWith("'capture_dgm_img'"),
                "The message has to name the call the ordinal counts: " + result.errorMessage());
        assertEquals(2, result.errorLine(),
                "The line has to belong to that same call: " + result.errorMessage());
        assertEquals(1, result.errorToolCallIndex(), result.errorMessage());
    }

    // A failure of the script itself carries no ordinal, and reporting it instead would drop the tool function failure the run is being rolled back for.
    @Test
    void execute_ng_reportsTheToolFailureWhenTheScriptCaughtItAndThenFailedOnItsOwn() {
        McpToolScriptExecutor.Result result = executor().execute(
                "try {\n"
                        + "  tools.capture_dgm_img({});\n"
                        + "} catch (e) {\n"
                        + "  print('caught');\n"
                        + "}\n"
                        + "var broken = null;\n"
                        + "broken.missing();\n");

        assertFalse(result.ok());
        assertTrue(result.errorMessage().startsWith("'capture_dgm_img'"),
                "A caught tool failure must not be lost because the script then failed on its own: " + result.errorMessage());
        assertEquals(2, result.errorLine(), result.errorMessage());
        assertEquals(1, result.errorToolCallIndex(), result.errorMessage());
    }

    // The innermost frame of the script, not the outermost: the line to fix is where the call was made, not where the function holding it was reached.
    @Test
    void execute_ng_reportsTheLineInsideTheFunctionThatMadeTheFailingCall() {
        McpToolScriptExecutor.Result result = executor().execute(
                "function step() {\n"
                        + "  tools.capture_dgm_img({});\n"
                        + "}\n"
                        + "step();\n");

        assertFalse(result.ok());
        assertEquals(2, result.errorLine(),
                "The line inside the function is the one that failed: " + result.errorMessage());
    }

    // The frames of the prelude that binds the tool functions run closer to the failure than the script's own, and must not be mistaken for it: line 1 of the prelude would be a plausible-looking wrong answer here.
    @Test
    void execute_ng_reportsTheScriptsOwnLineRatherThanAPreludeLine() {
        McpToolScriptExecutor.Result result = executor().execute(
                "var padding = 1;\n".repeat(20) + "tools.capture_dgm_img({});\n");

        assertFalse(result.ok());
        assertEquals(21, result.errorLine(),
                "The line has to come from the script, not from the prelude: " + result.errorMessage());
    }

    @Test
    void execute_ng_saysWhichToolCallFailedForAFailureInsideAHandler() {
        McpToolScriptExecutor.Result result = executor().execute(
                "tools.get_class_info({ id: 'a' });\n"
                        + "tools.get_class_info({ id: 'b' });\n"
                        + "tools.no_such_tool_here({});");

        assertFalse(result.ok());
        // An unknown name never reaches the dispatcher, so this one is a plain script error and keeps its line
        assertTrue(result.errorMessage().contains("not a function"), result.errorMessage());
        assertEquals(3, result.errorLine(), "A script-level error still reports its line");
    }

    @Test
    void execute_ok_stillBindsEveryCallableTool() {
        McpToolScriptExecutor.Result result = executor().execute("'' + Object.keys(tools).length;");

        assertTrue(result.ok(), result.errorMessage());
        assertEquals("5", result.value(), "Every tool of the registry is bound, callable or not");
    }

    @Test
    void execute_ng_reportsAnUnknownToolAsATypeError() {
        McpToolScriptExecutor.Result result = executor().execute("tools.no_such_tool({});");

        assertFalse(result.ok());
        assertTrue(result.errorMessage().contains("not a function"), result.errorMessage());
    }

    @Test
    void execute_ok_capturesPrintOutput() {
        McpToolScriptExecutor.Result result = executor().execute("print('hello'); 1;");

        assertTrue(result.ok(), result.errorMessage());
        assertTrue(result.output().contains("hello"), result.output());
    }

    @Test
    void execute_ok_leavesTheRawAstahApiUnbound() {
        assertEquals("undefined", evaluate("typeof astah;"));
        assertEquals("undefined", evaluate("typeof projectAccessor;"));
    }

    @Test
    void execute_ok_removesTheShellBuiltins() {
        assertEquals("undefined", evaluate("typeof exit;"));
        assertEquals("undefined", evaluate("typeof quit;"));
        assertEquals("undefined", evaluate("typeof load;"));
        assertEquals("undefined", evaluate("typeof loadWithNewGlobal;"));
        assertEquals("undefined", evaluate("typeof readFully;"));
    }

    @Test
    void execute_ok_removesTheJavaClassEntryPoints() {
        assertEquals("undefined", evaluate("typeof Java;"));
        assertEquals("undefined", evaluate("typeof JavaImporter;"));
        assertEquals("undefined", evaluate("typeof Packages;"));
        assertEquals("undefined", evaluate("typeof JSAdapter;"));
        assertEquals("undefined", evaluate("typeof java;"));
        assertEquals("undefined", evaluate("typeof javax;"));
        assertEquals("undefined", evaluate("typeof org;"));
    }

    // The engine object is the worst of the injected globals: engine.getFactory().getScriptEngine() is an ordinary
    // method call, not reflection, and returns an engine with no ClassFilter at all.
    @Test
    void execute_ok_removesTheJsr223Globals() {
        assertEquals("undefined", evaluate("typeof engine;"));
        assertEquals("undefined", evaluate("typeof context;"));
        assertEquals("undefined", evaluate("typeof __noSuchProperty__;"));
    }

    @Test
    void execute_ok_locksEveryNameTheGuideReserves() {
        for (String name : McpToolScriptExampleTool.RESERVED_NAMES) {
            assertEquals("undefined", evaluate("typeof " + name + ";"),
                    name + " is reserved in the mcp tool script guide but is not locked here");
        }
    }

    @Test
    void execute_ng_refusesAnAssignmentToAReservedNameOnTheLineThatMadeIt() {
        McpToolScriptExecutor.Result result = executor().execute("var first = 1;\nvar engine = 'something';\n");

        assertFalse(result.ok(), "Assigning to a locked name must stop the run");
        assertTrue(result.errorMessage().contains("engine"),
                "The failure has to name the variable: " + result.errorMessage());
        assertEquals(2, result.errorLine(),
                "The failure has to point at the line that made the assignment: " + result.errorMessage());
    }

    @Test
    void execute_ng_refusesAnAssignmentToAnUndeclaredVariable() {
        McpToolScriptExecutor.Result result = executor().execute("var pistonId = 1;\npistnoId = 2;\n");

        assertFalse(result.ok(), "Assigning to an undeclared variable must stop the run");
        assertTrue(result.errorMessage().contains("pistnoId"),
                "The failure has to name the variable: " + result.errorMessage());
    }

    @Test
    void execute_ok_parsesEveryShippedExampleInStrictMode() throws Exception {
        for (String file : McpToolScriptExampleTool.EXAMPLE_FILES) {
            String source = McpToolScriptExampleTool.loadExample(file);

            McpToolScriptExecutor.Result result =
                    executor().execute("function __unused() {\n" + source + "\n}\n");

            assertTrue(result.ok(), file + " does not parse in strict mode: " + result.errorMessage());
        }
    }

    @Test
    void execute_ng_cannotBuildASecondUnfilteredEngine() {
        McpToolScriptExecutor.Result result =
                executor().execute("engine.getFactory().getScriptEngine().eval(\"1\");");

        assertFalse(result.ok(), "Reaching a second engine must fail");
    }

    @Test
    void execute_ng_cannotResolveJavaClasses() {
        assertFalse(executor().execute("Java.type('java.lang.System');").ok());
        assertFalse(executor().execute("({}).getClass();").ok());
    }

    @Test
    void execute_ok_cannotRecoverTheRemovedGlobals() {
        assertEquals("undefined", evaluate("typeof eval('this').engine;"));
        assertEquals("undefined", evaluate("typeof eval('this').exit;"));
        assertEquals("undefined", evaluate("typeof eval('this').load;"));
        assertEquals("undefined", evaluate("typeof eval('this').Java;"));

        // The Function constructor route does not even reach the global any more: the engine is strict, so the
        // function it builds is strict too, and calling that with no receiver gives undefined rather than the global.
        assertEquals("undefined", evaluate("typeof Function('return this')();"));
    }

    // A script that could swap a tool function out could then act on a result no tool ever produced.
    //
    // The engine is strict, so each of these stops the run rather than being quietly ignored. What matters either way
    // is the second half: a script that catches the refusal and carries on still finds the real tool in place.
    @Test
    void execute_ng_cannotSwapAToolFunctionOut() {
        assertFalse(executor().execute(
                "tools.get_class_info = function () { return { name: 'hijacked' }; };").ok(),
                "Swapping a tool function out must stop the run");
        assertFalse(executor().execute("tools.get_class_info = 1;").ok(),
                "Overwriting a tool function must stop the run");
        assertFalse(executor().execute("tools = {};").ok(),
                "Replacing the tools object must stop the run");
        assertFalse(executor().execute("delete tools.get_class_info;").ok(),
                "Deleting a tool function must stop the run");

        assertEquals("Sample", evaluate(
                "try { tools.get_class_info = function () { return { name: 'hijacked' }; }; } catch (e) {}"
                        + " tools.get_class_info({ id: 'x' }).name;"));

        assertEquals("function", evaluate(
                "try { tools = {}; } catch (e) {} typeof tools.get_class_info;"));
    }

    // Watches for the effect rather than for a name, so it still fires for an escape route nobody has thought of.
    @Test
    void execute_ng_cannotReadALocalFile(@TempDir Path tempDir) throws Exception {
        Path canary = tempDir.resolve("canary.txt");
        Files.writeString(canary, "canary-value", StandardCharsets.UTF_8);
        String path = canary.toAbsolutePath().toString().replace("\\", "\\\\");

        for (String attempt : List.of(
                "readFully('" + path + "');",
                "load('" + path + "');",
                "Java.type('java.nio.file.Files');",
                "new java.io.File('" + path + "');",
                "Packages.java.io.File;",
                "engine.getFactory().getScriptEngine().eval(\"Java.type('java.nio.file.Files')\");")) {

            McpToolScriptExecutor.Result result = executor().execute(attempt);

            assertFalse(result.ok(), "This should not have run: " + attempt);
            assertFalse(String.valueOf(result.value()).contains("canary-value"),
                    "A script read the canary file: " + attempt);
            assertFalse(result.output().contains("canary-value"),
                    "A script read the canary file: " + attempt);
        }
    }

    @Test
    void execute_ng_cannotReachTheNetwork() {
        for (String attempt : List.of(
                "load('http://127.0.0.1:1/canary.js');",
                "Java.type('java.net.URL');",
                "new java.net.URL('http://127.0.0.1:1/');",
                "engine.getFactory().getScriptEngine().eval(\"Java.type('java.net.URL')\");")) {

            assertFalse(executor().execute(attempt).ok(), "This should not have run: " + attempt);
        }
    }

    @Test
    void execute_ng_rejectsASourceAboveTheSizeLimit() {
        String script = "var x = '" + "y".repeat(McpServerConfig.MCP_TOOL_SCRIPT_MAX_SOURCE_BYTES) + "';";

        McpToolScriptExecutor.Result result = executor().execute(script);

        assertFalse(result.ok());
        assertTrue(result.errorMessage().contains("above the limit"), result.errorMessage());
    }

    @Test
    void execute_ok_truncatesRunawayOutputAndSaysSo() {
        McpToolScriptExecutor.Result result = executor().execute(
                "for (var i = 0; i < 20000; i++) { print('0123456789012345678901234567890123456789'); } 'done';");

        assertTrue(result.ok(), result.errorMessage());
        assertTrue(result.output().getBytes(StandardCharsets.UTF_8).length
                        <= McpServerConfig.MCP_TOOL_SCRIPT_MAX_STDOUT_BYTES,
                "Output must stay within its limit");
        assertTrue(result.errorOutput().contains("truncated"),
                "A truncated output must say so: " + result.errorOutput());
    }

    @Test
    void execute_ng_stopsAScriptThatCallsToolsTooManyTimes() {
        McpToolScriptExecutor.Result result = executor().execute(
                "for (var i = 0; i < " + (McpServerConfig.MCP_TOOL_SCRIPT_MAX_CALLS + 5)
                        + "; i++) { tools.get_class_info({ id: '' + i }); } 'done';");

        assertFalse(result.ok());
        assertTrue(result.errorMessage().contains("tool function calls"), result.errorMessage());
    }

    @Test
    void execute_ng_reportsLimitFailuresWithoutLeakingInternals() {
        McpToolScriptExecutor.Result result = executor().execute(
                "var s = '" + "y".repeat(McpServerConfig.MCP_TOOL_SCRIPT_MAX_SOURCE_BYTES) + "';");

        assertFalse(result.ok());
        assertFalse(result.errorMessage().contains("com.astahpromcp"), result.errorMessage());
        assertFalse(result.errorMessage().contains(".java:"), result.errorMessage());
    }

    @Test
    void execute_ng_timesOutAndDoesNotLeaveAccessSuspendedWhenTheScriptStops() throws Exception {
        // A script that waits in an interruptible call, against a one second budget
        McpToolScriptExecutor timeoutExecutor =
                new McpToolScriptExecutor(interruptibleRegistry(), 1, new FakeTransactionBoundary());

        McpToolScriptExecutor.Result result = timeoutExecutor.execute("tools.wait_forever({});");

        assertFalse(result.ok());
        assertTrue(result.errorMessage().contains("timed out"), result.errorMessage());

        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS),
                "Astah API access should be restored once the interrupted script thread terminates");
    }

    // After the timeout the abandoned thread must not start further tool calls: the request thread has stopped
    // waiting, so anything begun now would change the model with nobody to report it to.
    @Test
    void execute_ng_startsNoFurtherToolCallsAfterATimeout() throws Exception {
        AtomicInteger callsAfterTimeout = new AtomicInteger();
        McpToolScriptExecutor timeoutExecutor =
                new McpToolScriptExecutor(countingAfterWaitRegistry(callsAfterTimeout), 1, new FakeTransactionBoundary());

        McpToolScriptExecutor.Result result = timeoutExecutor.execute(
                "tools.wait_forever({}); for (var i = 0; i < 50; i++) { try { tools.count_me({}); } catch (e) {} }");

        assertFalse(result.ok());
        Thread.sleep(1_000);
        assertEquals(0, callsAfterTimeout.get(), "No tool call may start after the run was abandoned");

        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS));
    }

    @Test
    void execute_ok_leavesAccessUsable() {
        McpToolScriptExecutor.Result result = executor().execute("1 + 1;");

        assertTrue(result.ok(), result.errorMessage());
        assertNull(AstahApiLock.suspensionReason(), "A successful script must not suspend Astah API access");
    }

    @Test
    void execute_ok_opensNoTransactionWhenTheScriptOnlyReads() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();

        McpToolScriptExecutor.Result result =
                new McpToolScriptExecutor(registry(), 30, transaction).execute("tools.get_class_info({ id: '1' }).name;");

        assertTrue(result.ok(), result.errorMessage());
        assertEquals(0, transaction.beginCount(), "A script that only reads must never open a transaction");
        assertEquals(0, transaction.commitCount());
        assertEquals(0, transaction.abortCount());
        assertFalse(transaction.isInTransaction());
    }

    @Test
    void execute_ok_toleratesAnUnopenableTransactionWhenTheScriptOnlyReads() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        transaction.failOnBegin();

        McpToolScriptExecutor.Result result =
                new McpToolScriptExecutor(registry(), 30, transaction).execute("tools.get_class_info({ id: '1' }).name;");

        assertTrue(result.ok(), result.errorMessage());
        assertEquals(0, transaction.beginCount(), "A script that only reads must never even try to open a transaction");
    }

    @Test
    void execute_ok_commitsTheRunWhenAnEditSucceeded() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();

        McpToolScriptExecutor.Result result =
                new McpToolScriptExecutor(registry(), 30, transaction).execute("tools.edit_class_info({ id: '1' });");

        assertTrue(result.ok(), result.errorMessage());
        assertEquals(1, transaction.beginCount(), "The first edit opens the shared transaction");
        assertEquals(1, transaction.commitCount(), "A run in which nothing failed keeps what it did");
        assertEquals(0, transaction.abortCount());
        assertFalse(transaction.isInTransaction(), "No transaction may be left open");
    }

    @Test
    void execute_ng_rollsTheRunBackWhenTheScriptItselfFails() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();

        McpToolScriptExecutor.Result result = new McpToolScriptExecutor(registry(), 30, transaction)
                .execute("tools.edit_class_info({ id: '1' }); throw new Error('boom');");

        assertFalse(result.ok());
        assertEquals(0, transaction.commitCount(), "A script that threw must not have its edits kept");
        assertEquals(1, transaction.abortCount());
        assertFalse(transaction.isInTransaction());
    }

    // The case a bare "did the script finish?" would get wrong, and the reason the dispatcher keeps the failure.
    @Test
    void execute_ng_rollsTheRunBackWhenTheScriptCaughtTheToolFailure() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();

        McpToolScriptExecutor.Result result = new McpToolScriptExecutor(registry(), 30, transaction)
                .execute("tools.edit_class_info({ id: '1' }); try { tools.capture_dgm_img({}); } catch (e) {} 'finished';");

        assertFalse(result.ok(), "The script finished, but the run did not");
        assertEquals(0, transaction.commitCount());
        assertEquals(1, transaction.abortCount());
    }

    // A run the request thread gave up on has already been answered with a timeout failure, so whatever it does afterwards must not reach the project. Only a script that ignores its interrupt gets this far.
    @Test
    void execute_ng_rollsBackARunThatOutlivedItsTimeout() throws Exception {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        McpToolScriptExecutor timeoutExecutor = new McpToolScriptExecutor(registry(), 1, transaction);

        McpToolScriptExecutor.Result result = timeoutExecutor.execute(
                "tools.edit_class_info({ id: '1' });"
                        + " var start = Date.now(); while (Date.now() - start < 2500) { }");

        assertFalse(result.ok());
        assertTrue(result.errorMessage().contains("timed out"), result.errorMessage());

        // The abandoned thread is still running the loop; it reaches the end of the run on its own shortly after.
        assertTrue(awaitAccessRestored(10, TimeUnit.SECONDS),
                "The abandoned thread should finish its loop and terminate");

        assertEquals(0, transaction.commitCount(),
                "The caller was told the run failed, so the run must not keep what it did");
        assertEquals(1, transaction.abortCount());
        assertFalse(transaction.isInTransaction());
    }

    // Astah keeps the transaction when it refuses a commit, and changes nobody could confirm must not survive it.
    @Test
    void execute_ng_abortsWhenTheCommitIsRefused() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        transaction.failOnCommit();

        McpToolScriptExecutor.Result result =
                new McpToolScriptExecutor(registry(), 30, transaction).execute("tools.edit_class_info({ id: '1' });");

        assertFalse(result.ok(), "A run whose commit was refused did not succeed");
        assertEquals(1, transaction.abortCount(), "The refused commit has to be rolled back");
        assertFalse(transaction.isInTransaction());
        assertTrue(result.errorOutput().contains("Failed to close the transaction"), result.errorOutput());
    }

    // Someone else holds the transaction: an Astah GUI command mid-flight, or a run that timed out and never let go.
    @Test
    void execute_ng_rollsBackWhenTheTransactionCannotBeOpenedForAnEdit() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        transaction.failOnBegin();

        McpToolScriptExecutor.Result result = new McpToolScriptExecutor(registry(), 30, transaction)
                .execute("tools.edit_class_info({ id: '1' }); print('ran anyway');");

        assertFalse(result.ok());
        assertTrue(result.errorMessage().contains("Retry shortly"), result.errorMessage());
        assertFalse(result.output().contains("ran anyway"), "Nothing after the failed edit may run");
        assertEquals(0, transaction.commitCount());
        assertEquals(0, transaction.abortCount());
    }

    @Test
    void execute_ng_rejectsAnEmptyScript() {
        assertFalse(executor().execute("").ok());
        assertFalse(executor().execute("   ").ok());
        assertFalse(executor().execute(null).ok());
    }

    private static AstahToolRegistry interruptibleRegistry() {
        ToolProvider provider = () -> List.of(new ToolDefinition(schema("wait_forever"), ToolDefinition.ResultKind.DTO, (exchange, request) -> {
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return ResponseSupport.success(Map.of("ok", true));
        }));

        return AstahToolRegistry.of(provider.createToolDefinitions(), Map.of());
    }

    private static AstahToolRegistry countingAfterWaitRegistry(AtomicInteger callsAfterTimeout) {
        ToolProvider provider = () -> List.of(
                new ToolDefinition(schema("wait_forever"), ToolDefinition.ResultKind.DTO, (exchange, request) -> {
                    try {
                        Thread.sleep(3_000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return ResponseSupport.success(Map.of("ok", true));
                }),
                new ToolDefinition(schema("count_me"), ToolDefinition.ResultKind.DTO, (exchange, request) -> {
                    callsAfterTimeout.incrementAndGet();
                    return ResponseSupport.success(Map.of("ok", true));
                }));

        return AstahToolRegistry.of(provider.createToolDefinitions(), Map.of());
    }

    private static boolean awaitAccessRestored(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadline) {
            if (AstahApiLock.suspensionReason() == null) {
                return true;
            }
            Thread.sleep(50);
        }

        return AstahApiLock.suspensionReason() == null;
    }
}
