package com.astahpromcp.tool.astah.pro.common.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ReportDTO(
    @JsonPropertyDescription("Report contents")
    String contents
) {
}
