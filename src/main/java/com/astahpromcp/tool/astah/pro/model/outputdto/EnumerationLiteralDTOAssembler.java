package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.change_vision.jude.api.inf.model.IEnumerationLiteral;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumerationLiteralDTOAssembler {
    public static EnumerationLiteralDTO toDTO(@NonNull IEnumerationLiteral astahEnumerationLiteral) throws Exception {
        
        return new EnumerationLiteralDTO(
            NamedElementDTOAssembler.toDTO(astahEnumerationLiteral),
            astahEnumerationLiteral.getValue());
    }
}
