package com.astahpromcp.tool;

import com.astahpromcp.tool.astah.pro.AstahProToolFactory;
import com.astahpromcp.tool.config.ConfigToolFactory;
import com.astahpromcp.tool.info.InfoToolFactory;
import com.astahpromcp.tool.knowledge.KnowledgeToolFactory;
import com.astahpromcp.tool.log.LogToolFactory;
import com.astahpromcp.tool.visualization.VisualizationToolFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ToolNameSnapshotTest {

    private static final Path SNAPSHOT_DIR = Path.of("src", "test", "resources", "toolnames");

    @TempDir
    Path workspaceDir;

    // Collect the tool names exactly as McpServerApp.registerToolProviders() assembles them
    private List<String> collectToolNames(boolean includeEditorTools) {
        ToolCategoryFlags categoryFlags = new ToolCategoryFlags(
            true, true, true, true, true, true, true, true, true, true);

        List<ToolProvider> providers = new ArrayList<>();
        providers.addAll(new AstahProToolFactory().createToolProviders(categoryFlags, includeEditorTools));
        providers.addAll(new LogToolFactory().createToolProviders(categoryFlags));
        providers.addAll(new KnowledgeToolFactory(workspaceDir).createToolProviders(categoryFlags));
        providers.addAll(new VisualizationToolFactory().createToolProviders(categoryFlags));
        providers.addAll(new ConfigToolFactory().createToolProviders(categoryFlags));
        providers.addAll(new InfoToolFactory().createToolProviders(categoryFlags));

        List<String> names = new ArrayList<>();
        for (ToolProvider provider : providers) {
            for (ToolDefinition definition : provider.createToolDefinitions()) {
                names.add(definition.toolSchema().name());
            }
        }
        return names;
    }

    private void assertToolNamesMatchSnapshot(String profileName, boolean includeEditorTools) throws IOException {
        List<String> actualNames = collectToolNames(profileName, includeEditorTools);
        Set<String> actual = new TreeSet<>(actualNames);

        Path snapshotFile = SNAPSHOT_DIR.resolve(profileName + ".txt");

        // Regenerate the snapshot instead of comparing when explicitly requested
        if (Boolean.getBoolean("updateToolNameSnapshots")) {
            Files.createDirectories(SNAPSHOT_DIR);
            Files.write(snapshotFile, actual, StandardCharsets.UTF_8);
            System.out.println("Updated tool name snapshot: " + snapshotFile + " (" + actual.size() + " tools)");
            return;
        }

        assertTrue(Files.exists(snapshotFile), "Snapshot file not found: " + snapshotFile + ". Generate it with -DupdateToolNameSnapshots=true and commit it.");

        Set<String> expected = new TreeSet<>(Files.readAllLines(snapshotFile, StandardCharsets.UTF_8));
        expected.remove("");

        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(actual);
        Set<String> unexpected = new TreeSet<>(actual);
        unexpected.removeAll(expected);

        assertTrue(missing.isEmpty() && unexpected.isEmpty(), "Tool names of profile '" + profileName + "' do not match the snapshot.");
    }

    // Collect and also verify that no tool name is registered twice within one profile
    private List<String> collectToolNames(String profileName, boolean includeEditorTools) {
        List<String> names = collectToolNames(includeEditorTools);

        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new TreeSet<>();
        for (String name : names) {
            if (!seen.add(name)) {
                duplicates.add(name);
            }
        }
        assertTrue(duplicates.isEmpty(), "Duplicate tool names registered in profile '" + profileName + "': " + duplicates);

        return names;
    }

    @Test
    void assertToolNamesMatchSnapshot_ok_fullProfile() throws IOException {
        assertToolNamesMatchSnapshot("full", true);
    }

    @Test
    void assertToolNamesMatchSnapshot_ok_queryOnlyProfile() throws IOException {
        assertToolNamesMatchSnapshot("query_only", false);
    }
}
