package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ChangeDTO(
    @JsonPropertyDescription("Change of the element: added, removed, or modified")
    ChangeKind kind,

    @JsonPropertyDescription("xmi.id of the element (the base's for a removed element)")
    String xmiId,

    @JsonPropertyDescription("xmi.id in the base when it differs from 'xmiId'; empty otherwise")
    String baseXmiId,

    @JsonPropertyDescription("XMI element name (e.g., UML:Class)")
    String type,

    @JsonPropertyDescription("Stereotype names")
    List<String> stereotypes,

    @JsonPropertyDescription("Name, or the label of a presentation; empty if none")
    String name,

    @JsonPropertyDescription("Nearest ancestor in the XMI that gives the element its context")
    ContextDTO xmlContextOwner,

    @JsonPropertyDescription("Context in the base when the element moved; empty otherwise")
    ContextDTO beforeContext,

    @JsonPropertyDescription("Context in the target when the element moved; empty otherwise")
    ContextDTO afterContext,

    @JsonPropertyDescription("Diagram of a presentation; empty otherwise")
    ElementRefDTO diagram,

    @JsonPropertyDescription("Model element of a presentation; empty otherwise")
    ElementRefDTO model,

    @JsonPropertyDescription("Index of the first of 'properties' among the properties of the whole change, counted from 0; above 0 only in a page that goes on with a change split across pages")
    int propertyStart,

    @JsonPropertyDescription("Number of properties of the whole change; more than 'properties' holds when the change is split across pages")
    int propertyCount,

    @JsonPropertyDescription("Changed values (every value for an added or removed element)")
    List<PropertyChangeDTO> properties
) {
}
