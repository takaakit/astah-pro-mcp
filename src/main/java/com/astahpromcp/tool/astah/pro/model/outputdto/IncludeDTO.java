package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record IncludeDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Including UseCase")
    NameIdTypeDTO includingUseCase,

    @JsonPropertyDescription("Included UseCase")
    NameIdTypeDTO includedUseCase
) {
}
