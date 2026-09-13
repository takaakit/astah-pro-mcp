package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ResponseSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.astah.pro.image.DiagramThumbnails;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import com.astahpromcp.tool.manifest.ToolCatalog;
import com.astahpromcp.tool.manifest.ToolManifest;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolInfoDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolInfoListDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolListDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolSummaryDTO;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CallableToolInfoToolTest {

    private static final String LIST_TOOL = "get_all_tools_callable_from_mcp_tool_script";
    private static final String INFO_TOOL = "get_info_of_tools_callable_from_mcp_tool_script";

    @TempDir
    Path workspaceDir;

    private static ToolDefinition definition(String name, String description) {
        McpSchema.Tool schema = McpSchema.Tool.builder(name, JsonSupport.MCP_JSON_MAPPER,
                        "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\"}}}")
                .description(description)
                .outputSchema(JsonSupport.MCP_JSON_MAPPER,
                        "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}")
                .build();

        return new ToolDefinition(schema, ToolDefinition.ResultKind.DTO, (exchange, request) -> ResponseSupport.success(Map.of("ok", true)));
    }

    // A tool function that answers with a DTO and contents together declares no result schema, and is callable all the same.
    private static ToolDefinition definitionWithoutOutputSchema(String name, String description) {
        McpSchema.Tool schema = McpSchema.Tool.builder(name, JsonSupport.MCP_JSON_MAPPER,
                        "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\"}}}")
                .description(description)
                .build();

        return new ToolDefinition(schema, ToolDefinition.ResultKind.DTO_AND_CONTENTS,
                (exchange, request) -> ResponseSupport.success(Map.of("ok", true), List.of()));
    }

    private static ToolDefinition definitionOf(String name, String description, int descriptionChars) {
        return definition(name, description.repeat(descriptionChars));
    }

    private static McpSchema.CallToolResult call(AstahToolRegistry registry, String toolName, Map<String, Object> arguments) {
        ToolDefinition definition = new CallableToolInfoTool(registry).createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No tool function is named " + toolName));

        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest(toolName, arguments, null));
    }

    private static CallableToolListDTO list(AstahToolRegistry registry) {
        McpSchema.CallToolResult result = call(registry, LIST_TOOL, Map.of());

        assertFalse(Boolean.TRUE.equals(result.isError()), String.valueOf(result.content()));

        return JsonSupport.OBJ_MAPPER.convertValue(result.structuredContent(), CallableToolListDTO.class);
    }

    private static CallableToolInfoListDTO info(AstahToolRegistry registry, List<String> toolNames) {
        McpSchema.CallToolResult result = infoResult(registry, toolNames);

        assertFalse(Boolean.TRUE.equals(result.isError()), String.valueOf(result.content()));

        return JsonSupport.OBJ_MAPPER.convertValue(result.structuredContent(), CallableToolInfoListDTO.class);
    }

    private static McpSchema.CallToolResult infoResult(AstahToolRegistry registry, List<String> toolNames) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("toolNames", toolNames);

        return call(registry, INFO_TOOL, arguments);
    }

    // The text of an error result, so that a test can check the caller is told what went wrong.
    private static String infoExpectingError(AstahToolRegistry registry, List<String> toolNames) {
        McpSchema.CallToolResult result = infoResult(registry, toolNames);

        assertTrue(Boolean.TRUE.equals(result.isError()),
                "The call was expected to fail but succeeded: " + result.structuredContent());

        return String.valueOf(result.content());
    }

    private static List<String> namesOf(CallableToolListDTO result) {
        return result.tools().stream().map(CallableToolSummaryDTO::name).toList();
    }

    private static AstahToolRegistry sampleRegistry() {
        ToolProvider provider = () -> List.of(
                definition("get_class_info", "Return the information of the specified class."),
                definition("get_attr_info", "Return the information of the specified attribute of a class."),
                definition("create_class", "Create a class in the project."),
                definitionWithoutOutputSchema("create_note", "Create a note on the specified diagram."),
                definition("capture_dgm_img", "Capture a PNG image of the specified diagram."),
                definition("get_info_of_ocl_spec", "Return the OCL specification document."));

        // The reasons the catalog derives for these two, stated here because this registry has no catalog behind it
        return AstahToolRegistry.of(provider.createToolDefinitions(), Map.of(
                "capture_dgm_img", NotMcpToolScriptCallableReason.RETURNS_BINARY_CONTENT,
                "get_info_of_ocl_spec", NotMcpToolScriptCallableReason.PERFORMS_BLOCKING_IO));
    }

    @Test
    void getAllToolsCallable_ok_returnsOnlyTheToolFunctionsAScriptCanCall() {
        CallableToolListDTO result = list(sampleRegistry());

        assertEquals(List.of("get_class_info", "get_attr_info", "create_class", "create_note"), namesOf(result));
        assertEquals(4, result.total());
        assertEquals(result.tools().size(), result.total(),
                "The list is never partial, so the total is what came back");
    }

    @Test
    void getAllToolsCallable_ok_leavesOutTheToolFunctionsPublishedDirectly() {
        List<String> names = namesOf(list(sampleRegistry()));

        assertFalse(names.contains("capture_dgm_img"), "it returns binary content");
        assertFalse(names.contains("get_info_of_ocl_spec"), "it performs blocking IO");
    }

    @Test
    void getAllToolsCallable_ok_keepsRegistrationOrder() {
        AstahToolRegistry registry = sampleRegistry();

        assertEquals(List.copyOf(registry.mcpToolScriptCallableNames()), namesOf(list(registry)),
                "The registry is ordered by the manifest, and the list must not reorder it");
    }

    @Test
    void getAllToolsCallable_ok_carriesTheDescriptionOfEveryToolFunction() {
        CallableToolSummaryDTO entry = list(sampleRegistry()).tools().get(0);

        assertEquals("get_class_info", entry.name());
        assertEquals("Return the information of the specified class.", entry.description());
    }

    @Test
    void getAllToolsCallable_ok_takesNoArgumentsAtAll() {
        McpSchema.Tool schema = new CallableToolInfoTool(sampleRegistry()).createToolDefinitions()
                .get(0).toolSchema();

        assertEquals(LIST_TOOL, schema.name());

        Object properties = schema.inputSchema() == null ? null : schema.inputSchema().get("properties");
        assertTrue(properties == null || ((Map<?, ?>) properties).isEmpty(),
                "The list is the whole list, so there is nothing to ask for: " + properties);
    }

    @Test
    void getAllToolsCallable_ok_staysWithinItsBudgetOverTheRealCatalog() {
        ToolCatalog catalog = ToolCatalog.build(DiagramThumbnails.OMIT, workspaceDir.resolve("images"), workspaceDir);
        ToolManifest manifest = ToolManifest.load();
        AstahToolRegistry registry = AstahToolRegistry.from(
                catalog,
                manifest.namesFor(ToolManifest.Profile.DIRECT),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC));

        CallableToolListDTO result = list(registry);

        assertFalse(result.tools().isEmpty(), "The real catalog must yield a list");

        int bytes = JsonSupport.OBJ_MAPPER.writeValueAsString(result).getBytes(StandardCharsets.UTF_8).length;
        assertTrue(bytes <= McpServerConfig.CALLABLE_TOOL_LIST_MAX_RESULT_BYTES,
                "The list of " + result.total() + " tool functions is " + bytes + " bytes, above the budget of "
                        + McpServerConfig.CALLABLE_TOOL_LIST_MAX_RESULT_BYTES
                        + ". Shorten the descriptions, or decide that a larger list is worth its context.");
    }

    @Test
    void getInfoOfToolsCallable_ok_returnsBothSchemasOfEveryNamedToolFunction() {
        CallableToolInfoListDTO result = info(sampleRegistry(), List.of("get_class_info", "create_class"));

        assertEquals(List.of("get_class_info", "create_class"),
                result.tools().stream().map(CallableToolInfoDTO::name).toList(),
                "They come back in the order they were asked for");
        assertTrue(result.problems().isEmpty(), String.valueOf(result.problems()));

        CallableToolInfoDTO entry = result.tools().get(0);
        assertEquals("Return the information of the specified class.", entry.description());
        assertTrue(entry.inputSchema().contains("\"id\""), entry.inputSchema());
        assertTrue(entry.outputSchema().contains("\"name\""), entry.outputSchema());
    }

    @Test
    void getInfoOfToolsCallable_ok_leavesTheResultSchemaEmptyWhenAToolFunctionDeclaresNone() {
        CallableToolInfoDTO entry = info(sampleRegistry(), List.of("create_note")).tools().get(0);

        assertFalse(entry.inputSchema().isEmpty(), "The arguments are still described");
        assertEquals("", entry.outputSchema());
    }

    @Test
    void getInfoOfToolsCallable_ok_reportsAnUnknownNameWithoutLosingTheOthers() {
        CallableToolInfoListDTO result = info(sampleRegistry(), List.of("get_class_info", "zzzznotatool"));

        assertEquals(List.of("get_class_info"), result.tools().stream().map(CallableToolInfoDTO::name).toList(),
                "One wrong name must not cost the caller the schemas it did ask for");
        assertEquals(1, result.problems().size());
        assertTrue(result.problems().get(0).contains("zzzznotatool"), result.problems().get(0));
        assertTrue(result.problems().get(0).contains(LIST_TOOL), result.problems().get(0));
    }

    @Test
    void getInfoOfToolsCallable_ok_saysWhyAToolFunctionPublishedDirectlyIsNotDescribed() {
        CallableToolInfoListDTO result = info(sampleRegistry(), List.of("get_class_info", "capture_dgm_img"));

        assertEquals(1, result.tools().size());
        assertEquals(1, result.problems().size());
        assertTrue(result.problems().get(0).contains("directly as an MCP tool"), result.problems().get(0));
    }

    @Test
    void getInfoOfToolsCallable_ng_failsWhenNoNameCouldBeDescribed() {
        String message = infoExpectingError(sampleRegistry(), List.of("zzzznotatool", "capture_dgm_img"));

        assertTrue(message.contains("zzzznotatool"), message);
        assertTrue(message.contains("directly as an MCP tool"), message);
    }

    @Test
    void getInfoOfToolsCallable_ng_rejectsAnEmptyRequest() {
        assertTrue(infoExpectingError(sampleRegistry(), List.of()).contains("toolNames"));
        assertTrue(infoExpectingError(sampleRegistry(), null).contains("toolNames"));
    }

    @Test
    void getInfoOfToolsCallable_ok_asksForEachToolFunctionOnlyOnce() {
        CallableToolInfoListDTO result = info(sampleRegistry(),
                List.of("get_class_info", "get_class_info", " get_class_info "));

        assertEquals(1, result.tools().size(), "A repeated name must not be described twice");
        assertTrue(result.problems().isEmpty(), String.valueOf(result.problems()));
    }

    @Test
    void getInfoOfToolsCallable_ng_capsHowManyNamesOneCallAcceptsAtOnce() {
        List<ToolDefinition> many = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < McpServerConfig.CALLABLE_TOOL_INFO_MAX_NAMES + 1; i++) {
            many.add(definition("tool_" + i, "Tool number " + i));
            names.add("tool_" + i);
        }
        ToolProvider provider = () -> many;

        String message = infoExpectingError(AstahToolRegistry.of(provider.createToolDefinitions(), Map.of()), names);

        assertTrue(message.contains(String.valueOf(McpServerConfig.CALLABLE_TOOL_INFO_MAX_NAMES)), message);
    }

    @Test
    void getInfoOfToolsCallable_ok_staysWithinItsResponseSizeLimitAndSaysWhatItLeftOut() {
        List<ToolDefinition> many = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < McpServerConfig.CALLABLE_TOOL_INFO_MAX_NAMES; i++) {
            many.add(definitionOf("tool_" + i, "x", 20_000));
            names.add("tool_" + i);
        }
        ToolProvider provider = () -> many;

        CallableToolInfoListDTO result = info(AstahToolRegistry.of(provider.createToolDefinitions(), Map.of()), names);

        int bytes = JsonSupport.OBJ_MAPPER.writeValueAsString(result).getBytes(StandardCharsets.UTF_8).length;
        assertTrue(bytes <= McpServerConfig.CALLABLE_TOOL_INFO_MAX_RESULT_BYTES + 25_000,
                "The response grew past its limit: " + bytes);
        assertTrue(result.tools().size() < names.size(), "Nothing was left out at all");
        assertEquals(names.size(), result.tools().size() + result.problems().size(),
                "Every name asked for is either described or accounted for in 'problems'");
        assertTrue(result.problems().get(0).contains("did not fit"), result.problems().get(0));
    }

    @Test
    void getInfoOfToolsCallable_ok_alwaysDescribesTheFirstToolFunctionHoweverLargeItIs() {
        ToolProvider provider = () -> List.of(definitionOf("tool_0", "x", McpServerConfig.CALLABLE_TOOL_INFO_MAX_RESULT_BYTES + 1_000));

        CallableToolInfoListDTO result = info(AstahToolRegistry.of(provider.createToolDefinitions(), Map.of()), List.of("tool_0"));

        assertEquals(1, result.tools().size());
        assertTrue(result.problems().isEmpty(), String.valueOf(result.problems()));
    }
}
