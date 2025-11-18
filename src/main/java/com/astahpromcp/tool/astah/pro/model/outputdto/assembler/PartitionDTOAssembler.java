package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IActivityNode;
import com.change_vision.jude.api.inf.model.IPartition;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.PartitionDTO;

import java.util.ArrayList;
import java.util.List;

public class PartitionDTOAssembler {
    public static PartitionDTO toDTO(@NonNull IPartition astahPartition) throws Exception {

        List<NameIdTypeDTO> activityNodes = new ArrayList<>();
        for (IActivityNode astahActivityNode : astahPartition.getActivityNodes()) {
            activityNodes.add(NameIdTypeDTOAssembler.toDTO(astahActivityNode));
        }

        NameIdTypeDTO previousPartition;
        if (astahPartition.getPreviousPartition() != null) {
            previousPartition = NameIdTypeDTOAssembler.toDTO(astahPartition.getPreviousPartition());
        } else {
            previousPartition = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO nextPartition;
        if (astahPartition.getNextPartition() != null) {
            nextPartition = NameIdTypeDTOAssembler.toDTO(astahPartition.getNextPartition());
        } else {
            nextPartition = NameIdTypeDTO.empty();
        }

        NameIdTypeDTO superPartition;
        if (astahPartition.getSuperPartition() != null) {
            superPartition = NameIdTypeDTOAssembler.toDTO(astahPartition.getSuperPartition());
        } else {
            superPartition = NameIdTypeDTO.empty();
        }

        List<NameIdTypeDTO> subPartitions = new ArrayList<>();
        for (IPartition astahSubPartition : astahPartition.getSubPartitions()) {
            subPartitions.add(NameIdTypeDTOAssembler.toDTO(astahSubPartition));
        }

        return new PartitionDTO(
            NamedElementDTOAssembler.toDTO(astahPartition),
            activityNodes,
            previousPartition,
            nextPartition,
            superPartition,
            subPartitions,
            astahPartition.isHorizontal());
    }
}
