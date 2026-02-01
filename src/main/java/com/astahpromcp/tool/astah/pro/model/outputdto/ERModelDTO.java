package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import java.util.List;

public record ERModelDTO(
    @JsonPropertyDescription("Package information")
    PackageDTO pkg,

    @JsonPropertyDescription("ER schemata")
    List<NameIdTypeDTO> schemata
) {
}
