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
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolChunkDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolInfoDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolInfoListDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolSummaryDTO;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.json.schema.jackson3.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CallableToolInfoToolTest {

    private static final String CHUNK_TOOL = "get_chunk_of_tools_callable_from_mcp_tool_script";
    private static final String INFO_TOOL = "get_info_of_tools_callable_from_mcp_tool_script";

    // Smaller than any entry, so that every tool function gets a chunk of its own.
    private static final int ONE_TOOL_PER_CHUNK = 1;

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

    private static McpSchema.CallToolResult call(CallableToolInfoTool tool, String toolName, Map<String, Object> arguments) {
        ToolDefinition definition = tool.createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No tool function is named " + toolName));

        return definition.toolHandler().apply(null, new McpSchema.CallToolRequest(toolName, arguments, null));
    }

    private static McpSchema.CallToolResult chunkResult(CallableToolInfoTool tool, Integer chunkIndex) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("chunkIndex", chunkIndex);

        return call(tool, CHUNK_TOOL, arguments);
    }

    private static CallableToolChunkDTO chunk(CallableToolInfoTool tool, int chunkIndex) {
        McpSchema.CallToolResult result = chunkResult(tool, chunkIndex);

        assertFalse(Boolean.TRUE.equals(result.isError()), String.valueOf(result.content()));

        return JsonSupport.OBJ_MAPPER.convertValue(result.structuredContent(), CallableToolChunkDTO.class);
    }

    private static List<CallableToolChunkDTO> allChunks(CallableToolInfoTool tool) {
        List<CallableToolChunkDTO> chunks = new ArrayList<>();
        chunks.add(chunk(tool, 0));
        for (int index = 1; index < chunks.get(0).totalChunks(); index++) {
            chunks.add(chunk(tool, index));
        }
        return chunks;
    }

    private static CallableToolInfoListDTO info(AstahToolRegistry registry, List<String> toolNames) {
        return info(new CallableToolInfoTool(registry), toolNames);
    }

    private static CallableToolInfoListDTO info(CallableToolInfoTool tool, List<String> toolNames) {
        McpSchema.CallToolResult result = infoResult(tool, toolNames);

        assertFalse(Boolean.TRUE.equals(result.isError()), String.valueOf(result.content()));

        return JsonSupport.OBJ_MAPPER.convertValue(result.structuredContent(), CallableToolInfoListDTO.class);
    }

    private static McpSchema.CallToolResult infoResult(AstahToolRegistry registry, List<String> toolNames) {
        return infoResult(new CallableToolInfoTool(registry), toolNames);
    }

    private static McpSchema.CallToolResult infoResult(CallableToolInfoTool tool, List<String> toolNames) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("toolNames", toolNames);

        return call(tool, INFO_TOOL, arguments);
    }

    // The schema of the answer this tool function publishes, which is what the MCP SDK checks every answer against
    private static Map<String, Object> publishedInfoOutputSchema(CallableToolInfoTool tool) {
        return tool.createToolDefinitions().stream()
                .filter(candidate -> candidate.toolSchema().name().equals(INFO_TOOL))
                .findFirst()
                .orElseThrow()
                .toolSchema()
                .outputSchema();
    }

    private static String errorOf(McpSchema.CallToolResult result) {
        assertTrue(Boolean.TRUE.equals(result.isError()),
                "The call was expected to fail but succeeded: " + result.structuredContent());

        return String.valueOf(result.content());
    }

    private static String infoExpectingError(AstahToolRegistry registry, List<String> toolNames) {
        return errorOf(infoResult(registry, toolNames));
    }

    private static List<String> namesOf(List<CallableToolChunkDTO> chunks) {
        return chunks.stream().flatMap(chunk -> chunk.tools().stream()).map(CallableToolSummaryDTO::name).toList();
    }

    // The bytes the entries of a chunk take up, counted the way the split counts them
    private static int entryBytesOf(CallableToolChunkDTO chunk) {
        int bytes = 0;
        for (CallableToolSummaryDTO entry : chunk.tools()) {
            bytes += JsonSupport.OBJ_MAPPER.writeValueAsString(entry).getBytes(StandardCharsets.UTF_8).length + 1;
        }
        return bytes;
    }

    private static AstahToolRegistry registryOf(ToolDefinition... definitions) {
        ToolProvider provider = () -> List.of(definitions);
        return AstahToolRegistry.of(provider.createToolDefinitions(), Map.of());
    }

    private static AstahToolRegistry manyTools(int count, int descriptionChars) {
        ToolDefinition[] definitions = new ToolDefinition[count];
        for (int i = 0; i < count; i++) {
            definitions[i] = definitionOf("tool_" + i, "x", descriptionChars);
        }
        return registryOf(definitions);
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

    private AstahToolRegistry realRegistry() {
        ToolCatalog catalog = ToolCatalog.build(DiagramThumbnails.OMIT, workspaceDir.resolve("images"), workspaceDir);
        ToolManifest manifest = ToolManifest.load();

        return AstahToolRegistry.from(
                catalog,
                manifest.namesFor(ToolManifest.Profile.DIRECT),
                manifest.namesFor(ToolManifest.Profile.PROGRAMMATIC));
    }

    @Test
    void getChunkOfToolsCallable_ok_returnsOnlyTheToolFunctionsAScriptCanCall() {
        List<CallableToolChunkDTO> chunks = allChunks(new CallableToolInfoTool(sampleRegistry()));

        assertEquals(List.of("get_class_info", "get_attr_info", "create_class", "create_note"), namesOf(chunks));
        assertEquals(4, chunks.get(0).totalTools());
    }

    @Test
    void getChunkOfToolsCallable_ok_leavesOutTheToolFunctionsPublishedDirectly() {
        List<String> names = namesOf(allChunks(new CallableToolInfoTool(sampleRegistry(), ONE_TOOL_PER_CHUNK)));

        assertFalse(names.contains("capture_dgm_img"), "it returns binary content");
        assertFalse(names.contains("get_info_of_ocl_spec"), "it performs blocking IO");
    }

    @Test
    void getChunkOfToolsCallable_ok_keepsRegistrationOrderAcrossChunks() {
        AstahToolRegistry registry = sampleRegistry();

        List<CallableToolChunkDTO> chunks = allChunks(new CallableToolInfoTool(registry, ONE_TOOL_PER_CHUNK));

        assertEquals(4, chunks.size());
        for (int index = 0; index < chunks.size(); index++) {
            assertEquals(index, chunks.get(index).chunkIndex(), "Every chunk states the index it answers for");
        }
        assertEquals(List.copyOf(registry.mcpToolScriptCallableNames()), namesOf(chunks),
                "The registry is ordered by the manifest, and reading the chunks in index order must not reorder it");
    }

    @Test
    void getChunkOfToolsCallable_ok_carriesTheDescriptionOfEveryToolFunction() {
        CallableToolSummaryDTO entry = chunk(new CallableToolInfoTool(sampleRegistry()), 0).tools().get(0);

        assertEquals("get_class_info", entry.name());
        assertEquals("Return the information of the specified class.", entry.description());
    }

    @Test
    void getChunkOfToolsCallable_ng_requiresTheChunkIndex() {
        CallableToolInfoTool tool = new CallableToolInfoTool(sampleRegistry());
        McpSchema.Tool schema = tool.createToolDefinitions().get(0).toolSchema();

        assertEquals(CHUNK_TOOL, schema.name());
        assertEquals(List.of("chunkIndex"), schema.inputSchema().get("required"));

        String message = errorOf(call(tool, CHUNK_TOOL, Map.of()));
        assertTrue(message.contains("chunkIndex"), message);
    }

    @Test
    void getChunkOfToolsCallable_ng_rejectsANullChunkIndex() {
        errorOf(chunkResult(new CallableToolInfoTool(sampleRegistry()), null));
    }

    @Test
    void getChunkOfToolsCallable_ok_staysWithinItsBudgetOverTheRealCatalog() {
        AstahToolRegistry registry = realRegistry();

        List<CallableToolChunkDTO> chunks = allChunks(new CallableToolInfoTool(registry));

        assertEquals(List.copyOf(registry.mcpToolScriptCallableNames()), namesOf(chunks),
                "Reading every chunk must yield every callable tool function, once each and in order");
        assertEquals(namesOf(chunks).size(), chunks.get(0).totalTools(),
                "'totalTools' must count every entry the chunks carry");
        for (CallableToolChunkDTO chunk : chunks) {
            assertEquals(chunks.size(), chunk.totalChunks(), "Every chunk reports the same number of chunks");
            assertEquals(chunks.get(0).totalTools(), chunk.totalTools(), "Every chunk reports the same total");
        }

        for (CallableToolChunkDTO chunk : chunks) {
            int entryBytes = entryBytesOf(chunk);
            assertTrue(entryBytes <= McpServerConfig.CALLABLE_TOOL_CHUNK_MAX_BYTES,
                    "Chunk " + chunk.chunkIndex() + " holds " + entryBytes + " bytes of entries, above the budget of "
                            + McpServerConfig.CALLABLE_TOOL_CHUNK_MAX_BYTES + ". A single description has outgrown a whole chunk.");

            int responseBytes = JsonSupport.OBJ_MAPPER.writeValueAsString(chunk).getBytes(StandardCharsets.UTF_8).length;
            assertTrue(responseBytes <= McpServerConfig.CALLABLE_TOOL_CHUNK_MAX_BYTES + 1_024,
                    "Chunk " + chunk.chunkIndex() + " answers with " + responseBytes + " bytes, more than its entries and a small envelope");
        }
    }

    @Test
    void getChunkOfToolsCallable_ok_splitsIntoChunksWithinTheBudget() {
        int budget = 1_000;

        List<CallableToolChunkDTO> chunks = allChunks(new CallableToolInfoTool(manyTools(30, 100), budget));

        assertTrue(chunks.size() > 1, "Thirty entries of about 130 bytes do not fit in one chunk of " + budget + " bytes");
        for (CallableToolChunkDTO chunk : chunks) {
            assertTrue(entryBytesOf(chunk) <= budget, "Chunk " + chunk.chunkIndex() + " is over the budget: " + entryBytesOf(chunk));
        }

        List<String> expected = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            expected.add("tool_" + i);
        }
        assertEquals(expected, namesOf(chunks), "Every entry is in exactly one chunk, in order");
    }

    @Test
    void getChunkOfToolsCallable_ok_keepsAToolFunctionLargerThanTheBudgetInAChunkOfItsOwn() {
        AstahToolRegistry registry = registryOf(
                definitionOf("small_before", "x", 10),
                definitionOf("huge", "x", 5_000),
                definitionOf("small_after", "x", 10));

        List<CallableToolChunkDTO> chunks = allChunks(new CallableToolInfoTool(registry, 1_000));

        assertEquals(List.of("small_before", "huge", "small_after"), namesOf(chunks), "Nothing may be left out, however large");
        CallableToolChunkDTO huge = chunks.stream()
                .filter(chunk -> chunk.tools().stream().anyMatch(entry -> entry.name().equals("huge")))
                .findFirst()
                .orElseThrow();
        assertEquals(1, huge.tools().size(), "The oversized entry must not crowd out the entries around it");
    }

    @Test
    void getChunkOfToolsCallable_ok_answersInOneChunkWhenEverythingFits() {
        CallableToolInfoTool tool = new CallableToolInfoTool(sampleRegistry());

        CallableToolChunkDTO only = chunk(tool, 0);

        assertEquals(1, only.totalChunks());
        assertEquals(4, only.tools().size());
    }

    @Test
    void getChunkOfToolsCallable_ok_keepsIndexZeroValidWhenNothingIsCallable() {
        CallableToolChunkDTO empty = chunk(new CallableToolInfoTool(registryOf()), 0);

        assertEquals(1, empty.totalChunks());
        assertEquals(0, empty.totalTools());
        assertEquals(List.of(), empty.tools());
    }

    @Test
    void getChunkOfToolsCallable_ng_rejectsAnIndexOutOfRange() {
        CallableToolInfoTool tool = new CallableToolInfoTool(sampleRegistry(), ONE_TOOL_PER_CHUNK);

        String negative = errorOf(chunkResult(tool, -1));
        String pastTheEnd = errorOf(chunkResult(tool, 4));

        assertTrue(negative.contains("from 0 to 3"), negative);
        assertTrue(pastTheEnd.contains("from 0 to 3"), pastTheEnd);
    }

    @Test
    void getInfoOfToolsCallable_ok_returnsBothSchemasOfEveryNamedToolFunction() {
        AstahToolRegistry registry = sampleRegistry();

        CallableToolInfoListDTO result = info(registry, List.of("get_class_info", "create_class"));

        assertEquals(List.of("get_class_info", "create_class"),
                result.tools().stream().map(CallableToolInfoDTO::name).toList(),
                "They come back in the order they were asked for");
        assertTrue(result.problems().isEmpty(), String.valueOf(result.problems()));

        CallableToolInfoDTO entry = result.tools().get(0);
        McpSchema.Tool declared = registry.find("get_class_info").toolSchema();
        assertEquals("Return the information of the specified class.", entry.description());
        assertEquals(declared.inputSchema(), entry.inputSchema(), "The arguments' schema comes as the JSON object the tool function declares");
        assertEquals(declared.outputSchema(), entry.outputSchema(), "So does the result's schema");
    }

    @Test
    void getInfoOfToolsCallable_ok_leavesTheResultSchemaEmptyWhenAToolFunctionDeclaresNone() {
        CallableToolInfoDTO entry = info(sampleRegistry(), List.of("create_note")).tools().get(0);

        assertFalse(entry.inputSchema().isEmpty(), "The arguments are still described");
        assertEquals(Map.of(), entry.outputSchema(), "An empty object stands for the result schema it does not declare");
    }

    @Test
    void getInfoOfToolsCallable_ok_carriesTheDeclaredSchemasUnchangedOverTheRealCatalog() {
        AstahToolRegistry registry = realRegistry();
        CallableToolInfoTool tool = new CallableToolInfoTool(registry);

        for (String name : registry.mcpToolScriptCallableNames()) {
            CallableToolInfoDTO entry = info(tool, List.of(name)).tools().get(0);
            McpSchema.Tool declared = registry.find(name).toolSchema();
            Map<String, Object> declaredOutput = declared.outputSchema() == null ? Map.of() : declared.outputSchema();

            // Compared as JSON text, so that the order of the keys has to match as well as the content
            assertEquals(JsonSupport.OBJ_MAPPER.writeValueAsString(declared.inputSchema()), JsonSupport.OBJ_MAPPER.writeValueAsString(entry.inputSchema()),
                    "The arguments' schema of " + name + " must come as the tool function declares it");
            assertEquals(JsonSupport.OBJ_MAPPER.writeValueAsString(declaredOutput), JsonSupport.OBJ_MAPPER.writeValueAsString(entry.outputSchema()),
                    "The result's schema of " + name + " must come as the tool function declares it, or as an empty object when it declares none");
        }
    }

    // The MCP SDK checks every answer against the schema this tool function publishes, and an answer that fails the check reaches the caller as an error.
    @Test
    void getInfoOfToolsCallable_ok_answersWithinItsPublishedSchemaOverTheRealCatalog() {
        AstahToolRegistry registry = realRegistry();
        CallableToolInfoTool tool = new CallableToolInfoTool(registry);
        Map<String, Object> schema = publishedInfoOutputSchema(tool);
        JsonSchemaValidator validator = new DefaultJsonSchemaValidator(JsonSupport.OBJ_MAPPER);

        // One name at a time covers every tool function; as many names as one call accepts covers answers with several entries and with names left out for room.
        List<String> names = List.copyOf(registry.mcpToolScriptCallableNames());
        List<List<String>> requests = new ArrayList<>();
        names.forEach(name -> requests.add(List.of(name)));
        for (int from = 0; from < names.size(); from += McpServerConfig.CALLABLE_TOOL_INFO_MAX_NAMES) {
            requests.add(names.subList(from, Math.min(from + McpServerConfig.CALLABLE_TOOL_INFO_MAX_NAMES, names.size())));
        }

        for (List<String> request : requests) {
            McpSchema.CallToolResult result = infoResult(tool, request);
            assertFalse(Boolean.TRUE.equals(result.isError()), String.valueOf(result.content()));

            JsonSchemaValidator.ValidationResponse validation = validator.validate(schema, result.structuredContent());
            assertTrue(validation.valid(), "The answer for " + request + " does not fit the published schema: " + validation.errorMessage());
        }
    }

    @Test
    void getInfoOfToolsCallable_ok_publishesBothSchemasAsJsonObjects() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.valueToTree(publishedInfoOutputSchema(new CallableToolInfoTool(sampleRegistry())));
        JsonNode entry = schema.at("/properties/tools/items/properties");

        assertEquals("object", entry.at("/inputSchema/type").asString(), String.valueOf(schema));
        assertEquals("object", entry.at("/outputSchema/type").asString(), String.valueOf(schema));
        assertNull(schema.get("$defs"), "The schemas must be written in place, not as a named definition: " + schema);
    }

    @Test
    void getInfoOfToolsCallable_ok_reportsAnUnknownNameWithoutLosingTheOthers() {
        CallableToolInfoListDTO result = info(sampleRegistry(), List.of("get_class_info", "zzzznotatool"));

        assertEquals(List.of("get_class_info"), result.tools().stream().map(CallableToolInfoDTO::name).toList(),
                "One wrong name must not cost the caller the schemas it did ask for");
        assertEquals(1, result.problems().size());
        assertTrue(result.problems().get(0).contains("zzzznotatool"), result.problems().get(0));
        assertTrue(result.problems().get(0).contains(CHUNK_TOOL), result.problems().get(0));
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
