package com.astahpromcp.tool.common.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ChunkDTO(
    @JsonPropertyDescription("Index of the chunk to retrieve. The index starts from 0.")
    int chunkIndex
) {
}
