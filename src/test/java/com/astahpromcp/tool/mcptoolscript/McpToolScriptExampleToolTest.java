package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.script.Compilable;
import javax.script.ScriptEngine;

import static org.junit.jupiter.api.Assertions.*;

public class McpToolScriptExampleToolTest {

    @TempDir
    Path workspaceDir;

    // How an example calls a tool function
    private static final Pattern TOOL_CALL = Pattern.compile("tools\\.([a-zA-Z0-9_]+)\\s*\\(");

    private static ToolDefinition definition(String toolName) {
        return new McpToolScriptExampleTool().createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("this provider registers no tool function named " + toolName));
    }

    private static McpSchema.CallToolResult call(String toolName, Map<String, Object> arguments) {
        return definition(toolName).toolHandler()
                .apply(null, new McpSchema.CallToolRequest(toolName, arguments, null));
    }

    private static List<String> textBlocks(McpSchema.CallToolResult result) {
        assertFalse(Boolean.TRUE.equals(result.isError()), String.valueOf(result.content()));

        List<String> blocks = new ArrayList<>();
        for (McpSchema.Content content : result.content()) {
            assertInstanceOf(McpSchema.TextContent.class, content, "the tool returns text blocks only");
            blocks.add(((McpSchema.TextContent) content).text());
        }

        return blocks;
    }

    private static List<String> guideBlocks() {
        return textBlocks(call("mcp_tool_script_guide", Map.of()));
    }

    private static List<String> exampleBlocks(String exampleName) {
        return textBlocks(call("get_mcp_tool_script_example", namedArgument(exampleName)));
    }

    private static Map<String, Object> namedArgument(String exampleName) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("exampleName", exampleName);
        return arguments;
    }

    private static String errorText(McpSchema.CallToolResult result) {
        assertTrue(Boolean.TRUE.equals(result.isError()), "the call was expected to fail: " + result.content());
        return ((McpSchema.TextContent) result.content().get(0)).text();
    }

    private AstahToolRegistry registry() {
        ToolManifest manifest = ToolManifest.load();
        ToolCatalog catalog = ToolCatalog.build(DiagramThumbnails.OMIT, workspaceDir.resolve("images"), workspaceDir);

        return AstahToolRegistry.from(
                catalog,
                manifest.namesFor(ToolManifest.Profile.DIRECT),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC));
    }

    private static Set<String> toolNamesCalledBy(String source) {
        Set<String> names = new TreeSet<>();

        Matcher matcher = TOOL_CALL.matcher(source);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }

        return names;
    }

    @Test
    void examples_ok_areAllPresentOnTheClasspath() throws Exception {
        for (String file : McpToolScriptExampleTool.EXAMPLE_FILES) {
            assertFalse(McpToolScriptExampleTool.loadExample(file).isBlank(), file + " is empty");
        }
    }

    @Test
    void examples_ok_leaveNoShippedFileUnlisted() throws Exception {
        URL directory = getClass().getResource(McpToolScriptExampleTool.EXAMPLE_RESOURCE_DIR);
        assertNotNull(directory, "the example directory is not on the classpath");

        Set<String> shipped = new TreeSet<>();
        try (Stream<Path> files = Files.list(Path.of(directory.toURI()))) {
            files.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".js"))
                    .forEach(shipped::add);
        }

        assertEquals(shipped, new TreeSet<>(McpToolScriptExampleTool.EXAMPLE_FILES));
    }

    @Test
    void examples_ok_splitIntoCoreAndOnDemandWithNothingLostOrDoubled() {
        List<McpToolScriptExampleTool.Example> joined = Stream.concat(
                McpToolScriptExampleTool.CORE_EXAMPLES.stream(),
                McpToolScriptExampleTool.ON_DEMAND_EXAMPLES.stream()).toList();

        assertEquals(McpToolScriptExampleTool.EXAMPLES, joined,
                "the two lists, in order, are exactly the shipped examples");
        assertEquals(joined.size(), new TreeSet<>(McpToolScriptExampleTool.EXAMPLE_FILES).size(),
                "no example belongs to both lists");
        assertFalse(McpToolScriptExampleTool.CORE_EXAMPLES.isEmpty());
        assertFalse(McpToolScriptExampleTool.ON_DEMAND_EXAMPLES.isEmpty());
    }

    @Test
    void examples_ok_compileAsEcmaScript51() throws Exception {
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine(className -> false);

        for (String file : McpToolScriptExampleTool.EXAMPLE_FILES) {
            String source = McpToolScriptExampleTool.loadExample(file);

            assertDoesNotThrow(() -> ((Compilable) engine).compile(source), file + " does not compile");
        }
    }

    @Test
    void examples_ok_callOnlyToolFunctionsAScriptCanCall() throws Exception {
        AstahToolRegistry registry = registry();

        Set<String> unknown = new TreeSet<>();
        Set<String> refused = new TreeSet<>();

        for (String file : McpToolScriptExampleTool.EXAMPLE_FILES) {
            for (String name : toolNamesCalledBy(McpToolScriptExampleTool.loadExample(file))) {
                if (registry.find(name) == null) {
                    unknown.add(name + " (" + file + ")");
                } else if (registry.notMcpToolScriptCallableReason(name) != null) {
                    refused.add(name + " (" + file + ")");
                }
            }
        }

        assertEquals(Set.of(), unknown, "the examples call tool functions this server does not have");
        assertEquals(Set.of(), refused, "the examples call tool functions a script is refused, which this server publishes directly instead");
    }

    @Test
    void examples_ok_haveADescription() {
        for (McpToolScriptExampleTool.Example example : McpToolScriptExampleTool.EXAMPLES) {
            assertFalse(example.description().isBlank(), example.file() + " has no description");
        }
    }

    @Test
    void guide_ok_returnsTheCatalogueThenTheEnvironmentNoteThenEveryCoreExampleInOrder() throws Exception {
        List<String> blocks = guideBlocks();

        assertEquals(2 + McpToolScriptExampleTool.CORE_EXAMPLES.size(), blocks.size(),
                "the catalogue, the environment note, then one block per core example, nothing else");

        for (int i = 0; i < McpToolScriptExampleTool.CORE_EXAMPLES.size(); i++) {
            String file = McpToolScriptExampleTool.CORE_EXAMPLES.get(i).file();
            String source = McpToolScriptExampleTool.loadExample(file);

            assertEquals("// " + file + "\n\n" + source, blocks.get(i + 2));
        }
    }

    @Test
    void guide_ok_catalogueListsEveryExample() {
        String catalogue = guideBlocks().get(0);

        for (McpToolScriptExampleTool.Example example : McpToolScriptExampleTool.EXAMPLES) {
            assertTrue(catalogue.contains(example.file()), "the catalogue does not name " + example.file());
            assertTrue(catalogue.contains(example.description()),
                    "the catalogue does not describe " + example.file());
        }
    }

    @Test
    void example_ok_returnsTheHeaderAndTheNamedExample() throws Exception {
        for (String file : McpToolScriptExampleTool.EXAMPLE_FILES) {
            List<String> blocks = exampleBlocks(file);

            assertEquals(2, blocks.size(), "the header and the one example, nothing else");
            assertEquals("// " + file + "\n\n" + McpToolScriptExampleTool.loadExample(file), blocks.get(1));
        }
    }

    @Test
    void example_ok_headerNamesOnlyTheExampleThatCameAlong() {
        for (McpToolScriptExampleTool.Example example : McpToolScriptExampleTool.EXAMPLES) {
            String header = exampleBlocks(example.file()).get(0);

            assertTrue(header.contains(example.file()), "the header does not name " + example.file() + ": " + header);
            assertTrue(header.contains(example.description()),
                    "the header does not describe " + example.file() + ": " + header);

            for (String other : McpToolScriptExampleTool.EXAMPLE_FILES) {
                if (!other.equals(example.file())) {
                    assertFalse(header.contains(other),
                            other + " did not come along, so the header must not name it: " + header);
                }
            }
        }
    }

    @Test
    void example_ng_refusesANameNoExampleHasAndSaysWhichNamesExist() {
        String message = errorText(call("get_mcp_tool_script_example", namedArgument("create-sequence-diagram")));

        assertTrue(message.contains("create-sequence-diagram"), "the message should quote what was asked for: " + message);
        for (String file : McpToolScriptExampleTool.EXAMPLE_FILES) {
            assertTrue(message.contains(file), "the message should name " + file + " so the client can correct itself: " + message);
        }
    }

    @Test
    void example_ng_refusesANullName() {
        String message = errorText(call("get_mcp_tool_script_example", namedArgument(null)));

        for (String file : McpToolScriptExampleTool.EXAMPLE_FILES) {
            assertTrue(message.contains(file), "the message should name " + file + ": " + message);
        }
    }

    @Test
    void example_ng_refusesAnOmittedName() {
        String message = errorText(call("get_mcp_tool_script_example", Map.of()));

        assertTrue(message.contains("exampleName"), "the message should name the missing property: " + message);
    }

    @Test
    void descriptions_ok_nameNoExampleThisServerDoesNotShip() {
        Pattern exampleName = Pattern.compile("[A-Za-z0-9-]+\\.js");

        for (ToolDefinition definition : new McpToolScriptExampleTool().createToolDefinitions()) {
            McpSchema.Tool schema = definition.toolSchema();
            String published = schema.description() + " "
                    + JsonSupport.OBJ_MAPPER.writeValueAsString(schema.inputSchema());

            Matcher matcher = exampleName.matcher(published);
            while (matcher.find()) {
                assertTrue(McpToolScriptExampleTool.EXAMPLE_FILES.contains(matcher.group()),
                        schema.name() + " names '" + matcher.group() + "', which this server does not ship");
            }
        }
    }

    @Test
    void toolDefinitions_ok_areTheOnesTheManifestPublishes() {
        List<ToolDefinition> definitions = new McpToolScriptExampleTool().createToolDefinitions();

        assertEquals(2, definitions.size());
        assertEquals("mcp_tool_script_guide", definitions.get(0).toolSchema().name());
        assertEquals("get_mcp_tool_script_example", definitions.get(1).toolSchema().name());
        for (ToolDefinition definition : definitions) {
            assertEquals(ToolDefinition.ResultKind.CONTENTS, definition.resultKind());
        }
    }
}
