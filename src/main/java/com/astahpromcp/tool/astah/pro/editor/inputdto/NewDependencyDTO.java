package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewDependencyDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("Client node presentation identifier")
    String clientNodePresentationId,

    @JsonPropertyDescription("Supplier node presentation identifier")
    String supplierNodePresentationId,

    @JsonPropertyDescription("New dependency name")
    String newDependencyName
) {
}
