package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ContextDTO(
    @JsonPropertyDescription("xmi.id; empty if none")
    String xmiId,

    @JsonPropertyDescription("XMI element name")
    String type,

    @JsonPropertyDescription("Name, or the label of a presentation; empty if none")
    String name,

    @JsonPropertyDescription("Class at the end of an association end without a name; empty otherwise")
    String participant,

    @JsonPropertyDescription("XMI path from this ancestor down to the element")
    String ownerPath
) {
    public static ContextDTO empty() {
        return new ContextDTO(
            "",
            "",
            "",
            "",
            "");
    }
}
