package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DependencyDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Client: The element that depends on the supplier.")
    NameIdTypeDTO client,

    @JsonPropertyDescription("Supplier: The element that the client depends on.")
    NameIdTypeDTO supplier
) {
}
