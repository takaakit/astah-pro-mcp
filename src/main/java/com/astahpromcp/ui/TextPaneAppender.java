package com.astahpromcp.ui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import javax.swing.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class TextPaneAppender extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private static volatile Consumer<String> logMessageConsumer;

    // Set the consumer that receives formatted log messages
    public static void setLogMessageConsumer(Consumer<String> consumer) {
        logMessageConsumer = consumer;
    }

    @Override
    protected void append(ILoggingEvent event) {
        // Read the volatile once: it may be cleared from another thread between the guard and the use.
        Consumer<String> consumer = logMessageConsumer;
        if (consumer == null) {
            return;
        }

        String timeStamp = TIME_FORMAT.format(Instant.ofEpochMilli(event.getTimeStamp()));
        String formattedMessage = timeStamp + "  " + event.getFormattedMessage();

        // Update the UI on the Swing EDT
        SwingUtilities.invokeLater(() -> consumer.accept(formattedMessage));
    }
}
