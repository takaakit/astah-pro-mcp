package com.astahpromcp.tool.astah.pro.astahapiscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.astah.pro.WatchedRunner;
import com.astahpromcp.tool.common.BoundedWriter;
import com.astahpromcp.tool.common.StopWhenInterruptedWriter;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.PrintWriter;

// Runs an astah api script -- JavaScript (the bundled Nashorn engine, via JSR-223) evaluated inside Astah's JVM with the Astah API bound as global variables.
@Slf4j
public class AstahApiScriptExecutor {

    // Immutable outcome of one astah api script run.
    public record Result(
            boolean ok,
            String value,
            String output,
            String errorOutput,
            String errorMessage,
            int errorLine,
            int errorColumn) {

        static Result success(String value, String output, String errorOutput) {
            return new Result(
                true,
                value,
                output,
                errorOutput,
                "",
                -1,
                -1);
        }

        static Result failure(String errorMessage, int errorLine, int errorColumn, String output, String errorOutput) {
            return new Result(
                false,
                "",
                output,
                errorOutput,
                errorMessage,
                errorLine,
                errorColumn);
        }
    }

    private final ProjectAccessor projectAccessor;
    private final long timeoutSeconds;

    public AstahApiScriptExecutor(ProjectAccessor projectAccessor) {
        this(projectAccessor, McpServerConfig.ASTAH_API_SCRIPT_TIMEOUT_SECONDS);
    }

    public AstahApiScriptExecutor(ProjectAccessor projectAccessor, long timeoutSeconds) {
        this.projectAccessor = projectAccessor;
        this.timeoutSeconds = timeoutSeconds;
    }

    // Evaluates the astah api script on a dedicated thread with a bounded timeout.
    // Never throws: every failure mode is reported through the Result.
    public Result execute(String script) {
        WatchedRunner.Outcome<Result> outcome = WatchedRunner.run(
                "astah-api-script-runner", "an astah api script", timeoutSeconds, () -> evaluate(script));

        return switch (outcome.kind()) {
            case COMPLETED -> outcome.value();
            case TIMED_OUT -> timeoutFailure();
            case INTERRUPTED -> interruptionFailure();
            case FAILED -> unexpectedFailure(outcome.failure());
        };
    }

    // The runner was interrupted but may still be running, and WatchedRunner has suspended Astah API access until it stops.
    private Result timeoutFailure() {
        String message = String.format(
            "Astah API script execution timed out after %d seconds. The script thread was interrupted but may still be running, so Astah API access is blocked until it stops. Wait and retry; restart Astah if it never stops. Keep scripts short and avoid blocking operations.",
            timeoutSeconds);
        log.warn(message);

        return Result.failure(
            message,
            -1,
            -1,
            "",
            "");
    }

    // The wait was cut short, not the script: the runner may still be running, and WatchedRunner has suspended Astah API access until it stops.
    private Result interruptionFailure() {
        String message = "Interrupted while waiting for astah api script execution. The script thread was interrupted but may still be running, so Astah API access is blocked until it stops.";
        // Logged because the caller that was waiting for this result is usually gone: what an agent sees is the next tool call being refused.
        log.warn(message);

        return Result.failure(
            message,
            -1,
            -1,
            "",
            "");
    }

    // evaluate() reports its own failures through the Result; this is a safety net.
    private static Result unexpectedFailure(Throwable failure) {
        return Result.failure(
            String.valueOf(failure),
            -1,
            -1,
            "",
            "");
    }

    // Evaluates the astah api script on the calling thread. Never throws: every failure mode is reported through the Result.
    private Result evaluate(String script) {
        BoundedWriter output = new BoundedWriter(McpServerConfig.ASTAH_API_SCRIPT_MAX_STDOUT_BYTES);
        BoundedWriter errorOutput = new BoundedWriter(McpServerConfig.ASTAH_API_SCRIPT_MAX_STDOUT_BYTES);

        ScriptEngine engine = createEngine();
        if (engine == null) {
            return Result.failure(
                "No JavaScript engine is available for the astah api script",
                -1,
                -1,
                "",
                "");
        }
        // Capture print() and error output per run instead of hijacking System.out/err
        engine.getContext().setWriter(new PrintWriter(new StopWhenInterruptedWriter(output), true));
        engine.getContext().setErrorWriter(new PrintWriter(new StopWhenInterruptedWriter(errorOutput), true));
        engine.put("astah", projectAccessor);
        engine.put("projectAccessor", projectAccessor);

        Object value = null;
        Throwable error = null;
        try {
            value = engine.eval(script);
        } catch (Throwable throwable) {
            error = throwable;
        }

        // Abort a dangling transaction before building the result, so that the note it appends to the error output is included in the result.
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
                output.toString(),
                errorOutput.toString());
        
        } else {
            return Result.failure(
                String.valueOf(error),
                -1,
                -1,
                output.toString(),
                errorOutput.toString());
        }
    }

    // Safety net for editing astah api scripts that fail between beginTransaction and endTransaction
    private static void abortDanglingTransaction(BoundedWriter errorOutput) {
        try {
            if (TransactionManager.isInTransaction()) {
                TransactionManager.abortTransaction();
                errorOutput.write("A transaction left open by the astah api script was aborted.\n");
            }
        
        } catch (Throwable t) {
            // If the abort itself fails, every later edit would be blocked; make it visible.
            log.warn("Failed to abort a transaction left open by the astah api script", t);
            errorOutput.write("Failed to abort a transaction left open by the astah api script: " + t + "\n");
        }
    }

    private static void appendTruncationNotes(BoundedWriter output, BoundedWriter errorOutput) {
        if (output.truncated()) {
            errorOutput.write(String.format(
                "Output was truncated at %d bytes; print less from the astah api script.%n",
                McpServerConfig.ASTAH_API_SCRIPT_MAX_STDOUT_BYTES));
        }
    }

    private static ScriptEngine createEngine() {
        // Use this bundle's class loader so the embedded Nashorn is found even though the JRE no longer ships an ECMAScript engine.
        ScriptEngineManager manager = new ScriptEngineManager(AstahApiScriptExecutor.class.getClassLoader());

        ScriptEngine engine = manager.getEngineByName("JavaScript");
        if (engine == null) {
            engine = manager.getEngineByName("ECMAScript");
        }
        if (engine == null) {
            engine = manager.getEngineByName("nashorn");
        }

        return engine;
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
