package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record AllLabelIdTypeInfoDTO(
    @JsonPropertyDescription("Total number of chunks")
    int totalChunks,

    @JsonPropertyDescription("The first chunk of the list of label, identifier and type")
    List<LabelIdTypeDTO> firstChunk
) {
}
