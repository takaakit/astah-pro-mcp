package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.LabelIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.LabelIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.MindMapDiagramDTO;
import com.change_vision.jude.api.inf.model.IMindMapDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MindMapDiagramDTOAssembler {
    public static MindMapDiagramDTO toDTO(@NonNull IMindMapDiagram astahMindMapDiagram) throws Exception {

        LabelIdTypeDTO rootTopic;
        if (astahMindMapDiagram.getRoot() != null) {
            rootTopic = LabelIdTypeDTOAssembler.toDTO(astahMindMapDiagram.getRoot());
        } else {
            rootTopic = LabelIdTypeDTO.empty();
        }

        List<LabelIdTypeDTO> floatingTopics = new ArrayList<>();
        for (INodePresentation presentation : astahMindMapDiagram.getFloatingTopics()) {
            floatingTopics.add(LabelIdTypeDTOAssembler.toDTO(presentation));
        }

        return new MindMapDiagramDTO(
            DiagramDTOAssembler.toDTO(astahMindMapDiagram),
            rootTopic,
            floatingTopics);
    }
}
