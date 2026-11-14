package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.mcptoolscript.outputdto.ModelChangesKind;

import java.util.List;
import java.util.concurrent.Callable;

// What an mcp tool script run needs from whoever owns the rules for calling the tool functions: a watched thread, one unit of change, and what settles after each call.
public interface McpToolScriptHost {
    
    // How a run ended, as far as the caller can tell.
    enum RunKind {
        COMPLETED,
        TIMED_OUT,
        INTERRUPTED,
        FAILED
    }
    
    // What became of one run: value is set only for COMPLETED, failure only for FAILED.
    record RunOutcome<T>(RunKind kind, T value, Throwable failure) {
    }
    
    // Runs the work on a thread of its own for at most timeoutSeconds.
    // On giving up, runs onAbandon on the calling thread before interrupting the runner and before returning.
    <T> RunOutcome<T> runWatched(long timeoutSeconds, Callable<T> work, Runnable onAbandon);
    
    // Makes the tool functions called on this thread share one unit of change until the scope stops sharing; call it on the thread that runs the script.
    RunScope openRunScope();

    // Lets the application finish applying one tool function call, failed or not, before the script reads anything back.
    void afterToolCall(String toolName);
    
    // One run's unit of change.
    interface RunScope {
        
        // From here on the tool functions called on this thread manage their own changes again. Safe to call more than once.
        void stopSharing();
        
        // Keeps the run's changes when commit is true and undoes them otherwise. Never throws; call it after stopSharing, on the thread that opened the scope.
        Closing close(boolean commit);
    }
    
    // What closing did with the run's changes, what went wrong (or null), and the notes the run's error output has to carry, in order.
    record Closing(ModelChangesKind modelChanges, Throwable failure, List<String> notes) {
        
        public Closing {
            notes = List.copyOf(notes);
        }
    }
}
