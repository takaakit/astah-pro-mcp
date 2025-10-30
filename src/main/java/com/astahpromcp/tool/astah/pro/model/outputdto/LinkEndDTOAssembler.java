package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.change_vision.jude.api.inf.model.ILinkEnd;
import lombok.NonNull;

public class LinkEndDTOAssembler {
    public static LinkEndDTO toDTO(@NonNull ILinkEnd astahLinkEnd) throws Exception {
        
        return new LinkEndDTO(
                NamedElementDTOAssembler.toDTO(astahLinkEnd),
                astahLinkEnd.isAggregate(),
                astahLinkEnd.isComposite(),
                "Navigable".equals(astahLinkEnd.getNavigability()));
    }
}
