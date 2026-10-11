package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.DefinitionNameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record AllDefinitionNameIdTypeDTO(
    @JsonPropertyDescription("Snapshot ID that identifies this list. Pass it to get_chunk_of_all_definitions together with the index of each of the other chunks.")
    String snapshotId,

    @JsonPropertyDescription("Total number of chunks")
    int totalChunks,

    @JsonPropertyDescription("The first chunk of the list of definition, name, identifier and type")
    List<DefinitionNameIdTypeDTO> firstChunk
) {
}
