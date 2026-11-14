package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.common.BoundedWriter;
import com.astahpromcp.tool.common.ScriptLine;
import com.astahpromcp.tool.common.StopWhenInterruptedWriter;
import com.astahpromcp.tool.mcptoolscript.outputdto.ModelChangesKind;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.ClassFilter;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Supplier;

// Runs an mcp tool script -- JavaScript that may call this server's tool functions, and nothing else.
@Slf4j
public class McpToolScriptExecutor {
    
    // Immutable outcome of one mcp tool script run
    public record Result(
            boolean ok,
            String value,
            String output,
            String errorOutput,
            String errorMessage,
            int errorLine,
            int errorColumn,
            int errorToolCallIndex,
            ModelChangesKind modelChanges) {
        
        static Result success(String value, String output, String errorOutput, ModelChangesKind modelChanges) {
            return new Result(true, value, output, errorOutput, "", -1, -1, -1, modelChanges);
        }
        
        static Result failure(String errorMessage, int errorLine, int errorColumn, int errorToolCallIndex, String output, String errorOutput, ModelChangesKind modelChanges) {
            return new Result(false, "", output, errorOutput, errorMessage, errorLine, errorColumn, errorToolCallIndex, modelChanges);
        }
    }
    
    // Removes the globals a script could otherwise escape through, before any script of ours or the user's runs.
    //
    // A deny-all ClassFilter is not enough on its own. It stops class resolution and Java reflection, but the shell
    // built-ins (exit, quit, load) are not classes, and JSR-223 injects 'engine' and 'context'. 'engine' is the worst
    // of them: engine.getFactory().getScriptEngine() is an ordinary method call, not reflection, and hands back a
    // fresh engine with no filter at all -- from which Java.type() works without restriction.
    //
    // 'engine' and 'context' are not own properties of the global; Nashorn resolves them through __noSuchProperty__
    // against the ScriptContext, which is why neutralizing __noSuchProperty__ closes the channel itself rather than
    // just the two names known today. Values injected later with engine.put() become real own properties and are
    // unaffected by that.
    private static final String HARDENING_BOOTSTRAP = """
            (function (global) {
                "use strict";
                var denied = [
                    "exit", "quit", "load", "loadWithNewGlobal", "readFully",
                    "Java", "JavaImporter", "Packages", "JSAdapter",
                    "com", "edu", "java", "javafx", "javax", "org",
                    "engine", "context",
                    "__noSuchProperty__"
                ];

                for (var i = 0; i < denied.length; i++) {
                    var name = denied[i];
                    try { delete global[name]; } catch (ignored) {}
                    try {
                        Object.defineProperty(global, name, {
                            value: undefined,
                            writable: false,
                            configurable: false,
                            enumerable: false
                        });
                    } catch (ignored) {}
                }

                var survivors = [];
                for (var j = 0; j < denied.length; j++) {
                    if (typeof global[denied[j]] !== "undefined") {
                        survivors.push(denied[j]);
                    }
                }
                if (survivors.length > 0) {
                    throw new Error("Failed to neutralize globals: " + survivors.join(", "));
                }
            })(this);
            """;
    
    // Builds the tools object the script calls through.
    //
    // The functions and the object are fixed in place so a script cannot swap one out and then believe the result it
    // gets back came from the tool. ECMAScript 5.1 only: Nashorn has no let, const, arrow functions or template literals.
    private static final String TOOLS_PRELUDE = """
            var tools = (function () {
                var names = JSON.parse(__astahToolNamesJson);
                var t = {};
                for (var i = 0; i < names.length; i++) {
                    (function (name) {
                        Object.defineProperty(t, name, {
                            value: function (args) {
                                var json = __astahToolDispatcher.invoke(
                                    name,
                                    JSON.stringify(args === undefined || args === null ? {} : args));
                                return json === null || json === "" ? null : JSON.parse(json);
                            },
                            writable: false,
                            configurable: false,
                            enumerable: true
                        });
                    })(names[i]);
                }
                return Object.freeze(t);
            })();

            Object.defineProperty(this, "tools", {
                value: tools,
                writable: false,
                configurable: false,
                enumerable: true
            });
            """;
    
    // Read on use rather than taken at construction: the registry may list the tool this executor runs for, so it can only be built after that tool is.
    private final Supplier<AstahToolRegistry> registry;
    private final long timeoutSeconds;
    
    // Keeps a run safe while it calls the tool functions: its thread, its transaction, and what settles after each call.
    private final McpToolScriptHost host;
    
    public McpToolScriptExecutor(Supplier<AstahToolRegistry> registry, McpToolScriptHost host) {
        this(registry, McpServerConfig.MCP_TOOL_SCRIPT_TIMEOUT_SECONDS, host);
    }
    
    // Package-private so that no profile can reach it, and taking the timeout so that a test does not have to sit through the real one.
    McpToolScriptExecutor(AstahToolRegistry registry, long timeoutSeconds, McpToolScriptHost host) {
        this(() -> registry, timeoutSeconds, host);
    }
    
    private McpToolScriptExecutor(Supplier<AstahToolRegistry> registry, long timeoutSeconds, McpToolScriptHost host) {
        this.registry = registry;
        this.timeoutSeconds = timeoutSeconds;
        this.host = host;
    }
    
    // Evaluates the mcp tool script on a dedicated thread with a bounded timeout.
    // Never throws: every failure mode is reported through the Result.
    public Result execute(String script) {
        if (script == null || script.trim().isEmpty()) {
            return Result.failure("The mcp tool script is empty.", -1, -1, -1, "", "", ModelChangesKind.NONE);
        }
        
        int sourceBytes = script.getBytes(StandardCharsets.UTF_8).length;
        if (sourceBytes > McpServerConfig.MCP_TOOL_SCRIPT_MAX_SOURCE_BYTES) {
            return Result.failure(
                String.format("The mcp tool script is %d bytes, above the limit of %d bytes. Split it into several runs.",
                        sourceBytes, McpServerConfig.MCP_TOOL_SCRIPT_MAX_SOURCE_BYTES),
                -1, -1, -1, "", "", ModelChangesKind.NONE);
        }
        
        McpToolScriptRunCompletion completion = new McpToolScriptRunCompletion();
        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registry.get(), host, completion);
        
        McpToolScriptHost.RunOutcome<Result> outcome = host.runWatched(
                timeoutSeconds,
                () -> evaluate(script, dispatcher, completion),
                completion::abandon);
        
        // The host runs onAbandon on this thread before runWatched returns, so the completion is already settled here.
        return switch (outcome.kind()) {
            case COMPLETED -> outcome.value();
            case TIMED_OUT -> timeoutFailure(completion.commitStarted());
            case INTERRUPTED -> interruptionFailure(completion.commitStarted());
            case FAILED -> unexpectedFailure(outcome.failure());
        };
    }
    
    // The runner was interrupted but may still be running, and the host has suspended Astah API access until it stops.
    private Result timeoutFailure(boolean commitStarted) {
        String message = String.format("MCP tool script execution timed out after %d seconds.", timeoutSeconds)
                + abandonedRunNote(commitStarted)
                + " Keep scripts short and avoid blocking operations.";
        log.warn(message);
        
        return Result.failure(message, -1, -1, -1, "", "", ModelChangesKind.UNKNOWN);
    }
    
    // The wait was cut short, not the script: the runner may still be running, and the host has suspended Astah API access until it stops.
    private static Result interruptionFailure(boolean commitStarted) {
        String message = "Interrupted while waiting for mcp tool script execution." + abandonedRunNote(commitStarted);
        // Logged because the caller that was waiting for this result is usually gone: what an agent sees is the next tool call being refused.
        log.warn(message);
        
        return Result.failure(message, -1, -1, -1, "", "", ModelChangesKind.UNKNOWN);
    }
    
    // modelChanges stays unknown while the run is still going; this says whether the run can still keep its changes.
    private static String abandonedRunNote(boolean commitStarted) {
        if (commitStarted) {
            return " The script had already finished and the run was committing its changes, and the commit was not called off, so the changes may have been kept. Astah API access is blocked until the commit ends. Once access is restored, check the model before running the script again: running a script whose changes were kept would apply them twice. The server log records how the run ended.";
        }
        
        return " The run was stopped before it could commit, so it will keep none of its changes: its thread discards them when it stops, and restarting Astah discards them too. The thread was interrupted but may still be running, so Astah API access is blocked until it stops, and the transaction the run opened stays open until then, so editing in Astah itself may fail meanwhile. Wait until access is restored, then retry; restart Astah if it never stops.";
    }
    
    // evaluate() reports its own failures through the Result; this is a safety net.
    private static Result unexpectedFailure(Throwable failure) {
        return Result.failure(String.valueOf(failure), -1, -1, -1, "", "", ModelChangesKind.UNKNOWN);
    }
    
    // Evaluates the mcp tool script on the calling thread. Never throws: every failure mode is reported through the Result.
    private Result evaluate(String script, McpToolScriptDispatcher dispatcher, McpToolScriptRunCompletion completion) {
        BoundedWriter output = new BoundedWriter(McpServerConfig.MCP_TOOL_SCRIPT_MAX_STDOUT_BYTES);
        BoundedWriter errorOutput = new BoundedWriter(McpServerConfig.MCP_TOOL_SCRIPT_MAX_STDOUT_BYTES);
        
        ScriptEngine engine;
        try {
            engine = createEngine();
            
        } catch (Throwable t) {
            log.error("Failed to create the JavaScript engine for an mcp tool script", t);
            return Result.failure("No JavaScript engine is available for the mcp tool script", -1, -1, -1, "", "", ModelChangesKind.NONE);
        }
        
        // Capture print() and error output per run instead of hijacking System.out/err
        engine.getContext().setWriter(new PrintWriter(new StopWhenInterruptedWriter(output), true));
        engine.getContext().setErrorWriter(new PrintWriter(new StopWhenInterruptedWriter(errorOutput), true));
        
        // Order matters. The hardening has to complete before anything else is put into the engine, because it
        // deletes global names, and before any script runs, because that is what it is protecting them from.
        try {
            engine.eval(HARDENING_BOOTSTRAP);
            
        } catch (Throwable t) {
            log.error("Failed to neutralize the mcp tool script globals; refusing to run it", t);
            return Result.failure("Failed to prepare a safe mcp tool script environment: " + t, -1, -1, -1,
                    output.toString(), errorOutput.toString(), ModelChangesKind.NONE);
        }
        
        try {
            engine.put("__astahToolNamesJson", dispatcher.toolNamesJson());
            engine.put("__astahToolDispatcher", dispatcher);
            engine.eval(TOOLS_PRELUDE);
            
        } catch (Throwable t) {
            log.error("Failed to build the tool functions for an mcp tool script", t);
            return Result.failure("Failed to prepare the tool functions: " + t, -1, -1, -1,
                    output.toString(), errorOutput.toString(), ModelChangesKind.NONE);
        }
        
        // From here to the end of the script, everything the tool functions change is one unit, kept or undone as a whole.
        McpToolScriptHost.RunScope scope = host.openRunScope();
        
        // Naming the source here, after the preludes and before the script itself, is what lets a failure be traced back to the line of the script that caused it rather than to the prelude line that passed the call on.
        engine.getContext().setAttribute(
            ScriptEngine.FILENAME, McpServerConfig.MCP_TOOL_SCRIPT_SOURCE_NAME, ScriptContext.ENGINE_SCOPE);
        
        Object value = null;
        Throwable error = null;
        try {
            value = engine.eval(script);
        } catch (Throwable throwable) {
            error = throwable;
        } finally {
            // Unconditional: from here on the tool functions manage their own changes again.
            scope.stopSharing();
        }
        
        // A tool function whose failure the script caught has still left a half-applied edit in this transaction, so
        // whether the script itself finished is not enough to decide what to do with it.
        //
        // The caught exception is gone by now, so the message the dispatcher kept is the only thing that can say which
        // call went wrong. Without it the agent would be told that the run was rolled back and left to guess what to
        // fix -- and a script that catches failures deliberately is exactly the one that prints nothing.
        //
        // The first failure of the run, not wherever the run ended: the line and the ordinal below are read from that
        // same call, so a message taken from a later failure would name one call and count another, and the agent
        // would read that as the wrong call being blamed.
        if (dispatcher.failed()) {
            error = new IllegalStateException(dispatcher.firstFailure());
        }
        
        // Settled once, against a caller that may be giving up right now; reading a flag and then committing would miss that.
        // A run that loses was told it keeps nothing, so it rolls back; one that wins commits even if the caller gives up.
        if (error == null && !completion.beginCommit()) {
            error = new IllegalStateException("The mcp tool script run was stopped.");
        }
        
        // Closed before the result is built, so that what closing it had to say is included.
        McpToolScriptHost.Closing closing = scope.close(error == null);
        for (String note : closing.notes()) {
            errorOutput.write(note + "\n");
        }
        if (error == null) {
            error = closing.failure();
        }
        ModelChangesKind modelChanges = closing.modelChanges();
        logOutcome(dispatcher.callCount(), modelChanges, completion.abandonRequested());
        
        appendTruncationNotes(output, errorOutput);
        
        if (error == null) {
            return Result.success(
                value == null ? "" : String.valueOf(value),
                output.toString(),
                errorOutput.toString(),
                modelChanges);
            
        } else if (error instanceof ScriptException exception) {
            return Result.failure(
                withModelChangesNote(scriptErrorMessage(exception), modelChanges),
                exception.getLineNumber(),
                exception.getColumnNumber(),
                -1,
                output.toString(),
                errorOutput.toString(),
                modelChanges);
            
        } else {
            // A tool function failure arrives as a plain Java exception, which knows no line of its own but still carries the frames of the script that called it.
            int line = ScriptLine.of(error, McpServerConfig.MCP_TOOL_SCRIPT_SOURCE_NAME);
            
            return Result.failure(
                withModelChangesNote(javaErrorMessage(error), modelChanges),
                line >= 0 ? line : dispatcher.firstFailureLine(),
                -1,
                dispatcher.firstFailureCallIndex(),
                output.toString(),
                errorOutput.toString(),
                modelChanges);
        }
    }
    
    // Logged on the runner, because a run whose caller stopped waiting has nobody else to report how it ended to.
    private static void logOutcome(int callCount, ModelChangesKind modelChanges, boolean callerStoppedWaiting) {
        // Spelled as the result spells it
        String changes = modelChanges.name().toLowerCase(Locale.ROOT);
        if (callerStoppedWaiting) {
            log.warn("An mcp tool script whose caller had stopped waiting has ended after {} tool function call(s); model changes: {}",
                    callCount, changes);
        } else {
            log.info("MCP tool script made {} tool function call(s); model changes: {}", callCount, changes);
        }
    }
    
    // The message says what became of the model as well as modelChanges does, because the message is what an agent reads
    // first. It follows what the transaction actually did, not merely that something failed.
    //
    // A script error from Nashorn ends without a full stop ("... at column number 0"), so one is supplied before the
    // note, which would otherwise run on as though it were part of the error.
    private static String withModelChangesNote(String message, ModelChangesKind modelChanges) {
        String note = modelChangesNote(modelChanges);
        String trimmed = message.stripTrailing();
        if (note.isEmpty() || trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")) {
            return trimmed + note;
        }
        
        return trimmed + "." + note;
    }
    
    private static String modelChangesNote(ModelChangesKind modelChanges) {
        return switch (modelChanges) {
            case ROLLED_BACK -> " Every change the run made was rolled back.";
            case NONE -> " The run had not changed the model, so nothing was lost.";
            case UNKNOWN -> " Whether the changes the run made were rolled back could not be confirmed, so check the model.";
            case COMMITTED -> "";
        };
    }
    
    // A tool function refusing a call arrives here as a Java exception, and String.valueOf would prefix it with the
    // exception class ("java.lang.IllegalStateException: ..."). That prefix tells the agent nothing it can act on and
    // buries the sentence that does, so the message is used on its own whenever there is one.
    private static String javaErrorMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return String.valueOf(error);
        }
        
        return message;
    }
    
    private static void appendTruncationNotes(BoundedWriter output, BoundedWriter errorOutput) {
        if (output.truncated()) {
            errorOutput.write(String.format(
                "Output was truncated at %d bytes; print less from the mcp tool script.%n",
                McpServerConfig.MCP_TOOL_SCRIPT_MAX_STDOUT_BYTES));
        }
    }
    
    private ScriptEngine createEngine() {
        // A new engine for every run. Neutralizing globals only affects the engine it ran in, so a shared engine would hand the second script an environment the first one could have altered.
        // The factory is instantiated directly rather than looked up through ScriptEngineManager because the engine has to be created with a ClassFilter, which only this factory can do.
        ClassFilter denyAll = className -> false;
        
        return new NashornScriptEngineFactory().getScriptEngine(
                new String[] { "-strict" },
                McpToolScriptExecutor.class.getClassLoader(),
                denyAll);
    }
    
    private static String scriptErrorMessage(ScriptException exception) {
        String message = exception.getLocalizedMessage();
        if (message == null) {
            return exception.toString();
        } else {
            return message.replaceFirst("javax\\.script\\.ScriptException: ", "");
        }
    }
}
