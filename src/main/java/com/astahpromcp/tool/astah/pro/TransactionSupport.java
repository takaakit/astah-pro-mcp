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

    private final ITransactionManager transactionManager;

    public TransactionSupport(ITransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // Runs the given action (a model edit with result) inside a transaction
    public <T> T call(TransactionalAction<T> action) throws Exception {
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

    // Runs the given command (a model edit with no result) inside a transaction
    public void run(TransactionalCommand command) throws Exception {
        call(() -> {
            command.execute();
            return null;
        });
    }
}
