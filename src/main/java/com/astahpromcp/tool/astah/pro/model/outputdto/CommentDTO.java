package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CommentDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Comment body text")
    String body,

    @JsonPropertyDescription("Annotated elements")
    List<ElementDTO> annotatedElements
) {
}
