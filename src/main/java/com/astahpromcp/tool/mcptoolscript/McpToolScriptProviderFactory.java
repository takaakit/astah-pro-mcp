package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.ExclusiveToolProvider;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// The tools that exist only on the programmatic profile.
@Slf4j
public class McpToolScriptProviderFactory {

    // The tools the programmatic profile adds on top of the ones the manifest selects from the catalog.
    public List<ToolProvider> createToolProviders(ToolCatalog catalog, ToolManifest manifest) {
        // The two arguments are the whole rule: the direct profile, which publishes every tool, is what the registry can reach, and the programmatic column names the tools this profile hands the agent directly, which is exactly what an mcp tool script may not call.
        AstahToolRegistry registry = AstahToolRegistry.from(
                catalog,
                manifest.namesFor(ToolManifest.Profile.DIRECT),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC));
        log.info("MCP tool script registry holds {} tool functions, {} of them callable from an mcp tool script and the rest published directly",
                registry.size(), registry.mcpToolScriptCallableNames().size());

        McpToolScriptExecutor mcpToolScriptExecutor = new McpToolScriptExecutor(registry);

        return List.of(
                // Reads a classpath resource only, so it takes no lock either.
                new McpToolScriptExampleTool(),

                // Reads the registry only, so it must not take the Astah API lock: doing so would make a plain lookup queue behind whatever edit another agent is running.
                new CallableToolInfoTool(registry),

                // Takes the lock for the whole run, which is what serializes the tool calls the mcp tool script makes and what lets the dispatcher call unwrapped handlers without locking again.
                new ExclusiveToolProvider(new McpToolScriptTool(mcpToolScriptExecutor)));
    }
}
