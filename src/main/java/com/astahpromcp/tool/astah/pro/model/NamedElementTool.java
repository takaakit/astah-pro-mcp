package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/10_1_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/INamedElement.html
@Slf4j
public class NamedElementTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;

    public NamedElementTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                    ToolSupport.definition(
                            "get_named_elem_info",
                            "Return the named element information of the specified named element (specified by ID).",
                            this::getInfo,
                            IdDTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "set_name",
                            "Set the name of the specified named element (specified by ID), and return the named element information after it is edited.",
                            this::setName,
                            NamedElementWithNameDTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "set_alias1",
                            "Set the alias1 of the specified named element (specified by ID), and return the named element information after it is edited.",
                            this::setAlias1,
                            NamedElementWithAlias1DTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "set_alias2",
                            "Set the alias2 of the specified named element (specified by ID), and return the named element information after it is edited.",
                            this::setAlias2,
                            NamedElementWithAlias2DTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "set_def",
                            "Set the definition of the specified named element (specified by ID), and return the named element information after it is edited.",
                            this::setDefinition,
                            NamedElementWithDefinitionDTO.class,
                            NamedElementDTO.class),

                    ToolSupport.definition(
                            "set_visi",
                            "Set the visibility of the specified named element (specified by ID), and return the named element information after it is edited.",
                            this::setVisibility,
                            NamedElementWithVisibilityDTO.class,
                            NamedElementDTO.class)
            );
        } catch (Exception e) {
            log.error("Failed to create named element tools", e);
            return List.of();
        }
    }
    
    private NamedElementDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get named element information: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.id());

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO setName(McpSyncServerExchange exchange, NamedElementWithNameDTO param) throws Exception {
        log.debug("Set name of named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        try {
            transactionManager.beginTransaction();
            astahNamedElement.setName(param.name());
            transactionManager.endTransaction();
            
            return NamedElementDTOAssembler.toDTO(astahNamedElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NamedElementDTO setAlias1(McpSyncServerExchange exchange, NamedElementWithAlias1DTO param) throws Exception {
        log.debug("Set alias1 of named element: {}", param);
        
        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        try {
            transactionManager.beginTransaction();
            astahNamedElement.setAlias1(param.alias1());
            transactionManager.endTransaction();
            
            return NamedElementDTOAssembler.toDTO(astahNamedElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
    
    private NamedElementDTO setAlias2(McpSyncServerExchange exchange, NamedElementWithAlias2DTO param) throws Exception {
        log.debug("Set alias2 of named element: {}", param);
        
        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        try {
            transactionManager.beginTransaction();
            astahNamedElement.setAlias2(param.alias2());
            transactionManager.endTransaction();
            
            return NamedElementDTOAssembler.toDTO(astahNamedElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NamedElementDTO setDefinition(McpSyncServerExchange exchange, NamedElementWithDefinitionDTO param) throws Exception {
        log.debug("Set definition of named element: {}", param);
        
        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        try {
            transactionManager.beginTransaction();
            astahNamedElement.setDefinition(param.definition());
            transactionManager.endTransaction();
            
            return NamedElementDTOAssembler.toDTO(astahNamedElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private NamedElementDTO setVisibility(McpSyncServerExchange exchange, NamedElementWithVisibilityDTO param) throws Exception {
        log.debug("Set visibility of named element: {}", param);
        
        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        try {
            transactionManager.beginTransaction();
            astahNamedElement.setVisibility(param.visibility().toAstahValue());
            transactionManager.endTransaction();
            
            return NamedElementDTOAssembler.toDTO(astahNamedElement);
            
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}

