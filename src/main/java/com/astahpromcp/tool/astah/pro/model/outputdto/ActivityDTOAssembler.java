package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.*;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ActivityDTOAssembler {
    public static ActivityDTO toDTO(@NonNull IActivity astahActivity) throws Exception {

        NameIdTypeDTO activityDiagram;
        if (astahActivity.getActivityDiagram() != null) {
            activityDiagram = NameIdTypeDTOAssembler.toDTO(astahActivity.getActivityDiagram());
        } else {
            activityDiagram = NameIdTypeDTO.empty();
        }

        List<NameIdTypeDTO> activityNodes = new ArrayList<>();
        for (IActivityNode astahActivityNode : astahActivity.getActivityNodes()) {
            activityNodes.add(NameIdTypeDTOAssembler.toDTO(astahActivityNode));
        }

        List<NameIdTypeDTO> callBehaviorActions = new ArrayList<>();
        for (IAction astahCallBehaviorAction : astahActivity.getCallBehaviorActions()) {
            callBehaviorActions.add(NameIdTypeDTOAssembler.toDTO(astahCallBehaviorAction));
        }

        List<NameIdTypeDTO> flows = new ArrayList<>();
        for (IFlow astahFlow : astahActivity.getFlows()) {
            flows.add(NameIdTypeDTOAssembler.toDTO(astahFlow));
        }

        List<NameIdTypeDTO> partitions = new ArrayList<>();
        for (IPartition astahPartition : astahActivity.getPartitions()) {
            partitions.add(NameIdTypeDTOAssembler.toDTO(astahPartition));
        }

        return new ActivityDTO(
            NamedElementDTOAssembler.toDTO(astahActivity),
            activityDiagram,
            activityNodes,
            callBehaviorActions,
            flows,
            partitions);
    }
}
