package com.astahpromcp.tool.astah.pro.mcptoolscript;

import com.astahpromcp.tool.astah.pro.FakeTransactionBoundary;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.mcptoolscript.McpToolScriptHost;
import com.astahpromcp.tool.mcptoolscript.outputdto.ModelChangesKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class AstahMcpToolScriptHostTest {
    
    // AWT is always up inside Astah; starting it here keeps it from starting on an interrupted test thread, which clears the interrupt.
    @BeforeAll
    static void startAwt() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
    }
    
    @AfterEach
    void stopSharing() {
        TransactionSupport.stopSharingOnThisThread();
    }
    
    // The handlers an mcp tool script calls are unwrapped, so the host drains the EDT after each one, as the lock wrapper does for a direct call.
    @Test
    void afterToolCall_ok_drainsTheEventDispatchThread() {
        AstahMcpToolScriptHost host = new AstahMcpToolScriptHost(new FakeTransactionBoundary());
        
        for (int i = 0; i < 3; i++) {
            AtomicBoolean done = new AtomicBoolean(false);
            // Stands in for the work Astah queues on the EDT while applying an edit. Slow on purpose: without the drain the call returns long before this finishes, so dropping the drain fails every run instead of most of them.
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                done.set(true);
            });
            
            host.afterToolCall("set_name_of_named_element");
            
            assertTrue(done.get(), "Work queued on the EDT by call " + (i + 1) + " must have run before the call returns");
        }
    }
    
    // A run that timed out interrupts the runner thread, and the drain after its tool call must not swallow that interrupt.
    @Test
    void afterToolCall_ok_keepsTheInterruptOfAnInterruptedCaller() throws Exception {
        AstahMcpToolScriptHost host = new AstahMcpToolScriptHost(new FakeTransactionBoundary());
        
        AtomicBoolean interruptKept = new AtomicBoolean();
        Thread caller = new Thread(() -> {
            Thread.currentThread().interrupt();
            host.afterToolCall("set_name_of_named_element");
            interruptKept.set(Thread.currentThread().isInterrupted());
        }, "interrupted-caller");
        caller.start();
        caller.join(10_000);
        
        assertFalse(caller.isAlive(), "The call should have returned");
        assertTrue(interruptKept.get(), "The caller's interrupt flag must survive the EDT drain");
    }
    
    // The run's edits join the transaction the scope shares, which TransactionSupport opens on the first edit.
    @Test
    void openRunScope_ok_makesTheToolFunctionsOnThisThreadShareOneTransaction() throws Exception {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        AstahMcpToolScriptHost host = new AstahMcpToolScriptHost(transaction);
        TransactionSupport support = new TransactionSupport(null);
        
        McpToolScriptHost.RunScope scope = host.openRunScope();
        support.run(() -> { });
        support.run(() -> { });
        scope.stopSharing();
        McpToolScriptHost.Closing closing = scope.close(true);
        
        assertEquals(1, transaction.beginCount(), "Two edits of one run open one transaction");
        assertEquals(1, transaction.commitCount());
        assertEquals(ModelChangesKind.COMMITTED, closing.modelChanges());
        assertNull(closing.failure());
        assertEquals(List.of(), closing.notes());
    }
    
    // A run that only read never opened the transaction, and closing it must not commit an empty one: committing marks the project modified.
    @Test
    void close_ok_reportsNoChangesWhenTheRunNeverEdited() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        McpToolScriptHost.RunScope scope = new AstahMcpToolScriptHost(transaction).openRunScope();
        scope.stopSharing();
        
        McpToolScriptHost.Closing closing = scope.close(true);
        
        assertEquals(ModelChangesKind.NONE, closing.modelChanges());
        assertEquals(0, transaction.commitCount());
        assertEquals(0, transaction.abortCount());
    }
    
    @Test
    void close_ng_rollsBackWhenTheRunFailed() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        McpToolScriptHost.RunScope scope = new AstahMcpToolScriptHost(transaction).openRunScope();
        transaction.begin();
        scope.stopSharing();
        
        McpToolScriptHost.Closing closing = scope.close(false);
        
        assertEquals(ModelChangesKind.ROLLED_BACK, closing.modelChanges());
        assertEquals(0, transaction.commitCount());
        assertEquals(1, transaction.abortCount());
        assertNull(closing.failure());
    }
    
    // Astah keeps a transaction whose commit it refused, and changes nobody could confirm must not survive that.
    @Test
    void close_ng_rollsBackAndSaysSoWhenTheCommitIsRefused() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        transaction.failOnCommit();
        McpToolScriptHost.RunScope scope = new AstahMcpToolScriptHost(transaction).openRunScope();
        transaction.begin();
        scope.stopSharing();
        
        McpToolScriptHost.Closing closing = scope.close(true);
        
        assertEquals(ModelChangesKind.ROLLED_BACK, closing.modelChanges());
        assertNotNull(closing.failure(), "The refused commit is what the run reports as its failure");
        assertEquals(1, closing.notes().size());
        assertTrue(closing.notes().get(0).startsWith("Failed to close the transaction of the mcp tool script"), closing.notes().get(0));
        assertFalse(transaction.isInTransaction());
    }
    
    // When even the abort is refused, whether the changes are gone cannot be told.
    @Test
    void close_ng_reportsUnknownWhenNeitherCommitNorAbortSucceeds() {
        FakeTransactionBoundary transaction = new FakeTransactionBoundary();
        transaction.failOnCommit();
        transaction.failOnAbort();
        McpToolScriptHost.RunScope scope = new AstahMcpToolScriptHost(transaction).openRunScope();
        transaction.begin();
        scope.stopSharing();
        
        McpToolScriptHost.Closing closing = scope.close(true);
        
        assertEquals(ModelChangesKind.UNKNOWN, closing.modelChanges());
        assertNotNull(closing.failure());
        assertTrue(closing.notes().size() >= 2, "Both the refused commit and the transaction still left open are reported: " + closing.notes());
    }
}
