package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.info.McpServerInfo;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolRegistrar;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// One MCP profile served over HTTP: the transport, the MCP server, the approval servlet and the Jetty server that publish one profile's tools on one port.
@Slf4j
final class ProfileServer {
    
    private final String name;
    private HttpServletStreamableServerTransportProvider transport;
    private McpSyncServer mcpServer;
    private Server jettyServer;
    
    private ProfileServer(String name) {
        this.name = name;
    }
    
    // Build and start one profile, and return it only once it is whole.
    static ProfileServer start(String name,
            int port,
            List<ToolProvider> providers,
            String instructions,
            Supplier<HttpServletStreamableServerTransportProvider> transportFactory) throws Exception {
        
        ProfileServer server = new ProfileServer(name);
        
        try {
            // Create the transport provider
            server.transport = transportFactory.get();
            log.info("Created transport with keep-alive interval: {} s", McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS);
            
            // Build the MCP server
            server.mcpServer = buildMcpServer(server.transport, providers, instructions);
            
            // Create the servlet
            McpClientApprovalServlet approvalServlet = new McpClientApprovalServlet(server.transport,
                    new ConnectionApproval(new SwingApprovalPrompt()),
                    new InitializeRateLimit());
            
            // Create the Jetty server
            server.jettyServer = createJettyServer(McpServerConfig.HOST, port, approvalServlet);
            server.jettyServer.start();
            
            log.info("Started MCP profile '{}' on port {}", name, port);
            return server;
            
        } catch (Exception e) {
            // Kept as suppressed rather than thrown: a failure to clean up must not replace the reason the profile did not start.
            for (Exception cleanupFailure : server.stop()) {
                e.addSuppressed(cleanupFailure);
            }
            
            throw e;
        }
    }
    
    // Release what this profile owns, whether it finished starting or not.
    List<Exception> stop() {
        List<Exception> failures = release(name, mcpServer, transport, jettyServer);
        mcpServer = null;
        transport = null;
        jettyServer = null;
        return failures;
    }
    
    // Exposed for tests to inspect the Jetty configuration; null once the profile is stopped.
    Server jettyServer() {
        return jettyServer;
    }
    
    // The settings every profile builds its transport with
    static HttpServletStreamableServerTransportProvider.Builder transportBuilder() {
        return HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .jsonMapper(JsonSupport.MCP_JSON_MAPPER)
                .keepAliveInterval(Duration.ofSeconds(McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS))
                .maxRequestSize(McpServerConfig.MCP_MAX_REQUEST_SIZE_BYTES)
                // Off until the SDK stops the sweeper's thread on close (ProfileResourceReleaseTest pins this); then turn it on at 30 minutes
                .sessionSweepInterval(null);
    }
    
    // Release the given resources of one profile
    static List<Exception> release(String profileName,
            McpSyncServer mcpServer,
            HttpServletStreamableServerTransportProvider transport,
            Server jettyServer) {
        List<Exception> failures = new ArrayList<>();
        
        if (mcpServer != null) {
            try {
                log.debug("Close MCP server for profile '{}'", profileName);
                mcpServer.closeGracefully();
                
            } catch (Exception e) {
                log.warn("Failed to close MCP server for profile '{}': {}", profileName, e.getMessage());
                failures.add(e);
            }
            
        } else if (transport != null) {
            try {
                log.debug("Closing transport for profile '{}'", profileName);
                transport.closeGracefully().block();
                
            } catch (Exception e) {
                log.warn("Failed to close transport for profile '{}': {}", profileName, e.getMessage());
                failures.add(e);
            }
        }
        
        if (jettyServer != null) {
            try {
                log.debug("Stopping Jetty server for profile '{}'", profileName);
                jettyServer.stop();
                
            } catch (Exception e) {
                log.warn("Failed to stop Jetty server for profile '{}': {}", profileName, e.getMessage());
                failures.add(e);
                
            } finally {
                try {
                    jettyServer.destroy();
                    
                } catch (Exception destroyError) {
                    log.warn("Failed to destroy Jetty server for profile '{}': {}", profileName, destroyError.getMessage());
                    failures.add(destroyError);
                }
            }
        }
        
        return failures;
    }
    
    // Build the MCP server capabilities
    private static McpSyncServer buildMcpServer(HttpServletStreamableServerTransportProvider transport,
            List<ToolProvider> toolProviders,
            String instructions) {
        
        log.info("Build MCP server capabilities");
        
        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder()
                .tools(false)               // listChanged off
                .build();
        
        McpServer.SyncSpecification<?> serverBuilder = McpServer.sync(transport)
                .serverInfo(McpSchema.Implementation.builder(McpServerInfo.ARTIFACT_ID, McpServerInfo.VERSION)
                        .title("Astah Pro MCP")
                        .build())
                .instructions(instructions)
                .capabilities(capabilities);
        
        log.info("Register all tools");
        ToolRegistrar.registerAll(serverBuilder, toolProviders);
        
        McpSyncServer server = serverBuilder.build();
        
        return server;
    }
    
    // Create the Jetty server
    private static Server createJettyServer(String host,
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
        // The delegate transport requires async support (@WebServlet(asyncSupported = true)); embedded Jetty already defaults to true, this just makes the requirement explicit
        holder.setAsyncSupported(true);
        context.addServlet(holder, "/mcp");
        
        return server;
    }
}
