package com.astahpromcp.server;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tls.emudhra.EmudhraWorkaround;
import com.astahpromcp.tool.ToolCallStatistics;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.ExclusiveToolProvider;
import com.astahpromcp.tool.astah.pro.assembly.AstahProToolFactory;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.astah.pro.mcptoolscript.AstahMcpToolScriptHost;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.manifest.ToolManifestValidator;
import com.astahpromcp.tool.mcptoolscript.McpToolScriptProviderFactory;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import com.astahpromcp.tool.visualization.PlantumlSandbox;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
    
    // Puts a provider behind the Astah API lock
    static final UnaryOperator<ToolProvider> ASTAH_API_LOCK = ExclusiveToolProvider::new;
    
    // Where the programmatic profile's instructions list the kinds of tool function an mcp tool script cannot call.
    static final String NOT_MCP_TOOL_SCRIPT_CALLABLE_PLACEHOLDER = "{notMcpToolScriptCallableTools}";
    
    private static final String DIRECT_PROFILE_INSTRUCTIONS ="This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, the MCP client (you) can reference and edit the project currently open in Astah. Note that the MCP client (you) MUST call the 'astah_pro_mcp_guide' tool function before referencing or editing the Astah project to understand how to use this MCP server, and MUST call the 'uml_modeling_architecture_insights' and 'architectural_design_smells' tool functions before creating, editing, or reviewing a UML model in order to advance your modeling capabilities. Furthermore, if the MCP client (you) performs context compression, you MUST re-reference the contents of that guide, those insights, and those smells after the compression. You MUST also require any subagents you launch to reference the contents of that guide, those insights, and those smells immediately upon launch.";
    
    private static final String PROGRAMMATIC_PROFILE_INSTRUCTIONS ="This MCP server operates as a plugin for the modeling tool Astah. Using the tool functions it provides, the MCP client (you) can reference and edit the project currently open in Astah; it is intended for trusted local automation and has full access to that project. This port exposes only a small part of its tool functions directly. Read every chunk of 'get_chunk_of_tools_callable_from_mcp_tool_script' to see the others (call it with 'chunkIndex' 0 first, then request every remaining index its 'totalChunks' names, all in parallel) and use 'get_info_of_tools_callable_from_mcp_tool_script' to learn the arguments of the ones you choose, then call them from 'run_mcp_tool_script' as tools.<name>({ ... }), which lets one script perform many operations in a single round trip. There are two kinds of script here. An mcp tool script, run by 'run_mcp_tool_script', calls this server's tool functions; an astah api script, run by 'run_astah_api_script', uses the raw Astah API. Prefer the mcp tool script, and use an astah api script only for operations that require the raw Astah API. One run of 'run_mcp_tool_script' is one Astah transaction: if any tool function call fails, every change the script made is rolled back. " + NOT_MCP_TOOL_SCRIPT_CALLABLE_PLACEHOLDER + "The MCP client (you) MUST call the 'astah_pro_mcp_guide' tool function before referencing or editing the Astah project, MUST call the 'mcp_tool_script_guide' tool function before using 'run_mcp_tool_script', MUST call the 'astah_api_script_guide' tool function before using 'run_astah_api_script', and MUST call the 'uml_modeling_architecture_insights' and 'architectural_design_smells' tool functions before creating, editing, or reviewing a UML model in order to advance your modeling capabilities. Furthermore, if the MCP client (you) performs context compression, you MUST re-reference the contents of that guide, those insights, and those smells after the compression. You MUST also require any subagents you launch to reference the contents of that guide, those insights, and those smells immediately upon launch.";
    
    private File workspaceDir;
    
    // The profiles that finished starting, in start order.
    private final List<ProfileServer> profileServers = new ArrayList<>();
    
    // Read once at startup and never again: the tool list of a running server does not change (the servers are built with ServerCapabilities.tools(false)).
    private ToolManifest manifest;
    
    // One catalog per thumbnail setting, so that the providers the setting does not affect are built once
    private Map<DiagramThumbnails, ToolCatalog> catalogs;
    
    // The tools that run mcp tool scripts, built once; the manifest decides which profiles publish them
    private McpToolScriptProviderFactory.McpToolScriptTools mcpToolScriptTools;
    
    // The project comparisons, which both the tool that starts them and the tool that fetches their results share on every profile.
    private ProjectDiffJobs projectDiffJobs;
    
    public void start() throws Exception {
        log.info("=== MCP SERVER STARTING ===");
        
        try {
            log.info("Fixing PlantUML to its sandbox security profile...");
            fixPlantumlSandbox();
            
            EmudhraWorkaround.logStatus(); // EMUDHRA-WORKAROUND
            
            log.info("Creating workspace directory...");
            createWorkspaceDirectory();
            
            log.info("Loading the tool manifest and building the tool catalog...");
            loadManifestAndCatalogs();
            
            log.info("Start MCP profiles");
            startProfiles();
            
            log.info("=== MCP SERVER STARTED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            log.error("=== MCP SERVER START FAILED ===", e);
            stop();
            throw e;
        }
    }
    
    // Fix PlantUML to its SANDBOX profile before anything else can make PlantUML read its profile
    private void fixPlantumlSandbox() {
        try {
            PlantumlSandbox.ensure();
            
        } catch (IllegalStateException e) {
            log.error("Failed to fix PlantUML to its sandbox security profile; every PlantUML rendering will be refused", e);
        }
    }
    
    private void createWorkspaceDirectory() throws IOException {
        workspaceDir = McpServerConfig.WORKSPACE_DIR.toFile();
        WorkspaceDirectories.prepare(McpServerConfig.WORKSPACE_DIR, WorkspaceDirectories::isProcessAlive);
    }
    
    // Load the manifest and build every tool, then check the two against each other.
    private void loadManifestAndCatalogs() {
        manifest = ToolManifest.load();
        projectDiffJobs = new ProjectDiffJobs(McpServerConfig.WORKSPACE_DIR.resolve("project-diff"));
        catalogs = ToolCatalogAssembler.buildAll(McpServerConfig.WORKSPACE_DIR.resolve("images"), McpServerConfig.WORKSPACE_DIR, McpServerConfig.DOCUMENT_CACHE_DIR, projectDiffJobs);
        mcpToolScriptTools = new McpToolScriptProviderFactory().createToolProviders(scriptCatalog(catalogs), manifest, new AstahMcpToolScriptHost());
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
                profileServers.add(startProfile(profile));
            }
            
        } catch (Exception e) {
            log.error("Failed to start MCP profile: {}", e.getMessage(), e);
            stopProfiles();
            throw e;
        }
    }
    
    // Start one profile: decide which tools it publishes, then have a ProfileServer build and start what serves them.
    private ProfileServer startProfile(ServerProfileConfig profile) throws Exception {
        
        log.info("Initialize MCP profile '{}' on port {} (manifest column={})", profile.name(), profile.port(), profile.manifestColumn());
        
        // Register the tool providers
        List<ToolProvider> providers = registerToolProviders(profile);
        
        return ProfileServer.start(profile.name(), profile.port(), providers, instructionsOf(profile, manifest), transportFactory);
    }
    
    // Package-private so that a test can hold on to the transport a failed start creates: the failure path drops it,
    // and what has to be proven is that it was closed before that.
    Supplier<HttpServletStreamableServerTransportProvider> transportFactory = () -> ProfileServer.transportBuilder().build();
    
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
        
        List<ToolProvider> toolProviders = new ArrayList<>(mcpToolScriptTools.select(names, ASTAH_API_LOCK));
        toolProviders.addAll(publishingCatalog(catalogs)
                .select(names)
                .withAstahLock(ASTAH_API_LOCK));
        
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
    
    public void stop() {
        try {
            stopProfiles();
            
        } finally {
            ToolCallStatistics.logAndReset();
            // The comparisons write into the workspace, from threads and processes of their own, so the workspace is deleted only once they are known to have stopped.
            if (stopProjectDiffs()) {
                cleanupWorkspaceDirectory();
            } else {
                log.warn("Keep the workspace directory {}, because not every project comparison could be confirmed to have stopped", workspaceDir);
            }
        }
    }
    
    // Stop the project comparisons
    private boolean stopProjectDiffs() {
        ProjectDiffJobs jobs = projectDiffJobs;
        projectDiffJobs = null;
        return jobs == null || jobs.shutdown();
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
        for (ProfileServer profileServer : profileServers) {
            profileServer.stop();
        }
        profileServers.clear();
    }
    
    // Exposed for tests to inspect Jetty configuration.
    List<Server> getJettyServers() {
        return profileServers.stream()
                .map(ProfileServer::jettyServer)
                .toList();
    }
}
