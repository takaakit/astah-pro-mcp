package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IMultiplicityRange;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;

public class AttributeDTOAssembler {
    public static AttributeDTO toDTO(@NonNull IAttribute astahAttribute) throws Exception {
        
        return new AttributeDTO(
            NamedElementDTOAssembler.toDTO(astahAttribute),
            astahAttribute.isAggregate(),
            astahAttribute.isChangeable(),
            astahAttribute.isComposite(),
            astahAttribute.isDerived(),
            astahAttribute.isStatic(),
            astahAttribute.getInitialValue(),
            astahAttribute.getMultiplicityRangeString(),
            astahAttribute.getNavigability(),
            NameIdTypeDTOAssembler.toDTO(astahAttribute.getType()),
            astahAttribute.getTypeExpression());
    }
}
