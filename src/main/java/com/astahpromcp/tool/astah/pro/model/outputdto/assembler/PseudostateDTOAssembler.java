package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.PseudostateKind;
import com.change_vision.jude.api.inf.model.IPseudostate;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.PseudostateDTO;

public class PseudostateDTOAssembler {
    public static PseudostateDTO toDTO(@NonNull IPseudostate astahPseudostate) throws Exception {
        PseudostateKind pseudostateKind;
        if (astahPseudostate.isChoicePseudostate()) {
            pseudostateKind = PseudostateKind.CHOICE;
        } else if (astahPseudostate.isDeepHistoryPseudostate()) {
            pseudostateKind = PseudostateKind.DEEP_HISTORY;
        } else if (astahPseudostate.isEntryPointPseudostate()) {
            pseudostateKind = PseudostateKind.ENTRY_POINT;
        } else if (astahPseudostate.isExitPointPseudostate()) {
            pseudostateKind = PseudostateKind.EXIT_POINT;
        } else if (astahPseudostate.isForkPseudostate()) {
            pseudostateKind = PseudostateKind.FORK;
        } else if (astahPseudostate.isInitialPseudostate()) {
            pseudostateKind = PseudostateKind.INITIAL;
        } else if (astahPseudostate.isJoinPseudostate()) {
            pseudostateKind = PseudostateKind.JOIN;
        } else if (astahPseudostate.isJunctionPseudostate()) {
            pseudostateKind = PseudostateKind.JUNCTION;
        } else if (astahPseudostate.isShallowHistoryPseudostate()) {
            pseudostateKind = PseudostateKind.SHALLOW_HISTORY;
        } else if (astahPseudostate.isStubState()) {
            pseudostateKind = PseudostateKind.STUB;
        } else {
            pseudostateKind = PseudostateKind.INITIAL;
        }
        
        return new PseudostateDTO(
            VertexDTOAssembler.toDTO(astahPseudostate),
            pseudostateKind);
    }
}
