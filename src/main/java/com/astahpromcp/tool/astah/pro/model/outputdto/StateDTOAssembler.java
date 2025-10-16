package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTOAssembler;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.model.IVertex;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StateDTOAssembler {
    public static StateDTO toDTO(@NonNull IState astahState) throws Exception {
        
        // Vertex information
        VertexDTO vertex = VertexDTOAssembler.toDTO(astahState);
        
        // SubmachineState
        NameIdTypeDTO submachineState;
        if (astahState.getSubmachine() != null) {
            submachineState = NameIdTypeDTOAssembler.toDTO(astahState.getSubmachine());
        } else {
            submachineState = NameIdTypeDTO.empty();
        }
        
        // Rectangles of region
        List<RectangleDTO> regionRectangles = new ArrayList<>();
        for (int i = 0; i < astahState.getRegionSize(); i++) {
            regionRectangles.add(RectangleDTOAssembler.toDTO(astahState.getRegionRectangle(i)));
        }
        
        // Internal Transitions
        List<NameIdTypeDTO> internalTransitions = new ArrayList<>();
        for (ITransition transition : astahState.getInternalTransitions()) {
            internalTransitions.add(NameIdTypeDTOAssembler.toDTO(transition));
        }
        
        // SubVertexes
        List<NameIdTypeDTO> subVertexes = new ArrayList<>();
        for (IVertex subVertex : astahState.getSubvertexes()) {
            subVertexes.add(NameIdTypeDTOAssembler.toDTO(subVertex));
        }
        
        return new StateDTO(
            vertex,
            astahState.getEntry(),
            astahState.getDoActivity(),
            astahState.getExit(),
            astahState.isSubmachineState(),
            submachineState,
            regionRectangles,
            internalTransitions,
            subVertexes);
    }
}
