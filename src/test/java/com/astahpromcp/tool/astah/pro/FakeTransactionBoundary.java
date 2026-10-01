package com.astahpromcp.tool.astah.pro;

public class FakeTransactionBoundary implements TransactionBoundary {

    private boolean inTransaction;
    private int beginCount;
    private int commitCount;
    private int abortCount;
    private boolean failOnCommit;
    private boolean failOnBegin;
    private boolean failOnAbort;
    private boolean failOnFirstAbortOnly;

    @Override
    public void begin() {
        beginCount++;
        if (failOnBegin) {
            throw new IllegalStateException("someone else holds the transaction");
        }
        inTransaction = true;
    }

    @Override
    public void commit() {
        commitCount++;
        if (failOnCommit) {
            // Astah refuses the commit and keeps the transaction, exactly as it does when the model is left invalid.
            throw new IllegalStateException("commit refused");
        }
        inTransaction = false;
    }

    @Override
    public void abort() {
        abortCount++;
        if (failOnAbort) {
            if (failOnFirstAbortOnly) {
                failOnAbort = false;
            }
            throw new IllegalStateException("abort refused");
        }
        inTransaction = false;
    }

    @Override
    public boolean isInTransaction() {
        return inTransaction;
    }

    public int beginCount() {
        return beginCount;
    }

    public int commitCount() {
        return commitCount;
    }

    public int abortCount() {
        return abortCount;
    }

    public void failOnCommit() {
        failOnCommit = true;
    }

    public void failOnBegin() {
        failOnBegin = true;
    }

    public void failOnAbort() {
        failOnAbort = true;
    }

    // Refuses the next abort only, as a transaction does that is still busy the first time it is asked.
    public void failOnFirstAbort() {
        failOnAbort = true;
        failOnFirstAbortOnly = true;
    }
}
