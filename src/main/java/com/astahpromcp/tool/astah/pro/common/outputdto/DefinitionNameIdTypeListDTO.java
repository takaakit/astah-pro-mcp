package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record DefinitionNameIdTypeListDTO(
    @JsonPropertyDescription("List value of definition, name, identifier and type")
    List<DefinitionNameIdTypeDTO> value
) {
}
