package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewConnectorPresentationDTO(
    @JsonPropertyDescription("Target composite structure diagram identifier")
    String targetCompositeStructureDiagramId,

    @JsonPropertyDescription("Source node presentation identifier. It must be a part presentation or port presentation.")
    String sourceNodePresentationId,

    @JsonPropertyDescription("Target node presentation identifier. It must be a part presentation or port presentation.")
    String targetNodePresentationId,

    @JsonPropertyDescription("New connector name")
    String newConnectorName
) {
}
