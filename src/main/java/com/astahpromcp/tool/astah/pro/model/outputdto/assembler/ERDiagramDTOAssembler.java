package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IERDiagram;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDiagramDTO;

public class ERDiagramDTOAssembler {
    public static ERDiagramDTO toDTO(@NonNull IERDiagram astahERDiagram) throws Exception {
        
        return new ERDiagramDTO(
            DiagramDTOAssembler.toDTO(astahERDiagram),
            astahERDiagram.getInitialDisplayLevel(),
            astahERDiagram.getModelType(),
            astahERDiagram.getNotation(),
            astahERDiagram.isAlignAttributeItems());
    }
}
