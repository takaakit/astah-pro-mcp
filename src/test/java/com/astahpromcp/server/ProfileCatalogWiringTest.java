package com.astahpromcp.server;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.ExclusiveToolProvider;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.mcptoolscript.McpToolScriptProviderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

// Which tools each profile publishes, which catalog they come from, and the instructions the profile hands its clients.
public class ProfileCatalogWiringTest {

    // The one editing tool the programmatic profile publishes directly, and therefore the only one that can show this.
    private static final String DIRECTLY_PUBLISHED_EDITING_TOOL = "insert_svg_img_on_dgm";

    // The sentence the programmatic profile's instructions held as written text before it was made from the manifest
    private static final String NOT_MCP_TOOL_SCRIPT_CALLABLE_SENTENCE = "Tool functions that return an image or other content instead of structured content, that fetch over the network or convert a PDF, that create, open or save a project, that drive the diagram view (opening and closing a diagram, selection, highlighting, z-order), that answer a standalone question about the project, the Astah view, the MCP server or the logs, and 'run_astah_api_script' itself cannot be called from an mcp tool script; every one of them is already in your tool list, so call it directly as an MCP tool. ";

    @TempDir
    Path workspaceDir;

    private Map<DiagramThumbnails, ToolCatalog> catalogs() {
        return ToolCatalog.buildAll(workspaceDir.resolve("images"), workspaceDir);
    }

    private static McpToolScriptProviderFactory.McpToolScriptTools mcpToolScriptTools(Map<DiagramThumbnails, ToolCatalog> catalogs, ToolManifest manifest) {
        return new McpToolScriptProviderFactory().createToolProviders(McpServerApp.scriptCatalog(catalogs), manifest);
    }

    private static McpServerApp.ServerProfileConfig profile(String name) {
        return McpServerApp.profiles().stream()
                .filter(candidate -> candidate.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No profile is named " + name));
    }

    private static Set<String> publishedNames(List<ToolProvider> providers) {
        Set<String> names = new TreeSet<>();
        for (ToolProvider provider : providers) {
            for (ToolDefinition definition : provider.createToolDefinitions()) {
                names.add(definition.toolSchema().name());
            }
        }
        return names;
    }

    @Test
    void publishingCatalog_ok_isTheCapturingOne() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();

        assertSame(catalogs.get(DiagramThumbnails.INCLUDE), McpServerApp.publishingCatalog(catalogs));
        assertNotSame(catalogs.get(DiagramThumbnails.OMIT), McpServerApp.publishingCatalog(catalogs));
    }

    @Test
    void scriptCatalog_ok_isTheOneWithoutCaptures() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();

        assertSame(catalogs.get(DiagramThumbnails.OMIT), McpServerApp.scriptCatalog(catalogs));
    }

    @Test
    void toolProvidersOf_ok_publishesDirectlyFromTheCapturingCatalogOnly() {
        Map<DiagramThumbnails, ToolCatalog> all = catalogs();
        ToolManifest manifest = ToolManifest.load();
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(all, manifest);
        Map<DiagramThumbnails, ToolCatalog> capturingOnly = Map.of(DiagramThumbnails.INCLUDE, all.get(DiagramThumbnails.INCLUDE));

        List<ToolProvider> providers = assertDoesNotThrow(
                () -> McpServerApp.toolProvidersOf(profile("programmatic"), capturingOnly, tools, manifest),
                "the directly published tools must come from the capturing catalog alone");

        assertTrue(publishedNames(providers).contains(DIRECTLY_PUBLISHED_EDITING_TOOL),
                DIRECTLY_PUBLISHED_EDITING_TOOL + " is published directly, so it comes from the capturing catalog");
    }

    @Test
    void toolProvidersOf_ok_publishesExactlyWhatTheManifestSelects() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        ToolManifest manifest = ToolManifest.load();
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(catalogs, manifest);

        Set<String> existing = new TreeSet<>(McpServerApp.publishingCatalog(catalogs).names());
        existing.addAll(tools.names());

        for (McpServerApp.ServerProfileConfig profile : McpServerApp.profiles()) {
            Set<String> expected = new TreeSet<>(manifest.namesFor(profile.manifestColumn()));
            expected.retainAll(existing);

            Set<String> published = publishedNames(McpServerApp.toolProvidersOf(profile, catalogs, tools, manifest));

            assertEquals(expected, published, profile.name());
        }
        assertTrue(publishedNames(McpServerApp.toolProvidersOf(profile("programmatic"), catalogs, tools, manifest))
                .containsAll(tools.names()), "the shipped manifest publishes every tool that runs mcp tool scripts on the programmatic profile");
    }

    @Test
    void toolProvidersOf_ok_publishesTheToolsThatRunScriptsWhereverTheManifestSaysSo() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        ToolManifest manifest = ToolManifest.parse(List.of(
                "Common\trun_mcp_tool_script\tx\t-",
                "Common\tmcp_tool_script_guide\t-\tx"), "test");
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(catalogs, manifest);

        List<ToolProvider> direct = McpServerApp.toolProvidersOf(profile("direct"), catalogs, tools, manifest);
        List<ToolProvider> programmatic = McpServerApp.toolProvidersOf(profile("programmatic"), catalogs, tools, manifest);

        assertEquals(Set.of("run_mcp_tool_script"), publishedNames(direct));
        assertEquals(Set.of("mcp_tool_script_guide"), publishedNames(programmatic));

        // A script run holds the Astah API lock for its whole duration, on whichever profile runs it
        assertEquals(1, direct.size());
        assertInstanceOf(ExclusiveToolProvider.class, direct.get(0));
    }

    @Test
    void toolProvidersOf_ok_publishesACatalogToolOnlyOnTheProgrammaticProfileWhenTheManifestSaysSo() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        ToolManifest manifest = ToolManifest.parse(List.of(
                "Common\tget_class_info\t-\tx"), "test");
        McpToolScriptProviderFactory.McpToolScriptTools tools = mcpToolScriptTools(catalogs, manifest);

        assertEquals(Set.of(), publishedNames(McpServerApp.toolProvidersOf(profile("direct"), catalogs, tools, manifest)));
        assertEquals(Set.of("get_class_info"), publishedNames(McpServerApp.toolProvidersOf(profile("programmatic"), catalogs, tools, manifest)));

        assertNull(tools.registry().find("get_class_info"));
    }

    @Test
    void instructionsOf_ok_listsTheKindsOfToolAScriptCannotCallAsTheyReadBefore() {
        ToolManifest manifest = ToolManifest.load();

        assertEquals(NOT_MCP_TOOL_SCRIPT_CALLABLE_SENTENCE, McpServerApp.notMcpToolScriptCallableSentence(manifest));

        String programmatic = McpServerApp.instructionsOf(profile("programmatic"), manifest);
        assertTrue(programmatic.contains("rolled back. " + NOT_MCP_TOOL_SCRIPT_CALLABLE_SENTENCE + "The MCP client (you) MUST call"), programmatic);
        assertFalse(programmatic.contains(McpServerApp.NOT_MCP_TOOL_SCRIPT_CALLABLE_PLACEHOLDER), programmatic);

        String direct = McpServerApp.instructionsOf(profile("direct"), manifest);
        assertEquals(profile("direct").instructions(), direct, "the direct profile's instructions hold no list to fill in");
    }

    @Test
    void notMcpToolScriptCallableSentence_ok_followsTheManifest() {
        ToolManifest oneKind = ToolManifest.parse(List.of(
                "Common\topen_dgm\tx\tx\tview",
                "Common\tdgm_guide\tx\tx\tdirect"), "test");
        assertEquals("Tool functions that drive the diagram view (opening and closing a diagram, selection, highlighting, z-order) cannot be called from an mcp tool script; every one of them is already in your tool list, so call it directly as an MCP tool. ",
                McpServerApp.notMcpToolScriptCallableSentence(oneKind));

        ToolManifest twoKinds = ToolManifest.parse(List.of(
                "Common\topen_dgm\tx\tx\tview",
                "Common\tsave_proj\tx\tx\tproject_file"), "test");
        assertEquals("Tool functions that create, open or save a project and that drive the diagram view (opening and closing a diagram, selection, highlighting, z-order) cannot be called from an mcp tool script; every one of them is already in your tool list, so call it directly as an MCP tool. ",
                McpServerApp.notMcpToolScriptCallableSentence(twoKinds));

        ToolManifest onlyTheGenericReason = ToolManifest.parse(List.of(
                "Common\tdgm_guide\tx\tx\tdirect"), "test");
        assertEquals("", McpServerApp.notMcpToolScriptCallableSentence(onlyTheGenericReason));
    }
}
