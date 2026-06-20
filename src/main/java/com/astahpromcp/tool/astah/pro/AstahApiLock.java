package com.astahpromcp.tool.astah.pro;

import java.util.concurrent.locks.ReentrantLock;

// Process-wide lock that serializes access to the Astah API.
public final class AstahApiLock {

    // Fair lock: requests from multiple AI agents are served FIFO.
    public static final ReentrantLock LOCK = new ReentrantLock(true);

    private AstahApiLock() {
    }
}
