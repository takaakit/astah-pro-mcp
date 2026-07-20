package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewNoteDTO(
        @JsonPropertyDescription("Target diagram identifier")
        String targetDiagramId,

        @JsonPropertyDescription("Note content. The content is automatically wrapped at the right edge of the note, so there is no need to insert line breaks in the middle of a sentence to fit the note's display width.")
        String noteContent,

        @JsonPropertyDescription("Location X coordinate")
        int locationX,

        @JsonPropertyDescription("Location Y coordinate")
        int locationY
) {
}
