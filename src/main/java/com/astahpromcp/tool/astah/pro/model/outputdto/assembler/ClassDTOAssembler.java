package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.*;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.AssociationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.GeneralizationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PortDTO;

import java.util.ArrayList;
import java.util.List;

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

        List<PortDTO> portDTOs = new ArrayList<>();
        for (IPort port : astahClass.getPorts()) {
            portDTOs.add(PortDTOAssembler.toDTO(port));
        }

        List<NameIdTypeDTO> nestedClassDTOs = new ArrayList<>();
        for (IClass astahNestedClass : astahClass.getNestedClasses()) {
            nestedClassDTOs.add(NameIdTypeDTOAssembler.toDTO(astahNestedClass));
        }

        List<NameIdTypeDTO> templateParameterDTOs = new ArrayList<>();
        for (IClassifierTemplateParameter astahTemplateParameter : astahClass.getTemplateParameters()) {
            templateParameterDTOs.add(NameIdTypeDTOAssembler.toDTO(astahTemplateParameter.getType()));
        }

        List<NameIdTypeDTO> ownedDiagramDTOs = new ArrayList<>();
        for (IDiagram astahDiagram : astahClass.getOwnedDiagrams()) {
            ownedDiagramDTOs.add(NameIdTypeDTOAssembler.toDTO(astahDiagram));
        }

        return new ClassDTO(
            NamedElementDTOAssembler.toDTO(astahClass),
            attributeDTOs,
            operationDTOs,
            associationDTOs,
            generalizationDTOs,
            specializationDTOs,
            portDTOs,
            nestedClassDTOs,
            templateParameterDTOs,
            ownedDiagramDTOs,
            astahClass.isAbstract(),
            astahClass.isActive(),
            astahClass.isLeaf());
    }
}
