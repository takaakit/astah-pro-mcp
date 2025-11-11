package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.HttpHeaders;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

// Servlet that prompts the user before accepting an MCP client connection
@Slf4j
public class McpClientApprovalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final HttpServletStreamableServerTransportProvider delegate;
    private final Object approvalLock = new Object();
    private boolean userApproved;
    private final McpServerApp.ClientDisconnectHandler disconnectHandler;
    private final Set<String> originHostAllowlist;

    // For production
    public McpClientApprovalServlet(HttpServletStreamableServerTransportProvider delegate,
                                    McpServerApp.ClientDisconnectHandler disconnectHandler) {
        this(delegate, disconnectHandler, McpServerConfig.ORIGIN_HOST_ALLOWLIST);
    }

    // For testing
    public McpClientApprovalServlet(HttpServletStreamableServerTransportProvider delegate,
                                    McpServerApp.ClientDisconnectHandler disconnectHandler,
                                    Set<String> originHostAllowlist) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.disconnectHandler = disconnectHandler;

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

    public void resetApproval() {
        synchronized (approvalLock) {
            userApproved = false;
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestContext context = RequestContext.from(req);

        // If the origin is not allowed, reject the request
        if (!isOriginAllowed(req)) {
            String origin = Optional.ofNullable(req.getHeader("Origin")).orElse("<none>");
            log.warn("Rejected MCP request due to disallowed Origin header: {}", origin);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
            return;
        }

        // If the request is an initialize attempt and the user has not approved the connection, request approval
        if (context.isInitializeAttempt()) {
            boolean approved = ensureUserApproval(context);
            if (!approved) {
                log.info("Rejected MCP connection from {}", context.clientAddress());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Connection rejected by user");
                return;
            }
            log.info("Approved MCP connection from {}", context.clientAddress());
        }

        // Create a response wrapper to track the status of the request
        SessionTrackingResponseWrapper responseWrapper = new SessionTrackingResponseWrapper(resp);
        try {
            // Service the request
            delegate.service(req, responseWrapper);
            
            // Register the session only after initialization succeeds
            if (context.isInitializeAttempt()
                    && responseWrapper.isSuccessful()
                    && context.sessionId().isPresent()) {
                // Handle the start of the client connection
                if (disconnectHandler != null) {
                    disconnectHandler.registerClientSession(
                            context.sessionId().get(),
                            context.clientAddress(),
                            context.userAgent());
                }        
            }
            
        } finally {
            // If the request is an initialize attempt and the initialization failed, reset the approval flag
            if (context.isInitializeAttempt() && !responseWrapper.isSuccessful()) {
                log.warn("Initialization failed with status {}. Resetting approval flag.", responseWrapper.getStatus());
                resetApproval();
            }

            // If the request is a delete attempt and the request is successful, reset the approval flag
            if (context.isDeleteRequest() && responseWrapper.isSuccessful()) {
                log.info("Connection closed by MCP client. Resetting approval flag.");

                // Handle the client disconnect event
                if (disconnectHandler != null && context.sessionId().isPresent()) {
                    disconnectHandler.handleClientDisconnect(context.sessionId().get(), "client_disconnect");
                }
        
                resetApproval();
            }
        }
    }

    // Request approval from the user
    private boolean ensureUserApproval(RequestContext context) {
        synchronized (approvalLock) {
            // If the user has already approved the connection, return true
            if (userApproved) {
                return true;
            }

            // If the environment is headless, reject the connection
            if (GraphicsEnvironment.isHeadless()) {
                log.warn("Headless environment detected. Rejecting connection from {}:{}.",
                        context.remoteAddress(), context.remotePort());
                return false;
            }

            AtomicBoolean approved = new AtomicBoolean(false);
            String message = buildDialogMessage(context);
            Object[] options = {"Connect", "Cancel"};
            try {
                runOnEdtBlocking(() -> showApprovalDialog(message, options, approved));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (InvocationTargetException e) {
                log.error("Failed to display approval dialog", e.getCause());
                return false;
            }

            if (approved.get()) {
                userApproved = true;
            }
            return approved.get();
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

    // Session tracking response wrapper
    private static final class SessionTrackingResponseWrapper extends HttpServletResponseWrapper {

        private int status;

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

    private static final class RequestContext {

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

        boolean isInitializeAttempt() {
            return "POST".equalsIgnoreCase(method) && sessionId == null;
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
}
