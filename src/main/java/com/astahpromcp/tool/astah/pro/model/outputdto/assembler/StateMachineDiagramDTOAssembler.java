package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateMachineDiagramDTO;

public class StateMachineDiagramDTOAssembler {
    public static StateMachineDiagramDTO toDTO(@NonNull IStateMachineDiagram astahStateMachineDiagram) throws Exception {

        // StateMachine
        NameIdTypeDTO stateMachine;
        if (astahStateMachineDiagram.getStateMachine() != null) {
            stateMachine = NameIdTypeDTOAssembler.toDTO(astahStateMachineDiagram.getStateMachine());
        } else {
            stateMachine = NameIdTypeDTO.empty();
        }
        
        return new StateMachineDiagramDTO(
            NamedElementDTOAssembler.toDTO(astahStateMachineDiagram),
            stateMachine);
    }
}
