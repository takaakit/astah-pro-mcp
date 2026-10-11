package com.astahpromcp.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.astahpromcp.ui.TextPaneAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LogbackConfig {

    static final Level DEFAULT_LOG_PANEL_LEVEL = Level.INFO;

    private static volatile Path logFilePath;
    private static volatile Level logPanelLevel = DEFAULT_LOG_PANEL_LEVEL;

    private LogbackConfig() {
    }

    public static void configure(Path outputDir) {
        configure(outputDir, resolveLogPanelLevel(System.getenv(McpServerConfig.ENV_LOG_PANEL_LEVEL)));
    }

    static void configure(Path outputDir, Level panelLevel) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // Clear any existing configuration
        context.reset();
        
        // Create pattern encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss.SSS} [%-5level] %msg%n");
        encoder.start();
        
        // Create file appender
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
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
        textPaneAppender.addFilter(thresholdFilter(context, panelLevel));
        textPaneAppender.start();
        logPanelLevel = panelLevel;

        // Configure root logger
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(fileAppender);
        rootLogger.addAppender(textPaneAppender);

        // Root at INFO
        rootLogger.setLevel(Level.INFO);

        ch.qos.logback.classic.Logger appLogger = context.getLogger("com.astahpromcp");
        appLogger.setLevel(Level.DEBUG);

        ch.qos.logback.classic.Logger mcpLogger = context.getLogger("io.modelcontextprotocol");
        mcpLogger.setLevel(Level.DEBUG);
        
        // Ensure the log directory exists
        try {
            Files.createDirectories(logPath.getParent());
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }

    static Level resolveLogPanelLevel(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_LOG_PANEL_LEVEL;
        } else {
            return Level.toLevel(value.trim(), DEFAULT_LOG_PANEL_LEVEL);
        }
    }

    private static ThresholdFilter thresholdFilter(LoggerContext context, Level level) {
        ThresholdFilter filter = new ThresholdFilter();
        filter.setContext(context);
        filter.setLevel(level.toString());
        filter.start();
        return filter;
    }

    public static Path getLogFilePath() {
        return logFilePath;
    }

    public static Level getLogPanelLevel() {
        return logPanelLevel;
    }
}
