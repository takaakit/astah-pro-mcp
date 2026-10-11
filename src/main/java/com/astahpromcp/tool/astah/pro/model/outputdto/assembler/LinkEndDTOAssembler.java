package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.ILinkEnd;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkEndDTO;

import java.util.Objects;

public class LinkEndDTOAssembler {
    public static LinkEndDTO toDTO(@NonNull ILinkEnd astahLinkEnd) throws Exception {

        return new LinkEndDTO(
                NamedElementDTOAssembler.toDTO(astahLinkEnd),
                astahLinkEnd.isAggregate(),
                astahLinkEnd.isComposite(),
                Objects.requireNonNullElse(astahLinkEnd.getNavigability(), ""));
    }
}
