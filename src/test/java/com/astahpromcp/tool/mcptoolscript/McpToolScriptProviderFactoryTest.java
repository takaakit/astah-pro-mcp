package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class McpToolScriptProviderFactoryTest {

    private static final String CHUNK_TOOL = "get_chunk_of_tools_callable_from_mcp_tool_script";
    private static final String INFO_TOOL = "get_info_of_tools_callable_from_mcp_tool_script";

    @TempDir
    Path workspaceDir;

    // The project comparisons the tools of the catalog share
    private ProjectDiffJobs projectDiffJobs;

    @BeforeEach
    void setUp() {
        projectDiffJobs = new ProjectDiffJobs(workspaceDir.resolve("project-diff"));
    }

    @AfterEach
    void tearDown() {
        projectDiffJobs.shutdown();
    }

    private McpToolScriptProviderFactory.McpToolScriptTools tools(ToolManifest manifest) {
        ToolCatalog catalog = ToolCatalog.build(DiagramThumbnails.OMIT, workspaceDir.resolve("images"), workspaceDir, projectDiffJobs);
        return new McpToolScriptProviderFactory().createToolProviders(catalog, manifest);
    }

    private static Map<String, Object> parse(String json) {
        return JsonSupport.OBJ_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    private static Set<String> namesOf(Object entries) {
        Set<String> names = new TreeSet<>();
        for (Object entry : (List<?>) entries) {
            names.add(String.valueOf(((Map<?, ?>) entry).get("name")));
        }
        return names;
    }

    // The shipped manifest publishes them directly on the programmatic profile, so the registry holds them only to tell a script why it cannot call them
    @Test
    void createToolProviders_ok_holdsTheToolsThatRunScriptsButLetsNoScriptCallThemUnderTheShippedManifest() {
        McpToolScriptProviderFactory.McpToolScriptTools tools = tools(ToolManifest.load());

        assertEquals(5, tools.names().size(), "the comparison is only meaningful if the tools were actually built");
        for (String name : tools.names()) {
            assertNotNull(tools.registry().find(name), name);
            assertNotNull(tools.registry().notMcpToolScriptCallableReason(name), name);
            assertFalse(tools.registry().mcpToolScriptCallableNames().contains(name), name);
        }
    }

    // What a script can call is exactly the rows marked 's', in file order, all of which exist outside the Astah GUI too
    @Test
    void createToolProviders_ok_letsAScriptCallExactlyTheRowsMarkedS() {
        ToolManifest manifest = ToolManifest.load();
        McpToolScriptProviderFactory.McpToolScriptTools tools = tools(manifest);

        assertEquals(List.copyOf(manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC, ToolManifest.Reach.MCP_TOOL_SCRIPT)), List.copyOf(tools.registry().mcpToolScriptCallableNames()));
        assertTrue(manifest.namesReachedOn(ToolManifest.Profile.PROGRAMMATIC).containsAll(tools.registry().names()));
    }

    // programmatic = s makes them callable from a script, and the list a script reads then names them too
    @Test
    void createToolProviders_ok_letsAScriptCallTheToolsThatListTheCallableOnesWhenTheManifestSaysSo() {
        McpToolScriptProviderFactory.McpToolScriptTools tools = tools(ToolManifest.parse(List.of(
                "Common\t" + CHUNK_TOOL + "\t-\ts",
                "Common\t" + INFO_TOOL + "\t-\ts"), "test"));

        assertEquals(Set.of(CHUNK_TOOL, INFO_TOOL), tools.registry().mcpToolScriptCallableNames());

        McpToolScriptDispatcher dispatcher = new McpToolScriptDispatcher(tools.registry());

        Map<String, Object> chunk = parse(dispatcher.invoke(CHUNK_TOOL, "{\"chunkIndex\":0}"));
        assertEquals(2, chunk.get("totalTools"));
        assertEquals(Set.of(CHUNK_TOOL, INFO_TOOL), namesOf(chunk.get("tools")));

        Map<String, Object> info = parse(dispatcher.invoke(INFO_TOOL, "{\"toolNames\":[\"" + CHUNK_TOOL + "\"]}"));
        assertEquals(Set.of(CHUNK_TOOL), namesOf(info.get("tools")));
    }

    // Published directly on the programmatic profile, as the shipped manifest publishes it, the tool is refused from a script for the reason the manifest gives, before it runs
    @Test
    void createToolProviders_ok_refusesAToolThatRunsScriptsForTheReasonTheManifestGives() {
        McpToolScriptProviderFactory.McpToolScriptTools tools = tools(ToolManifest.parse(List.of(
                "Common\trun_mcp_tool_script\t-\tx\tdirect"), "test"));

        assertEquals(NotMcpToolScriptCallableReason.PUBLISHED_DIRECTLY,
                tools.registry().notMcpToolScriptCallableReason("run_mcp_tool_script"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new McpToolScriptDispatcher(tools.registry()).invoke("run_mcp_tool_script", "{\"script\":\"1;\"}"));
        assertEquals(NotMcpToolScriptCallableReason.PUBLISHED_DIRECTLY.message("run_mcp_tool_script"), e.getMessage());
    }

    // A tool that answers with text meets the check every such tool meets, rather than being left out of the registry
    @Test
    void createToolProviders_ok_turnsAwayAToolThatRunsScriptsAndAnswersWithTextLikeAnyOther() {
        McpToolScriptProviderFactory.McpToolScriptTools tools = tools(ToolManifest.parse(List.of(
                "Common\tmcp_tool_script_guide\t-\ts"), "test"));

        assertEquals(Set.of("mcp_tool_script_guide"), tools.registry().mcpToolScriptCallableNames());

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new McpToolScriptDispatcher(tools.registry()).invoke("mcp_tool_script_guide", "{}"));
        assertEquals("'mcp_tool_script_guide' returned no structured content, so it cannot be called from an mcp tool script. Call 'mcp_tool_script_guide' directly as an MCP tool instead.", e.getMessage());
    }
}
