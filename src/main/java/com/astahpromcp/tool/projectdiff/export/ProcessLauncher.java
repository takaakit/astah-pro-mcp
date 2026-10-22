package com.astahpromcp.tool.projectdiff.export;

import java.io.IOException;

// Starts the separate processes of comparisons and keeps track of them, so that stopping the server can stop them all
public interface ProcessLauncher {

    // Start the process and register it, or refuse when the server is stopping
    Process start(ProcessBuilder builder) throws IOException;

    // The process has ended, or has been given up on and stopped
    void finished(Process process);
}
