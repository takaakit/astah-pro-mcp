package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.IERDatatype;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDatatypeDTO;

public class ERDatatypeDTOAssembler {
    public static ERDatatypeDTO toDTO(@NonNull IERDatatype astahERDatatype) throws Exception {
        
        return new ERDatatypeDTO(
            NamedElementDTOAssembler.toDTO(astahERDatatype),
            astahERDatatype.getDefaultLengthPrecision(),
            astahERDatatype.getDefinition(),
            astahERDatatype.getLengthConstraint(),
            astahERDatatype.getPrecisionConstraint());
    }
}
