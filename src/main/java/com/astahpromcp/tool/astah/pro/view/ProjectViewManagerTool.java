package com.astahpromcp.tool.astah.pro.view;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementListDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IEntity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IProjectViewManager;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/view/IProjectViewManager.html
@Slf4j
public class ProjectViewManagerTool implements ToolProvider {
    
    private final ProjectAccessor projectAccessor;
    private final IProjectViewManager projectViewManager;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public ProjectViewManagerTool(ProjectAccessor projectAccessor, IProjectViewManager projectViewManager, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.projectViewManager = projectViewManager;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_slct_elms",
                            "Get the information of the selected elements in the project view.",
                            this::getSelectedElements,
                            NoInputDTO.class,
                            ElementListDTO.class),

                    ToolSupport.definition(
                            "show_in_prop_view",
                            "Show the property view of the element (specified by ID), and return the shown element information.",
                            this::showInPropertyView,
                            IdDTO.class,
                            ElementDTO.class),

                    ToolSupport.definition(
                            "show_in_strct_tree",
                            "Show the element (specified by ID) in the structure tree (aka model browser), and return the shown element information.",
                            this::showInStructureTree,
                            IdDTO.class,
                            ElementDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create project view manager tools", e);
            return List.of();
        }
    }

    private ElementListDTO getSelectedElements(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
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

    private ElementDTO showInPropertyView(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Show in property view: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        try {
            projectViewManager.showInPropertyView(astahElement);
        } catch (Exception e) {
            throw new RuntimeException("Failed to show in property view.");
        }

        return ElementDTOAssembler.toDTO(astahElement);
    }

    private ElementDTO showInStructureTree(McpSyncServerExchange exchange, IdDTO param) throws Exception {
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
