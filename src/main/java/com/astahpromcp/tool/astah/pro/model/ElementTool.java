package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithStereotypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithTaggedValueDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithTypeModifierDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ElementDTOAssembler;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IElement.html
@Slf4j
public class ElementTool extends AstahToolProvider {

    private static final String ASTAH_INTERNAL_TAGGED_VALUE_KEY_PREFIX = "jude.";

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ElementTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_dgms_of_element",
                "Returns all diagrams in which the presentations of the specified element (specified by ID) are displayed. Furthermore, if the base class or base classifier of an InstanceSpecification, Lifeline, or ObjectNode is the specified element, the return value includes diagrams in which the presentations of those InstanceSpecifications, Lifelines, or ObjectNodes are displayed. It also includes diagrams that are located under (i.e., owned by) the specified element.",
                this::getDiagramsOfElement,
                IdDTO.class,
                NameIdTypeListDTO.class),


            ToolSupport.toolDefinitionReturningDto(
                "add_stereotype",
                "Add a stereotype (specified by string) to the specified element (specified by ID), and return the element after it is edited.",
                this::addStereotype,
                ElementWithStereotypeDTO.class,
                ElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "remove_stereotype",
                "Remove the specified stereotype (specified by string) from the specified element (specified by ID), and return the element after it is edited.",
                this::removeStereotype,
                ElementWithStereotypeDTO.class,
                ElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_type_modifier",
                "Set a type modifier of the specified element (specified by ID), and return the element after it is edited. The type modifier is a symbol appended to the type name, such as * (C++ pointer) and & (C++ reference).",
                this::setTypeModifier,
                ElementWithTypeModifierDTO.class,
                ElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "change_tagged_val",
                "Change the value of the specified key (specified by string) of the specified element (specified by ID), and return the element after it is changed. The key is case-sensitive and must be the key of a tagged value that the element already has; otherwise an error is returned.",
                this::changeTaggedValue,
                ElementWithTaggedValueDTO.class,
                ElementDTO.class)
        );
    }

    private ElementDTO addStereotype(ElementWithStereotypeDTO param) throws Exception {
        log.debug("Add stereotype to element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        txnAstah.run( () -> {
            astahElement.addStereotype(param.stereotype());
        });

        return ElementDTOAssembler.toDTO(astahElement);
    }

    private ElementDTO removeStereotype(ElementWithStereotypeDTO param) throws Exception {
        log.debug("Remove stereotype from element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        txnAstah.run( () -> {
            astahElement.removeStereotype(param.stereotype());
        });

        return ElementDTOAssembler.toDTO(astahElement);
    }

    private ElementDTO setTypeModifier(ElementWithTypeModifierDTO param) throws Exception {
        log.debug("Set type modifier of element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        txnAstah.run( () -> {
            astahElement.setTypeModifier(param.typeModifier());
        });

        return ElementDTOAssembler.toDTO(astahElement);
    }

    private ElementDTO changeTaggedValue(ElementWithTaggedValueDTO param) throws Exception {
        log.debug("Change value of tagged value: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());

        ITaggedValue astahTaggedValue = findTaggedValue(astahElement, param.targetKey());
        if (astahTaggedValue == null) {
            throw new IllegalArgumentException(describeMissingTaggedValueKey(astahElement, param.targetKey()));
        }

        txnAstah.run( () -> {
            astahTaggedValue.setValue(param.value());
        });

        return ElementDTOAssembler.toDTO(astahElement);
    }

    private ITaggedValue findTaggedValue(IElement astahElement, String key) {

        for (ITaggedValue taggedValue : astahElement.getTaggedValues()) {
            if (taggedValue.getKey().equals(key)) {
                return taggedValue;
            }
        }

        return null;
    }

    private String describeMissingTaggedValueKey(IElement astahElement, String key) {

        List<String> listedKeys = new ArrayList<>();
        int internalKeyCount = 0;
        String caseInsensitiveMatch = null;

        for (ITaggedValue taggedValue : astahElement.getTaggedValues()) {
            String existingKey = taggedValue.getKey();

            // Keys managed by Astah are left out of the list, because an element can carry many of them and they are not meant to be changed by this tool.
            if (existingKey.startsWith(ASTAH_INTERNAL_TAGGED_VALUE_KEY_PREFIX)) {
                internalKeyCount++;
                continue;
            }

            listedKeys.add(existingKey);
            if (caseInsensitiveMatch == null && existingKey.equalsIgnoreCase(key)) {
                caseInsensitiveMatch = existingKey;
            }
        }

        StringBuilder message = new StringBuilder(String.format(
            "Failed to change the tagged value because the element (ID: %s) has no tagged value with the key '%s'. Keys are case-sensitive.",
            astahElement.getId(), key));

        if (caseInsensitiveMatch != null) {
            message.append(String.format(" Did you mean '%s'?", caseInsensitiveMatch));
        }

        message.append(listedKeys.isEmpty()
            ? " Existing keys: (none)."
            : " Existing keys: " + listedKeys + ".");

        if (internalKeyCount > 0) {
            message.append(String.format(
                " (%d Astah-internal key(s) starting with '%s' are not listed.)",
                internalKeyCount, ASTAH_INTERNAL_TAGGED_VALUE_KEY_PREFIX));
        }

        message.append(" To add a new tagged value, use create_tagged_val.");

        return message.toString();
    }

    private NameIdTypeListDTO getDiagramsOfElement(IdDTO param) throws Exception {
        log.debug("Get diagrams of element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        Set<NameIdTypeDTO> diagrams = new LinkedHashSet<>();
        INamedElement[] astahNamedElements = projectAccessor.findElements(IDiagram.class);
        for (INamedElement astahNamedElement : astahNamedElements) {
            IDiagram astahDiagram = (IDiagram) astahNamedElement;

            for (IPresentation astahPresentation : astahDiagram.getPresentations()) {
                // Check if the model is the same as the specified element
                IElement astahModel = astahPresentation.getModel();
                if (astahModel != null
                    && astahModel.equals(astahElement)) {
                    diagrams.add(NameIdTypeDTOAssembler.toDTO(astahDiagram));
                }

                // Check if the model is an instance specification and the classifier is the same as the specified element
                if (astahModel instanceof IInstanceSpecification) {
                    IInstanceSpecification astahInstanceSpecification = (IInstanceSpecification) astahModel;
                    if (astahInstanceSpecification.getClassifier() != null
                        && astahInstanceSpecification.getClassifier().equals(astahElement)) {
                        diagrams.add(NameIdTypeDTOAssembler.toDTO(astahDiagram));
                    }
                }

                // Check if the model is a lifeline and the base is the same as the specified element
                if (astahModel instanceof ILifeline) {
                    ILifeline astahLifeline = (ILifeline) astahModel;
                    if (astahLifeline.getBase() != null
                        && astahLifeline.getBase().equals(astahElement)) {
                        diagrams.add(NameIdTypeDTOAssembler.toDTO(astahDiagram));
                    }
                }

                // Check if the model is a object node and the base is the same as the specified element
                if (astahModel instanceof IObjectNode) {
                    IObjectNode astahObjectNode = (IObjectNode) astahModel;
                    if (astahObjectNode.getBase() != null
                        && astahObjectNode.getBase().equals(astahElement)) {
                        diagrams.add(NameIdTypeDTOAssembler.toDTO(astahDiagram));
                    }
                }
            }

            // Check if the diagram is owned by the specified element
            IElement owner = astahDiagram.getOwner();
            while (owner != null) {
                if (owner.equals(astahElement)) {
                    diagrams.add(NameIdTypeDTOAssembler.toDTO(astahDiagram));
                    break;
                }
                owner = owner.getOwner();
            }
        }

        return new NameIdTypeListDTO(new ArrayList<>(diagrams));
    }
}
