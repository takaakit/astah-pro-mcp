package com.astahpromcp.tool.knowledge;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeStoreToolTest {

    private static final int CONCURRENT_TRIALS = 200;

    // A store tool function and the provider that registers it
    private record StoreTool(
        String name,
        Function<Path,
        ToolProvider> providerFactory
    ) {
    }

    private static final List<StoreTool> STORE_TOOLS = List.of(
            new StoreTool("store_architectural_design_smells", ArchitecturalDesignSmellsTool::new),
            new StoreTool("store_uml_modeling_architecture_insights", UMLModelingArchitectureInsightsTool::new));

    private static ToolDefinition definitionOf(StoreTool tool, Path workspaceDir) {
        return tool.providerFactory().apply(workspaceDir).createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals(tool.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("this provider registers no tool function named " + tool.name()));
    }

    private static McpSchema.CallToolResult store(ToolDefinition definition, String contents) {
        String toolName = definition.toolSchema().name();
        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest(toolName, Map.of("contents", contents), null));
    }

    private static boolean isError(McpSchema.CallToolResult result) {
        return Boolean.TRUE.equals(result.isError());
    }

    private static List<Path> filesIn(Path directory) throws Exception {
        try (Stream<Path> entries = Files.list(directory)) {
            return entries.toList();
        }
    }

    @Test
    void store_ng_rejectsBlankContents(@TempDir Path rootDir) throws Exception {
        for (StoreTool tool : STORE_TOOLS) {
            Path workspaceDir = Files.createDirectory(rootDir.resolve(tool.name()));
            ToolDefinition definition = definitionOf(tool, workspaceDir);

            for (String blank : List.of("", "   ", "\n\t")) {
                McpSchema.CallToolResult result = store(definition, blank);
                assertTrue(isError(result), tool.name() + " must reject blank contents: " + result.content());
            }
            assertEquals(List.of(), filesIn(workspaceDir),
                    tool.name() + " must not create the file for blank contents");

            McpSchema.CallToolResult result = store(definition, "stored text");
            assertFalse(isError(result), tool.name() + " should store contents after blank ones were rejected: " + result.content());
            assertEquals("stored text", Files.readString(filesIn(workspaceDir).get(0)));
        }
    }

    @Test
    void store_ok_onlyOneOfTwoConcurrentStoresSucceeds(@TempDir Path rootDir) throws Exception {
        // Of different lengths, so that two writes mixed into one file would show
        String first = "A".repeat(20_000);
        String second = "B".repeat(10_000);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (StoreTool tool : STORE_TOOLS) {
                for (int trial = 0; trial < CONCURRENT_TRIALS; trial++) {
                    Path workspaceDir = Files.createDirectory(rootDir.resolve(tool.name() + "-" + trial));
                    ToolDefinition definition = definitionOf(tool, workspaceDir);

                    CyclicBarrier start = new CyclicBarrier(2);
                    Future<McpSchema.CallToolResult> firstResult = pool.submit(() -> {
                        start.await();
                        return store(definition, first);
                    });
                    Future<McpSchema.CallToolResult> secondResult = pool.submit(() -> {
                        start.await();
                        return store(definition, second);
                    });

                    boolean firstStored = !isError(firstResult.get());
                    boolean secondStored = !isError(secondResult.get());
                    assertTrue(firstStored ^ secondStored,
                            tool.name() + " must store exactly one of two concurrent calls (trial " + trial + "): first="
                                    + firstStored + ", second=" + secondStored);
                    assertEquals(firstStored ? first : second, Files.readString(filesIn(workspaceDir).get(0)),
                            tool.name() + " must keep the contents of the call that succeeded (trial " + trial + ")");
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
