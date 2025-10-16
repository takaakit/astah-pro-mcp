package com.astahpromcp.ui;

import com.astahpromcp.config.LogbackConfig;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class ExtraTabPanel extends JPanel {
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

    public void appendLogMessage(String message) {
        if (logTextArea != null) {
            logTextArea.append(message + "\n");
            javax.swing.SwingUtilities.invokeLater(() -> {
                logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            });
        }
    }

    private void openLogFile() {
        Path logFilePath = LogbackConfig.getLogFilePath();
        if (logFilePath != null && logFilePath.toFile().exists()) {
            try {
                Desktop.getDesktop().open(logFilePath.toFile());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to open log file: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Log file not found",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}

