package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewNoteDTO(
        @JsonPropertyDescription("Target diagram identifier")
        String targetDiagramId,

        @JsonPropertyDescription("Note content")
        String noteContent,

        @JsonPropertyDescription("Location X coordinate")
        int locationX,

        @JsonPropertyDescription("Location Y coordinate")
        int locationY
) {
}
