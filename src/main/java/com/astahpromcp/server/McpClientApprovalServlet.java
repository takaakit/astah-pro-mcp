package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.HttpHeaders;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

// Servlet that prompts the user before accepting an MCP client connection
@Slf4j
public class McpClientApprovalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final HttpServletStreamableServerTransportProvider delegate;

    // Serializes approval dialogs of this servlet instance, that is, of one port.
    // Initialize requests arriving while a dialog is open wait for their turn, so at most one dialog is shown at a time per port. Dialogs of the other port are independent by design.
    private final ReentrantLock dialogLock = new ReentrantLock();

    // Last user approval, used for the approval grace period (guarded by graceLock)
    private final Object graceLock = new Object();

    private long lastApprovalAtMillis;
    private String lastApprovedUserAgent;
    private final Set<String> originHostAllowlist;

    // For production
    public McpClientApprovalServlet(HttpServletStreamableServerTransportProvider delegate) {
        this(delegate, McpServerConfig.ORIGIN_HOST_ALLOWLIST);
    }

    // For testing
    public McpClientApprovalServlet(HttpServletStreamableServerTransportProvider delegate,
                                    Set<String> originHostAllowlist) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");

        if (originHostAllowlist != null) {
            this.originHostAllowlist = originHostAllowlist.stream()
                    .filter(Objects::nonNull)
                    .map(McpClientApprovalServlet::normalizeHost)
                    .filter(host -> host != null && !host.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
        } else {
            this.originHostAllowlist = Set.of();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpServletRequest actualReq = req;

        // Some clients pass the session ID as a query parameter (legacy HTTP+SSE convention) instead of the Mcp-Session-Id header; normalize it so the transport can find the session
        String sessionIdParam = req.getParameter("sessionId") != null
                ? req.getParameter("sessionId")
                : req.getParameter("session_id");
        if (sessionIdParam != null && !sessionIdParam.isBlank() && req.getHeader(HttpHeaders.MCP_SESSION_ID) == null) {
            actualReq = new SessionHeaderInjectingRequestWrapper(req, sessionIdParam.trim());
        }

        // The JSON-RPC method decides probe handling and approval below, and the servlet input stream is single-read, so cache the POST body and parse it once here
        JsonRpcCall call = JsonRpcCall.NONE;
        if ("POST".equalsIgnoreCase(actualReq.getMethod())) {
            CachedBodyRequestWrapper cachedReq = new CachedBodyRequestWrapper(actualReq);
            call = JsonRpcCall.parse(cachedReq.getBody());
            actualReq = cachedReq;
        }

        RequestContext context = RequestContext.from(actualReq);

        // If the origin is not allowed, reject the request
        if (!isOriginAllowed(actualReq)) {
            String origin = Optional.ofNullable(actualReq.getHeader("Origin")).orElse("<none>");
            log.warn("Rejected MCP request due to disallowed Origin header: {}", origin);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
            return;
        }

        // 'server/discover' is a pre-initialize probe sent by newer MCP clients. The transport rejects pre-session methods it does not know with a plain HTTP 400 instead of a JSON-RPC error envelope, so answer -32601 here; the client then falls back to the legacy initialize handshake.
        if ("server/discover".equals(call.method()) && context.sessionId().isEmpty()) {
            log.info("Received server/discover probe from {}; replying 'Method not found' to trigger the initialize fallback",
                    context.clientAddress());
            respondMethodNotFound(resp, call.id());
            return;
        }

        // If the request is an initialize attempt, request approval from the user
        if (call.isInitialize()) {
            if (!requestUserApproval(context)) {
                log.info("Rejected MCP connection from {}", context.clientAddress());
                resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Connection not approved by user");
                return;
            }
            log.info("Approved MCP connection from {}", context.clientAddress());
        }

        // Create a response wrapper to track the status and the issued session ID
        SessionTrackingResponseWrapper responseWrapper = new SessionTrackingResponseWrapper(resp);

        // Service the request
        delegate.service(actualReq, responseWrapper);

        // Log the session issued by the transport after a successful initialization
        if (call.isInitialize() && responseWrapper.isSuccessful()) {
            responseWrapper.issuedSessionId().ifPresent(sessionId ->
                    log.info("Registered client session: {} from {} (User-Agent: {})",
                            sessionId, context.clientAddress(), context.userAgent()));
        }

        // Log when the MCP client closes its session
        if (context.isDeleteRequest() && responseWrapper.isSuccessful() && context.sessionId().isPresent()) {
            log.info("Connection closed by MCP client (session '{}').", context.sessionId().get());
        }
    }

    // Reply with a JSON-RPC 'Method not found' error envelope
    private static void respondMethodNotFound(HttpServletResponse resp, JsonNode requestId) throws IOException {
        ObjectNode envelope = JsonSupport.OBJ_MAPPER.createObjectNode();
        envelope.put("jsonrpc", "2.0");
        ObjectNode error = envelope.putObject("error");
        error.put("code", -32601);
        error.put("message", "Method not found");
        envelope.set("id", requestId == null ? NullNode.getInstance() : requestId);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(JsonSupport.OBJ_MAPPER.writeValueAsString(envelope));
        resp.getWriter().flush();
    }

    // Request approval from the user and return whether the connection may proceed
    private boolean requestUserApproval(RequestContext context) {
        // Auto-approve follow-up initializes from the same client right after a user approval
        if (isWithinApprovalGracePeriod(context)) {
            log.info("Auto-approved MCP connection from {} within the approval grace period (User-Agent: {}).",
                    context.clientAddress(), context.userAgent());
            return true;
        }

        // Queue behind an approval dialog that is already open rather than rejecting the request.
        // The wait is bounded so that a dialog left unanswered cannot park every worker thread of this port, and interruptible so that a server shutdown does not have to wait for the user.
        boolean acquired;
        try {
            acquired = dialogLock.tryLock(McpServerConfig.APPROVAL_DIALOG_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for the approval dialog of another connection; rejecting {}",
                    context.clientAddress());
            return false;
        }

        if (!acquired) {
            log.warn("Gave up waiting for the approval dialog of another connection after {} seconds; rejecting {}",
                    McpServerConfig.APPROVAL_DIALOG_WAIT_TIMEOUT_SECONDS, context.clientAddress());
            return false;
        }

        try {
            if (promptUserForApproval(context)) {
                recordApproval(context);
                return true;
            }
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (InvocationTargetException e) {
            log.error("Failed to display approval dialog", e.getCause());
            return false;
        } finally {
            dialogLock.unlock();
        }
    }

    // Prompt the user with a modal dialog and return whether they approved the connection
    boolean promptUserForApproval(RequestContext context) throws InterruptedException, InvocationTargetException {
        if (GraphicsEnvironment.isHeadless()) {
            log.warn("Headless environment detected. Rejecting connection from {}:{}.", context.remoteAddress(), context.remotePort());
            return false;
        }

        AtomicBoolean approved = new AtomicBoolean(false);
        String message = buildDialogMessage(context);
        Object[] options = {"Connect", "Cancel"};
        runOnEdtBlocking(() -> showApprovalDialog(message, options, approved));
        return approved.get();
    }

    // Check whether the request falls within the grace period of the last user approval.
    // The window starts at the explicit user approval and is not extended by auto-approvals.
    private boolean isWithinApprovalGracePeriod(RequestContext context) {
        synchronized (graceLock) {
            return lastApprovedUserAgent != null
                    && lastApprovedUserAgent.equals(context.userAgent())
                    && System.currentTimeMillis() - lastApprovalAtMillis <= McpServerConfig.APPROVAL_GRACE_PERIOD_MS;
        }
    }

    private void recordApproval(RequestContext context) {
        synchronized (graceLock) {
            lastApprovalAtMillis = System.currentTimeMillis();
            lastApprovedUserAgent = context.userAgent();
        }
    }

    // Build the dialog message
    private String buildDialogMessage(RequestContext context) {
        String sb = "Connection request received from MCP client." + "\n" +
                "\n" +
                "Address: " + context.remoteAddress() + "\n" +
                "Host: " + context.remoteHost() + "\n" +
                "Port: " + context.remotePort() + "\n" +
                "User-Agent: " + context.userAgent() + "\n";

        return sb;
    }

    // Check if the origin is allowed
    private boolean isOriginAllowed(HttpServletRequest request) {
        // If the origin is not set, allow the connection
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isBlank()) {
            return true;
        }

        try {
            // Validate by host only
            URI originUri = URI.create(origin);
            String host = originUri.getHost();

            // If the host is not set, reject the connection
            if (host == null) {
                log.warn("Origin header missing host component: {}", origin);
                return false;
            }

            // Check if the host is in the allowlist
            String normalizedHost = normalizeHost(host);
            return normalizedHost != null && originHostAllowlist.contains(normalizedHost);

        } catch (IllegalArgumentException e) {
            // Reject malformed Origin
            log.warn("Invalid Origin header value: {}", origin, e);
            return false;
        }
    }

    private static String normalizeHost(String host) {
        if (host == null) {
            return null;
        }
        String trimmed = host.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.startsWith("[") && trimmed.endsWith("]") && trimmed.length() > 1) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private static void runOnEdtBlocking(Runnable task) throws InvocationTargetException, InterruptedException {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeAndWait(task);
        }
    }

    private static void showApprovalDialog(String message,
                                           Object[] options,
                                           AtomicBoolean approved) {
        // Create a JOptionPane that keeps the dialog on top
        JOptionPane optionPane = new JOptionPane(message,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                options[0]);

        java.awt.Frame tempOwner = null;
        javax.swing.JDialog dialog = null;
        try {
            // Simple approach: create a temporary invisible owner and set DOCUMENT_MODAL
            tempOwner = new java.awt.Frame();
            tempOwner.setUndecorated(true);
            tempOwner.setType(java.awt.Window.Type.UTILITY);
            tempOwner.setAlwaysOnTop(true);
            tempOwner.setLocationRelativeTo(null);
            tempOwner.setVisible(true);

            dialog = optionPane.createDialog(tempOwner, "MCP Connection Request");
            dialog.setModalityType(java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
            dialog.setAlwaysOnTop(true);
            dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);

            dialog.setVisible(true);

            Object selectedValue = optionPane.getValue();
            approved.set(selectedValue != null && selectedValue.equals(options[0]));

        } finally {
            if (dialog != null) {
                dialog.dispose();
            }
            if (tempOwner != null) {
                tempOwner.dispose();
            }
        }
    }

    // Minimal view of the JSON-RPC call carried in a POST body
    record JsonRpcCall(String method, JsonNode id) {

        static final JsonRpcCall NONE = new JsonRpcCall(null, null);

        static JsonRpcCall parse(String body) {
            try {
                JsonNode root = JsonSupport.OBJ_MAPPER.readTree(body);
                if (root != null && root.isObject()) {
                    JsonNode method = root.get("method");
                    return new JsonRpcCall(method != null && method.isString() ? method.asString() : null,
                            root.get("id"));
                }
            } catch (RuntimeException e) {
                // Malformed JSON; the transport reports the error to the client
            }
            return NONE;
        }

        boolean isInitialize() {
            return "initialize".equals(method);
        }
    }

    // Response wrapper that tracks the status and the session ID issued by the transport
    private static final class SessionTrackingResponseWrapper extends HttpServletResponseWrapper {

        private int status;
        private String issuedSessionId;

        SessionTrackingResponseWrapper(HttpServletResponse response) {
            super(response);
            this.status = response.getStatus();
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            this.status = sc;
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, value);
            captureSessionId(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, value);
            captureSessionId(name, value);
        }

        private void captureSessionId(String name, String value) {
            if (HttpHeaders.MCP_SESSION_ID.equalsIgnoreCase(name)) {
                this.issuedSessionId = value;
            }
        }

        public Optional<String> issuedSessionId() {
            return Optional.ofNullable(issuedSessionId);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(location);
            this.status = HttpServletResponse.SC_FOUND;
        }

        public int getStatus() {
            return status;
        }

        public boolean isSuccessful() {
            return status < HttpServletResponse.SC_BAD_REQUEST;
        }
    }

    static final class RequestContext {

        private final String method;
        private final String sessionId;
        private final String remoteAddress;
        private final int remotePort;
        private final String remoteHost;
        private final String userAgent;

        private RequestContext(String method,
                               String sessionId,
                               String remoteAddress,
                               int remotePort,
                               String remoteHost,
                               String userAgent) {
            this.method = method;
            this.sessionId = sessionId;
            this.remoteAddress = remoteAddress;
            this.remotePort = remotePort;
            this.remoteHost = remoteHost;
            this.userAgent = userAgent;
        }

        static RequestContext from(HttpServletRequest request) {
            String method = request.getMethod();
            String sessionIdHeader = Optional.ofNullable(request.getHeader(HttpHeaders.MCP_SESSION_ID))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .orElse(null);
            String remoteAddress = request.getRemoteAddr();
            int remotePort = request.getRemotePort();
            String remoteHost = request.getRemoteHost();
            String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("Unknown");
            return new RequestContext(method, sessionIdHeader, remoteAddress, remotePort, remoteHost, userAgent);
        }

        boolean isDeleteRequest() {
            return "DELETE".equalsIgnoreCase(method);
        }

        Optional<String> sessionId() {
            return Optional.ofNullable(sessionId);
        }

        String clientAddress() {
            return remoteAddress + ":" + remotePort;
        }

        String remoteAddress() {
            return remoteAddress;
        }

        int remotePort() {
            return remotePort;
        }

        String remoteHost() {
            return remoteHost;
        }

        String userAgent() {
            return userAgent;
        }
    }

    // Presents the session ID from a query parameter as the Mcp-Session-Id header
    private static final class SessionHeaderInjectingRequestWrapper extends HttpServletRequestWrapper {

        private final String sessionId;

        SessionHeaderInjectingRequestWrapper(HttpServletRequest request, String sessionId) {
            super(request);
            this.sessionId = sessionId;
        }

        @Override
        public String getHeader(String name) {
            if (HttpHeaders.MCP_SESSION_ID.equalsIgnoreCase(name)) {
                return sessionId;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (HttpHeaders.MCP_SESSION_ID.equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.singletonList(sessionId));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = new java.util.ArrayList<>(Collections.list(super.getHeaderNames()));
            boolean hasSessionId = names.stream().anyMatch(HttpHeaders.MCP_SESSION_ID::equalsIgnoreCase);
            if (!hasSessionId) {
                names.add(HttpHeaders.MCP_SESSION_ID);
            }
            return Collections.enumeration(names);
        }
    }

    // Buffers the request body so it can be inspected here and still be read by the transport
    private static final class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            request.getInputStream().transferTo(baos);
            this.cachedBody = baos.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new ServletInputStream() {
                private final ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);

                @Override
                public int read() throws IOException {
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
                    if (readListener != null) {
                        try {
                            readListener.onDataAvailable();
                            if (isFinished()) {
                                readListener.onAllDataRead();
                            }
                        } catch (IOException e) {
                            readListener.onError(e);
                        }
                    }
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        String getBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
    }
}
