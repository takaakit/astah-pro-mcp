package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpClientApprovalServletTest {

    @Mock
    private HttpServletStreamableServerTransportProvider delegate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private McpClientApprovalServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new McpClientApprovalServlet(delegate, McpServerConfig.ORIGIN_HOST_ALLOWLIST);
    }

    // Stub the request body; POST bodies are read through getInputStream by the servlet
    private void stubBody(String body) throws Exception {
        stubBody(request, body);
    }

    private void stubBody(HttpServletRequest target, String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        when(target.getInputStream()).thenReturn(new ServletInputStream() {
            private final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        });
    }

    @Test
    void service_ok_allowsRequestWhenOriginIsPermitted() throws Exception {
        when(request.getHeader("Origin")).thenReturn("http://127.0.0.1:3000");
        when(request.getHeader("Mcp-Session-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");

        assertDoesNotThrow(() -> servlet.service(request, response));

        verify(delegate).service(eq(request), ArgumentMatchers.any(HttpServletResponse.class));
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void service_ng_rejectsRequestWhenOriginNotPermitted() throws Exception {
        when(request.getHeader("Origin")).thenReturn("http://evil.example.com");
        when(request.getHeader("Mcp-Session-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");

        servlet.service(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
        verify(delegate, never()).service(any(), any());
    }

    @Test
    void service_ng_rejectsRequestWhenOriginHeaderMalformed() throws Exception {
        when(request.getHeader("Origin")).thenReturn("%%%invalid-uri%%% ");
        when(request.getHeader("Mcp-Session-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");

        servlet.service(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
        verify(delegate, never()).service(any(), any());
    }

    @Test
    void service_ok_passesThroughDeleteRequestWithoutApproval() throws Exception {
        when(request.getHeader("Origin")).thenReturn(null);
        when(request.getHeader("Mcp-Session-Id")).thenReturn("session-123");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");

        servlet.service(request, response);

        verify(delegate).service(eq(request), ArgumentMatchers.any(HttpServletResponse.class));
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void service_ok_passesThroughSessionRequestWithoutApproval() throws Exception {
        when(request.getHeader("Origin")).thenReturn(null);
        when(request.getHeader("Mcp-Session-Id")).thenReturn("session-123");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        stubBody("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}");

        servlet.service(request, response);

        verify(delegate).service(ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(HttpServletResponse.class));
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void service_ok_answersServerDiscoverProbeWithMethodNotFound() throws Exception {
        // 'server/discover' must be answered with a JSON-RPC -32601 envelope (without opening the approval dialog) so the client falls back to the legacy initialize handshake.
        when(request.getHeader("Origin")).thenReturn(null);
        when(request.getHeader("Mcp-Session-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        stubBody("{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"server/discover\",\"params\":{}}");
        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        servlet.service(request, response);

        verify(delegate, never()).service(any(), any());
        verify(response, never()).sendError(anyInt(), anyString());
        assertTrue(out.toString().contains("-32601"));
        assertTrue(out.toString().contains("\"id\":7"));
    }

    @Test
    void service_ok_toolsCallMentioningProbeMethodIsNotIntercepted() throws Exception {
        // A request whose arguments merely contain the string "server/discover" must reach the transport untouched; only the JSON-RPC method field triggers the probe reply.
        when(request.getHeader("Origin")).thenReturn(null);
        when(request.getHeader("Mcp-Session-Id")).thenReturn("session-123");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        stubBody("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\","
                + "\"params\":{\"name\":\"set_name\",\"arguments\":{\"name\":\"server/discover\"}}}");

        servlet.service(request, response);

        verify(delegate).service(ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(HttpServletResponse.class));
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void service_ng_rejectsDeclinedInitializeWithServiceUnavailableNotForbidden() throws Exception {
        // When the user declines, the rejection must be 503, not 401/403, so MCP clients
        // do not mistake it for an OAuth authentication challenge. The dialog is stubbed
        // via a spy so the outcome is deterministic and no real dialog is shown.
        McpClientApprovalServlet spyServlet = spy(servlet);
        doReturn(false).when(spyServlet).promptUserForApproval(any());

        when(request.getHeader("Origin")).thenReturn(null);
        when(request.getHeader("Mcp-Session-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        stubBody("{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"initialize\",\"params\":{}}");

        spyServlet.service(request, response);

        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Connection not approved by user");
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(response, never()).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verify(delegate, never()).service(any(), any());
    }

    @Test
    void service_ok_passesThroughApprovedInitialize() throws Exception {
        // When the user approves, the request is delegated to the transport and not rejected.
        McpClientApprovalServlet spyServlet = spy(servlet);
        doReturn(true).when(spyServlet).promptUserForApproval(any());

        when(request.getHeader("Origin")).thenReturn(null);
        when(request.getHeader("Mcp-Session-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        stubBody("{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"initialize\",\"params\":{}}");

        spyServlet.service(request, response);

        verify(delegate).service(ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(HttpServletResponse.class));
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void service_ok_allowsRequestForDefaultIpv6LoopbackOrigin() throws Exception {
        servlet = new McpClientApprovalServlet(delegate, McpServerConfig.ORIGIN_HOST_ALLOWLIST);

        when(request.getHeader("Origin")).thenReturn("http://[::1]:3000");
        when(request.getHeader("Mcp-Session-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("::1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getRemoteHost()).thenReturn("::1");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");

        assertDoesNotThrow(() -> servlet.service(request, response));

        verify(delegate).service(eq(request), ArgumentMatchers.any(HttpServletResponse.class));
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @Timeout(30)
    void service_ok_parallelInitializeWaitsForTheDialogOnScreen() throws Exception {
        // Clients such as Codex CLI open several connections at once and give up on the first refusal, so an initialize arriving while the dialog is on screen must wait for its turn, not be rejected.
        // The dialogs must not overlap either: for a single servlet, that is for a single port, at most one may be on screen at a time.
        McpClientApprovalServlet spyServlet = spy(servlet);
        AtomicInteger inDialog = new AtomicInteger();
        AtomicInteger maxInDialog = new AtomicInteger();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch dialogShown = new CountDownLatch(1);
        CountDownLatch releaseDialog = new CountDownLatch(1);
        doAnswer(invocation -> {
            maxInDialog.accumulateAndGet(inDialog.incrementAndGet(), Math::max);
            try {
                dialogShown.countDown();
                assertTrue(releaseDialog.await(10, TimeUnit.SECONDS), "Dialog was never released");
                return true;

            } finally {
                inDialog.decrementAndGet();
            }
        }).when(spyServlet).promptUserForApproval(any());
        HttpServletResponse secondResponse = mock(HttpServletResponse.class);

        Thread first = startService(spyServlet, newInitializeRequest(51325), mock(HttpServletResponse.class), failure);
        assertTrue(dialogShown.await(5, TimeUnit.SECONDS), "Dialog was never shown");
        Thread second = startService(spyServlet, newInitializeRequest(51336), secondResponse, failure);
        awaitQueuedOnDialog(second);

        // Neither request may reach the transport while the dialog of the first one is still on screen
        verify(delegate, never()).service(any(), any());

        releaseDialog.countDown();
        assertTrue(first.join(Duration.ofSeconds(10)), "The first initialize never finished");
        assertTrue(second.join(Duration.ofSeconds(10)), "The second initialize never finished");

        if (failure.get() != null) {
            fail("An initialize request failed", failure.get());
        }
        assertEquals(1, maxInDialog.get(), "Two approval dialogs were on screen at the same time");
        verify(secondResponse, never()).sendError(anyInt(), anyString());
        verify(delegate, times(2)).service(any(), any());
    }

    // Build a fully stubbed initialize POST coming from the given client port
    private HttpServletRequest newInitializeRequest(int remotePort) throws Exception {
        HttpServletRequest initializeRequest = mock(HttpServletRequest.class);
        when(initializeRequest.getHeader("Origin")).thenReturn(null);
        when(initializeRequest.getHeader("Mcp-Session-Id")).thenReturn(null);
        when(initializeRequest.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(initializeRequest.getMethod()).thenReturn("POST");
        when(initializeRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(initializeRequest.getRemotePort()).thenReturn(remotePort);
        when(initializeRequest.getRemoteHost()).thenReturn("localhost");
        stubBody(initializeRequest, "{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"initialize\",\"params\":{}}");
        return initializeRequest;
    }

    private Thread startService(McpClientApprovalServlet target,
                                HttpServletRequest req,
                                HttpServletResponse resp,
                                AtomicReference<Throwable> failure) {
        Thread thread = new Thread(() -> {
            try {
                target.service(req, resp);
            } catch (Throwable t) {
                // Reported by the test thread; throwing here would only reach stderr
                failure.compareAndSet(null, t);
            }
        });
        // Daemon so that a regression which never releases the dialog lock fails the test instead of hanging the build
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    // Wait until the thread has queued up behind the approval dialog
    private void awaitQueuedOnDialog(Thread thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            // The dialog lock is acquired with a timeout, so a queued thread parks in TIMED_WAITING
            Thread.State state = thread.getState();
            if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING) {
                return;
            }
            Thread.sleep(5);
        }
        fail("The second initialize never queued up behind the approval dialog (thread state: " + thread.getState() + ")");
    }
}
