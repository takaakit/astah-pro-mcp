package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PortWithServiceDTO(
    @JsonPropertyDescription("Target port identifier")
    String targetPortId,

    @JsonPropertyDescription("Is Service. If true, it is service, otherwise, it is not service.")
    boolean isService
) {
}
