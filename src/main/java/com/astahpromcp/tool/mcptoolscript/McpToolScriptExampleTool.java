package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.mcptoolscript.inputdto.McpToolScriptExampleDTO;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class McpToolScriptExampleTool implements ToolProvider {

    static final String EXAMPLE_RESOURCE_DIR = "/mcp-tool-script-example/";

    record Example(
        String file,
        String description
    ) {
    }

    // Which examples land in which list is a response-budget matter, so no tool description may restate it: the catalogue is what says what is here.
    static final List<Example> CORE_EXAMPLES = List.of(
        new Example("list-elements-by-package.js",
            "Lists the packages and the model elements they own as a tree, by walking down from the root package."),
        new Example("list-named-elements-by-type.js",
            "Walks every chunk of the named elements in the project, printing a census of them by type and then the name of each element of one chosen type."),
        new Example("list-presentation-and-model-ids.js",
            "Lists the presentations on a class diagram together with the model elements behind them, by crossing between the presentation IDs and the model element IDs."),
        new Example("export-all-diagrams-as-png.js",
            "Exports every diagram in the project as a PNG image file."),
        new Example("create-two-classes.js",
            "Creates two classes with definitions, attributes, operations and parameters, then places them on a class diagram."),
        new Example("create-association.js",
            "Creates an association between two existing classes, sets the association name, the role names and the multiplicities, then draws it on a class diagram."));

    static final List<Example> ON_DEMAND_EXAMPLES = List.of(
        new Example("create-class-diagram.js",
            "Draws a class diagram with classes, an interface and an enumeration, each with attributes, operations, parameters and return types, joined by association, generalization, realization and dependency lines."),
        new Example("create-composite-structure-diagram.js",
            "Draws a composite structure diagram with a structured class, parts typed by classes, ports on a part and on the structured class boundary, provided and required interfaces, and connectors between the parts."),
        new Example("create-usecase-diagram.js",
            "Draws a use case diagram with actors, use cases, a system boundary carrying the system name, and association, generalization, extend and include lines."),
        new Example("create-sequence-diagram.js",
            "Draws a sequence diagram with lifelines typed by a base class, synchronous messages, a create message and a combined fragment."),
        new Example("create-state-machine-diagram.js",
            "Draws a state machine diagram with an initial pseudostate, a final state, states, a composite state holding two regions, the substates inside those regions, and the transitions between them."),
        new Example("create-activity-diagram.js",
            "Draws an activity diagram with partitions that each represent a class, an initial and a final node, actions, an object node, decision, merge, fork and join nodes, input and output pins, and the control and object flows between them."),
        new Example("create-requirement-diagram.js",
            "Draws a requirement diagram with requirements and a test case, joined by nesting, deriveReqt, copy, satisfy, verify, refine and trace lines."),
        new Example("create-mind-map.js",
            "Draws a mind map with topics nested under the root topic, a multi-line topic label, a reordered sibling, a boundary around one branch, an icon inside a topic, a floating topic and a link between two topics."),
        new Example("trace-references-as-dot.js",
            "Traces four kinds of classifier reference in both directions, fairly limits each breadth-first layer, disambiguates classifiers by ID and namespace, and prints a bounded Graphviz DOT network of the references around one classifier. Use this to get a single-picture overview of reference relationships around a specific classifier."),
        new Example("find-circular-references.js",
            "Finds the classifiers that reference one another in a circle, along association, inheritance, realization and dependency lines, and prints the shortest circle through each of them."));

    static final List<Example> EXAMPLES = Stream.concat(
        CORE_EXAMPLES.stream(),
        ON_DEMAND_EXAMPLES.stream()).toList();

    static final List<String> EXAMPLE_FILES = EXAMPLES.stream().map(Example::file).toList();

    // The names McpToolScriptExecutor locks on the global object
    static final List<String> RESERVED_NAMES = List.of(
        "engine", "context",
        "Java", "JavaImporter", "Packages", "JSAdapter",
        "com", "edu", "java", "javafx", "javax", "org",
        "exit", "quit", "load", "loadWithNewGlobal", "readFully",
        "__noSuchProperty__");

    static final String ENVIRONMENT_NOTE = """
The script environment, where it differs from plain JavaScript.

Your script runs in strict mode. Assigning to a variable you never declared stops the run with a ReferenceError, so declare every variable with 'var'.

These names are already defined on the global object and locked, so that a script cannot reach out of its sandbox through them:

  %s

Assigning to one of them stops the run with a TypeError on the line that did it, however ordinary the name looks: 'var engine = tools.get_proj({});' fails there and then. Give the variable another name -- engineId, ctx, javaType -- and none of this arises.
            """
            .formatted(String.join(", ", RESERVED_NAMES));

    public McpToolScriptExampleTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningContents(
                    "mcp_tool_script_guide",
                    "MCP client (you) MUST call this tool function before using the 'run_mcp_tool_script' tool function. It returns worked example mcp tool scripts to write from, and a catalogue naming every example this server ships and what each one demonstrates. When you need one the catalogue names but that did not come with it, call 'get_mcp_tool_script_example'. This guide covers the mcp tool scripts that 'run_mcp_tool_script' runs; for the raw Astah API that 'run_astah_api_script' uses, call 'astah_api_script_guide' instead.",
                    this::getGuide,
                    NoInputDTO.class),

                ToolSupport.toolDefinitionReturningContents(
                    "get_mcp_tool_script_example",
                    "Return one worked example mcp tool script by name, as the catalogue spells it. Read the example closest to what you are about to write before writing it. Only the example you name comes back. Call 'mcp_tool_script_guide' first for the catalogue of every example this server ships; a name no example has comes back as an error that lists them all.",
                    this::getExample,
                    McpToolScriptExampleDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create mcp tool script example tools", e);
            return List.of();
        }
    }

    private List<McpSchema.Content> getGuide(NoInputDTO param) throws Exception {
        log.debug("Get mcp tool script guide: {}", param);

        List<McpSchema.Content> contents = new ArrayList<>();
        contents.add(catalogue(CORE_EXAMPLES));
        contents.add(McpSchema.TextContent.builder(ENVIRONMENT_NOTE).build());
        for (Example example : CORE_EXAMPLES) {
            contents.add(exampleBlock(example));
        }

        return contents;
    }

    private List<McpSchema.Content> getExample(McpToolScriptExampleDTO param) throws Exception {
        log.debug("Get mcp tool script example: {}", param);

        Example example = EXAMPLES.stream()
                .filter(candidate -> candidate.file().equals(param.exampleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no example named '" + param.exampleName()
                        + "'. The examples are: " + String.join(", ", EXAMPLE_FILES) + "."));

        return List.of(exampleHeader(example), exampleBlock(example));
    }

    private static McpSchema.TextContent catalogue(List<Example> included) {
        StringBuilder text = new StringBuilder();
        text.append("The worked example mcp tool scripts this server ships, simplest first.").append("\n\n").append("Included in this response:").append("\n");
        for (Example example : EXAMPLES) {
            if (included.contains(example)) {
                appendEntry(text, example);
            }
        }

        List<Example> rest = EXAMPLES.stream().filter(example -> !included.contains(example)).toList();
        if (!rest.isEmpty()) {
            text.append("\n").append("Read any of the rest with the 'get_mcp_tool_script_example' tool function:").append("\n");
            for (Example example : rest) {
                appendEntry(text, example);
            }
        }

        return McpSchema.TextContent.builder(text.toString()).build();
    }

    private static McpSchema.TextContent exampleHeader(Example example) {
        StringBuilder text = new StringBuilder();
        text.append("The worked example mcp tool script you asked for. 'mcp_tool_script_guide' catalogues every example this server ships.").append("\n\n");
        appendEntry(text, example);

        return McpSchema.TextContent.builder(text.toString()).build();
    }

    private static void appendEntry(StringBuilder text, Example example) {
        text.append("  ").append(example.file()).append("\n      ").append(example.description()).append("\n");
    }

    private static McpSchema.TextContent exampleBlock(Example example) throws Exception {
        return McpSchema.TextContent.builder("// " + example.file() + "\n\n" + loadExample(example.file())).build();
    }

    static String loadExample(String file) throws Exception {
        String resourcePath = EXAMPLE_RESOURCE_DIR + file;

        try (InputStream stream = McpToolScriptExampleTool.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new Exception("Resource not found on classpath: " + resourcePath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
