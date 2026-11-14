package com.astahpromcp.tool.astah.pro.mcptoolscript;

import com.astahpromcp.tool.astah.pro.EdtSupport;
import com.astahpromcp.tool.astah.pro.TransactionBoundary;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.WatchedRunner;
import com.astahpromcp.tool.mcptoolscript.McpToolScriptHost;
import com.astahpromcp.tool.mcptoolscript.outputdto.ModelChangesKind;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

// The rules for calling the Astah API applied to an mcp tool script run, whose handlers are called unwrapped under the lock ExclusiveToolProvider holds for the whole run.
@Slf4j
public final class AstahMcpToolScriptHost implements McpToolScriptHost {
    
    private final TransactionBoundary transaction;
    
    public AstahMcpToolScriptHost() {
        this(TransactionBoundary.ASTAH);
    }
    
    // Taking the transaction so that a test can watch what a run does with it.
    public AstahMcpToolScriptHost(TransactionBoundary transaction) {
        this.transaction = transaction;
    }
    
    @Override
    public <T> RunOutcome<T> runWatched(long timeoutSeconds, Callable<T> work, Runnable onAbandon) {
        WatchedRunner.Outcome<T> outcome = WatchedRunner.run(
                "mcp-tool-script-runner", "an mcp tool script", timeoutSeconds, work, onAbandon);
        
        RunKind kind = switch (outcome.kind()) {
            case COMPLETED -> RunKind.COMPLETED;
            case TIMED_OUT -> RunKind.TIMED_OUT;
            case INTERRUPTED -> RunKind.INTERRUPTED;
            case FAILED -> RunKind.FAILED;
        };
        return new RunOutcome<>(kind, outcome.value(), outcome.failure());
    }
    
    @Override
    public RunScope openRunScope() {
        // The whole run shares one transaction, but TransactionSupport opens it lazily -- only the first time a tool function actually edits the model -- so a script that only reads never opens one at all.
        TransactionSupport.shareOnThisThread(transaction);
        return new TransactionScope();
    }
    
    @Override
    public void afterToolCall(String toolName) {
        // As the lock wrapper does for a direct call, so that a script never reads back an edit Astah has not finished applying.
        EdtSupport.drain(toolName, "continuing the mcp tool script");
    }
    
    // One run's share of the Astah transaction
    private final class TransactionScope implements RunScope {
        
        @Override
        public void stopSharing() {
            // From here on the tool functions manage their own transactions again. A run that times out never reaches this, which is why sharing is thread-scoped.
            TransactionSupport.stopSharingOnThisThread();
        }
        
        @Override
        public Closing close(boolean commit) {
            List<String> notes = new ArrayList<>();
            
            Closing closing = closeTransaction(commit, notes);
            ModelChangesKind modelChanges = closing.modelChanges();
            
            // Whatever a dangling transaction held is gone once it is aborted, which settles what closeTransaction could not.
            if (abortDanglingTransaction(notes) && modelChanges != ModelChangesKind.COMMITTED) {
                modelChanges = ModelChangesKind.ROLLED_BACK;
            }
            return new Closing(modelChanges, closing.failure(), notes);
        }
    }
    
    // Commits the transaction the run opened, or rolls the whole run back. Returns what became of the run's changes, and what went wrong, or null.
    private Closing closeTransaction(boolean commit, List<String> notes) {
        boolean opened = false;
        try {
            // A script that never edited anything never opened the shared transaction, and there is nothing to keep.
            opened = transaction.isInTransaction();
            if (!opened) {
                return new Closing(ModelChangesKind.NONE, null, List.of());
            }
            
            if (commit) {
                transaction.commit();
                return new Closing(ModelChangesKind.COMMITTED, null, List.of());
            }
            
            transaction.abort();
            return new Closing(ModelChangesKind.ROLLED_BACK, null, List.of());
        
        } catch (Throwable t) {
            log.error("Failed to close the transaction of an mcp tool script", t);
            notes.add("Failed to close the transaction of the mcp tool script: " + t);
            
            // A failed commit leaves the transaction open, so abort it; only an abort seen to succeed counts as a rollback.
            boolean rolledBack = false;
            if (opened) {
                try {
                    if (transaction.isInTransaction()) {
                        transaction.abort();
                        rolledBack = true;
                    }
                } catch (Throwable abortFailure) {
                    log.warn("Failed to abort after the commit of an mcp tool script failed", abortFailure);
                }
            }
            return new Closing(rolledBack ? ModelChangesKind.ROLLED_BACK : ModelChangesKind.UNKNOWN, t, List.of());
        }
    }
    
    // The net under closeTransaction. Returns whether it had to abort a transaction.
    private boolean abortDanglingTransaction(List<String> notes) {
        try {
            if (transaction.isInTransaction()) {
                log.error("An mcp tool script left a transaction open. This should be impossible: the run closes the transaction it opens, and an mcp tool script cannot reach TransactionManager itself.");
                transaction.abort();
                notes.add("A transaction left open by the mcp tool script was aborted.");
                return true;
            }
        
        } catch (Throwable t) {
            log.warn("Failed to abort a transaction left open by the mcp tool script", t);
            notes.add("Failed to abort a transaction left open by the mcp tool script: " + t);
        }
        return false;
    }
}
