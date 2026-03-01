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
            kind = CombinedFragmentKind.ALT;
        } else if (astahCombinedFragment.isAssert()) {
            kind = CombinedFragmentKind.ASSERT;
        } else if (astahCombinedFragment.isBreak()) {
            kind = CombinedFragmentKind.BREAK;
        } else if (astahCombinedFragment.isConsider()) {
            kind = CombinedFragmentKind.CONSIDER;
        } else if (astahCombinedFragment.isCritical()) {
            kind = CombinedFragmentKind.CRITICAL;
        } else if (astahCombinedFragment.isIgnore()) {
            kind = CombinedFragmentKind.IGNORE;
        } else if (astahCombinedFragment.isLoop()) {
            kind = CombinedFragmentKind.LOOP;
        } else if (astahCombinedFragment.isNeg()) {
            kind = CombinedFragmentKind.NEG;
        } else if (astahCombinedFragment.isOpt()) {
            kind = CombinedFragmentKind.OPT;
        } else if (astahCombinedFragment.isPar()) {
            kind = CombinedFragmentKind.PAR;
        } else if (astahCombinedFragment.isSeq()) {
            kind = CombinedFragmentKind.SEQ;
        } else if (astahCombinedFragment.isStrict()) {
            kind = CombinedFragmentKind.STRICT;
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
