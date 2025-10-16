package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record PartitionDTO(
    @JsonPropertyDescription("Named element information")
    NamedElementDTO namedElement,

    @JsonPropertyDescription("Activity nodes")
    List<NameIdTypeDTO> activityNodes,

    @JsonPropertyDescription("Left partition or upper partition")
    NameIdTypeDTO previousPartition,

    @JsonPropertyDescription("Right partition or lower partition")
    NameIdTypeDTO nextPartition,

    @JsonPropertyDescription("Super partition")
    NameIdTypeDTO superPartition,

    @JsonPropertyDescription("Sub partitions")
    List<NameIdTypeDTO> subPartitions,

    @JsonPropertyDescription("Whether it is a horizontal partition or not")
    boolean isHorizontal
) {
}
