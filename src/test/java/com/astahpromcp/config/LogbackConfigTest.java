package com.astahpromcp.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.FilterReply;
import com.astahpromcp.ui.TextPaneAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class LogbackConfigTest {

    // Stop every appender so Windows releases the log file before @TempDir cleanup, then go back to logback-test.xml:
    // configure() replaces the whole logging setup of this JVM, and the tests that run after this one log through it.
    @AfterEach
    void tearDown() throws JoranException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        new ContextInitializer(context).autoConfig();
    }

    @Test
    void configure_ok_rootIsInfoAndAppLoggerIsDebug(@TempDir Path tempDir) {
        LogbackConfig.configure(tempDir);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.classic.Logger app = context.getLogger("com.astahpromcp");

        assertEquals(Level.INFO, root.getLevel(), "Root must be INFO so third-party libraries stop flooding logs");
        assertEquals(Level.DEBUG, app.getLevel(), "The plugin's own logger must stay at DEBUG");
    }

    @Test
    void configure_ok_panelIsFilteredAtInfoAndFileIsNotFiltered(@TempDir Path tempDir) {
        LogbackConfig.configure(tempDir, LogbackConfig.DEFAULT_LOG_PANEL_LEVEL);

        Appender<ILoggingEvent> file = rootAppender("FILE");
        Appender<ILoggingEvent> textpane = rootAppender("TEXTPANE");
        assertNotNull(file, "File appender must be attached to the root logger");
        assertNotNull(textpane, "UI (text pane) appender must be attached to the root logger");

        assertEquals(1, textpane.getCopyOfAttachedFiltersList().size(), "UI appender must have exactly the threshold filter");
        assertEquals(FilterReply.DENY, textpane.getFilterChainDecision(event(Level.DEBUG)), "DEBUG must not reach the panel");
        assertEquals(FilterReply.NEUTRAL, textpane.getFilterChainDecision(event(Level.INFO)), "INFO must reach the panel");
        assertEquals(FilterReply.NEUTRAL, textpane.getFilterChainDecision(event(Level.WARN)), "WARN must reach the panel");
        assertTrue(file.getCopyOfAttachedFiltersList().isEmpty(), "File appender must not be threshold-filtered, so it keeps DEBUG");
        assertEquals(Level.INFO, LogbackConfig.getLogPanelLevel());
    }

    @Test
    void configure_ok_debugGoesToTheFileButNotToThePanel(@TempDir Path tempDir) throws Exception {
        LogbackConfig.configure(tempDir, LogbackConfig.DEFAULT_LOG_PANEL_LEVEL);

        List<String> delivered = new CopyOnWriteArrayList<>();
        CountDownLatch infoDelivered = new CountDownLatch(1);
        TextPaneAppender.setLogMessageConsumer(message -> {
            delivered.add(message);
            if (message.endsWith("info-marker")) {
                infoDelivered.countDown();
            }
        });
        try {
            Logger log = LoggerFactory.getLogger("com.astahpromcp.LogbackConfigTest");
            log.debug("debug-marker");
            log.info("info-marker");

            // Events reach the panel through invokeLater in the order they were logged, so a DEBUG line that got
            // through would have arrived before the INFO line does.
            assertTrue(infoDelivered.await(5, TimeUnit.SECONDS), "The INFO line should reach the panel");
        } finally {
            TextPaneAppender.setLogMessageConsumer(null);
        }

        assertTrue(delivered.stream().noneMatch(message -> message.endsWith("debug-marker")),
                "The DEBUG line must not reach the panel, but it received: " + delivered);

        String logFile = Files.readString(tempDir.resolve("astah-pro-mcp.log"));
        assertTrue(logFile.contains("debug-marker"), "The log file must keep the DEBUG line");
        assertTrue(logFile.contains("info-marker"), "The log file must keep the INFO line");
    }

    @Test
    void configure_ok_panelLevelCanBeLoweredToDebug(@TempDir Path tempDir) {
        LogbackConfig.configure(tempDir, Level.DEBUG);

        Appender<ILoggingEvent> textpane = rootAppender("TEXTPANE");
        assertEquals(FilterReply.NEUTRAL, textpane.getFilterChainDecision(event(Level.DEBUG)),
                "With the panel level at DEBUG, DEBUG must reach the panel");
        assertEquals(Level.DEBUG, LogbackConfig.getLogPanelLevel());
    }

    @Test
    void resolveLogPanelLevel_ok_defaultsToInfoWhenUnset() {
        assertEquals(Level.INFO, LogbackConfig.resolveLogPanelLevel(null));
        assertEquals(Level.INFO, LogbackConfig.resolveLogPanelLevel(""));
        assertEquals(Level.INFO, LogbackConfig.resolveLogPanelLevel("   "));
    }

    @Test
    void resolveLogPanelLevel_ok_readsALevelNameInAnyCase() {
        assertEquals(Level.DEBUG, LogbackConfig.resolveLogPanelLevel("debug"));
        assertEquals(Level.DEBUG, LogbackConfig.resolveLogPanelLevel(" DEBUG "));
        assertEquals(Level.WARN, LogbackConfig.resolveLogPanelLevel("WARN"));
    }

    @Test
    void resolveLogPanelLevel_ok_defaultsToInfoForAnUnknownName() {
        assertEquals(Level.INFO, LogbackConfig.resolveLogPanelLevel("verbose"));
    }

    private static Appender<ILoggingEvent> rootAppender(String name) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender(name);
    }

    private static ILoggingEvent event(Level level) {
        LoggingEvent event = new LoggingEvent();
        event.setLevel(level);
        event.setMessage("message");
        return event;
    }
}
