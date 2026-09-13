package com.astahpromcp.tool.astah.pro.view;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ElementDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementListDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IEntity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IProjectViewManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/view/IProjectViewManager.html
@Slf4j
public class ProjectViewManagerTool extends AstahToolProvider {

    private final ProjectAccessor projectAccessor;
    private final IProjectViewManager projectViewManager;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public ProjectViewManagerTool(ProjectAccessor projectAccessor, IProjectViewManager projectViewManager, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.projectViewManager = projectViewManager;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "show_in_property_view",
                "Show the property view of the element (specified by ID), and return the model element of the target element.",
                this::showInPropertyView,
                IdDTO.class,
                ElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "show_in_structure_tree",
                "Show the element (specified by ID) in the structure tree (aka model browser), and return the model element of the target element.",
                this::showInStructureTree,
                IdDTO.class,
                ElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_selected_elements",
                "Get the model elements of the selected elements in the project view (including the structure tree (aka model browser)).",
                this::getSelectedElements,
                NoInputDTO.class,
                ElementListDTO.class)
        );
    }

    private ElementListDTO getSelectedElements(NoInputDTO param) throws Exception {
        log.debug("Get selected elements: {}", param);

        List<ElementDTO> elementDTOs = new ArrayList<>();
        for (IEntity astahEntity : projectViewManager.getSelectedEntities()) {
            IElement astahElement;
            try {
                astahElement = (IElement) astahEntity;
            } catch (ClassCastException e) {
                // Skip when the entity is not an element
                continue;
            }

            elementDTOs.add(ElementDTOAssembler.toDTO(astahElement));
        }

        return new ElementListDTO(elementDTOs);
    }

    private ElementDTO showInPropertyView(IdDTO param) throws Exception {
        log.debug("Show in property view: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        try {
            projectViewManager.showInPropertyView(astahElement);
        } catch (Exception e) {
            throw new RuntimeException("Failed to show in property view.");
        }

        return ElementDTOAssembler.toDTO(astahElement);
    }

    private ElementDTO showInStructureTree(IdDTO param) throws Exception {
        log.debug("Show in structure tree: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        try {
            projectViewManager.showInStructureTree(astahElement);
        } catch (Exception e) {
            throw new RuntimeException("Failed to show in structure tree.");
        }

        return ElementDTOAssembler.toDTO(astahElement);
    }
}
