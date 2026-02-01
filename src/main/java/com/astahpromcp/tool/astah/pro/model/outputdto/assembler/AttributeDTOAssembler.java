package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IMultiplicityRange;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;

public class AttributeDTOAssembler {
    public static AttributeDTO toDTO(@NonNull IAttribute astahAttribute) throws Exception {

        // Note: For the Port element, an exception occurs when retrieving the following values, so the access must be wrapped in a try-catch block.
        
        boolean isAggregate;
        try {
            isAggregate = astahAttribute.isAggregate();
        } catch (Exception e) {
            isAggregate = false;
        }

        boolean isChangeable;
        try {
            isChangeable = astahAttribute.isChangeable();
        } catch (Exception e) {
            isChangeable = false;
        }

        NameIdTypeDTO type;
        try {
            type = NameIdTypeDTOAssembler.toDTO(astahAttribute.getType());
        } catch (Exception e) {
            type = NameIdTypeDTO.empty();
        }

        boolean isComposite;
        try {
            isComposite = astahAttribute.isComposite();
        } catch (Exception e) {
            isComposite = false;
        }

        boolean isDerived;
        try {
            isDerived = astahAttribute.isDerived();
        } catch (Exception e) {
            isDerived = false;
        }

        boolean isStatic;
        try {
            isStatic = astahAttribute.isStatic();
        } catch (Exception e) {
            isStatic = false;
        }

        String initialValue;
        try {
            initialValue = astahAttribute.getInitialValue();
        } catch (Exception e) {
            initialValue = "";
        }

        String multiplicity;
        try {
            multiplicity = astahAttribute.getMultiplicityRangeString();
        } catch (Exception e) {
            multiplicity = "";
        }

        String navigability;
        try {
            navigability = astahAttribute.getNavigability();
        } catch (Exception e) {
            navigability = "";
        }

        String typeExpression;
        try {
            typeExpression = astahAttribute.getTypeExpression();
        } catch (Exception e) {
            typeExpression = "";
        }

        return new AttributeDTO(
            NamedElementDTOAssembler.toDTO(astahAttribute),
            isAggregate,
            isChangeable,
            isComposite,
            isDerived,
            isStatic,
            initialValue,
            multiplicity,
            navigability,
            type,
            typeExpression);
    }
}
