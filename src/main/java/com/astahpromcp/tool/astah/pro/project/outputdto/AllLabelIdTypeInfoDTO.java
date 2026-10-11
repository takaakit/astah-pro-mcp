package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record AllLabelIdTypeInfoDTO(
    @JsonPropertyDescription("Snapshot ID that identifies this list. Pass it to get_chunk_of_all_prsts together with the index of each of the other chunks.")
    String snapshotId,

    @JsonPropertyDescription("Total number of chunks")
    int totalChunks,

    @JsonPropertyDescription("The first chunk of the list of label, identifier and type")
    List<LabelIdTypeDTO> firstChunk
) {
}
