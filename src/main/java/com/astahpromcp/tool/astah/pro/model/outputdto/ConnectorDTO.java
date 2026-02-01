package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record ConnectorDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Parts (The first array: source side, The second array: target side)")
    List<AttributeDTO> parts,

    @JsonPropertyDescription("Ports (The first array: source side, The second array: target side)")
    List<PortDTO> ports,

    @JsonPropertyDescription("Type")
    NameIdTypeDTO type
) {
}
