package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.change_vision.jude.api.inf.model.IComment;
import com.change_vision.jude.api.inf.model.IElement;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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
