package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewNoteAnchorDTO(
        @JsonPropertyDescription("Target diagram identifier")
        String targetDiagramId,

        @JsonPropertyDescription("Target note identifier")
        String targetNoteId,

        @JsonPropertyDescription("Target presentation identifier")
        String targetPresentationId
) {
}
