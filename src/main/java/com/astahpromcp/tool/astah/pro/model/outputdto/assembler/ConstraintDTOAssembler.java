package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.model.IElement;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ConstraintDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;

import java.util.ArrayList;
import java.util.List;

public class ConstraintDTOAssembler {
    public static ConstraintDTO toDTO(@NonNull IConstraint astahConstraint) throws Exception {

        List<ElementDTO> constrainedElements = new ArrayList<>();
        for (IElement astahElement : astahConstraint.getConstrainedElement()) {
            constrainedElements.add(ElementDTOAssembler.toDTO(astahElement));
        }

        return new ConstraintDTO(
            NamedElementDTOAssembler.toDTO(astahConstraint),
            constrainedElements,
            astahConstraint.getSpecification());
    }
}
