package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IMultiplicityRange;
import lombok.NonNull;

public class AttributeDTOAssembler {
    public static AttributeDTO toDTO(@NonNull IAttribute astahAttribute) throws Exception {

        // Provide a default when the multiplicity array is empty
        int upper = IMultiplicityRange.UNDEFINED;
        int lower = IMultiplicityRange.UNDEFINED;
        if (astahAttribute.getMultiplicity() != null && astahAttribute.getMultiplicity().length > 0) {
            upper = astahAttribute.getMultiplicity()[0].getUpper();
            lower = astahAttribute.getMultiplicity()[0].getLower();
        }

        return new AttributeDTO(
            NamedElementDTOAssembler.toDTO(astahAttribute),
            astahAttribute.isAggregate(),
            astahAttribute.isChangeable(),
            astahAttribute.isComposite(),
            astahAttribute.isDerived(),
            astahAttribute.isEnable(),
            astahAttribute.isStatic(),
            astahAttribute.getInitialValue(),
            upper,
            lower,
            astahAttribute.getNavigability(),
            NameIdTypeDTOAssembler.toDTO(astahAttribute.getType()),
            astahAttribute.getTypeExpression());
    }
}
