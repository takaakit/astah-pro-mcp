package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
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
public class ElementTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ElementTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create element tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_dgms_of_element",
                "Returns all diagrams in which the presentations of the specified element (specified by ID) are displayed. Furthermore, if the base class or base classifier of an InstanceSpecification, Lifeline, or ObjectNode is the specified element, the return value includes diagrams in which the presentations of those InstanceSpecifications, Lifelines, or ObjectNodes are displayed. It also includes diagrams that are located under (i.e., owned by) the specified element.",
                this::getDiagramsOfElement,
                IdDTO.class,
                NameIdTypeListDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
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
                "Change the value of the specified key (specified by string) of the specified element (specified by ID), and return the element after it is changed.",
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

        for (ITaggedValue taggedValue : astahElement.getTaggedValues()) {
            if (taggedValue.getKey().equals(param.targetKey())) {
                txnAstah.run( () -> {
                    taggedValue.setValue(param.value());
                });
                break;
            }
        }

        return ElementDTOAssembler.toDTO(astahElement);
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
