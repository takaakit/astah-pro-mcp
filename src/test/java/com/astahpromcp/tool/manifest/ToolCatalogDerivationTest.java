package com.astahpromcp.tool.manifest;

import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.astah.pro.view.DiagramViewManagerTool;
import com.astahpromcp.tool.astah.pro.view.ProjectViewManagerTool;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import com.change_vision.jude.api.inf.view.IProjectViewManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ToolCatalogDerivationTest {

    @TempDir
    Path workspaceDir;

    private ToolCatalog catalog() {
        return ToolCatalog.build(DiagramThumbnails.INCLUDE, workspaceDir.resolve("images"), workspaceDir);
    }

    @Test
    void notMcpToolScriptCallableNames_ok_isTwentyEightToolsFromThreeStructuralFacts() {
        ToolCatalog catalog = catalog();

        assertEquals(28, catalog.notMcpToolScriptCallableNames().size());
        assertEquals(11, countWithReason(catalog, NotMcpToolScriptCallableReason.RETURNS_BINARY_CONTENT));
        assertEquals(16, countWithReason(catalog, NotMcpToolScriptCallableReason.PERFORMS_BLOCKING_IO));
        assertEquals(1, countWithReason(catalog, NotMcpToolScriptCallableReason.RUNS_AN_ASTAH_API_SCRIPT));
    }

    @Test
    void notMcpToolScriptCallableReason_ok_isNullForAToolAScriptMayCall() {
        assertNull(catalog().notMcpToolScriptCallableReason("get_class_info"));
    }

    @Test
    void notMcpToolScriptCallableReason_ok_isDrivesTheViewForEveryViewManagerTool() {
        ToolCatalog catalog = ToolCatalog.of(List.of(
                new DiagramViewManagerTool(null, mock(IDiagramViewManager.class), null, null),
                new ProjectViewManagerTool(null, mock(IProjectViewManager.class), null, null)), List.of());

        assertEquals(19, catalog.notMcpToolScriptCallableNames().size());
        for (String name : catalog.names()) {
            assertEquals(NotMcpToolScriptCallableReason.DRIVES_THE_VIEW, catalog.notMcpToolScriptCallableReason(name), name);
        }
    }

    @Test
    void astahLockedNames_ok_coversEveryToolThatReachesTheAstahApi() {
        ToolCatalog catalog = catalog();

        // Tools of the five factories that never touch Astah must not be in it
        assertFalse(catalog.astahLockedNames().contains("get_info_of_ocl_spec"));
        assertFalse(catalog.astahLockedNames().contains("generate_dgm_img_from_puml"));

        // Tools that do reach the API must be
        assertTrue(catalog.astahLockedNames().contains("get_class_info"));
        assertTrue(catalog.astahLockedNames().contains("create_class_in_parent_pkg"));

        assertTrue(catalog.names().containsAll(catalog.astahLockedNames()));
    }

    @Test
    void select_ok_dropsNamesTheCatalogDoesNotHold() {
        ToolCatalog catalog = catalog();

        ToolCatalog.Selection selection = catalog.select(new TreeSet<>(java.util.Set.of(
                "get_class_info", "no_such_tool_at_all")));

        assertEquals(java.util.List.of("get_class_info"),
                selection.unlocked().stream()
                        .flatMap(p -> p.createToolDefinitions().stream())
                        .map(d -> d.toolSchema().name())
                        .toList());
    }

    private static long countWithReason(ToolCatalog catalog, NotMcpToolScriptCallableReason reason) {
        return catalog.notMcpToolScriptCallableNames().stream()
                .filter(name -> catalog.notMcpToolScriptCallableReason(name) == reason)
                .count();
    }
}
