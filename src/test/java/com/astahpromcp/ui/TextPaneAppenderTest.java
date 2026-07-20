package com.astahpromcp.ui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TextPaneAppenderTest {

    @Test
    void append_ok_formatsWithTimestampAndDeliversToConsumer() throws Exception {
        AtomicReference<String> delivered = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        TextPaneAppender.setLogMessageConsumer(msg -> {
            delivered.set(msg);
            latch.countDown();
        });
        try {
            ILoggingEvent event = mock(ILoggingEvent.class);
            when(event.getTimeStamp()).thenReturn(0L);
            when(event.getFormattedMessage()).thenReturn("hello world");

            new TextPaneAppender().append(event);

            assertTrue(latch.await(5, TimeUnit.SECONDS), "The consumer should receive the message via the EDT");
            assertTrue(delivered.get().matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}  hello world"),
                    "Output must be 'HH:mm:ss.SSS  <message>' but was: " + delivered.get());
        } finally {
            TextPaneAppender.setLogMessageConsumer(null);
        }
    }

    @Test
    void append_ok_noConsumerReturnsWithoutTouchingTheEvent() {
        TextPaneAppender.setLogMessageConsumer(null);

        ILoggingEvent event = mock(ILoggingEvent.class);
        assertDoesNotThrow(() -> new TextPaneAppender().append(event));
        verifyNoInteractions(event);
    }
}
