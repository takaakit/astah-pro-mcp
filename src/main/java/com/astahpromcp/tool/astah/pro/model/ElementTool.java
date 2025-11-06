package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeListDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithStereotypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithTaggedValueDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithTypeModifierDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IElement.html
@Slf4j
public class ElementTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ElementTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
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
                ToolSupport.definition(
                        "get_dgms_of_elem",
                        "Returns all diagrams in which the presentations of the specified element (specified by ID) are displayed. Furthermore, if the base class or base classifier of an InstanceSpecification, Lifeline, or ObjectNode is the specified element, the return value includes diagrams in which the presentations of those InstanceSpecifications, Lifelines, or ObjectNodes are displayed. It also includes diagrams that are located under (i.e., owned by) the specified element.",
                        this::getDiagramsOfElement,
                        IdDTO.class,
                        NameIdTypeListDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "add_stereotype",
                        "Add a stereotype (specified by string) to the specified element (specified by ID), and return the element information after it is edited.",
                        this::addStereotype,
                        ElementWithStereotypeDTO.class,
                        ElementDTO.class),

                ToolSupport.definition(
                        "remove_stereotype",
                        "Remove the specified stereotype (specified by string) from the specified element (specified by ID), and return the element information after it is edited.",
                        this::removeStereotype,
                        ElementWithStereotypeDTO.class,
                        ElementDTO.class),

                ToolSupport.definition(
                        "set_type_modifier",
                        "Set a type modifier of the specified element (specified by ID), and return the element information after it is edited. The type modifier is a symbol appended to the type name, such as * (C++ pointer) and & (C++ reference).",
                        this::setTypeModifier,
                        ElementWithTypeModifierDTO.class,
                        ElementDTO.class),

                ToolSupport.definition(
                        "change_tagged_value",
                        "Change the value of the specified key (specified by string) of the specified element (specified by ID), and return the element information after it is changed.",
                        this::changeTaggedValue,
                        ElementWithTaggedValueDTO.class,
                        ElementDTO.class)
        );
    }

    private ElementDTO addStereotype(McpSyncServerExchange exchange, ElementWithStereotypeDTO param) throws Exception {
        log.debug("Add stereotype to element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        try {
            transactionManager.beginTransaction();
            astahElement.addStereotype(param.stereotype());
            transactionManager.endTransaction();

            return ElementDTOAssembler.toDTO(astahElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ElementDTO removeStereotype(McpSyncServerExchange exchange, ElementWithStereotypeDTO param) throws Exception {
        log.debug("Remove stereotype from element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        try {
            transactionManager.beginTransaction();
            astahElement.removeStereotype(param.stereotype());
            transactionManager.endTransaction();

            return ElementDTOAssembler.toDTO(astahElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ElementDTO setTypeModifier(McpSyncServerExchange exchange, ElementWithTypeModifierDTO param) throws Exception {
        log.debug("Set type modifier of element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        try {
            transactionManager.beginTransaction();
            astahElement.setTypeModifier(param.typeModifier());
            transactionManager.endTransaction();

            return ElementDTOAssembler.toDTO(astahElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ElementDTO changeTaggedValue(McpSyncServerExchange exchange, ElementWithTaggedValueDTO param) throws Exception {
        log.debug("Change value of tagged value: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());

        try {
            for (ITaggedValue taggedValue : astahElement.getTaggedValues()) {
                if (taggedValue.getKey().equals(param.targetKey())) {
                    transactionManager.beginTransaction();
                    taggedValue.setValue(param.value());
                    transactionManager.endTransaction();
                    break;
                }
            }

            return ElementDTOAssembler.toDTO(astahElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NameIdTypeListDTO getDiagramsOfElement(McpSyncServerExchange exchange, IdDTO param) throws Exception {
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
