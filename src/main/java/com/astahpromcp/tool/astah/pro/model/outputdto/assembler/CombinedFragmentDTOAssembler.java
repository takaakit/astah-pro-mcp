package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.CombinedFragmentKind;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.CombinedFragmentDTO;

import java.util.ArrayList;
import java.util.List;

public class CombinedFragmentDTOAssembler {
    public static CombinedFragmentDTO toDTO(@NonNull ICombinedFragment astahCombinedFragment) throws Exception {

        CombinedFragmentKind kind;
        if (astahCombinedFragment.isAlt()) {
            kind = CombinedFragmentKind.alt;
        } else if (astahCombinedFragment.isAssert()) {
            kind = CombinedFragmentKind.assert_;
        } else if (astahCombinedFragment.isBreak()) {
            kind = CombinedFragmentKind.break_;
        } else if (astahCombinedFragment.isConsider()) {
            kind = CombinedFragmentKind.consider;
        } else if (astahCombinedFragment.isCritical()) {
            kind = CombinedFragmentKind.critical;
        } else if (astahCombinedFragment.isIgnore()) {
            kind = CombinedFragmentKind.ignore;
        } else if (astahCombinedFragment.isLoop()) {
            kind = CombinedFragmentKind.loop;
        } else if (astahCombinedFragment.isNeg()) {
            kind = CombinedFragmentKind.neg;
        } else if (astahCombinedFragment.isOpt()) {
            kind = CombinedFragmentKind.opt;
        } else if (astahCombinedFragment.isPar()) {
            kind = CombinedFragmentKind.par;
        } else if (astahCombinedFragment.isSeq()) {
            kind = CombinedFragmentKind.seq;
        } else if (astahCombinedFragment.isStrict()) {
            kind = CombinedFragmentKind.strict;
        } else {
            throw new Exception("Invalid combined fragment kind");
        }

        List<NameIdTypeDTO> interactionOperands = new ArrayList<>();
        for (IInteractionOperand interactionOperand : astahCombinedFragment.getInteractionOperands()) {
            interactionOperands.add(NameIdTypeDTOAssembler.toDTO(interactionOperand));
        }

        return new CombinedFragmentDTO(
            NamedElementDTOAssembler.toDTO(astahCombinedFragment),
            kind.toString(),
            interactionOperands);
    }
}
