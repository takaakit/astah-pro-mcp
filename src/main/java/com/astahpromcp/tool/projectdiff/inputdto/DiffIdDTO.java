package com.astahpromcp.tool.projectdiff.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiffIdDTO(
    @JsonPropertyDescription("diffId returned when the comparison was started")
    String diffId,

    @JsonPropertyDescription("Index of the first change to return, counting the changes of the whole result from 0: 0 for the first page, then the 'nextIndex' of the previous answer. Must be 0 unless 'status' is 'done'")
    int startIndex,

    @JsonPropertyDescription("Index of the first property to return of the change at 'startIndex', counting its properties from 0: 0 for the first page, then the 'nextPropertyIndex' of the previous answer. Must be 0 unless 'status' is 'done'")
    int startPropertyIndex
) {
}
