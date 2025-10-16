package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IUsage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UsageDTOAssembler {
    public static UsageDTO toDTO(@NonNull IUsage astahUsage) throws Exception {

        return new UsageDTO(
            NamedElementDTOAssembler.toDTO(astahUsage),
            NameIdTypeDTOAssembler.toDTO(astahUsage.getClient()),
            NameIdTypeDTOAssembler.toDTO(astahUsage.getSupplier()));
    }
}
