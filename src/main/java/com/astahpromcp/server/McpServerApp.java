package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolRegistrar;
import com.astahpromcp.tool.astah.pro.AstahProToolFactory;
import com.astahpromcp.tool.knowledge.KnowledgeToolFactory;
import com.astahpromcp.tool.log.LogToolFactory;
import com.astahpromcp.tool.visualization.VisualizationToolFactory;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

// MCP server exposing tools over Streamable HTTP.
@Slf4j
public final class McpServerApp {

    private static final int PORT = McpServerConfig.DEFAULT_PORT;
    private static final String HOST = McpServerConfig.DEFAULT_HOST;

    private Server jetty;
    private HttpServletStreamableServerTransportProvider transport;
    private McpClientApprovalServlet approvalServlet;
    private McpSyncServer mcpServer;
    private File workspaceDir;
    
    // Manage client sessions
    private final ConcurrentHashMap<String, ClientSession> activeSessions = new ConcurrentHashMap<>();
    private final AtomicReference<ClientDisconnectHandler> disconnectHandler = new AtomicReference<>();

    public void start() throws Exception {
        log.info("=== MCP SERVER STARTING ===");
        log.info("Host: {}, Port: {}", HOST, PORT);

        try {
            log.info("Creating workspace directory...");
            createWorkspaceDirectory();

            log.info("Configuring transport...");
            configureTransport();
            
            log.info("Registering tool providers...");
            List<ToolProvider> providers = registerToolProviders();
            
            log.info("Building MCP server...");
            mcpServer = buildMcpServer(providers);
            
            log.info("Creating Jetty server...");
            jetty = createJettyServer();
            
            log.info("Starting Jetty server...");
            jetty.start();

            log.info("=== MCP SERVER STARTED SUCCESSFULLY ===");

        } catch (Exception e) {
            log.error("=== MCP SERVER START FAILED ===", e);
            throw e;
        }
    }

    private void createWorkspaceDirectory() throws IOException {
        workspaceDir = McpServerConfig.DEFAULT_WORKSPACE_DIR.toFile();
        FileUtils.forceMkdir(workspaceDir);
    }

    private void configureTransport() {
        transport = HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .jsonMapper(JsonSupport.MCP_JSON_MAPPER)
                .build();
        
        // Configure the client disconnect handler
        ClientDisconnectHandler handler = new ClientDisconnectHandler();
        handler.setServerApp(this);
        disconnectHandler.set(handler);
        approvalServlet = new McpClientApprovalServlet(transport, handler);
    }

    private List<ToolProvider> registerToolProviders() {
        List<ToolProvider> toolProviders = new ArrayList<>();
        
        // Create the ToolCategoryFlags instance
        ToolCategoryFlags categoryFlags = new ToolCategoryFlags(
            true,       // is ClassDiagramEnabled
            true,       // is SequenceDiagramEnabled
            true,       // is ActivityDiagramEnabled
            false,      // is StateMachineDiagramEnabled
            false,      // is UseCaseDiagramEnabled
            false,      // is RequirementDiagramEnabled
            false       // is CommunicationDiagramEnabled
        );
        
        log.info("Adding tool providers...");
        toolProviders.addAll(new AstahProToolFactory().createToolProviders(categoryFlags));
        toolProviders.addAll(new LogToolFactory().createToolProviders(categoryFlags));
        toolProviders.addAll(new KnowledgeToolFactory(McpServerConfig.DEFAULT_WORKSPACE_DIR).createToolProviders(categoryFlags));
        toolProviders.addAll(new VisualizationToolFactory().createToolProviders(categoryFlags));
        
        log.info("Total tool providers registered: {}", toolProviders.size());

        log.info("Tool providers:");
        for (ToolProvider toolProvider : toolProviders) {
            log.info("- {}", toolProvider.getClass().getSimpleName());
        }

        return toolProviders;
    }

    private McpSyncServer buildMcpServer(List<ToolProvider> toolProviders) {
        
        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder()
                .tools(!toolProviders.isEmpty())
                .build();

        McpServer.SyncSpecification<?> serverBuilder = McpServer.sync(transport)
                .serverInfo(new McpSchema.Implementation(getArtifactId(), getVersion()))
                .instructions("This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, the MCP client (you) can reference and edit the project currently open in Astah. Note that the MCP client (you) MUST call 'astah_pro_mcp_guide' tool function before referencing or editing the Astah project to understand how to use this MCP server.")
                .capabilities(capabilities);

        log.info("Registering all tools...");
        ToolRegistrar.registerAll(serverBuilder, toolProviders);
        
        McpSyncServer server = serverBuilder.build();

        return server;
    }

    private Server createJettyServer() {
        
        log.info("Creating Jetty Server instance with thread pool configuration...");
        
        // Configure the thread pool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(McpServerConfig.JETTY_MIN_THREADS);
        threadPool.setMaxThreads(McpServerConfig.JETTY_MAX_THREADS);
        threadPool.setName("MCP-Server-ThreadPool");
        
        log.info("Configured thread pool:");
        log.info("- Min threads: {}", McpServerConfig.JETTY_MIN_THREADS);
        log.info("- Max threads: {}", McpServerConfig.JETTY_MAX_THREADS);
        
        Server server = new Server(threadPool);
        
        log.info("Creating ServerConnector with timeout and thread settings...");
        ServerConnector connector = new ServerConnector(server, 
                McpServerConfig.JETTY_ACCEPTOR_THREADS, 
                McpServerConfig.JETTY_SELECTOR_THREADS);
        connector.setHost(HOST);
        connector.setPort(PORT);
        
        // Apply the timeout configuration to the connector
        connector.setIdleTimeout(McpServerConfig.JETTY_IDLE_TIMEOUT_MS);
        
        // Disable SO_REUSEPORT setting for Windows environment to avoid UnsupportedOperationException
        try {
            connector.setReusePort(false);
        } catch (UnsupportedOperationException e) {
            log.debug("SO_REUSEPORT not supported on this platform: {}", e.getMessage());
        }
        
        log.info("Applied connection and thread settings:");
        log.info("- Idle timeout: {} ms", McpServerConfig.JETTY_IDLE_TIMEOUT_MS);
        log.info("- Acceptor threads: {}", McpServerConfig.JETTY_ACCEPTOR_THREADS);
        log.info("- Selector threads: {}", McpServerConfig.JETTY_SELECTOR_THREADS);
        
        server.addConnector(connector);

        log.info("Creating ServletContextHandler...");
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        log.info("Adding transport servlet...");
        ServletHolder holder = new ServletHolder(approvalServlet);
        context.addServlet(holder, "/mcp");

        return server;
    }

    private String getArtifactId() {
        return getProjectProperty("project.artifactId");
    }

    private String getVersion() {
        return getProjectProperty("project.version");
    }

    private String getProjectProperty(String key) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("project.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                String value = props.getProperty(key);
                return value != null ? value : "";
            }
        } catch (IOException e) {
            log.warn("Failed to load project properties, returning empty string for key: {}", key, e);
        }
        return "";
    }

    // Register a client session.
    public void registerClientSession(String sessionId, String clientAddress, String userAgent) {
        ClientSession session = new ClientSession(sessionId, clientAddress, userAgent);
        activeSessions.put(sessionId, session);
        log.info("Registered client session: {} from {}", sessionId, clientAddress);
    }
    
    // Terminate a client session and perform clean-up.
    public void terminateClientSession(String sessionId, String reason) {
        ClientSession session = activeSessions.remove(sessionId);
        if (session != null) {
            log.info("Terminating client session: {} (reason: {})", sessionId, reason);
        } else {
            log.warn("Attempted to terminate non-existent session: {}", sessionId);
        }
    }
    
    // Retrieve the number of active sessions.
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    // Retrieve all active sessions.
    public java.util.Collection<ClientSession> getActiveSessions() {
        return new java.util.ArrayList<>(activeSessions.values());
    }

    // Send a shutdown notification to MCP clients when the MCP server stops.
    private void sendShutdownNotification() {
        if (transport != null) {
            try {
                log.info("Sending shutdown notification to MCP clients...");
                
                // Send the notification in the format expected by the MCP protocol
                // Parameters for the notifications/cancelled event
                java.util.Map<String, Object> params = new java.util.HashMap<String, Object>();
                params.put("reason", "server_shutdown");
                params.put("message", "MCP server is shutting down");
                
                // Send asynchronously and enforce a timeout
                transport.notifyClients("notifications/cancelled", params)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .doOnSuccess(v -> log.info("Shutdown notification sent successfully"))
                    .doOnError(e -> log.warn("Failed to send shutdown notification: {}", e.getMessage()))
                    .onErrorComplete()
                    .block();
                    
            } catch (Exception e) {
                log.warn("Error sending shutdown notification: {}", e.getMessage(), e);
            }
        }
    }

    public void stop() throws Exception {
        // Notify clients about the MCP server shutdown before stopping components
        sendShutdownNotification();
        
        if (mcpServer != null) {
            log.debug("Closing MCP server");
            mcpServer.closeGracefully();
            mcpServer = null;
        }
        if (transport != null) {
            log.debug("Closing transport");
            transport.closeGracefully().block();
            transport = null;
        }
        if (approvalServlet != null) {
            log.debug("Resetting connection approval state");
            approvalServlet.resetApproval();
            approvalServlet = null;
        }
        if (jetty != null) {
            log.debug("Stopping Jetty server");
            jetty.stop();
            jetty = null;
        }

        if (workspaceDir != null && workspaceDir.exists()) {
            log.debug("Deleting workspace directory");
            try {
                FileUtils.deleteDirectory(workspaceDir);
            } catch (IOException e) {
                log.warn("Failed to delete workspace directory {}", workspaceDir.getAbsolutePath(), e);
            }
            workspaceDir = null;
        }
    }
    
    // Holds client session metadata.
    public static class ClientSession {

        private final String sessionId;
        private final String clientAddress;
        private final String userAgent;
        private final long connectedAt;
        
        public ClientSession(String sessionId, String clientAddress, String userAgent) {
            this.sessionId = sessionId;
            this.clientAddress = clientAddress;
            this.userAgent = userAgent;
            this.connectedAt = System.currentTimeMillis();
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public String getClientAddress() {
            return clientAddress;
        }
        
        public String getUserAgent() {
            return userAgent;
        }
        
        public long getConnectedAt() {
            return connectedAt;
        }
        
        public long getConnectionDuration() {
            return System.currentTimeMillis() - connectedAt;
        }
        
        @Override
        public String toString() {
            return String.format("ClientSession{sessionId='%s', clientAddress='%s', userAgent='%s', connectedAt=%d}", 
                    sessionId, clientAddress, userAgent, connectedAt);
        }
    }
    
    // Handles client disconnect events.
    public static class ClientDisconnectHandler {

        private McpServerApp serverApp;
        
        public void setServerApp(McpServerApp serverApp) {
            this.serverApp = serverApp;
        }
        
        public void handleClientDisconnect(String sessionId, String reason) {
            if (serverApp != null) {
                serverApp.terminateClientSession(sessionId, reason);
            }
        }
        
        public void registerClientSession(String sessionId, String clientAddress, String userAgent) {
            if (serverApp != null) {
                serverApp.registerClientSession(sessionId, clientAddress, userAgent);
            }
        }
    }
    
    // Exposed for tests to inspect Jetty configuration.
    Server getJettyServer() {
        return jetty;
    }
}
