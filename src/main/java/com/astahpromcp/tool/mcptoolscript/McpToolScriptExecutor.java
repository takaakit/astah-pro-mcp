package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.astah.pro.AstahApiLock;
import com.astahpromcp.tool.astah.pro.TransactionBoundary;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.common.ScriptLine;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.ClassFilter;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
            int errorToolCallIndex) {

        static Result success(String value, String output, String errorOutput) {
            return new Result(true, value, output, errorOutput, "", -1, -1, -1);
        }

        static Result failure(String errorMessage, int errorLine, int errorColumn, int errorToolCallIndex, String output, String errorOutput) {
            return new Result(false, "", output, errorOutput, errorMessage, errorLine, errorColumn, errorToolCallIndex);
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

    private final AstahToolRegistry registry;
    private final long timeoutSeconds;
    private final TransactionBoundary transaction;

    public McpToolScriptExecutor(AstahToolRegistry registry) {
        this(registry, McpServerConfig.MCP_TOOL_SCRIPT_TIMEOUT_SECONDS, TransactionBoundary.ASTAH);
    }

    // Package-private so that no profile can reach it, and taking the transaction so that a test can watch what this class does with it.
    McpToolScriptExecutor(AstahToolRegistry registry, long timeoutSeconds, TransactionBoundary transaction) {
        this.registry = registry;
        this.timeoutSeconds = timeoutSeconds;
        this.transaction = transaction;
    }

    // Evaluates the mcp tool script on a dedicated thread with a bounded timeout.
    // Never throws: every failure mode is reported through the Result.
    public Result execute(String script) {
        if (script == null || script.trim().isEmpty()) {
            return Result.failure("The mcp tool script is empty.", -1, -1, -1, "", "");
        }

        int sourceBytes = script.getBytes(StandardCharsets.UTF_8).length;
        if (sourceBytes > McpServerConfig.MCP_TOOL_SCRIPT_MAX_SOURCE_BYTES) {
            return Result.failure(
                String.format("The mcp tool script is %d bytes, above the limit of %d bytes. Split it into several runs.",
                        sourceBytes, McpServerConfig.MCP_TOOL_SCRIPT_MAX_SOURCE_BYTES),
                -1, -1, -1, "", "");
        }

        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(registry);

        FutureTask<Result> task = new FutureTask<>(() -> evaluate(script, dispatcher));
        // One thread per run: a lingering stuck script must not poison later runs.
        Thread runner = new Thread(task, "mcp-tool-script-runner");
        runner.setDaemon(true);
        runner.start();

        try {
            return task.get(timeoutSeconds, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            // Best effort: a script blocked in an interruptible call stops here.
            // A script stuck in a plain computation loop cannot be stopped at all: Java has no way to terminate a
            // thread, and killing one mid-transaction would corrupt the model anyway.
            dispatcher.abandon();
            runner.interrupt();

            String message = String.format(
                "MCP tool script execution timed out after %d seconds. The script thread was interrupted but may still be running, so Astah API access is blocked until it stops, and the transaction the run opened stays open until then, so editing in Astah itself may fail meanwhile. Every change the run made is rolled back when it stops, and restarting Astah discards them too. Wait and retry; restart Astah if it never stops. Keep scripts short and avoid blocking operations.",
                timeoutSeconds);
            log.warn(message);

            // The abandoned thread may still be calling the Astah API, so no tool may run until it terminates.
            AstahApiLock.suspend(runner, String.format(
                "an mcp tool script that timed out after %d seconds is still running", timeoutSeconds));

            return Result.failure(message, -1, -1, -1, "", "");

        } catch (InterruptedException e) {
            // The wait was cut short, not the script: this thread stopped waiting while the runner may still be calling the Astah API.
            Thread.currentThread().interrupt();
            dispatcher.abandon();
            runner.interrupt();

            String message = "Interrupted while waiting for mcp tool script execution. The script thread was interrupted but may still be running, so Astah API access is blocked until it stops, and the transaction the run opened stays open until then, so editing in Astah itself may fail meanwhile. Every change the run made is rolled back when it stops.";
            // Logged because the caller that was waiting for this result is usually gone: what an agent sees is the next tool call being refused.
            log.warn(message);

            // The abandoned thread may still be calling the Astah API, so no tool may run until it terminates.
            // Registered before returning, while this call still holds the Astah API lock, so that whoever acquires the lock next sees it.
            AstahApiLock.suspend(runner, "an mcp tool script whose caller stopped waiting is still running");

            return Result.failure(message, -1, -1, -1, "", "");

        } catch (ExecutionException e) {
            // evaluate() reports its own failures through the Result; this is a safety net.
            Throwable cause = e.getCause();

            return Result.failure(String.valueOf(cause != null ? cause : e), -1, -1, -1, "", "");
        }
    }

    // Evaluates the mcp tool script on the calling thread. Never throws: every failure mode is reported through the Result.
    private Result evaluate(String script, McpToolScriptDispatcher dispatcher) {
        BoundedWriter output = new BoundedWriter(McpServerConfig.MCP_TOOL_SCRIPT_MAX_STDOUT_BYTES);
        BoundedWriter errorOutput = new BoundedWriter(McpServerConfig.MCP_TOOL_SCRIPT_MAX_STDOUT_BYTES);

        ScriptEngine engine;
        try {
            engine = createEngine();

        } catch (Throwable t) {
            log.error("Failed to create the JavaScript engine for an mcp tool script", t);
            return Result.failure("No JavaScript engine is available for the mcp tool script", -1, -1, -1, "", "");
        }

        // Capture print() and error output per run instead of hijacking System.out/err
        engine.getContext().setWriter(new PrintWriter(output, true));
        engine.getContext().setErrorWriter(new PrintWriter(errorOutput, true));

        // Order matters. The hardening has to complete before anything else is put into the engine, because it
        // deletes global names, and before any script runs, because that is what it is protecting them from.
        try {
            engine.eval(HARDENING_BOOTSTRAP);

        } catch (Throwable t) {
            log.error("Failed to neutralize the mcp tool script globals; refusing to run it", t);
            return Result.failure("Failed to prepare a safe mcp tool script environment: " + t, -1, -1, -1,
                    output.toString(), errorOutput.toString());
        }

        try {
            engine.put("__astahToolNamesJson", dispatcher.toolNamesJson());
            engine.put("__astahToolDispatcher", dispatcher);
            engine.eval(TOOLS_PRELUDE);

        } catch (Throwable t) {
            log.error("Failed to build the tool functions for an mcp tool script", t);
            return Result.failure("Failed to prepare the tool functions: " + t, -1, -1, -1,
                    output.toString(), errorOutput.toString());
        }

        // The whole run shares one transaction, but TransactionSupport opens it lazily -- only the first time a tool function actually edits the model -- so a script that only reads never opens one at all.
        TransactionSupport.shareOnThisThread(transaction);

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
            // Unconditional, and harmless when it was never turned on: from here on the tool functions manage their own transactions again. A run that times out never reaches this, which is why sharing is thread-scoped.
            TransactionSupport.stopSharingOnThisThread();
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
            error = new IllegalStateException(dispatcher.firstFailure()
                    + " Every change the run made was rolled back.");
        }

        // A run the request thread abandoned has already been answered with a timeout failure. Reaching this line
        // means it outlived that answer -- a computation loop ignores its interrupt and then finishes on its own --
        // and committing now would leave the project holding changes the caller was told it did not get.
        // Read after eval rather than before: the timeout may land at any point during the run.
        if (error == null && dispatcher.abandoned()) {
            error = new IllegalStateException(
                    "The mcp tool script run was stopped, so every change it made was rolled back.");
        }

        Throwable closeFailure = closeTransaction(error == null, errorOutput);
        if (error == null) {
            error = closeFailure;
        }

        // Abort a dangling transaction before building the result, so that the note it appends is included.
        abortDanglingTransaction(errorOutput);

        appendTruncationNotes(output, errorOutput);

        if (error == null) {
            return Result.success(
                value == null ? "" : String.valueOf(value),
                output.toString(),
                errorOutput.toString());

        } else if (error instanceof ScriptException exception) {
            return Result.failure(
                scriptErrorMessage(exception),
                exception.getLineNumber(),
                exception.getColumnNumber(),
                -1,
                output.toString(),
                errorOutput.toString());

        } else {
            // A tool function failure arrives as a plain Java exception, which knows no line of its own but still carries the frames of the script that called it.
            int line = ScriptLine.of(error, McpServerConfig.MCP_TOOL_SCRIPT_SOURCE_NAME);

            return Result.failure(
                javaErrorMessage(error),
                line >= 0 ? line : dispatcher.firstFailureLine(),
                -1,
                dispatcher.firstFailureCallIndex(),
                output.toString(),
                errorOutput.toString());
        }
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

    // Commits the transaction the run opened, or rolls the whole run back. Returns what went wrong, or null.
    private Throwable closeTransaction(boolean commit, BoundedWriter errorOutput) {
        try {
            // A script that never edited anything never opened the shared transaction, and there is nothing to keep.
            if (transaction.isInTransaction()) {
                if (commit) {
                    transaction.commit();
                } else {
                    transaction.abort();
                }
            }
            return null;

        } catch (Throwable t) {
            log.error("Failed to close the transaction of an mcp tool script", t);
            errorOutput.write("Failed to close the transaction of the mcp tool script: " + t + "\n");

            // A commit that failed leaves the transaction open, and changes nobody could confirm must not survive it.
            if (transaction.isInTransaction()) {
                try {
                    transaction.abort();
                } catch (Throwable abortFailure) {
                    log.warn("Failed to abort after the commit of an mcp tool script failed", abortFailure);
                }
            }
            return t;
        }
    }

    // The net under closeTransaction.
    private void abortDanglingTransaction(BoundedWriter errorOutput) {
        try {
            if (transaction.isInTransaction()) {
                log.error("An mcp tool script left a transaction open. This should be impossible: the run closes the transaction it opens, and an mcp tool script cannot reach TransactionManager itself.");
                transaction.abort();
                errorOutput.write("A transaction left open by the mcp tool script was aborted.\n");
            }

        } catch (Throwable t) {
            log.warn("Failed to abort a transaction left open by the mcp tool script", t);
            errorOutput.write("Failed to abort a transaction left open by the mcp tool script: " + t + "\n");
        }
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
