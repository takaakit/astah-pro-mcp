package com.astahpromcp.tool.astah.pro.project.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NameIdTypeNamespaceDTO(
    @JsonPropertyDescription("Name")
    String name,

    @JsonPropertyDescription("Identifier")
    String id,

    @JsonPropertyDescription("Type")
    String type,

    @JsonPropertyDescription("Full namespace")
    String namespace
) {
}
