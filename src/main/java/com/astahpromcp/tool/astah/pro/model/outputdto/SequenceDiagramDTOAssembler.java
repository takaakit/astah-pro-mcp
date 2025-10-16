package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SequenceDiagramDTOAssembler {
    public static SequenceDiagramDTO toDTO(@NonNull ISequenceDiagram astahSequenceDiagram) throws Exception {

        NameIdTypeDTO interaction;
        if (astahSequenceDiagram.getInteraction() != null) {
            interaction = NameIdTypeDTOAssembler.toDTO(astahSequenceDiagram.getInteraction());
        } else {
            interaction = NameIdTypeDTO.empty();
        }

        return new SequenceDiagramDTO(
            DiagramDTOAssembler.toDTO(astahSequenceDiagram),
            interaction);
    }
}
