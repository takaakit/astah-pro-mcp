package com.astahpromcp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SwingApprovalPromptTest {
    
    @Test
    void buildDialogMessage_ok_namesTheClientPortAndTheServerPort() {
        // The plugin listens on one port per profile, so the dialog must say which of them the client reached, not only where the client came from.
        String message = SwingApprovalPrompt.buildDialogMessage(new ApprovalRequest("127.0.0.1", "localhost", 51325, 18888, "Test-Agent"));
        
        assertTrue(message.contains("127.0.0.1"), message);
        assertTrue(message.contains("localhost"), message);
        assertTrue(message.contains("51325"), message);
        assertTrue(message.contains("18888"), message);
        assertTrue(message.contains("Test-Agent"), message);
    }
    
    @Test
    void ask_ng_approvesNothingWhereNoDialogCanBeShown() throws Exception {
        SwingApprovalPrompt headless = new SwingApprovalPrompt(() -> true);
        
        assertFalse(headless.ask(new ApprovalRequest("127.0.0.1", "localhost", 51325, 18888, "Test-Agent")));
    }
}
