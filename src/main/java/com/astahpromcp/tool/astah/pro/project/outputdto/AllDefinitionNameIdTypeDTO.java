package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.DefinitionNameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record AllDefinitionNameIdTypeDTO(
    @JsonPropertyDescription("Total number of chunks")
    int totalChunks,

    @JsonPropertyDescription("The first chunk of the list of definition, name, identifier and type")
    List<DefinitionNameIdTypeDTO> firstChunk
) {
}
