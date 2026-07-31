package com.astahpromcp.tool.astah.pro.script;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.astah.pro.AstahApiLock;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Runs JavaScript (the bundled Nashorn engine, via JSR-223) inside Astah's JVM with the Astah API bound as global variables.
@Slf4j
public class AstahScriptExecutor {

    // Immutable outcome of one script run.
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

    public AstahScriptExecutor(ProjectAccessor projectAccessor) {
        this(projectAccessor, McpServerConfig.SCRIPT_EXECUTION_TIMEOUT_SECONDS);
    }

    public AstahScriptExecutor(ProjectAccessor projectAccessor, long timeoutSeconds) {
        this.projectAccessor = projectAccessor;
        this.timeoutSeconds = timeoutSeconds;
    }

    // Evaluates the script on a dedicated thread with a bounded timeout.
    // Never throws: every failure mode is reported through the Result.
    public Result execute(String script) {
        FutureTask<Result> task = new FutureTask<>(() -> evaluate(script));
        // One thread per run: a lingering stuck script must not poison later runs.
        Thread runner = new Thread(task, "astah-script-runner");
        runner.setDaemon(true);
        runner.start();

        try {
            return task.get(timeoutSeconds, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            // Best effort: a script blocked in an interruptible call (sleep/wait/IO) stops here.
            // A script stuck in a plain computation loop cannot be stopped at all: Java has no way to terminate a thread, and killing one mid-transaction would corrupt the model anyway.
            runner.interrupt();

            String message = String.format(
                "Script execution timed out after %d seconds. The script thread was interrupted but may still be running, so Astah API access is blocked until it stops. Wait and retry; restart Astah if it never stops. Keep scripts short and avoid blocking operations.",
                timeoutSeconds);
            log.warn(message);

            // The abandoned thread may still be calling the Astah API, so no tool may run until it terminates. Only the fact is recorded here: whether waiting helps or Astah must be restarted depends on whether the thread honours the interrupt, which is not known yet and is diagnosed by AstahApiLock when a later tool call is refused.
            AstahApiLock.suspend(runner, String.format(
                "a script that timed out after %d seconds is still running", timeoutSeconds));

            return Result.failure(
                message,
                -1,
                -1,
                "",
                "");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            runner.interrupt();
            
            return Result.failure(
                "Interrupted while waiting for script execution",
                -1,
                -1,
                "",
                "");

        } catch (ExecutionException e) {
            // evaluate() reports its own failures through the Result; this is a safety net.
            Throwable cause = e.getCause();
            
            return Result.failure(
                String.valueOf(cause != null ? cause : e),
                -1,
                -1,
                "",
                "");
        }
    }

    // Evaluates the script on the calling thread. Never throws: every failure mode is reported through the Result.
    private Result evaluate(String script) {
        StringWriter output = new StringWriter();
        StringWriter errorOutput = new StringWriter();

        ScriptEngine engine = createEngine();
        if (engine == null) {
            return Result.failure(
                "No JavaScript engine is available",
                -1,
                -1,
                "",
                "");
        }
        // Capture print() and error output per run instead of hijacking System.out/err
        engine.getContext().setWriter(new PrintWriter(output, true));
        engine.getContext().setErrorWriter(new PrintWriter(errorOutput, true));
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

    // Safety net for edit scripts that fail between beginTransaction and endTransaction
    private static void abortDanglingTransaction(StringWriter errorOutput) {
        try {
            if (TransactionManager.isInTransaction()) {
                TransactionManager.abortTransaction();
                errorOutput.write("A transaction left open by the script was aborted.\n");
            }
        
        } catch (Throwable t) {
            // If the abort itself fails, every later edit would be blocked; make it visible.
            log.warn("Failed to abort a transaction left open by the script", t);
            errorOutput.write("Failed to abort a transaction left open by the script: " + t + "\n");
        }
    }

    private static ScriptEngine createEngine() {
        // Use this bundle's class loader so the embedded Nashorn is found even though the JRE no longer ships an ECMAScript engine.
        ScriptEngineManager manager = new ScriptEngineManager(AstahScriptExecutor.class.getClassLoader());

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
