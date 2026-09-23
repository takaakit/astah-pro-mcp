package com.astahpromcp.tool.mcptoolscript;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.JsonSupport;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.ChunkDTO;
import com.astahpromcp.tool.manifest.NotMcpToolScriptCallableReason;
import com.astahpromcp.tool.mcptoolscript.inputdto.CallableToolNamesDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolChunkDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolInfoDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolInfoListDTO;
import com.astahpromcp.tool.mcptoolscript.outputdto.CallableToolSummaryDTO;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Tool for listing the tool functions an mcp tool script can call.
@Slf4j
public class CallableToolInfoTool implements ToolProvider {

    private final AstahToolRegistry registry;

    private final List<List<CallableToolSummaryDTO>> chunks;
    private final int totalTools;

    public CallableToolInfoTool(AstahToolRegistry registry) {
        this(registry, McpServerConfig.CALLABLE_TOOL_CHUNK_MAX_BYTES);
    }

    CallableToolInfoTool(AstahToolRegistry registry, int chunkMaxBytes) {
        this.registry = registry;

        List<CallableToolSummaryDTO> summaries = new ArrayList<>();
        for (ToolDefinition definition : registry.mcpToolScriptCallableDefinitions()) {
            McpSchema.Tool schema = definition.toolSchema();
            summaries.add(new CallableToolSummaryDTO(schema.name(), nullToEmpty(schema.description())));
        }

        this.chunks = splitIntoChunks(summaries, chunkMaxBytes);
        this.totalTools = summaries.size();
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_chunk_of_tools_callable_from_mcp_tool_script",
                "Return one chunk of the list of tool functions an mcp tool script can call: the name and the description of each, so that you can find the ones to call from 'run_mcp_tool_script' as tools.<name>({ ... }). This server exposes only a small number of tool functions directly; this list is how you find the rest. One chunk is NOT the whole list: call this tool with 'chunkIndex' 0 first, read 'totalChunks' in the answer, then call it once for every remaining index, all in parallel in a single message, and choose tool functions only after you have read every chunk. It returns what each tool function is for, not what arguments it takes: call 'get_info_of_tools_callable_from_mcp_tool_script' for the argument and result schemas of the tool functions you decide to call. NOTE: this lists the tool functions of THIS server, NOT the Astah Java API; to work with the raw Astah API, call 'astah_api_script_guide' and use 'run_astah_api_script'.",
                this::getChunkOfCallableTools,
                ChunkDTO.class,
                CallableToolChunkDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_info_of_tools_callable_from_mcp_tool_script",
                "Return the name, the description and the JSON schemas of the arguments and of the result of the named tool functions, so that you can call them from 'run_mcp_tool_script' as tools.<name>({ ... }) with the right arguments. Find the names in the chunks of 'get_chunk_of_tools_callable_from_mcp_tool_script' first.",
                this::getInfoOfCallableTools,
                CallableToolNamesDTO.class,
                CallableToolInfoListDTO.class)
        );
    }

    private CallableToolChunkDTO getChunkOfCallableTools(ChunkDTO param) throws Exception {
        log.debug("Get a chunk of the tool functions callable from an mcp tool script: {}", param);

        int index = param.chunkIndex();
        if (index < 0 || index >= chunks.size()) {
            String range = chunks.size() == 1
                    ? "the whole list of tool functions fits in one chunk, so it must be 0"
                    : "the list of tool functions is split into " + chunks.size() + " chunks, so it must be from 0 to " + (chunks.size() - 1);
            throw new IllegalArgumentException("'chunkIndex' is " + index + ", but " + range + ".");
        }

        return new CallableToolChunkDTO(index, chunks.size(), totalTools, chunks.get(index));
    }

    private CallableToolInfoListDTO getInfoOfCallableTools(CallableToolNamesDTO param) throws Exception {
        log.debug("Get info of tool functions callable from an mcp tool script: {}", param);

        Set<String> names = requestedNames(param.toolNames());

        List<CallableToolInfoDTO> tools = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        int responseBytes = 0;

        for (String name : names) {
            String problem = problemWith(name);
            if (problem != null) {
                problems.add(problem);
                continue;
            }

            CallableToolInfoDTO info = toInfo(registry.find(name).toolSchema());
            int infoBytes = JsonSupport.OBJ_MAPPER.writeValueAsString(info).getBytes(StandardCharsets.UTF_8).length;

            // The result schema alone runs to several kilobytes for many tool functions, so a request at the name limit can still outgrow one answer.
            if (!tools.isEmpty() && responseBytes + infoBytes > McpServerConfig.CALLABLE_TOOL_INFO_MAX_RESULT_BYTES) {
                problems.add("'" + name + "' was left out because its schemas did not fit in the room left in this answer. A later, smaller one may still have fitted, so this says nothing about the ones that did. Ask for it again in a call with fewer names.");
                continue;
            }

            tools.add(info);
            responseBytes += infoBytes;
        }

        // Nothing answered leaves no partial result to keep, and failing is what stops the caller reading an empty answer as a full one.
        if (tools.isEmpty()) {
            throw new IllegalArgumentException("None of the names could be described. " + String.join(" ", problems));
        }

        return new CallableToolInfoListDTO(List.copyOf(tools), List.copyOf(problems));
    }

    private static List<List<CallableToolSummaryDTO>> splitIntoChunks(List<CallableToolSummaryDTO> summaries, int maxBytes) {
        List<List<CallableToolSummaryDTO>> chunks = new ArrayList<>();
        List<CallableToolSummaryDTO> current = new ArrayList<>();
        int currentBytes = 0;

        for (CallableToolSummaryDTO summary : summaries) {
            // One more byte for the comma that separates it from the entry before it
            int bytes = JsonSupport.OBJ_MAPPER.writeValueAsString(summary).getBytes(StandardCharsets.UTF_8).length + 1;

            // A tool function larger than the budget still gets a chunk of its own rather than being left out.
            if (!current.isEmpty() && currentBytes + bytes > maxBytes) {
                chunks.add(List.copyOf(current));
                current = new ArrayList<>();
                currentBytes = 0;
            }

            current.add(summary);
            currentBytes += bytes;
        }

        // Always at least one chunk, so that index 0 is valid even when nothing is callable.
        chunks.add(List.copyOf(current));

        return List.copyOf(chunks);
    }

    // The names to describe, in the order they were asked for and without the repeats that would spend the answer twice on one tool function
    private static Set<String> requestedNames(List<String> toolNames) {
        Set<String> names = new LinkedHashSet<>();
        if (toolNames != null) {
            for (String name : toolNames) {
                if (name != null && !name.isBlank()) {
                    names.add(name.trim());
                }
            }
        }

        if (names.isEmpty()) {
            throw new IllegalArgumentException("'toolNames' is empty. Name at least one tool function, as the chunks of 'get_chunk_of_tools_callable_from_mcp_tool_script' spell it.");
        }
        if (names.size() > McpServerConfig.CALLABLE_TOOL_INFO_MAX_NAMES) {
            throw new IllegalArgumentException("'toolNames' holds " + names.size() + " names, and at most "
                    + McpServerConfig.CALLABLE_TOOL_INFO_MAX_NAMES + " are accepted in one call, because the schemas of that many tool functions already fill an answer. Split them across several calls.");
        }

        return names;
    }

    // Why the given tool function cannot be described, or null when it can
    private String problemWith(String name) {
        if (registry.find(name) == null) {
            return "There is no tool function named '" + name + "'. Read the chunks of 'get_chunk_of_tools_callable_from_mcp_tool_script' for the names, spelled as this server spells them.";
        }

        NotMcpToolScriptCallableReason reason = registry.notMcpToolScriptCallableReason(name);
        return reason == null ? null : reason.message(name);
    }

    // The schemas go in as JSON objects, not as JSON text: text would escape every quote in them, which only adds to the answer
    // An empty object stands for a schema the tool function does not declare
    private static CallableToolInfoDTO toInfo(McpSchema.Tool schema) {
        return new CallableToolInfoDTO(
                schema.name(),
                nullToEmpty(schema.description()),
                emptyIfNull(schema.inputSchema()),
                emptyIfNull(schema.outputSchema()));
    }

    private static Map<String, Object> emptyIfNull(Map<String, Object> schema) {
        return schema == null ? Map.of() : schema;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
