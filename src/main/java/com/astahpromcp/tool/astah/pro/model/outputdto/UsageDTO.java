package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record UsageDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Client: The element that uses the supplier.")
    NameIdTypeDTO client,

    @JsonPropertyDescription("Supplier: The element that the client uses.")
    NameIdTypeDTO supplier
) {
}
