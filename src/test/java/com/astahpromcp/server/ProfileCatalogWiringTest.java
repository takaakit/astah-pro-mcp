package com.astahpromcp.server;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

// Which catalog each half of a profile is built from.
public class ProfileCatalogWiringTest {

    // The one editing tool the programmatic profile publishes directly, and therefore the only one that can show this.
    private static final String DIRECTLY_PUBLISHED_EDITING_TOOL = "insert_svg_img_on_dgm";

    @TempDir
    Path workspaceDir;

    private Map<DiagramThumbnails, ToolCatalog> catalogs() {
        return ToolCatalog.buildAll(workspaceDir.resolve("images"), workspaceDir);
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
    void profiles_ok_onlyTheProgrammaticProfileSkipsTheCapture() {
        assertEquals(DiagramThumbnails.INCLUDE, profile("direct").scriptThumbnails());
        assertEquals(DiagramThumbnails.OMIT, profile("programmatic").scriptThumbnails());

        assertTrue(profile("programmatic").mcpToolScriptSurface(),
                "the setting saves the captures a script's inner calls would make, so only a profile that runs scripts may hold it");
    }

    @Test
    void publishingCatalog_ok_isTheCapturingOne() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();

        assertSame(catalogs.get(DiagramThumbnails.INCLUDE), McpServerApp.publishingCatalog(catalogs));
        assertNotSame(catalogs.get(DiagramThumbnails.OMIT), McpServerApp.publishingCatalog(catalogs));
    }

    @Test
    void toolProvidersOf_ok_publishesDirectlyWithoutConsultingTheThumbnailSetting() {
        Map<DiagramThumbnails, ToolCatalog> capturingOnly =
                Map.of(DiagramThumbnails.INCLUDE, catalogs().get(DiagramThumbnails.INCLUDE));

        McpServerApp.ServerProfileConfig skipsCapturesInScripts = new McpServerApp.ServerProfileConfig(
                "test_profile",
                0,
                "",
                ToolManifest.Profile.PROGRAMMATIC,
                DiagramThumbnails.OMIT,
                false);

        List<ToolProvider> providers = assertDoesNotThrow(
                () -> McpServerApp.toolProvidersOf(skipsCapturesInScripts, capturingOnly, ToolManifest.load()),
                "the directly published half asked for a catalog by the profile's thumbnail setting, which is the setting it must ignore");

        assertTrue(publishedNames(providers).contains(DIRECTLY_PUBLISHED_EDITING_TOOL),
                DIRECTLY_PUBLISHED_EDITING_TOOL + " is published directly, so it comes from the capturing catalog whatever the profile says about scripts");
    }

    @Test
    void toolProvidersOf_ok_publishesExactlyWhatTheManifestSelects() {
        Map<DiagramThumbnails, ToolCatalog> catalogs = catalogs();
        ToolManifest manifest = ToolManifest.load();
        ToolCatalog catalog = McpServerApp.publishingCatalog(catalogs);

        Set<String> expected = new TreeSet<>(Set.of(
                "get_all_tools_callable_from_mcp_tool_script",
                "get_info_of_tools_callable_from_mcp_tool_script",
                "run_mcp_tool_script",
                "mcp_tool_script_guide",
                "get_mcp_tool_script_example"));
        for (String name : manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC)) {
            if (catalog.contains(name)) {
                expected.add(name);
            }
        }

        Set<String> published = publishedNames(
                McpServerApp.toolProvidersOf(profile("programmatic"), catalogs, manifest));

        assertEquals(expected, published);
        assertTrue(published.contains(DIRECTLY_PUBLISHED_EDITING_TOOL));
    }
}
