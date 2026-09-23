package com.astahpromcp.tool.manifest;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ToolManifestConsistencyTest {

    @TempDir
    Path workspaceDir;

    private ToolCatalog catalog() {
        return ToolCatalog.build(DiagramThumbnails.INCLUDE, workspaceDir.resolve("images"), workspaceDir);
    }

    private static String row(String name, String direct, String programmatic) {
        return ToolManifest.Category.COMMON.label() + "\t" + name + "\t" + direct + "\t" + programmatic;
    }

    private static Set<String> namesOf(List<ToolProvider> providers) {
        Set<String> names = new TreeSet<>();
        Set<String> seen = new HashSet<>();
        for (ToolProvider provider : providers) {
            for (ToolDefinition definition : provider.createToolDefinitions()) {
                String name = definition.toolSchema().name();
                assertTrue(seen.add(name), "the same tool name was registered twice: " + name);
                names.add(name);
            }
        }
        return names;
    }

    @Test
    void select_ok_registersExactlyWhatTheManifestSelects() {
        ToolManifest manifest = ToolManifest.load();
        ToolCatalog catalog = catalog();

        for (ToolManifest.Profile profile : ToolManifest.Profile.values()) {
            Set<String> expected = new TreeSet<>();
            for (String name : manifest.namesFor(profile)) {
                if (catalog.contains(name)) {
                    expected.add(name);
                }
            }

            Set<String> registered = namesOf(catalog.select(manifest.namesFor(profile)).withAstahLock());

            assertEquals(expected, registered, "profile " + profile);
        }
    }

    @Test
    void select_ok_yieldsTheExpectedCountsOutsideTheAstahGui() {
        ToolManifest manifest = ToolManifest.load();
        ToolCatalog catalog = catalog();

        // 399 = rows with direct = x, 19 = the view manager tools the catalog lacks outside the GUI.
        assertEquals(399 - 19,
                catalog.select(manifest.namesFor(ToolManifest.Profile.DIRECT)).unlocked().stream()
                        .mapToLong(p -> p.createToolDefinitions().size()).sum());
        // 84 = rows with programmatic = x, 17 = the view manager tools the catalog lacks outside the GUI,
        // 5 = get_chunk_of_tools_callable_from_mcp_tool_script, get_info_of_tools_callable_from_mcp_tool_script, run_mcp_tool_script, mcp_tool_script_guide and get_mcp_tool_script_example, which McpToolScriptProviderFactory builds rather than the catalog.
        assertEquals(84 - 17 - 5,
                catalog.select(manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC)).unlocked().stream()
                        .mapToLong(p -> p.createToolDefinitions().size()).sum());
    }

    @Test
    void verify_ok_acceptsTheShippedManifestAgainstTheCatalog() {
        assertDoesNotThrow(() -> ToolManifestValidator.verify(ToolManifest.load(), List.of(catalog())));
    }

    @Test
    void verify_ng_refusesToStartWhenAToolHasNoRow() {
        ToolCatalog catalog = catalog();
        ToolManifest withoutOneTool = ToolManifest.parse(
                List.of(row("get_class_info", "x", "-")), "test");

        Exception e = assertThrows(IllegalStateException.class,
                () -> ToolManifestValidator.verify(withoutOneTool, List.of(catalog)));
        assertTrue(e.getMessage().contains("no manifest row"), e.getMessage());
    }

    @Test
    void verify_ng_refusesToStartWhenAnUncallableToolIsNotPublishedDirectly() {
        ToolCatalog catalog = catalog();
        String uncallable = catalog.notMcpToolScriptCallableNames().iterator().next();

        List<String> rows = new java.util.ArrayList<>();
        for (String name : catalog.names()) {
            boolean programmatic = !name.equals(uncallable) && catalog.notMcpToolScriptCallableNames().contains(name);
            rows.add(row(name, "x", programmatic ? "x" : "-"));
        }

        Exception e = assertThrows(IllegalStateException.class,
                () -> ToolManifestValidator.verify(ToolManifest.parse(rows, "test"), List.of(catalog)));
        assertTrue(e.getMessage().contains(uncallable), e.getMessage());
    }

    @Test
    void verify_ng_refusesToStartWhenAProgrammaticProfileToolIsNotOnTheDirectProfile() {
        ToolCatalog catalog = catalog();

        List<String> rows = new java.util.ArrayList<>();
        for (String name : catalog.names()) {
            boolean uncallable = catalog.notMcpToolScriptCallableNames().contains(name);
            // "get_class_info" is callable from an mcp tool script, so putting it on the programmatic profile only is what breaks the subset rule
            boolean direct = !name.equals("get_class_info");
            rows.add(row(name, direct ? "x" : "-", uncallable || !direct ? "x" : "-"));
        }

        Exception e = assertThrows(IllegalStateException.class,
                () -> ToolManifestValidator.verify(ToolManifest.parse(rows, "test"), List.of(catalog)));
        assertTrue(e.getMessage().contains("get_class_info"), e.getMessage());
    }
}
