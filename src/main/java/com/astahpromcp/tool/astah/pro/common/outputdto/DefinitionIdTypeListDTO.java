package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record DefinitionIdTypeListDTO(
    @JsonPropertyDescription("List value of definition, identifier and type")
    List<DefinitionIdTypeDTO> value
) {
}
