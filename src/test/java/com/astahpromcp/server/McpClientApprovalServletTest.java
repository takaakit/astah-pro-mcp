package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        servlet = new McpClientApprovalServlet(delegate, null, Set.of("localhost", "127.0.0.1"));
    }

    @Test
    void service_allowsRequestWhenOriginIsPermitted() throws Exception {
        when(request.getHeader("Origin")).thenReturn("http://localhost:3000");
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
    void service_rejectsRequestWhenOriginNotPermitted() throws Exception {
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
    void service_rejectsRequestWhenOriginHeaderMalformed() throws Exception {
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
    void service_allowsRequestForDefaultIpv6LoopbackOrigin() throws Exception {
        servlet = new McpClientApprovalServlet(delegate, null, McpServerConfig.DEFAULT_ORIGIN_HOST_ALLOWLIST);

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
}
