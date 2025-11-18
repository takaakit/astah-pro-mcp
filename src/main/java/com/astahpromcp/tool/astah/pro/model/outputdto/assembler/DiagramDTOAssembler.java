package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;

import java.util.ArrayList;
import java.util.List;

public class DiagramDTOAssembler {
    public static DiagramDTO toDTO(@NonNull IDiagram astahDiagram) throws Exception {

        List<String> containedPresentationIds = new ArrayList<>();
        for (IPresentation presentation : astahDiagram.getPresentations()) {
            containedPresentationIds.add(presentation.getID());
        }

        return new DiagramDTO(
            NamedElementDTOAssembler.toDTO(astahDiagram),
            containedPresentationIds);
    }
}
