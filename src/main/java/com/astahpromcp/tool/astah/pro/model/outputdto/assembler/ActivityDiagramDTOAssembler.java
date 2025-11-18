package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDiagramDTO;

public class ActivityDiagramDTOAssembler {
    public static ActivityDiagramDTO toDTO(@NonNull IActivityDiagram astahActivityDiagram) throws Exception {

        NameIdTypeDTO activity;
        if (astahActivityDiagram.getActivity() != null) {
            activity = NameIdTypeDTOAssembler.toDTO(astahActivityDiagram.getActivity());
        } else {
            activity = NameIdTypeDTO.empty();
        }

        return new ActivityDiagramDTO(
            DiagramDTOAssembler.toDTO(astahActivityDiagram),
            activity);
    }
}
