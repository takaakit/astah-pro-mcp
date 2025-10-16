package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;
import java.util.Map;

public record ElementDTO(
        @JsonPropertyDescription("Element identifier")
        String id,

        @JsonPropertyDescription("Type modifier: A symbol appended to the type name, such as * (C++ pointer) and & (C++ reference).")
        String typeModifier,

        @JsonPropertyDescription("Stereotypes")
        List<String> stereotypes,

        @JsonPropertyDescription("Tagged values")
        Map<String, String> taggedValues,

        @JsonPropertyDescription("Identifiers of corresponding presentations")
        List<String> correspondingPresentationIds
) {
}
