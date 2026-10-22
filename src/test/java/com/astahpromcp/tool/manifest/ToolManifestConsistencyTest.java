package com.astahpromcp.tool.manifest;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.mcptoolscript.McpToolScriptProviderFactory;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ToolManifestConsistencyTest {

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

    private ToolCatalog catalog() {
        return ToolCatalog.build(DiagramThumbnails.INCLUDE, workspaceDir.resolve("images"), workspaceDir, projectDiffJobs);
    }

    // The names of the tools that run mcp tool scripts, which no catalog holds
    private static Set<String> mcpToolScriptToolNames(ToolCatalog catalog, ToolManifest manifest) {
        return new McpToolScriptProviderFactory().createToolProviders(catalog, manifest).names();
    }

    // A row the parser accepts: a row the programmatic profile publishes directly gets the generic reason
    private static String row(String name, String direct, String programmatic) {
        String row = ToolManifest.Category.COMMON.label() + "\t" + name + "\t" + direct + "\t" + programmatic;
        return programmatic.equals("x") ? row + "\t" + NotMcpToolScriptCallableReason.PUBLISHED_DIRECTLY.code() : row;
    }

    // The shipped manifest, without the row of the named tool
    private static List<String> shippedRowsWithout(String name) throws IOException {
        List<String> rows = new ArrayList<>();
        boolean removed = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                ToolManifest.class.getResourceAsStream(ToolManifest.RESOURCE_PATH), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\t", -1);
                if (!line.startsWith("#") && columns.length >= 2 && columns[1].trim().equals(name)) {
                    removed = true;
                    continue;
                }
                rows.add(line);
            }
        }
        assertTrue(removed, "the shipped manifest has no row for " + name);
        return rows;
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

        // 391 = rows with direct = x, 19 = the view manager tools the catalog lacks outside the GUI.
        assertEquals(391 - 19,
                catalog.select(manifest.namesFor(ToolManifest.Profile.DIRECT)).unlocked().stream()
                        .mapToLong(p -> p.createToolDefinitions().size()).sum());
        // 84 = rows with programmatic = x, 19 = the view manager tools the catalog lacks outside the GUI,
        // 5 = get_chunk_of_tools_callable_from_mcp_tool_script, get_info_of_tools_callable_from_mcp_tool_script, run_mcp_tool_script, mcp_tool_script_guide and get_mcp_tool_script_example, which McpToolScriptProviderFactory builds rather than the catalog.
        assertEquals(84 - 19 - 5,
                catalog.select(manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC)).unlocked().stream()
                        .mapToLong(p -> p.createToolDefinitions().size()).sum());
    }

    @Test
    void verify_ok_acceptsTheShippedManifestOutsideTheAstahGui() {
        ToolManifest manifest = ToolManifest.load();
        ToolCatalog catalog = catalog();

        assertDoesNotThrow(() -> ToolManifestValidator.verify(manifest, List.of(catalog), mcpToolScriptToolNames(catalog, manifest), false));
    }

    @Test
    void verify_ng_refusesToStartInsideTheAstahGuiWhenAPublishedToolDoesNotExist() {
        ToolManifest manifest = ToolManifest.load();
        ToolCatalog catalog = catalog();
        assertFalse(catalog.contains("get_current_dgm"), "the test relies on the catalog lacking the view manager tools");

        Exception e = assertThrows(IllegalStateException.class,
                () -> ToolManifestValidator.verify(manifest, List.of(catalog), mcpToolScriptToolNames(catalog, manifest), true));
        assertTrue(e.getMessage().contains("get_current_dgm"), e.getMessage());
    }

    @Test
    void verify_ng_refusesToStartInsideTheAstahGuiWhenAToolPublishedOnlyOnTheProgrammaticProfileDoesNotExist() {
        ToolCatalog catalog = catalog();
        List<String> rows = new ArrayList<>();
        for (String name : catalog.names()) {
            rows.add(row(name, "x", "s"));
        }
        rows.add(row("no_such_tool_at_all", "-", "x"));

        Exception e = assertThrows(IllegalStateException.class,
                () -> ToolManifestValidator.verify(ToolManifest.parse(rows, "test"), List.of(catalog), Set.of(), true));
        assertTrue(e.getMessage().contains("no_such_tool_at_all"), e.getMessage());
    }

    @Test
    void verify_ng_refusesToStartInsideTheAstahGuiWhenAToolOnlyAnMcpToolScriptReachesDoesNotExist() {
        ToolCatalog catalog = catalog();
        List<String> rows = new ArrayList<>();
        for (String name : catalog.names()) {
            rows.add(row(name, "x", "s"));
        }
        rows.add(row("no_such_tool_at_all", "-", "s"));

        Exception e = assertThrows(IllegalStateException.class,
                () -> ToolManifestValidator.verify(ToolManifest.parse(rows, "test"), List.of(catalog), Set.of(), true));
        assertTrue(e.getMessage().contains("no_such_tool_at_all"), e.getMessage());
    }

    @Test
    void verify_ok_acceptsInsideTheAstahGuiAManifestWhoseToolsAllExist() {
        ToolCatalog catalog = catalog();
        List<String> rows = new ArrayList<>();
        for (String name : catalog.names()) {
            rows.add(row(name, "x", "s"));
        }

        assertDoesNotThrow(() -> ToolManifestValidator.verify(ToolManifest.parse(rows, "test"), List.of(catalog), Set.of(), true));
    }

    @Test
    void verify_ng_refusesToStartWhenAToolHasNoRow() {
        ToolCatalog catalog = catalog();
        ToolManifest withoutOneTool = ToolManifest.parse(
                List.of(row("get_class_info", "x", "s")), "test");

        Exception e = assertThrows(IllegalStateException.class,
                () -> ToolManifestValidator.verify(withoutOneTool, List.of(catalog), Set.of(), false));
        assertTrue(e.getMessage().contains("no manifest row"), e.getMessage());
    }

    @Test
    void verify_ng_refusesToStartWhenAToolThatRunsMcpToolScriptsHasNoRow() throws IOException {
        ToolCatalog catalog = catalog();
        ToolManifest shipped = ToolManifest.load();
        ToolManifest withoutTheRow = ToolManifest.parse(shippedRowsWithout("run_mcp_tool_script"), "test");

        Exception e = assertThrows(IllegalStateException.class,
                () -> ToolManifestValidator.verify(withoutTheRow, List.of(catalog), mcpToolScriptToolNames(catalog, shipped), false));
        assertTrue(e.getMessage().contains("no manifest row"), e.getMessage());
        assertTrue(e.getMessage().contains("run_mcp_tool_script"), e.getMessage());
    }

    @Test
    void verify_ok_acceptsAToolPublishedOnlyOnTheProgrammaticProfile() {
        ToolCatalog catalog = catalog();

        List<String> rows = new ArrayList<>();
        for (String name : catalog.names()) {
            boolean direct = !name.equals("get_class_info");
            rows.add(row(name, direct ? "x" : "-", direct ? "s" : "x"));
        }

        assertDoesNotThrow(() -> ToolManifestValidator.verify(ToolManifest.parse(rows, "test"), List.of(catalog), Set.of(), true));
    }

    @Test
    void verify_ok_takesTheManifestsWordForEveryReason() {
        ToolCatalog catalog = catalog();
        List<String> rows = new ArrayList<>();
        for (String name : catalog.names()) {
            if (name.equals("capture_dgm_img")) {
                rows.add(ToolManifest.Category.COMMON.label() + "\t" + name + "\tx\tx\t" + NotMcpToolScriptCallableReason.PUBLISHED_DIRECTLY.code());
            } else if (name.equals("get_proj_path")) {
                rows.add(ToolManifest.Category.COMMON.label() + "\t" + name + "\tx\tx\t" + NotMcpToolScriptCallableReason.RUNS_AN_ASTAH_API_SCRIPT.code());
            } else {
                rows.add(row(name, "x", "s"));
            }
        }

        assertDoesNotThrow(() -> ToolManifestValidator.verify(ToolManifest.parse(rows, "test"), List.of(catalog), Set.of(), true));
    }
}
