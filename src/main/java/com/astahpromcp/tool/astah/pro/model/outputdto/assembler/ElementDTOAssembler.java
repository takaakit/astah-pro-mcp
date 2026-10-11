package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.ITaggedValue;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementDTOAssembler {
    public static ElementDTO toDTO(@NonNull IElement astahElement) throws Exception {

        List<String> stereotypes = new ArrayList<>();
        for (String stereotype : astahElement.getStereotypes()) {
            stereotypes.add(stereotype);
        }

        Map<String, String> taggedValues = new HashMap<>();
        for (ITaggedValue taggedValue : astahElement.getTaggedValues()) {
            taggedValues.put(taggedValue.getKey(), taggedValue.getValue());
        }
        
        // Only the presentations other tools can look up by ID are listed
        ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
        List<String> correspondingPresentationIds = new ArrayList<>();
        for (IPresentation presentation : AstahProToolSupport.getCorrespondingPresentations(astahElement)) {
            if (projectAccessor.getEntity(presentation.getID()) != null) {
                correspondingPresentationIds.add(presentation.getID());
            }
        }

        return new ElementDTO(
                astahElement.getId(),
                astahElement.getTypeModifier(),
                stereotypes,
                taggedValues,
                correspondingPresentationIds);
    }
}
