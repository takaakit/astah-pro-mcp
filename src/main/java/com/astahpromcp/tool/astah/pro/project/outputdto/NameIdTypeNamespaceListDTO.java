package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record NameIdTypeNamespaceListDTO(
    @JsonPropertyDescription("List value of name, identifier, type and namespace")
    List<NameIdTypeNamespaceDTO> value
) {
}
