package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ICommunicationDiagram;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.CommunicationDiagramDTO;

public class CommunicationDiagramDTOAssembler {
    public static CommunicationDiagramDTO toDTO(@NonNull ICommunicationDiagram astahCommunicationDiagram) throws Exception {

        NameIdTypeDTO interaction;
        if (astahCommunicationDiagram.getInteraction() != null) {
            interaction = NameIdTypeDTOAssembler.toDTO(astahCommunicationDiagram.getInteraction());
        } else {
            interaction = NameIdTypeDTO.empty();
        }

        return new CommunicationDiagramDTO(
            DiagramDTOAssembler.toDTO(astahCommunicationDiagram),
            interaction);
    }
}
