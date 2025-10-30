package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ChangeParentStateDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target state identifier")
    String targetStateId,

    @JsonPropertyDescription("Parent state identifier. If there is no parent state (i.e., when rendering at the top level), set an empty string.")
    String parentStateId
) {
}
