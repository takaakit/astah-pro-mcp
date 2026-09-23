package com.astahpromcp.tool.astah.pro.astahapiscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.astahapiscript.inputdto.AstahApiScriptExampleDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class AstahApiScriptExampleTool implements ToolProvider {

    static final String EXAMPLE_RESOURCE_DIR = "/astah-api-script-example/";

    record Example(
        String file,
        String description
    ) {
    }

    static final List<Example> CORE_EXAMPLES = List.of(
        new Example("move-transition-label.js",
            "Moves the label of a transition link, which shows its trigger, guard and action together, by an offset."),
        new Example("move-association-label.js",
            "Moves the labels of an association link -- its name, the role names, the multiplicities, the constraints and the stereotypes -- each by an offset of its own."));

    static final List<Example> ON_DEMAND_EXAMPLES = List.of(
        new Example("move-message-label.js",
            "Moves the label of a message on a sequence diagram sideways by an offset."),
        new Example("move-flow-label.js",
            "Moves the label of a control flow or an object flow, which shows its guard and action together, by an offset."));

    static final List<Example> EXAMPLES = Stream.concat(
        CORE_EXAMPLES.stream(),
        ON_DEMAND_EXAMPLES.stream()).toList();

    static final List<String> EXAMPLE_FILES = EXAMPLES.stream().map(Example::file).toList();

    static final String ENVIRONMENT_NOTE = """
The script environment, where it differs from plain JavaScript.

Your script runs on Nashorn, which implements ECMAScript 5.1. Declare variables with 'var', and write functions with the 'function' keyword: arrow functions, classes and template literals are not available.

The global variable 'astah', and its alias 'projectAccessor', is the ProjectAccessor of the running Astah. Load any other Java class with Java.type() or new JavaImporter(); importPackage is unavailable. Print output with print(); console.log is unavailable.

Wrap every change to the model in a transaction, and abort it when a change fails:

``` javascript
var transactionManager = astah.getTransactionManager();
transactionManager.beginTransaction();
try {
  // edit the model here
  transactionManager.endTransaction();
} catch (e) {
  transactionManager.abortTransaction();
  throw e;
}
```

A transaction the script leaves open is aborted when the run ends, which discards every change made inside it.

A run that has not finished after %d seconds is reported as timed out, and Astah stays blocked until the script stops. Keep scripts short and never run one that blocks or loops indefinitely.

NEVER save the Astah project from a script unless the user explicitly instructs you to do so.

For the classes and methods of the Astah API, refer to the JavaDoc:
  https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/index-all.html

For more sample scripts, refer to:
  https://astahblog.com/sample-scripts/
            """
            .formatted(McpServerConfig.ASTAH_API_SCRIPT_TIMEOUT_SECONDS);

    public AstahApiScriptExampleTool() {
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningContents(
                    "astah_api_script_guide",
                    "MCP client (you) MUST call this tool function before using the 'run_astah_api_script' tool function. It returns worked example astah api scripts to write from, and a catalogue naming every example this server ships and what each one demonstrates. When you need one the catalogue names but that did not come with it, call 'get_astah_api_script_example'. This guide covers the raw Astah API that 'run_astah_api_script' uses; for the mcp tool scripts that 'run_mcp_tool_script' runs, call 'mcp_tool_script_guide' instead.",
                    this::getGuide,
                    NoInputDTO.class),

                ToolSupport.toolDefinitionReturningContents(
                    "get_astah_api_script_example",
                    "Return one worked example astah api script by name, as the catalogue spells it. Read the example closest to what you are about to write before writing it. Only the example you name comes back. Call 'astah_api_script_guide' first for the catalogue of every example this server ships; a name no example has comes back as an error that lists them all.",
                    this::getExample,
                    AstahApiScriptExampleDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create astah api script example tools", e);
            return List.of();
        }
    }

    private List<McpSchema.Content> getGuide(NoInputDTO param) throws Exception {
        log.debug("Get astah api script guide: {}", param);

        List<McpSchema.Content> contents = new ArrayList<>();
        contents.add(catalogue(CORE_EXAMPLES));
        contents.add(McpSchema.TextContent.builder(ENVIRONMENT_NOTE).build());
        for (Example example : CORE_EXAMPLES) {
            contents.add(exampleBlock(example));
        }

        return contents;
    }

    private List<McpSchema.Content> getExample(AstahApiScriptExampleDTO param) throws Exception {
        log.debug("Get astah api script example: {}", param);

        Example example = EXAMPLES.stream()
                .filter(candidate -> candidate.file().equals(param.exampleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no example named '" + param.exampleName()
                        + "'. The examples are: " + String.join(", ", EXAMPLE_FILES) + "."));

        return List.of(exampleHeader(example), exampleBlock(example));
    }

    private static McpSchema.TextContent catalogue(List<Example> included) {
        StringBuilder text = new StringBuilder();
        text.append("The worked example astah api scripts this server ships, simplest first.").append("\n\n").append("Included in this response:").append("\n");
        for (Example example : EXAMPLES) {
            if (included.contains(example)) {
                appendEntry(text, example);
            }
        }

        List<Example> rest = EXAMPLES.stream().filter(example -> !included.contains(example)).toList();
        if (!rest.isEmpty()) {
            text.append("\n").append("Read any of the rest with the 'get_astah_api_script_example' tool function:").append("\n");
            for (Example example : rest) {
                appendEntry(text, example);
            }
        }

        return McpSchema.TextContent.builder(text.toString()).build();
    }

    private static McpSchema.TextContent exampleHeader(Example example) {
        StringBuilder text = new StringBuilder();
        text.append("The worked example astah api script you asked for. 'astah_api_script_guide' catalogues every example this server ships.").append("\n\n");
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

        try (InputStream stream = AstahApiScriptExampleTool.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new Exception("Resource not found on classpath: " + resourcePath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
