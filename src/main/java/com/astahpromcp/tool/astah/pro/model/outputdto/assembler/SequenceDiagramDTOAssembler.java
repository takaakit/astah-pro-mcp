package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.SequenceDiagramDTO;

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
