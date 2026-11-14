package com.astahpromcp.tool.astah.pro;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class AstahApiLockTest {
    
    @AfterEach
    void tearDown() {
        AstahApiLock.clearSuspension();
    }
    
    @Test
    void clearLiftedSuspension_ok_findsNothingWithoutASuspension() {
        assertFalse(AstahApiLock.clearLiftedSuspension(), "There is nothing to settle when no thread was abandoned");
    }
    
    @Test
    void clearLiftedSuspension_ok_keepsTheSuspensionWhileTheAbandonedThreadIsAlive() throws Exception {
        CountDownLatch stopAbandoned = new CountDownLatch(1);
        Thread abandoned = new Thread(() -> {
            try {
                stopAbandoned.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "abandoned-thread");
        abandoned.start();
        AstahApiLock.suspend(abandoned, "a timed-out script did not stop");
        
        try {
            assertFalse(AstahApiLock.clearLiftedSuspension(), "A suspension whose thread is alive has not lifted");
            assertNotNull(AstahApiLock.suspensionReason(), "Access should stay suspended while the abandoned thread is alive");
            
        } finally {
            stopAbandoned.countDown();
            abandoned.join();
        }
    }
    
    @Test
    void clearLiftedSuspension_ok_reportsATerminatedThreadOnlyOnce() throws Exception {
        Thread abandoned = new Thread(() -> {
        }, "abandoned-thread");
        abandoned.start();
        abandoned.join();
        AstahApiLock.suspend(abandoned, "a timed-out script that has since stopped");
        
        assertTrue(AstahApiLock.clearLiftedSuspension(), "The first caller should be told to settle what the terminated thread left behind");
        assertFalse(AstahApiLock.clearLiftedSuspension(), "A later caller should find nothing left to settle");
        assertNull(AstahApiLock.suspensionReason(), "Access should be usable once the abandoned thread has terminated");
    }
}
