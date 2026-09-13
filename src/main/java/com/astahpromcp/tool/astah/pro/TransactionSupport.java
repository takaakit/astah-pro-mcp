package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.editor.ITransactionManager;
import lombok.extern.slf4j.Slf4j;

// Helper for running Astah model edits within a transaction
@Slf4j
public final class TransactionSupport {

    @FunctionalInterface
    public interface TransactionalAction<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface TransactionalCommand {
        void execute() throws Exception;
    }

    // Set while a caller owns the transaction for a whole run of its own. An mcp tool script run is the one
    // caller that does this: it wants a single transaction around the whole script so that a failure partway
    // through rolls every tool function call back, and Astah refuses a nested beginTransaction, so every edit
    // inside has to join the open one instead of opening its own. The shared transaction is opened lazily, by
    // whichever call reaches here first while it is set, so a script that never edits anything never opens one --
    // committing a transaction marks the project modified even when nothing inside it changed.
    private static final ThreadLocal<TransactionBoundary> SHARED = new ThreadLocal<>();

    // Join the given transaction for the duration of one run on this thread, instead of managing its own.
    public static void shareOnThisThread(TransactionBoundary boundary) {
        SHARED.set(boundary);
    }

    // Give transaction management back to this class.
    public static void stopSharingOnThisThread() {
        SHARED.remove();
    }

    private final ITransactionManager transactionManager;

    public TransactionSupport(ITransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // Runs the given action (a model edit with result) inside a transaction
    public <T> T call(TransactionalAction<T> action) throws Exception {
        TransactionBoundary shared = SHARED.get();
        if (shared != null) {
            if (!shared.isInTransaction()) {
                try {
                    shared.begin();
                } catch (Throwable t) {
                    throw new IllegalStateException("Could not open the shared Astah transaction: " + t + ". Retry shortly.", t);
                }
            }
            // Committing or aborting the shared transaction is the caller's decision, not this one's: the caller owns it for the whole run and is the only one who knows whether the run as a whole succeeded.
            return action.execute();

        } else {
            transactionManager.beginTransaction();
            try {
                T result = action.execute();
                transactionManager.endTransaction();
                return result;

            } catch (Throwable t) {
                try {
                    if (transactionManager.isInTransaction()) {
                        transactionManager.abortTransaction();
                    }
                } catch (Throwable abortFailure) {
                    log.warn("Failed to abort the transaction after a failed edit", abortFailure);
                }
                throw t;
            }
        }
    }

    // Runs the given command (a model edit with no result) inside a transaction
    public void run(TransactionalCommand command) throws Exception {
        call(() -> {
            command.execute();
            return null;
        });
    }
}
