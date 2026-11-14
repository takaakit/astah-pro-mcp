package com.astahpromcp.tool.mcptoolscript;

import java.util.concurrent.atomic.AtomicReference;

// Decides once per run whether it may commit: the runner's beginCommit and the caller's abandon race, and the first wins.
// Reading a flag and then committing would miss an abandon that lands in between or during the commit.
// Never handed to the script engine, so that a script has no say in whether its own run commits.
final class McpToolScriptRunCompletion {
    
    private enum Phase {
        RUNNING,
        ABANDONED,
        COMMITTING
    }
    
    private final AtomicReference<Phase> phase = new AtomicReference<>(Phase.RUNNING);
    
    // Set by every abandon, including one that came after the commit had begun.
    private volatile boolean abandonRequested;

    // The caller's side. True when this kept the run from committing, false when the commit had already begun.
    boolean abandon() {
        abandonRequested = true;
        return phase.compareAndSet(Phase.RUNNING, Phase.ABANDONED);
    }
    
    // The runner's side, once its script succeeded. False when the run was abandoned first and has to roll back.
    boolean beginCommit() {
        return phase.compareAndSet(Phase.RUNNING, Phase.COMMITTING);
    }
    
    // Whether the run may no longer start any work, and will not commit.
    boolean abandoned() {
        return phase.get() == Phase.ABANDONED;
    }
    
    // Whether the run went on to commit before anybody gave up on it.
    boolean commitStarted() {
        return phase.get() == Phase.COMMITTING;
    }
    
    // Whether the caller stopped waiting, whichever side won.
    boolean abandonRequested() {
        return abandonRequested;
    }
}
