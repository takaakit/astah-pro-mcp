package com.astahpromcp.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.astahpromcp.ui.TextPaneAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class LogbackConfig {

    private static volatile Path logFilePath;

    private LogbackConfig() {
    }

    public static void configure(Path outputDir) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // Clear any existing configuration
        context.reset();
        
        // Create pattern encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss.SSS} [%-5level] %msg%n");
        encoder.start();
        
        // Create file appender
        FileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("FILE");
        
        // Set log file path under the provided output directory
        Path logPath = outputDir.resolve("astah-pro-mcp.log");
        logFilePath = logPath;  // Store for getter access
        fileAppender.setFile(logPath.toString());
        
        // Set append to false (overwrite file)
        fileAppender.setAppend(false);
        
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        
        // Create TextPaneAppender
        TextPaneAppender textPaneAppender = new TextPaneAppender();
        textPaneAppender.setContext(context);
        textPaneAppender.setName("TEXTPANE");
        textPaneAppender.start();
        
        // Configure root logger
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(fileAppender);
        rootLogger.addAppender(textPaneAppender);
        rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        
        // Ensure the log directory exists
        try {
            java.nio.file.Files.createDirectories(logPath.getParent());
        } catch (java.io.IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }

    // Get the log file path
    public static Path getLogFilePath() {
        return logFilePath;
    }
}
