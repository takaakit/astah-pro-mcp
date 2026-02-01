package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithAlignAttributeItemsDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithInitialDisplayLevelDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithModelTypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDiagramWithNotationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERDiagramDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERDiagram.html
@Slf4j
public class ERDiagramTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERDiagramTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER diagram tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_er_dgm_info",
                        "Return detailed information about the specified ER diagram (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        ERDiagramDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_align_er_attr_items_of_er_dgm",
                        "Set the align attribute items of the specified ER diagram (specified by ID), and return the ER diagram information after it is set.",
                        this::setAlignAttributeItems,
                        ERDiagramWithAlignAttributeItemsDTO.class,
                        ERDiagramDTO.class),

                ToolSupport.definition(
                        "set_initial_display_level_of_er_dgm",
                        "Set the initial display level (specified by string) of the specified ER diagram (specified by ID), and return the ER diagram information after it is set.",
                        this::setInitialDisplayLevel,
                        ERDiagramWithInitialDisplayLevelDTO.class,
                        ERDiagramDTO.class),

                ToolSupport.definition(
                        "set_model_type_of_er_dgm",
                        "Set the model type (specified by string) of the specified ER diagram (specified by ID), and return the ER diagram information after it is set.",
                        this::setModelType,
                        ERDiagramWithModelTypeDTO.class,
                        ERDiagramDTO.class),

                ToolSupport.definition(
                        "set_notation_of_er_dgm",
                        "Set the notation (specified by string) of the specified ER diagram (specified by ID), and return the ER diagram information after it is set.",
                        this::setNotation,
                        ERDiagramWithNotationDTO.class,
                        ERDiagramDTO.class)
        );
    }

    private ERDiagramDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get ER diagram information: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.id());

        return ERDiagramDTOAssembler.toDTO(astahERDiagram);
    }

    private ERDiagramDTO setAlignAttributeItems(McpSyncServerExchange exchange, ERDiagramWithAlignAttributeItemsDTO param) throws Exception {
        log.debug("Set align attribute items of ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        try {
            transactionManager.beginTransaction();
            astahERDiagram.setAlignAttributeItems(param.isAlignAttributeItems());
            transactionManager.endTransaction();

            return ERDiagramDTOAssembler.toDTO(astahERDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDiagramDTO setInitialDisplayLevel(McpSyncServerExchange exchange, ERDiagramWithInitialDisplayLevelDTO param) throws Exception {
        log.debug("Set initial display level of ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        try {
            transactionManager.beginTransaction();
            astahERDiagram.setInitialDisplayLevel(param.initialDisplayLevel());
            transactionManager.endTransaction();

            return ERDiagramDTOAssembler.toDTO(astahERDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDiagramDTO setModelType(McpSyncServerExchange exchange, ERDiagramWithModelTypeDTO param) throws Exception {
        log.debug("Set model type of ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        try {
            transactionManager.beginTransaction();
            astahERDiagram.setModelType(param.modelType());
            transactionManager.endTransaction();

            return ERDiagramDTOAssembler.toDTO(astahERDiagram);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDiagramDTO setNotation(McpSyncServerExchange exchange, ERDiagramWithNotationDTO param) throws Exception {
        log.debug("Set notation of ER diagram: {}", param);

        IERDiagram astahERDiagram = astahProToolSupport.getERDiagram(param.targetERDiagramId());

        try {
            transactionManager.beginTransaction();
            astahERDiagram.setNotation(param.notation());
            transactionManager.endTransaction();

            return ERDiagramDTOAssembler.toDTO(astahERDiagram);
            
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
