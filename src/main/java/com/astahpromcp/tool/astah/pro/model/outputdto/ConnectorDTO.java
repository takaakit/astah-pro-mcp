package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record ConnectorDTO(
    @JsonPropertyDescription("Named element info")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Parts (The first element: source side, The second element: target side). An element is empty when that side is connected via a port instead of a part.")
    List<NameIdTypeDTO> parts,

    @JsonPropertyDescription("Ports (The first element: source side, The second element: target side). An element is empty when that side is connected via a part instead of a port.")
    List<NameIdTypeDTO> ports,

    @JsonPropertyDescription("Parts that own the connected ports (The first element: source side, The second element: target side). An element is empty when that side is not connected via a port on a part, that is, when that side is connected via a part or via a port of the structured class itself.")
    List<NameIdTypeDTO> partsWithPort,

    @JsonPropertyDescription("Type")
    NameIdTypeDTO type
) {
}
