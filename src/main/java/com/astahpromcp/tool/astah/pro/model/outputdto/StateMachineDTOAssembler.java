package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IStateMachine;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class StateMachineDTOAssembler {
    public static StateMachineDTO toDTO(@NonNull IStateMachine astahStateMachine) throws Exception {

        // StateMachine diagram
        NameIdTypeDTO stateMachineDiagram;
        if (astahStateMachine.getStateMachineDiagram() != null) {
            stateMachineDiagram = NameIdTypeDTOAssembler.toDTO(astahStateMachine.getStateMachineDiagram());
        } else {
            stateMachineDiagram = NameIdTypeDTO.empty();
        }
        
        // States
        List<NameIdTypeDTO> states = new ArrayList<>();
        for (INamedElement state : astahStateMachine.getStates()) {
            states.add(NameIdTypeDTOAssembler.toDTO(state));
        }
        
        // Transitions
        List<NameIdTypeDTO> transitions = new ArrayList<>();
        for (INamedElement transition : astahStateMachine.getTransitions()) {
            transitions.add(NameIdTypeDTOAssembler.toDTO(transition));
        }
        
        // Vertexes
        List<NameIdTypeDTO> vertexes = new ArrayList<>();
        for (INamedElement vertex : astahStateMachine.getVertexes()) {
            vertexes.add(NameIdTypeDTOAssembler.toDTO(vertex));
        }
        
        return new StateMachineDTO(
            NamedElementDTOAssembler.toDTO(astahStateMachine),
            stateMachineDiagram,
            states,
            transitions,
            vertexes);
    }
}
