package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.change_vision.jude.api.inf.model.IControlNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ControlNodeDTOAssembler {
    public static ControlNodeDTO toDTO(@NonNull IControlNode astahControlNode) throws Exception {
        return new ControlNodeDTO(
            ActivityNodeDTOAssembler.toDTO(astahControlNode),
            astahControlNode.isConnector(),
            astahControlNode.isDecisionMergeNode(),
            astahControlNode.isFinalNode(),
            astahControlNode.isFlowFinalNode(),
            astahControlNode.isForkNode(),
            astahControlNode.isInitialNode(),
            astahControlNode.isJoinNode(),
            astahControlNode.isMergeNode());
    }
}
