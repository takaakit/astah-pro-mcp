package com.astahpromcp.tool.astah.pro.project;

import com.astahpromcp.tool.astah.pro.EdtSupport;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class CurrentProjectXmiExporterTest {

    @Test
    void conflictReason_ok_acceptsACaptureNothingGotInTheWayOf() {
        assertNull(CurrentProjectXmiExporter.conflictReason(0, false, false, false, EdtSupport.DrainOutcome.DRAINED));
    }

    @Test
    void conflictReason_ng_refusesACaptureAnEditOrASwitchGotInTheWayOf() {
        assertNotNull(CurrentProjectXmiExporter.conflictReason(1, false, false, false, EdtSupport.DrainOutcome.DRAINED));
        assertNotNull(CurrentProjectXmiExporter.conflictReason(0, true, false, false, EdtSupport.DrainOutcome.DRAINED));
        assertNotNull(CurrentProjectXmiExporter.conflictReason(0, false, true, false, EdtSupport.DrainOutcome.DRAINED));
        assertNotNull(CurrentProjectXmiExporter.conflictReason(0, false, false, true, EdtSupport.DrainOutcome.DRAINED));
        assertNotNull(CurrentProjectXmiExporter.conflictReason(0, false, false, false, EdtSupport.DrainOutcome.TIMED_OUT));
        assertNotNull(CurrentProjectXmiExporter.conflictReason(0, false, false, false, EdtSupport.DrainOutcome.INTERRUPTED));
    }

    // Stands for Astah's exception, which the capture recognizes by its name
    private static final class BadTransactionException extends RuntimeException {
        BadTransactionException() {
            super("Operation should be out of transaction.");
        }
    }

    @Test
    void failureOf_ok_acceptsACaptureThatNothingGotInTheWayOf() {
        assertNull(CurrentProjectXmiExporter.failureOf(null, null));
    }

    @Test
    void failureOf_ng_keepsWhatTheExportThrewAsTheCauseOfAConflict() {
        RuntimeException thrown = new RuntimeException("stopped half way");

        IllegalStateException failure = CurrentProjectXmiExporter.failureOf("the project was being edited", thrown);

        assertTrue(failure.getMessage().contains("could not be captured"), failure.getMessage());
        assertSame(thrown, failure.getCause());
    }

    @Test
    void failureOf_ng_takesARefusalForAnOpenTransactionAsAConflict() {
        BadTransactionException thrown = new BadTransactionException();

        IllegalStateException failure = CurrentProjectXmiExporter.failureOf(null, thrown);

        assertTrue(failure.getMessage().contains("being edited"), failure.getMessage());
        assertSame(thrown, failure.getCause());
    }

    @Test
    void failureOf_ng_reportsAnotherFailureOfTheExportWithItsCause() {
        RuntimeException thrown = new RuntimeException("disk full");

        IllegalStateException failure = CurrentProjectXmiExporter.failureOf(null, thrown);

        assertTrue(failure.getMessage().startsWith("Failed to write the XMI"), failure.getMessage());
        assertTrue(failure.getMessage().contains("disk full"), failure.getMessage());
        assertSame(thrown, failure.getCause());
    }

    @Test
    void transactionWatcher_ok_remembersATransactionThatOpenedAndClosedMeanwhile() throws Exception {
        AtomicBoolean open = new AtomicBoolean();
        CurrentProjectXmiExporter.TransactionWatcher watcher = new CurrentProjectXmiExporter.TransactionWatcher(open::get);
        watcher.start();

        open.set(true);
        Thread.sleep(50);
        open.set(false);

        assertTrue(watcher.stop());
    }

    @Test
    void transactionWatcher_ok_seesNothingWhenNothingOpened() throws Exception {
        CurrentProjectXmiExporter.TransactionWatcher watcher = new CurrentProjectXmiExporter.TransactionWatcher(() -> false);
        watcher.start();
        Thread.sleep(20);

        assertFalse(watcher.stop());
    }
}
