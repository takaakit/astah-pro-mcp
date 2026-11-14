package com.astahpromcp.tool.astah.pro;

import java.util.concurrent.CountDownLatch;

public class FakeTransactionBoundary implements TransactionBoundary {
    
    private boolean inTransaction;
    private int beginCount;
    private int commitCount;
    private int abortCount;
    private boolean failOnCommit;
    private boolean failOnBegin;
    private boolean failOnAbort;
    private boolean failOnFirstAbortOnly;
    private CountDownLatch commitEntered;
    private CountDownLatch commitRelease;
    private volatile boolean interruptedDuringCommit;
    
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
        if (commitEntered != null) {
            commitEntered.countDown();
            awaitReleaseThroughInterrupts();
        }
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
    
    // Makes the commit signal that it has begun and then wait, through interrupts, until the test releases it.
    public void holdCommit(CountDownLatch entered, CountDownLatch release) {
        commitEntered = entered;
        commitRelease = release;
    }
    
    // Whether a held commit saw an interrupt, before or during its wait.
    public boolean interruptedDuringCommit() {
        return interruptedDuringCommit;
    }
    
    private void awaitReleaseThroughInterrupts() {
        boolean interrupted = Thread.currentThread().isInterrupted();
        while (true) {
            try {
                commitRelease.await();
                break;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            interruptedDuringCommit = true;
            // Restored, as a blocking call that ignores an interrupt still leaves the flag set.
            Thread.currentThread().interrupt();
        }
    }
}
