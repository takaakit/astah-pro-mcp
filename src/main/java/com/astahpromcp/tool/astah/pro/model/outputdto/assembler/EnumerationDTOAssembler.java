package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IEnumeration;
import com.change_vision.jude.api.inf.model.IEnumerationLiteral;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.EnumerationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EnumerationLiteralDTO;

import java.util.ArrayList;
import java.util.List;

public class EnumerationDTOAssembler {
    public static EnumerationDTO toDTO(@NonNull IEnumeration astahEnumeration) throws Exception {
        
        List<EnumerationLiteralDTO> enumerationLiteralDTOs = new ArrayList<>();
        for (IEnumerationLiteral astahEnumerationLiteral : astahEnumeration.getEnumerationLiterals()) {
            enumerationLiteralDTOs.add(EnumerationLiteralDTOAssembler.toDTO(astahEnumerationLiteral));
        }

        return new EnumerationDTO(
            ClassDTOAssembler.toDTO(astahEnumeration),
            enumerationLiteralDTOs);
    }
    
}
