package com.astahpromcp.tool.astah.pro;

public class FakeTransactionBoundary implements TransactionBoundary {

    private boolean inTransaction;
    private int beginCount;
    private int commitCount;
    private int abortCount;
    private boolean failOnCommit;
    private boolean failOnBegin;

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
}
