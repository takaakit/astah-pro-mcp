package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.info.McpServerInfo;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolCallStatistics;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolRegistrar;
import com.astahpromcp.tool.astah.pro.AstahProToolFactory;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
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
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

// MCP server exposing tools over Streamable HTTP.
@Slf4j
public final class McpServerApp {

    record ServerProfileConfig(
        String name,
        int port,
        String instructions,
        // Which column of the manifest selects this profile's tools
        ToolManifest.Profile manifestColumn) {
    }

    // Where the programmatic profile's instructions list the kinds of tool function an mcp tool script cannot call.
    static final String NOT_MCP_TOOL_SCRIPT_CALLABLE_PLACEHOLDER = "{notMcpToolScriptCallableTools}";

    private static final String DIRECT_PROFILE_INSTRUCTIONS ="This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, the MCP client (you) can reference and edit the project currently open in Astah. Note that the MCP client (you) MUST call the 'astah_pro_mcp_guide' tool function before referencing or editing the Astah project to understand how to use this MCP server, and MUST call the 'uml_modeling_architecture_insights' and 'architectural_design_smells' tool functions before creating, editing, or reviewing a UML model in order to advance your modeling capabilities. Furthermore, if the MCP client (you) performs context compression, you MUST re-reference the contents of that guide, those insights, and those smells after the compression. You MUST also require any subagents you launch to reference the contents of that guide, those insights, and those smells immediately upon launch.";

    private static final String PROGRAMMATIC_PROFILE_INSTRUCTIONS ="This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, the MCP client (you) can reference and edit the project currently open in Astah; it is intended for trusted local automation and has full access to that project. This port exposes only a small part of its tool functions directly. Read every chunk of 'get_chunk_of_tools_callable_from_mcp_tool_script' to see the others (call it with 'chunkIndex' 0 first, then request every remaining index its 'totalChunks' names, all in parallel) and use 'get_info_of_tools_callable_from_mcp_tool_script' to learn the arguments of the ones you choose, then call them from 'run_mcp_tool_script' as tools.<name>({ ... }), which lets one script perform many operations in a single round trip. There are two kinds of script here. An mcp tool script, run by 'run_mcp_tool_script', calls this server's tool functions; an astah api script, run by 'run_astah_api_script', uses the raw Astah API. Prefer the mcp tool script, and use an astah api script only for operations that require the raw Astah API. One run of 'run_mcp_tool_script' is one Astah transaction: if any tool function call fails, every change the script made is rolled back. " + NOT_MCP_TOOL_SCRIPT_CALLABLE_PLACEHOLDER + "The MCP client (you) MUST call the 'astah_pro_mcp_guide' tool function before referencing or editing the Astah project, MUST call the 'mcp_tool_script_guide' tool function before using 'run_mcp_tool_script', MUST call the 'astah_api_script_guide' tool function before using 'run_astah_api_script', and MUST call the 'uml_modeling_architecture_insights' and 'architectural_design_smells' tool functions before creating, editing, or reviewing a UML model in order to advance your modeling capabilities. Furthermore, if the MCP client (you) performs context compression, you MUST re-reference the contents of that guide, those insights, and those smells after the compression. You MUST also require any subagents you launch to reference the contents of that guide, those insights, and those smells immediately upon launch.";

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

    // The tools that run mcp tool scripts, built once; the manifest decides which profiles publish them
    private McpToolScriptProviderFactory.McpToolScriptTools mcpToolScriptTools;

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
        WorkspaceDirectories.prepare(McpServerConfig.WORKSPACE_DIR, WorkspaceDirectories::isProcessAlive);
    }

    // Load the manifest and build every tool, then check the two against each other.
    private void loadManifestAndCatalogs() {
        manifest = ToolManifest.load();
        catalogs = ToolCatalog.buildAll(McpServerConfig.WORKSPACE_DIR.resolve("images"), McpServerConfig.WORKSPACE_DIR);
        mcpToolScriptTools = new McpToolScriptProviderFactory().createToolProviders(scriptCatalog(catalogs), manifest);
        ToolManifestValidator.verify(manifest, catalogs.values(), mcpToolScriptTools.names(), AstahProToolFactory.isRunningInsideAstahGui());
    }

    // The profiles this server starts, in start order
    static List<ServerProfileConfig> profiles() {
        return List.of(
                new ServerProfileConfig(
                    "direct",
                    McpServerConfig.PORT_FOR_DIRECT,
                    DIRECT_PROFILE_INSTRUCTIONS,
                    ToolManifest.Profile.DIRECT),
                new ServerProfileConfig(
                    "programmatic",
                    McpServerConfig.PORT_FOR_PROGRAMMATIC,
                    PROGRAMMATIC_PROFILE_INSTRUCTIONS,
                    ToolManifest.Profile.PROGRAMMATIC)
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

    // Start a server instance for a given profile.
    // A profile is registered only once it is whole, so anything created before a failure is unreachable to stopProfiles()
    // and is released here instead: the transport starts a keep-alive scheduler the moment it is built, and that task
    // holds on to the transport, so losing the local variable does not stop it.
    private ServerInstance startServerInstance(ServerProfileConfig profile) throws Exception {

        log.info("Initialize MCP profile '{}' on port {} (manifest column={})", profile.name(), profile.port(), profile.manifestColumn());

        HttpServletStreamableServerTransportProvider transport = null;
        McpSyncServer mcpSyncServer = null;
        Server jettyServer = null;

        try {
            // Create the transport provider
            transport = transportFactory.get();
            log.info("Created transport with keep-alive interval: {} s", McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS);

            // Register the tool providers
            List<ToolProvider> providers = registerToolProviders(profile);

            // Build the MCP server
            mcpSyncServer = buildMcpServer(transport, providers, instructionsOf(profile, manifest));

            // Create the servlet
            McpClientApprovalServlet approvalServlet = new McpClientApprovalServlet(transport);

            // Create the Jetty server
            jettyServer = createJettyServer(McpServerConfig.HOST, profile.port(), approvalServlet);
            jettyServer.start();

            log.info("Started MCP profile '{}' on port {}", profile.name(), profile.port());
            return new ServerInstance(profile,
                    transport,
                    mcpSyncServer,
                    jettyServer);

        } catch (Exception e) {
            // Kept as suppressed rather than thrown: a failure to clean up must not replace the reason the profile did not start.
            for (Exception cleanupFailure : releaseProfileResources(profile.name(), mcpSyncServer, transport, jettyServer)) {
                e.addSuppressed(cleanupFailure);
            }

            throw e;
        }
    }

    // Package-private so that a test can hold on to the transport a failed start creates: the failure path drops it,
    // and what has to be proven is that it was closed before that.
    Supplier<HttpServletStreamableServerTransportProvider> transportFactory = McpServerApp::newTransport;

    private static HttpServletStreamableServerTransportProvider newTransport() {
        return HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .jsonMapper(JsonSupport.MCP_JSON_MAPPER)
                .keepAliveInterval(Duration.ofSeconds(McpServerConfig.TRANSPORT_KEEP_ALIVE_INTERVAL_SECONDS))
                .maxRequestSize(McpServerConfig.MCP_MAX_REQUEST_SIZE_BYTES)
                .build();
    }

    // Release what one profile owns, whether it finished starting or not.
    // Every step is attempted even when an earlier one fails, and the failures are returned rather than thrown, so that
    // the caller decides what they mean.
    static List<Exception> releaseProfileResources(String profileName,
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

    // Register the tool providers this profile publishes.
    private List<ToolProvider> registerToolProviders(ServerProfileConfig profile) {
        log.info("Registering tool providers for profile '{}'...", profile.name());

        List<ToolProvider> toolProviders = toolProvidersOf(profile, catalogs, mcpToolScriptTools, manifest);

        log.info("Total tool providers: {}", toolProviders.size());
        log.info("Tool providers:");
        for (ToolProvider toolProvider : toolProviders) {
            log.info("- {}", toolProvider.name());
        }

        // Check that the wiring registered what the manifest selects. This looks along the other axis than the startup checks: those read the manifest, this reads what came out of it.
        Set<String> registered = new LinkedHashSet<>();
        for (ToolProvider toolProvider : toolProviders) {
            toolProvider.createToolDefinitions().forEach(d -> registered.add(d.toolSchema().name()));
        }
        Set<String> existing = new LinkedHashSet<>(publishingCatalog(catalogs).names());
        existing.addAll(mcpToolScriptTools.names());
        ToolManifestValidator.verifyRegistered(profile.name(), registered, manifest, profile.manifestColumn(), existing);
        log.info("Profile '{}' publishes {} tools", profile.name(), registered.size());

        return toolProviders;
    }

    // The tools a profile publishes: whatever its manifest column selects, among the tools that run mcp tool scripts and the tools of the catalog.
    static List<ToolProvider> toolProvidersOf(ServerProfileConfig profile,
                                              Map<DiagramThumbnails, ToolCatalog> catalogs,
                                              McpToolScriptProviderFactory.McpToolScriptTools mcpToolScriptTools,
                                              ToolManifest manifest) {

        Set<String> names = manifest.namesFor(profile.manifestColumn());

        List<ToolProvider> toolProviders = new ArrayList<>(mcpToolScriptTools.select(names));
        toolProviders.addAll(publishingCatalog(catalogs)
                .select(names)
                .withAstahLock());

        return toolProviders;
    }

    // The catalog every profile publishes from directly.
    static ToolCatalog publishingCatalog(Map<DiagramThumbnails, ToolCatalog> catalogs) {
        return catalogs.get(DiagramThumbnails.INCLUDE);
    }

    // The catalog an mcp tool script calls into, on whichever profile runs it: the dispatcher keeps only the structured content of a call, so a diagram thumbnail captured for it would be thrown away.
    static ToolCatalog scriptCatalog(Map<DiagramThumbnails, ToolCatalog> catalogs) {
        return catalogs.get(DiagramThumbnails.OMIT);
    }

    // The instructions a profile's server hands its clients
    static String instructionsOf(ServerProfileConfig profile, ToolManifest manifest) {
        return profile.instructions().replace(NOT_MCP_TOOL_SCRIPT_CALLABLE_PLACEHOLDER, notMcpToolScriptCallableSentence(manifest));
    }

    // The sentence that lists the kinds of tool function an mcp tool script cannot call, made from the reasons the manifest gives, in the order NotMcpToolScriptCallableReason declares them.
    static String notMcpToolScriptCallableSentence(ToolManifest manifest) {
        Map<NotMcpToolScriptCallableReason, List<String>> namesByReason = new EnumMap<>(NotMcpToolScriptCallableReason.class);
        manifest.notMcpToolScriptCallableReasons().forEach((name, reason) ->
                namesByReason.computeIfAbsent(reason, r -> new ArrayList<>()).add(name));

        List<String> phrases = new ArrayList<>();
        namesByReason.forEach((reason, names) -> {
            String phrase = reason.instructionPhrase(names);
            if (phrase != null) {
                phrases.add(phrase);
            }
        });
        if (phrases.isEmpty()) {
            return "";
        }
        return "Tool functions " + joinedAsAList(phrases) + " cannot be called from an mcp tool script; every one of them is already in your tool list, so call it directly as an MCP tool. ";
    }

    // "a", "a and b", "a, b, and c"
    private static String joinedAsAList(List<String> items) {
        if (items.size() == 1) {
            return items.get(0);
        }
        if (items.size() == 2) {
            return items.get(0) + " and " + items.get(1);
        }
        return String.join(", ", items.subList(0, items.size() - 1)) + ", and " + items.get(items.size() - 1);
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

    public void stop() {
        try {
            stopProfiles();

        } finally {
            ToolCallStatistics.logAndReset();
            cleanupWorkspaceDirectory();
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
            releaseProfileResources(instance.profile.name(), instance.mcpServer, instance.transport, instance.jettyServer);
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
