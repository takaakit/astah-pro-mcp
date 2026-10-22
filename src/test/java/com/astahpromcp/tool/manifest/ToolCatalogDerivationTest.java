package com.astahpromcp.tool.manifest;

import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.projectdiff.ProjectDiffJobs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ToolCatalogDerivationTest {

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

    @Test
    void astahLockedNames_ok_coversEveryToolThatReachesTheAstahApi() {
        ToolCatalog catalog = catalog();

        // Tools of the six factories that never touch Astah must not be in it
        assertFalse(catalog.astahLockedNames().contains("get_info_of_ocl_spec"));
        assertFalse(catalog.astahLockedNames().contains("generate_dgm_img_from_puml"));
        assertFalse(catalog.astahLockedNames().contains("get_proj_diff_result"));
        assertFalse(catalog.astahLockedNames().contains("proj_diff_guide"));

        // Tools that do reach the API must be
        assertTrue(catalog.astahLockedNames().contains("get_class_info"));
        assertTrue(catalog.astahLockedNames().contains("create_class_in_parent_pkg"));
        assertTrue(catalog.astahLockedNames().contains("diff_proj"));

        assertTrue(catalog.names().containsAll(catalog.astahLockedNames()));
    }

    @Test
    void select_ok_dropsNamesTheCatalogDoesNotHold() {
        ToolCatalog catalog = catalog();

        ToolCatalog.Selection selection = catalog.select(new TreeSet<>(Set.of(
                "get_class_info", "no_such_tool_at_all")));

        assertEquals(List.of("get_class_info"),
                selection.unlocked().stream()
                        .flatMap(p -> p.createToolDefinitions().stream())
                        .map(d -> d.toolSchema().name())
                        .toList());
    }
}
