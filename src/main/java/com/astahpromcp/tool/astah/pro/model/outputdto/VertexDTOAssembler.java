package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.model.IVertex;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class VertexDTOAssembler {
    public static VertexDTO toDTO(@NonNull IVertex astahVertex) throws Exception {

        List<NameIdTypeDTO> incomingTransitions = new ArrayList<>();
        for (ITransition transition : astahVertex.getIncomings()) {
            incomingTransitions.add(NameIdTypeDTOAssembler.toDTO(transition));
        }

        List<NameIdTypeDTO> outgoingTransitions = new ArrayList<>();
        for (ITransition transition : astahVertex.getOutgoings()) {
            outgoingTransitions.add(NameIdTypeDTOAssembler.toDTO(transition));
        }

        return new VertexDTO(
            NamedElementDTOAssembler.toDTO(astahVertex),
            incomingTransitions,
            outgoingTransitions);
    }
}
