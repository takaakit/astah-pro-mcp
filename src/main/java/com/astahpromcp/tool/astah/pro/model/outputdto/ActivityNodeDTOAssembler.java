package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IActivityNode;
import com.change_vision.jude.api.inf.model.IFlow;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ActivityNodeDTOAssembler {
    public static ActivityNodeDTO toDTO(@NonNull IActivityNode astahActivityNode) throws Exception {

        List<NameIdTypeDTO> incomingFlows = new ArrayList<>();
        for (IFlow astahIncomingFlow : astahActivityNode.getIncomings()) {
            incomingFlows.add(NameIdTypeDTOAssembler.toDTO(astahIncomingFlow));
        }

        List<NameIdTypeDTO> outgoingFlows = new ArrayList<>();
        for (IFlow astahOutgoingFlow : astahActivityNode.getOutgoings()) {
            outgoingFlows.add(NameIdTypeDTOAssembler.toDTO(astahOutgoingFlow));
        }

        return new ActivityNodeDTO(
            NamedElementDTOAssembler.toDTO(astahActivityNode),
            incomingFlows,
            outgoingFlows);
    }
}
