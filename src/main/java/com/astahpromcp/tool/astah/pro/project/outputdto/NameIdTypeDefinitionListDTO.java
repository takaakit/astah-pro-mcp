package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.astahpromcp.tool.astah.pro.project.outputdto.NameIdTypeDefinitionDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NameIdTypeDefinitionListDTO(
    @JsonPropertyDescription("List value of name, identifier, type and definition")
    List<NameIdTypeDefinitionDTO> value
) {
}