package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ExtendDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Extending UseCase")
    NameIdTypeDTO extendingUseCase,

    @JsonPropertyDescription("Extended UseCase")
    NameIdTypeDTO extendedUseCase
) {
}
