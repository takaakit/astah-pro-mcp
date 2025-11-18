package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.PseudostateKind;
import com.change_vision.jude.api.inf.model.IPseudostate;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.PseudostateDTO;

public class PseudostateDTOAssembler {
    public static PseudostateDTO toDTO(@NonNull IPseudostate astahPseudostate) throws Exception {
        PseudostateKind pseudostateKind;
        if (astahPseudostate.isChoicePseudostate()) {
            pseudostateKind = PseudostateKind.choice;
        } else if (astahPseudostate.isDeepHistoryPseudostate()) {
            pseudostateKind = PseudostateKind.deep_history;
        } else if (astahPseudostate.isEntryPointPseudostate()) {
            pseudostateKind = PseudostateKind.entry_point;
        } else if (astahPseudostate.isExitPointPseudostate()) {
            pseudostateKind = PseudostateKind.exit_point;
        } else if (astahPseudostate.isForkPseudostate()) {
            pseudostateKind = PseudostateKind.fork;
        } else if (astahPseudostate.isInitialPseudostate()) {
            pseudostateKind = PseudostateKind.initial;
        } else if (astahPseudostate.isJoinPseudostate()) {
            pseudostateKind = PseudostateKind.join;
        } else if (astahPseudostate.isJunctionPseudostate()) {
            pseudostateKind = PseudostateKind.junction;
        } else if (astahPseudostate.isShallowHistoryPseudostate()) {
            pseudostateKind = PseudostateKind.shallow_history;
        } else if (astahPseudostate.isStubState()) {
            pseudostateKind = PseudostateKind.stub;
        } else {
            pseudostateKind = PseudostateKind.initial;
        }
        
        return new PseudostateDTO(
            VertexDTOAssembler.toDTO(astahPseudostate),
            pseudostateKind);
    }
}
