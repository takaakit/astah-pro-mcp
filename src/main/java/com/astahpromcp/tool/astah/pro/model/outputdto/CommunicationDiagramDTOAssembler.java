package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ICommunicationDiagram;
import lombok.NonNull;

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
