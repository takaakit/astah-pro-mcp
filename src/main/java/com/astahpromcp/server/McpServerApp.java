package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolCategoryFlags;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolRegistrar;
import com.astahpromcp.tool.astah.pro.AstahProToolFactory;
import com.astahpromcp.tool.config.ConfigToolFactory;
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

    private record ServerProfileConfig(
        String name,
        int port,
        boolean includeEditorTools) {
    }

    private static final class ServerInstance {
        private final ServerProfileConfig profile;
        private final HttpServletStreamableServerTransportProvider transport;
        private final McpClientApprovalServlet approvalServlet;
        private final McpSyncServer mcpServer;
        private final Server jettyServer;

        private ServerInstance(ServerProfileConfig profile,
                               HttpServletStreamableServerTransportProvider transport,
                               McpClientApprovalServlet approvalServlet,
                               McpSyncServer mcpServer,
                               Server jettyServer) {
            this.profile = profile;
            this.transport = transport;
            this.approvalServlet = approvalServlet;
            this.mcpServer = mcpServer;
            this.jettyServer = jettyServer;
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

    private File workspaceDir;
    
    // Manage client sessions
    private final ConcurrentHashMap<String, ClientSession> activeSessions = new ConcurrentHashMap<>();
    private final AtomicReference<ClientDisconnectHandler> disconnectHandler = new AtomicReference<>();
    private final List<ServerInstance> serverInstances = new ArrayList<>();

    public void start() throws Exception {
        log.info("=== MCP SERVER STARTING ===");

        try {
            log.info("Creating workspace directory...");
            createWorkspaceDirectory();

            log.info("Configure client disconnect handler");
            ClientDisconnectHandler handler = new ClientDisconnectHandler();
            handler.setServerApp(this);
            disconnectHandler.set(handler);
            
            log.info("Start MCP profiles");
            startProfiles(handler);

            log.info("=== MCP SERVER STARTED SUCCESSFULLY ===");

        } catch (Exception e) {
            log.error("=== MCP SERVER START FAILED ===", e);
            throw e;
        }
    }

    private void createWorkspaceDirectory() throws IOException {
        workspaceDir = McpServerConfig.WORKSPACE_DIR.toFile();
        FileUtils.forceMkdir(workspaceDir);
    }

    // Start the MCP profiles
    private void startProfiles(ClientDisconnectHandler handler) throws Exception {
        List<ServerProfileConfig> profiles = List.of(
                new ServerProfileConfig(
                    "full",
                    McpServerConfig.PORT_FOR_FULL_TOOL,
                    true),
                new ServerProfileConfig(
                    "query_only",
                    McpServerConfig.PORT_FOR_QUERY_ONLY_TOOL,
                    false)
        );

        try {
            for (ServerProfileConfig profile : profiles) {
                serverInstances.add(startServerInstance(profile, handler));
            }
            
        } catch (Exception e) {
            log.error("Failed to start MCP profile: {}", e.getMessage(), e);
            stopProfiles();
            throw e;
        }
    }

    // Start a server instance for a given profile
    private ServerInstance startServerInstance(ServerProfileConfig profile,
                                               ClientDisconnectHandler handler) throws Exception {

        log.info("Initialize MCP profile '{}' on port {} (includeEditorTools={})",
                profile.name(), profile.port(), profile.includeEditorTools());

        // Create the transport provider
        HttpServletStreamableServerTransportProvider transport = HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .jsonMapper(JsonSupport.MCP_JSON_MAPPER)
                .build();
        
        // Register the tool providers
        List<ToolProvider> providers = registerToolProviders(profile.includeEditorTools());

        // Build the MCP server
        McpSyncServer mcpSyncServer = buildMcpServer(transport, providers);

        // Create the servlet
        McpClientApprovalServlet approvalServlet = new McpClientApprovalServlet(transport, handler);

        // Create the Jetty server
        Server jettyServer = createJettyServer(McpServerConfig.HOST, profile.port(), approvalServlet);
        jettyServer.start();

        log.info("Started MCP profile '{}' on port {}", profile.name(), profile.port());
        return new ServerInstance(profile,
                transport,
                approvalServlet,
                mcpSyncServer,
                jettyServer);
    }

    // Register the tool providers
    private List<ToolProvider> registerToolProviders(boolean includeEditorTools) {
        log.info("Registering tool providers...");

        // Create the tool category flags
        ToolCategoryFlags categoryFlags = new ToolCategoryFlags(
            true,       // is ClassDiagramEnabled
            true,       // is SequenceDiagramEnabled
            true,       // is ActivityDiagramEnabled
            true,       // is StateMachineDiagramEnabled
            true,       // is UseCaseDiagramEnabled
            false,      // is RequirementDiagramEnabled
            true        // is CommunicationDiagramEnabled
        );

        log.info("Add tool providers");
        List<ToolProvider> toolProviders = new ArrayList<>();
        toolProviders.addAll(new AstahProToolFactory().createToolProviders(categoryFlags, includeEditorTools));
        toolProviders.addAll(new LogToolFactory().createToolProviders(categoryFlags));
        toolProviders.addAll(new KnowledgeToolFactory(McpServerConfig.WORKSPACE_DIR).createToolProviders(categoryFlags));
        toolProviders.addAll(new VisualizationToolFactory().createToolProviders(categoryFlags));
        toolProviders.addAll(new ConfigToolFactory().createToolProviders(categoryFlags));
        
        log.info("Total tool providers: {}", toolProviders.size());
        log.info("Tool providers:");
        for (ToolProvider toolProvider : toolProviders) {
            log.info("- {}", toolProvider.getClass().getSimpleName());
        }

        return toolProviders;
    }

    // Build the MCP server capabilities
    private McpSyncServer buildMcpServer(HttpServletStreamableServerTransportProvider transport,
                                         List<ToolProvider> toolProviders) {
        
        log.info("Build MCP server capabilities");

        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder()
                .tools(!toolProviders.isEmpty())
                .build();

        McpServer.SyncSpecification<?> serverBuilder = McpServer.sync(transport)
                .serverInfo(new McpSchema.Implementation(getArtifactId(), getVersion()))
                .instructions("This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, the MCP client (you) can reference and edit the project currently open in Astah. Note that the MCP client (you) MUST call 'astah_pro_mcp_guide' tool function before referencing or editing the Astah project to understand how to use this MCP server.")
                .capabilities(capabilities);

        log.info("Register all tools");
        ToolRegistrar.registerAll(serverBuilder, toolProviders);
        
        McpSyncServer server = serverBuilder.build();

        return server;
    }

    // Create the Jetty server
    private Server createJettyServer(String host,
                                     int port,
                                     McpClientApprovalServlet approvalServlet) {
        
        log.info("Create Jetty Server instance with thread pool configuration");
        
        // Configure the thread pool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(McpServerConfig.JETTY_MIN_THREADS);
        threadPool.setMaxThreads(McpServerConfig.JETTY_MAX_THREADS);
        threadPool.setName("MCP-Server-ThreadPool");
        
        log.info("Configured thread pool:");
        log.info("- Min threads: {}", McpServerConfig.JETTY_MIN_THREADS);
        log.info("- Max threads: {}", McpServerConfig.JETTY_MAX_THREADS);
        
        Server server = new Server(threadPool);
        
        log.info("Create ServerConnector with timeout and thread settings");
        ServerConnector connector = new ServerConnector(server, 
                McpServerConfig.JETTY_ACCEPTOR_THREADS, 
                McpServerConfig.JETTY_SELECTOR_THREADS);
        connector.setHost(host);
        connector.setPort(port);
        
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

        log.info("Create ServletContextHandler");
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        log.info("Add transport servlet");
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
            log.info("Terminate client session: '{}' (reason={})", sessionId, reason);
        } else {
            log.warn("Attempted to terminate non-existent session '{}'", sessionId);
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
        for (ServerInstance instance : serverInstances) {
            HttpServletStreamableServerTransportProvider transport = instance.transport;
            if (transport == null) {
                continue;
            }

            try {
                log.info("Send shutdown notification to MCP clients (profile='{}')", instance.profile.name());

                // Send the notification in the format expected by the MCP protocol
                // Parameters for the notifications/cancelled event
                java.util.Map<String, Object> params = new java.util.HashMap<>();
                params.put("reason", "server_shutdown");
                params.put("message", "MCP server is shutting down");

                transport.notifyClients("notifications/cancelled", params)
                        .timeout(java.time.Duration.ofSeconds(5))
                        .doOnSuccess(v -> log.info("Shutdown notification sent successfully for profile '{}'", instance.profile.name()))
                        .doOnError(e -> log.warn("Failed to send shutdown notification for profile '{}': {}", instance.profile.name(), e.getMessage()))
                        .onErrorComplete()
                        .block();

            } catch (Exception e) {
                log.warn("Error sending shutdown notification for profile '{}': {}", instance.profile.name(), e.getMessage(), e);
            }
        }
    }

    public void stop() throws Exception {
        Exception failure = null;

        try {
            // Notify clients about the MCP server shutdown before stopping components
            sendShutdownNotification();

        } catch (Exception e) {
            log.warn("Failed to send shutdown notification: {}", e.getMessage(), e);
            failure = e;
            
        } finally {
            try {
                stopProfiles();
                
            } catch (RuntimeException e) {
                log.warn("Failed to stop MCP profiles cleanly: {}", e.getMessage(), e);
                if (failure == null) {
                    failure = e;
                }
                
            } finally {
                cleanupWorkspaceDirectory();
            }
        }

        if (failure != null) {
            throw failure;
        }
    }

    private void cleanupWorkspaceDirectory() {
        File directory = workspaceDir;
        workspaceDir = null;

        if (directory == null || !directory.exists()) {
            return;
        }

        log.debug("Delete workspace directory");
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            log.warn("Failed to delete workspace directory {}", directory.getAbsolutePath(), e);
        }
    }

    private void stopProfiles() {
        for (ServerInstance instance : serverInstances) {
            if (instance.mcpServer != null) {
                try {
                    log.debug("Close MCP server for profile '{}'", instance.profile.name());
                    instance.mcpServer.closeGracefully();
                    
                } catch (Exception e) {
                    log.warn("Failed to close MCP server for profile '{}': {}", instance.profile.name(), e.getMessage());
                }
            }
            
            if (instance.transport != null) {
                try {
                    log.debug("Closing transport for profile '{}'", instance.profile.name());
                    instance.transport.closeGracefully().block();
                    
                } catch (Exception e) {
                    log.warn("Failed to close transport for profile '{}': {}", instance.profile.name(), e.getMessage());
                }
            }
            
            if (instance.approvalServlet != null) {
                try {
                    log.debug("Resetting approval state for profile '{}'", instance.profile.name());
                    instance.approvalServlet.resetApproval();
                    
                } catch (Exception e) {
                    log.warn("Failed to reset approval state for profile '{}': {}", instance.profile.name(), e.getMessage());
                }
            }
            
            if (instance.jettyServer != null) {
                try {
                    log.debug("Stopping Jetty server for profile '{}'", instance.profile.name());
                    instance.jettyServer.stop();
                    
                } catch (Exception e) {
                    log.warn("Failed to stop Jetty server for profile '{}': {}", instance.profile.name(), e.getMessage());
                    
                } finally {
                    try {
                        instance.jettyServer.destroy();
                        
                    } catch (Exception destroyError) {
                        log.warn("Failed to destroy Jetty server for profile '{}': {}", instance.profile.name(), destroyError.getMessage());
                    }
                }
            }
        }
        serverInstances.clear();
    }
    
    // Exposed for tests to inspect Jetty configuration.
    Server getJettyServer() {
        if (serverInstances.isEmpty()) {
            return null;
        }
        return serverInstances.get(0).jettyServer;
    }
}
