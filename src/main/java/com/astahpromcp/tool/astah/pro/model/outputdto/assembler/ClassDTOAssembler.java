package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.AssociationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.GeneralizationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PortDTO;
import com.change_vision.jude.api.inf.model.*;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassDTOAssembler {
    public static ClassDTO toDTO(@NonNull IClass astahClass) throws Exception {
        
        List<AttributeDTO> attributeDTOs = new ArrayList<>();
        List<AssociationDTO> associationDTOs = new ArrayList<>();
        // Both ends of a self-association are attributes of this class, so the association is reached twice; the second time adds nothing.
        Set<String> addedAssociationIds = new HashSet<>();
        for (IAttribute astahAttribute : astahClass.getAttributes()) {
            IAssociation astahAssociation = astahAttribute.getAssociation();
            if (astahAssociation != null) {
                if (addedAssociationIds.add(astahAssociation.getId())) {
                    associationDTOs.add(AssociationDTOAssembler.toDTO(astahAssociation));
                }
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
            templateParameterDTOs.add(NameIdTypeDTOAssembler.toDTO(astahTemplateParameter));
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
            astahClass.isAbstract(),
            astahClass.isActive(),
            astahClass.isLeaf());
    }
}
