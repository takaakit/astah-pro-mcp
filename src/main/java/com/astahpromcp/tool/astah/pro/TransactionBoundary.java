package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.editor.TransactionManager;

// The Astah transaction, reduced to what a caller that owns one for a whole run of its own needs from it. It exists to be replaceable.
public interface TransactionBoundary {

    // The real one: the transaction of the running Astah.
    TransactionBoundary ASTAH = new TransactionBoundary() {

        @Override
        public void begin() {
            TransactionManager.beginTransaction();
        }

        @Override
        public void commit() {
            TransactionManager.endTransaction();
        }

        @Override
        public void abort() {
            TransactionManager.abortTransaction();
        }

        @Override
        public boolean isInTransaction() {
            return TransactionManager.isInTransaction();
        }
    };

    void begin();

    void commit();

    void abort();

    boolean isInTransaction();
}
