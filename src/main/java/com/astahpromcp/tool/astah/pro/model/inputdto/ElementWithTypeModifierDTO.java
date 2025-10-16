package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ElementWithTypeModifierDTO(
    @JsonPropertyDescription("Target element identifier")
    String id,

    @JsonPropertyDescription("Type modifier: A symbol appended to the type name, such as * (C++ pointer) and & (C++ reference).")
    String typeModifier
) {
}
