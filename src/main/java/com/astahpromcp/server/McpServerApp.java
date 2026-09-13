package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolRegistrar;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.manifest.ToolManifestValidator;
import com.astahpromcp.tool.mcptoolscript.McpToolScriptProviderFactory;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// MCP server exposing tools over Streamable HTTP.
@Slf4j
public final class McpServerApp {

    record ServerProfileConfig(
        String name,
        int port,
        String instructions,
        // Which column of the manifest selects this profile's tools
        ToolManifest.Profile manifestColumn,
        // Whether the editing tools an mcp tool script calls capture the diagram they change
        DiagramThumbnails scriptThumbnails,
        // Whether this profile also gets the tool-function lookup tools and run_mcp_tool_script
        boolean mcpToolScriptSurface) {
    }

    private static final String DIRECT_PROFILE_INSTRUCTIONS ="This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, the MCP client (you) can reference and edit the project currently open in Astah. Note that the MCP client (you) MUST call the 'astah_pro_mcp_guide' tool function before referencing or editing the Astah project to understand how to use this MCP server, and MUST call the 'uml_modeling_architecture_insights' and 'architectural_design_smells' tool functions before creating, editing, or reviewing a UML model in order to advance your modeling capabilities. Furthermore, if the MCP client (you) performs context compression, you MUST re-reference the contents of that guide, those insights, and those smells after the compression. You MUST also require any subagents you launch to reference the contents of that guide, those insights, and those smells immediately upon launch.";

    private static final String PROGRAMMATIC_PROFILE_INSTRUCTIONS ="This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, the MCP client (you) can reference and edit the project currently open in Astah; it is intended for trusted local automation and has full access to that project. This port exposes only a small part of its tool functions directly. Use 'get_all_tools_callable_from_mcp_tool_script' to see the others and 'get_info_of_tools_callable_from_mcp_tool_script' to learn the arguments of the ones you choose, then call them from 'run_mcp_tool_script' as tools.<name>({ ... }), which lets one script perform many operations in a single round trip. There are two kinds of script here. An mcp tool script, run by 'run_mcp_tool_script', calls this server's tool functions; an astah api script, run by 'run_astah_api_script', uses the raw Astah API. Prefer the mcp tool script, and use an astah api script only for operations that require the raw Astah API. One run of 'run_mcp_tool_script' is one Astah transaction: if any tool function call fails, every change the script made is rolled back. Tool functions that return an image, that fetch over the network or convert a PDF, that save the project, that drive the diagram view (opening and closing a diagram, selection, highlighting, z-order), that answer a standalone question about the project or the logs, and 'run_astah_api_script' itself cannot be called from an mcp tool script; every one of them is already in your tool list, so call it directly as an MCP tool. The MCP client (you) MUST call the 'astah_pro_mcp_guide' tool function before referencing or editing the Astah project, MUST call the 'mcp_tool_script_guide' tool function before using 'run_mcp_tool_script', MUST call the 'astah_api_script_guide' tool function before using 'run_astah_api_script', and MUST call the 'uml_modeling_architecture_insights' and 'architectural_design_smells' tool functions before creating, editing, or reviewing a UML model in order to advance your modeling capabilities. Furthermore, if the MCP client (you) performs context compression, you MUST re-reference the contents of that guide, those insights, and those smells after the compression. You MUST also require any subagents you launch to reference the contents of that guide, those insights, and those smells immediately upon launch.";

    private static final class ServerInstance {
        private final ServerProfileConfig profile;
        private final HttpServletStreamableServerTransportProvider transport;
        private final McpSyncServer mcpServer;
        private final Server jettyServer;

        private ServerInstance(ServerProfileConfig profile,
                               HttpServletStreamableServerTransportProvider transport,
                               McpSyncServer mcpServer,
                               Server jettyServer) {
            this.profile = profile;
            this.transport = transport;
            this.mcpServer = mcpServer;
            this.jettyServer = jettyServer;
        }
    }
        
    private File workspaceDir;

    private final List<ServerInstance> serverInstances = new ArrayList<>();

    // Read once at startup and never again: the tool list of a running server does not change (the servers are built with ServerCapabilities.tools(false)).
    private ToolManifest manifest;

    // One catalog per thumbnail setting, so that the providers the setting does not affect are built once
    private Map<DiagramThumbnails, ToolCatalog> catalogs;

    public void start() throws Exception {
        log.info("=== MCP SERVER STARTING ===");

        try {
            log.info("Creating workspace directory...");
            createWorkspaceDirectory();

            log.info("Loading the tool manifest and building the tool catalog...");
            loadManifestAndCatalogs();

            log.info("Start MCP profiles");
            startProfiles();

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

    // Load the manifest and build the catalogs, then check the two against each other.
    private void loadManifestAndCatalogs() {
        manifest = ToolManifest.load();
        catalogs = ToolCatalog.buildAll(McpServerConfig.WORKSPACE_DIR.resolve("images"), McpServerConfig.WORKSPACE_DIR);
        ToolManifestValidator.verify(manifest, catalogs.values());
    }

    // The profiles this server starts, in start order
    static List<ServerProfileConfig> profiles() {
        return List.of(
                new ServerProfileConfig(
                    "direct",
                    McpServerConfig.PORT_FOR_DIRECT,
                    DIRECT_PROFILE_INSTRUCTIONS,
                    ToolManifest.Profile.DIRECT,
                    DiagramThumbnails.INCLUDE,
                    false),
                new ServerProfileConfig(
                    "programmatic",
                    McpServerConfig.PORT_FOR_PROGRAMMATIC,
                    PROGRAMMATIC_PROFILE_INSTRUCTIONS,
                    ToolManifest.Profile.PROGRAMMATIC,
                    DiagramThumbnails.OMIT,
                    true)
        );
    }

    // Start the MCP profiles
    private void startProfiles() throws Exception {
        List<ServerProfileConfig> profiles = profiles();

        try {
            for (ServerProfileConfig profile : profiles) {
                serverInstances.add(startServerInstance(profile));
            }

        } catch (Exception e) {
            log.error("Failed to start MCP profile: {}", e.getMessage(), e);
            stopProfiles();
            throw e;
        }
    }

    // Start a server instance for a given profile
    private ServerInstance startServerInstance(ServerProfileConfig profile) throws Exception {

        log.info("Initialize MCP profile '{}' on port {} (manifest column={}, script thumbnails={})",
                profile.name(), profile.port(), profile.manifestColumn(), profile.scriptThumbnails());

        // Create the transport provider
        HttpServletStreamableServerTransportProvider transport = HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .jsonMapper(JsonSupport.MCP_JSON_MAPPER)
                .keepAliveInterval(Duration.ofSeconds(McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS))
                .build();
        log.info("Created transport with keep-alive interval: {} s", McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS);
        
        // Register the tool providers
        List<ToolProvider> providers = registerToolProviders(profile);

        // Build the MCP server
        McpSyncServer mcpSyncServer = buildMcpServer(transport, providers, profile.instructions());

        // Create the servlet
        McpClientApprovalServlet approvalServlet = new McpClientApprovalServlet(transport);

        // Create the Jetty server
        Server jettyServer = createJettyServer(McpServerConfig.HOST, profile.port(), approvalServlet);
        jettyServer.start();

        log.info("Started MCP profile '{}' on port {}", profile.name(), profile.port());
        return new ServerInstance(profile,
                transport,
                mcpSyncServer,
                jettyServer);
    }

    // Register the tool providers this profile publishes.
    private List<ToolProvider> registerToolProviders(ServerProfileConfig profile) {
        log.info("Registering tool providers for profile '{}'...", profile.name());

        List<ToolProvider> toolProviders = toolProvidersOf(profile, catalogs, manifest);

        log.info("Total tool providers: {}", toolProviders.size());
        log.info("Tool providers:");
        for (ToolProvider toolProvider : toolProviders) {
            log.info("- {}", toolProvider.name());
        }

        // Check that the wiring registered what the manifest selects. This looks along the other axis than the startup checks: those read the manifest, this reads what came out of it.
        Set<String> registered = new java.util.LinkedHashSet<>();
        for (ToolProvider toolProvider : toolProviders) {
            toolProvider.createToolDefinitions().forEach(d -> registered.add(d.toolSchema().name()));
        }
        Set<String> extras = profile.mcpToolScriptSurface() ? Set.of("get_all_tools_callable_from_mcp_tool_script", "get_info_of_tools_callable_from_mcp_tool_script", "run_mcp_tool_script", "mcp_tool_script_guide", "get_mcp_tool_script_example") : Set.of();
        ToolManifestValidator.verifyRegistered(profile.name(), registered, manifest, profile.manifestColumn(), publishingCatalog(catalogs), extras);
        log.info("Profile '{}' publishes {} tools", profile.name(), registered.size());

        return toolProviders;
    }

    // The tools a profile publishes, drawn from the catalog each half of the profile is entitled to.
    static List<ToolProvider> toolProvidersOf(ServerProfileConfig profile,
                                              Map<DiagramThumbnails, ToolCatalog> catalogs,
                                              ToolManifest manifest) {

        List<ToolProvider> toolProviders = new ArrayList<>();
        if (profile.mcpToolScriptSurface()) {
            // Only the tool functions an mcp tool script reaches are subject to the profile's thumbnail setting: the dispatcher keeps their structured content and drops everything else, so a capture made for them would be thrown away.
            toolProviders.addAll(new McpToolScriptProviderFactory()
                    .createToolProviders(catalogs.get(profile.scriptThumbnails()), manifest));
        }
        toolProviders.addAll(publishingCatalog(catalogs)
                .select(manifest.namesFor(profile.manifestColumn()))
                .withAstahLock());

        return toolProviders;
    }

    // The catalog every profile publishes from directly.
    static ToolCatalog publishingCatalog(Map<DiagramThumbnails, ToolCatalog> catalogs) {
        return catalogs.get(DiagramThumbnails.INCLUDE);
    }

    // Build the MCP server capabilities
    private McpSyncServer buildMcpServer(HttpServletStreamableServerTransportProvider transport,
                                         List<ToolProvider> toolProviders,
                                         String instructions) {

        log.info("Build MCP server capabilities");

        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder()
                .tools(false)               // listChanged off
                .build();

        McpServer.SyncSpecification<?> serverBuilder = McpServer.sync(transport)
                .serverInfo(McpSchema.Implementation.builder(getArtifactId(), getVersion())
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
        // The delegate transport requires async support (@WebServlet(asyncSupported = true)); embedded Jetty already defaults to true, this just makes the requirement explicit
        holder.setAsyncSupported(true);
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
    List<Server> getJettyServers() {
        return serverInstances.stream()
                .map(instance -> instance.jettyServer)
                .toList();
    }
}
