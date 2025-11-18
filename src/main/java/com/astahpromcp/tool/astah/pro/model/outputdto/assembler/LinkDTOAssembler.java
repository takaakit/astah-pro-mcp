package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkDTO;

import java.util.ArrayList;
import java.util.List;

public class LinkDTOAssembler {
    public static LinkDTO toDTO(@NonNull ILink astahLink) throws Exception {

        List<NameIdTypeDTO> linkEndDTOs = new ArrayList<>();
        for (ILinkEnd astahLinkEnd : astahLink.getMemberEnds()) {
            linkEndDTOs.add(NameIdTypeDTOAssembler.toDTO(astahLinkEnd));
        }

        return new LinkDTO(
            NamedElementDTOAssembler.toDTO(astahLink),
            linkEndDTOs);
    }
}
