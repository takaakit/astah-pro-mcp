package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.ITaggedValue;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import lombok.NonNull;

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
        
        List<String> correspondingPresentationIds = new ArrayList<>();
        for (IPresentation presentation : astahElement.getPresentations()) {
            correspondingPresentationIds.add(presentation.getID());
        }

        return new ElementDTO(
                astahElement.getId(),
                astahElement.getTypeModifier(),
                stereotypes,
                taggedValues,
                correspondingPresentationIds);
    }
}
