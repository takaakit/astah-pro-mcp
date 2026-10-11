package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record AllNameIdTypeInfoDTO(
    @JsonPropertyDescription("Snapshot ID that identifies this list. Pass it to get_chunk_of_all_named_elements together with the index of each of the other chunks.")
    String snapshotId,

    @JsonPropertyDescription("Total number of chunks")
    int totalChunks,

    @JsonPropertyDescription("The first chunk of the list of name, identifier and type")
    List<NameIdTypeDTO> firstChunk
) {
}
