package com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.RectangleDTOAssembler;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;

import java.util.ArrayList;
import java.util.List;

public class NodePresentationDTOAssembler {
    public static NodePresentationDTO toDTO(@NonNull INodePresentation astahNodePresentation) throws Exception {

        List<String> linkIds = new ArrayList<>();
        for (ILinkPresentation linkPresentation : astahNodePresentation.getLinks()) {
            linkIds.add(linkPresentation.getID());
        }

        return new NodePresentationDTO(
            PresentationDTOAssembler.toDTO(astahNodePresentation),
            linkIds,
            RectangleDTOAssembler.toDTO(astahNodePresentation.getRectangle())
        );
    }
}
