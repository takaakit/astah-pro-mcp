package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.editor.ITransactionManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionSupportTest {

    // Fake transaction manager that tracks the transaction state and call counts.
    private static class FakeTransactionManager implements ITransactionManager {

        private boolean inTransaction;
        private int beginCount;
        private int endCount;
        private int abortCount;
        private boolean failOnAbort;

        @Override
        public void beginTransaction() {
            inTransaction = true;
            beginCount++;
        }

        @Override
        public void endTransaction() {
            inTransaction = false;
            endCount++;
        }

        @Override
        public void abortTransaction() {
            if (failOnAbort) {
                throw new IllegalStateException("abort failure");
            }
            inTransaction = false;
            abortCount++;
        }

        @Override
        public boolean isInTransaction() {
            return inTransaction;
        }
    }

    @Test
    void call_ok_commitsAndReturnsResult() throws Exception {
        FakeTransactionManager manager = new FakeTransactionManager();
        TransactionSupport support = new TransactionSupport(manager);

        String result = support.call(() -> "done");

        assertEquals("done", result);
        assertEquals(1, manager.beginCount, "Transaction should be started once");
        assertEquals(1, manager.endCount, "Transaction should be committed once");
        assertEquals(0, manager.abortCount, "Transaction should not be aborted on success");
        assertFalse(manager.isInTransaction(), "No transaction should be left open");
    }

    @Test
    void call_ng_abortsWhenActionThrowsException() {
        FakeTransactionManager manager = new FakeTransactionManager();
        TransactionSupport support = new TransactionSupport(manager);

        Exception thrown = assertThrows(IllegalArgumentException.class,
                () -> support.call(() -> {
                    throw new IllegalArgumentException("action failure");
                }));

        assertEquals("action failure", thrown.getMessage(), "The action's exception should propagate as is");
        assertEquals(0, manager.endCount, "Transaction should not be committed on failure");
        assertEquals(1, manager.abortCount, "Transaction should be aborted on failure");
        assertFalse(manager.isInTransaction(), "No transaction should be left open");
    }

    @Test
    void call_ng_abortsWhenActionThrowsError() {
        FakeTransactionManager manager = new FakeTransactionManager();
        TransactionSupport support = new TransactionSupport(manager);

        Error thrown = assertThrows(StackOverflowError.class,
                () -> support.call(() -> {
                    throw new StackOverflowError("action error");
                }));

        assertEquals("action error", thrown.getMessage(), "The action's error should propagate as is");
        assertEquals(0, manager.endCount, "Transaction should not be committed on failure");
        assertEquals(1, manager.abortCount, "Transaction should be aborted even when an Error is thrown");
        assertFalse(manager.isInTransaction(), "No transaction should be left open");
    }

    @Test
    void call_ng_doesNotMaskOriginalFailureWhenAbortFails() {
        FakeTransactionManager manager = new FakeTransactionManager();
        manager.failOnAbort = true;
        TransactionSupport support = new TransactionSupport(manager);

        Exception thrown = assertThrows(IllegalArgumentException.class,
                () -> support.call(() -> {
                    throw new IllegalArgumentException("action failure");
                }));

        assertEquals("action failure", thrown.getMessage(),
                "The action's exception should propagate even when the abort itself fails");
    }

    @Test
    void run_ok_commitsOnSuccess() throws Exception {
        FakeTransactionManager manager = new FakeTransactionManager();
        TransactionSupport support = new TransactionSupport(manager);

        support.run(() -> {
        });

        assertEquals(1, manager.beginCount, "Transaction should be started once");
        assertEquals(1, manager.endCount, "Transaction should be committed once");
        assertEquals(0, manager.abortCount, "Transaction should not be aborted on success");
        assertFalse(manager.isInTransaction(), "No transaction should be left open");
    }

    @Test
    void run_ng_abortsWhenCommandThrowsException() {
        FakeTransactionManager manager = new FakeTransactionManager();
        TransactionSupport support = new TransactionSupport(manager);

        Exception thrown = assertThrows(IllegalArgumentException.class,
                () -> support.run(() -> {
                    throw new IllegalArgumentException("command failure");
                }));

        assertEquals("command failure", thrown.getMessage(), "The command's exception should propagate as is");
        assertEquals(0, manager.endCount, "Transaction should not be committed on failure");
        assertEquals(1, manager.abortCount, "Transaction should be aborted on failure");
        assertFalse(manager.isInTransaction(), "No transaction should be left open");
    }

    @Test
    void run_ng_abortsWhenCommandThrowsError() {
        FakeTransactionManager manager = new FakeTransactionManager();
        TransactionSupport support = new TransactionSupport(manager);

        Error thrown = assertThrows(StackOverflowError.class,
                () -> support.run(() -> {
                    throw new StackOverflowError("command error");
                }));

        assertEquals("command error", thrown.getMessage(), "The command's error should propagate as is");
        assertEquals(0, manager.endCount, "Transaction should not be committed on failure");
        assertEquals(1, manager.abortCount, "Transaction should be aborted even when an Error is thrown");
        assertFalse(manager.isInTransaction(), "No transaction should be left open");
    }
}
