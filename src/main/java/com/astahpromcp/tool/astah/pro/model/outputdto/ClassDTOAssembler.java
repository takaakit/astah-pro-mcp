package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ClassDTOAssembler {
    public static ClassDTO toDTO(@NonNull IClass astahClass) throws Exception {

        List<AttributeDTO> attributeDTOs = new ArrayList<>();
        List<AssociationDTO> associationDTOs = new ArrayList<>();
        for (IAttribute astahAttribute : astahClass.getAttributes()) {
            if (astahAttribute.getAssociation() != null) {
                associationDTOs.add(AssociationDTOAssembler.toDTO(astahAttribute.getAssociation()));
            } else {
                attributeDTOs.add(AttributeDTOAssembler.toDTO(astahAttribute));
            }
        }

        List<OperationDTO> operationDTOs = new ArrayList<>();
        for (IOperation astahOperation : astahClass.getOperations()) {
            operationDTOs.add(OperationDTOAssembler.toDTO(astahOperation));
        }

        List<GeneralizationDTO> generalizationDTOs = new ArrayList<>();
        for (IGeneralization astahGeneralization : astahClass.getGeneralizations()) {
            generalizationDTOs.add(GeneralizationDTOAssembler.toDTO(astahGeneralization));
        }

        List<GeneralizationDTO> specializationDTOs = new ArrayList<>();
        for (IGeneralization astahGeneralization : astahClass.getSpecializations()) {
            specializationDTOs.add(GeneralizationDTOAssembler.toDTO(astahGeneralization));
        }

        List<NameIdTypeDTO> nestedClassDTOs = new ArrayList<>();
        for (IClass astahNestedClass : astahClass.getNestedClasses()) {
            nestedClassDTOs.add(NameIdTypeDTOAssembler.toDTO(astahNestedClass));
        }

        List<NameIdTypeDTO> templateParameterDTOs = new ArrayList<>();
        for (IClassifierTemplateParameter astahTemplateParameter : astahClass.getTemplateParameters()) {
            templateParameterDTOs.add(NameIdTypeDTOAssembler.toDTO(astahTemplateParameter.getType()));
        }

        return new ClassDTO(
            NamedElementDTOAssembler.toDTO(astahClass),
            attributeDTOs,
            operationDTOs,
            associationDTOs,
            generalizationDTOs,
            specializationDTOs,
            nestedClassDTOs,
            templateParameterDTOs,
            astahClass.isAbstract(),
            astahClass.isActive(),
            astahClass.isLeaf());
    }
}
