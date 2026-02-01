package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import java.util.List;

public record ERPackageDTO(
    @JsonPropertyDescription("Package information")
    PackageDTO pkg,

    @JsonPropertyDescription("ER entities located directly under this package. If you need to retrieve all entities under this package, you must recursively retrieve the entities in its subordinate packages.")
    List<NameIdTypeDTO> erEntities
) {
}
