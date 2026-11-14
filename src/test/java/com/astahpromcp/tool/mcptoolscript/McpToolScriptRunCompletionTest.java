package com.astahpromcp.tool.mcptoolscript;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class McpToolScriptRunCompletionTest {
    
    @Test
    void new_ok_isNeitherAbandonedNorCommitting() {
        McpToolScriptRunCompletion completion = new McpToolScriptRunCompletion();
        
        assertFalse(completion.abandoned());
        assertFalse(completion.commitStarted());
        assertFalse(completion.abandonRequested(), "Nobody has stopped waiting for a run that has just begun");
    }
    
    // The caller gave up first, so the run must not commit: the caller has been told it keeps nothing.
    @Test
    void beginCommit_ng_isRefusedOnceTheRunWasAbandoned() {
        McpToolScriptRunCompletion completion = new McpToolScriptRunCompletion();
        
        assertTrue(completion.abandon(), "Giving up on a run that is still going keeps it from committing");
        assertFalse(completion.beginCommit());
        
        assertTrue(completion.abandoned());
        assertFalse(completion.commitStarted());
        assertTrue(completion.abandonRequested());
    }
    
    // The run asked to commit first, so a later abandon does not call the commit off, and says so.
    @Test
    void abandon_ng_doesNotCallOffACommitThatHasBegun() {
        McpToolScriptRunCompletion completion = new McpToolScriptRunCompletion();
        
        assertTrue(completion.beginCommit());
        assertFalse(completion.abandon(), "The commit had begun, so this abandonment did not stop it");
        
        assertTrue(completion.commitStarted());
        assertFalse(completion.abandoned(), "A committing run is not refused further work it will never ask for anyway");
        assertTrue(completion.abandonRequested(), "The caller did stop waiting, and how the run ended has to be logged for it");
    }
    
    // Whatever the interleaving, exactly one side wins, and the completion agrees with the winner.
    @Test
    void abandonAndBeginCommit_ok_settleOnExactlyOneWinnerWhenTheyRace() throws Exception {
        for (int i = 0; i < 1_000; i++) {
            McpToolScriptRunCompletion completion = new McpToolScriptRunCompletion();
            CyclicBarrier start = new CyclicBarrier(2);
            AtomicBoolean committed = new AtomicBoolean();
            
            Thread runner = new Thread(() -> {
                await(start);
                committed.set(completion.beginCommit());
            });
            runner.start();
            await(start);
            boolean abandoned = completion.abandon();
            runner.join(10_000);
            
            assertFalse(runner.isAlive());
            assertNotEquals(abandoned, committed.get(), "Exactly one side may win, on pass " + i);
            assertEquals(abandoned, completion.abandoned(), "On pass " + i);
            assertEquals(committed.get(), completion.commitStarted(), "On pass " + i);
        }
    }
    
    private static void await(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
