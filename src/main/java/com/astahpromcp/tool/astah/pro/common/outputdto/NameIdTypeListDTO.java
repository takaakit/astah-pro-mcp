package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NameIdTypeListDTO(
    @JsonPropertyDescription("List value of name, identifier and type")
    List<NameIdTypeDTO> value
) {
}