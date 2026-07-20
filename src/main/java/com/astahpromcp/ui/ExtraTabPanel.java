package com.astahpromcp.ui;

import com.astahpromcp.config.LogbackConfig;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ExtraTabPanel extends JPanel {

    // Bound memory: keep roughly the last ~1 MB of log text in the panel.
    private static final int MAX_LOG_CHARS = 1_000_000;

    private JTextArea logTextArea;

    public ExtraTabPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(createInfoPanel(), BorderLayout.NORTH);
        add(createLogPane(), BorderLayout.CENTER);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel logFileLinkLabel = new JLabel("<html><a href='#'>Open log file</a></html>");
        logFileLinkLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        logFileLinkLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        logFileLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openLogFile();
            }
        });

        panel.add(logFileLinkLabel, BorderLayout.EAST);

        return panel;
    }

    private JScrollPane createLogPane() {
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setLineWrap(false);
        logTextArea.setBorder(null);

        Color currentForeground = logTextArea.getForeground();
        Color currentBackground = logTextArea.getBackground();
        int red = (int) ((currentForeground.getRed() + currentBackground.getRed()) * 0.5);
        int green = (int) ((currentForeground.getGreen() + currentBackground.getGreen()) * 0.5);
        int blue = (int) ((currentForeground.getBlue() + currentBackground.getBlue()) * 0.5);
        logTextArea.setForeground(new Color(red, green, blue));

        DefaultCaret caret = (DefaultCaret) logTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        return scrollPane;
    }

    // Called on the Swing EDT, so the document edits below are EDT-safe.
    public void appendLogMessage(String message) {
        if (logTextArea == null) {
            return;
        }
        logTextArea.append(message + "\n");

        // Drop the oldest text once the buffer exceeds the cap, to keep memory bounded in long sessions.
        Document doc = logTextArea.getDocument();
        int overflow = overflowChars(doc.getLength(), MAX_LOG_CHARS);
        if (overflow > 0) {
            try {
                doc.remove(0, overflow);
            } catch (BadLocationException e) {
                // Best-effort trim; ignore.
            }
        }

        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    // Number of leading characters to drop so that the buffer stays within the cap
    static int overflowChars(int length, int cap) {
        return Math.max(0, length - cap);
    }

    private void openLogFile() {
        Path logFilePath = LogbackConfig.getLogFilePath();
        if (logFilePath == null) {
            showDialog("Log file not found", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CompletableFuture.runAsync(() -> {
            File logFile = logFilePath.toFile();
            if (!logFile.exists()) {
                showDialog("Log file not found", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                showDialog("Opening the log file is not supported on this platform.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Desktop.getDesktop().open(logFile);
            } catch (IOException | SecurityException e) {
                showDialog("Failed to open log file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showDialog(String message, String title, int messageType) {
        Runnable task = () -> JOptionPane.showMessageDialog(this, message, title, messageType);
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }
}

