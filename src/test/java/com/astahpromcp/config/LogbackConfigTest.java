package com.astahpromcp.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LogbackConfigTest {

    // Close and detach the file appender so Windows releases the log file before @TempDir cleanup.
    @AfterEach
    void releaseLogFile() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> file = root.getAppender("FILE");
        if (file != null) {
            file.stop();
            root.detachAppender(file);
        }
    }

    @Test
    void configure_ok_rootIsInfoAndAppLoggerIsDebug(@TempDir Path tempDir) {
        LogbackConfig.configure(tempDir);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.classic.Logger app = context.getLogger("com.astahpromcp");

        assertEquals(Level.INFO, root.getLevel(), "Root must be INFO so third-party libraries stop flooding logs");
        assertEquals(Level.DEBUG, app.getLevel(), "The plugin's own logger must stay at DEBUG");
    }

    @Test
    void configure_ok_bothAppendersAttachedWithoutThresholdFilters(@TempDir Path tempDir) {
        LogbackConfig.configure(tempDir);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        Appender<ILoggingEvent> file = root.getAppender("FILE");
        Appender<ILoggingEvent> textpane = root.getAppender("TEXTPANE");
        assertNotNull(file, "File appender must be attached to the root logger");
        assertNotNull(textpane, "UI (text pane) appender must be attached to the root logger");

        // Neither appender is threshold-filtered: the effective level is controlled by the loggers
        // (root=INFO, com.astahpromcp=DEBUG), so the UI panel shows the plugin's DEBUG logs too.
        assertTrue(textpane.getCopyOfAttachedFiltersList().isEmpty(),
                "UI appender must not be threshold-filtered, so DEBUG logs appear in the panel");
        assertTrue(file.getCopyOfAttachedFiltersList().isEmpty(),
                "File appender must not be threshold-filtered");
    }
}
