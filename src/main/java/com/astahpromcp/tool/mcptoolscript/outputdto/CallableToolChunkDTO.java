package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CallableToolChunkDTO(
    @JsonPropertyDescription("Index of this chunk, starting from 0")
    int chunkIndex,

    @JsonPropertyDescription("Number of chunks the list of tool functions is split into. One chunk is not the whole list: read every chunk to see them all.")
    int totalChunks,

    @JsonPropertyDescription("Number of tool functions an mcp tool script can call, across every chunk")
    int totalTools,

    @JsonPropertyDescription("The tool functions in this chunk, grouped by category and sorted by name within each category. The tool functions this server publishes directly are NOT here: they are already in your tool list and are called directly as MCP tools.")
    List<CallableToolSummaryDTO> tools
) {
}
