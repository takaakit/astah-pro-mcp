package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewStateMachineDiagramDTO(
    @JsonPropertyDescription("Parent named element identifier")
    String parentNamedElementId,

    @JsonPropertyDescription("New state machine diagram name")
    String newStateMachineDiagramName
) {
}
