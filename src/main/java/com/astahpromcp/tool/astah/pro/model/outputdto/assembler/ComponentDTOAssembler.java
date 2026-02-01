package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.model.outputdto.ComponentDTO;
import com.change_vision.jude.api.inf.model.IComponent;
import lombok.NonNull;

public class ComponentDTOAssembler {
    public static ComponentDTO toDTO(@NonNull IComponent astahComponent) throws Exception {
        return new ComponentDTO(ClassDTOAssembler.toDTO(astahComponent));
    }
}
