package com.astahpromcp.tool.astah.pro.project.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SnapshotChunkDTO(
    @JsonPropertyDescription("Snapshot ID that the get_info tool function returned along with the first chunk. It identifies the list to read the chunk from.")
    String snapshotId,

    @JsonPropertyDescription("Index of the chunk to retrieve. The index starts from 0.")
    int chunkIndex
) {
}
