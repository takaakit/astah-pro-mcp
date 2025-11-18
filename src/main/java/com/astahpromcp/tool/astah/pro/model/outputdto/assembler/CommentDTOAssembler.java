package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IComment;
import com.change_vision.jude.api.inf.model.IElement;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.CommentDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;

import java.util.ArrayList;
import java.util.List;

public class CommentDTOAssembler {
    public static CommentDTO toDTO(@NonNull IComment astahComment) throws Exception {

        List<ElementDTO> annotatedElementDTOs = new ArrayList<>();
        for (IElement astahElement : astahComment.getAnnotatedElements()) {
            annotatedElementDTOs.add(ElementDTOAssembler.toDTO(astahElement));
        }

        return new CommentDTO(
            NamedElementDTOAssembler.toDTO(astahComment),
            astahComment.getBody(),
            annotatedElementDTOs);
    }
}
