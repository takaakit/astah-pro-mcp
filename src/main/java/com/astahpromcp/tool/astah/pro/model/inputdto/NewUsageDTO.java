package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewUsageDTO(
        @JsonPropertyDescription("Client class identifier")
        String clientClassId,

        @JsonPropertyDescription("Supplier class identifier")
        String supplierClassId
) {
}
