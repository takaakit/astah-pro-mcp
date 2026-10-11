package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.SequenceDiagramLayout.DrawnContents;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionOperandDTO;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.INamedElement;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InteractionOperandDTOAssembler {
    public static InteractionOperandDTO toDTO(@NonNull IInteractionOperand astahInteractionOperand, @NonNull DrawnContents drawnContents) throws Exception {

        List<NameIdTypeDTO> lifelineDTOs = new ArrayList<>();
        for (ILifeline astahLifeline : astahInteractionOperand.getLifelines()) {
            lifelineDTOs.add(NameIdTypeDTOAssembler.toDTO(astahLifeline));
        }

        List<NameIdTypeDTO> messageDTOs = new ArrayList<>();
        for (IMessage astahMessage : astahInteractionOperand.getMessages()) {
            messageDTOs.add(NameIdTypeDTOAssembler.toDTO(astahMessage));
        }

        List<NameIdTypeDTO> drawnLifelineDTOs = new ArrayList<>();
        for (ILifeline astahLifeline : drawnContents.lifelines()) {
            drawnLifelineDTOs.add(NameIdTypeDTOAssembler.toDTO(astahLifeline));
        }

        List<NameIdTypeDTO> drawnMessageDTOs = new ArrayList<>();
        for (IMessage astahMessage : drawnContents.messages()) {
            drawnMessageDTOs.add(NameIdTypeDTOAssembler.toDTO(astahMessage));
        }

        // The model and the diagram list the same elements in different orders, so they are compared as sets.
        boolean matchesDiagram = ids(astahInteractionOperand.getLifelines()).equals(ids(drawnContents.lifelines().toArray(new INamedElement[0])))
            && ids(astahInteractionOperand.getMessages()).equals(ids(drawnContents.messages().toArray(new INamedElement[0])));

        return new InteractionOperandDTO(
            NamedElementDTOAssembler.toDTO(astahInteractionOperand),
            astahInteractionOperand.getGuard(),
            lifelineDTOs,
            messageDTOs,
            drawnLifelineDTOs,
            drawnMessageDTOs,
            drawnContents.height(),
            matchesDiagram);
    }

    private static Set<String> ids(INamedElement[] elements) {
        Set<String> ids = new HashSet<>();
        for (INamedElement element : elements) {
            ids.add(element.getId());
        }
        return ids;
    }
}
