package com.astahpromcp.ui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import javax.swing.*;
import java.util.function.Consumer;

public class TextPaneAppender extends AppenderBase<ILoggingEvent> {
    
    private static Consumer<String> logMessageConsumer;
    
    // Set the consumer that receives formatted log messages
    public static void setLogMessageConsumer(Consumer<String> consumer) {
        logMessageConsumer = consumer;
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        if (logMessageConsumer != null) {
            // Format the log message with a timestamp
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
            String timeStamp = timeFormat.format(new java.util.Date(event.getTimeStamp()));
            String formattedMessage = String.format("%s  %s", 
                timeStamp,
                event.getFormattedMessage());
            
            // Update the UI on the Swing EDT
            SwingUtilities.invokeLater(() -> {
                logMessageConsumer.accept(formattedMessage);
            });
        }
    }
}
