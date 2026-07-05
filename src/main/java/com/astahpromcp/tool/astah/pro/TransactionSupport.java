package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.editor.ITransactionManager;

// Helper for running Astah model edits within a transaction
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
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    // Runs the given command (a model edit with no result) inside a transaction
    public void run(TransactionalCommand command) throws Exception {
        transactionManager.beginTransaction();
        try {
            command.execute();
            transactionManager.endTransaction();
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
