package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.NamedElementDTOAssembler;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/INamedElement.html
@Slf4j
public class NamedElementTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public NamedElementTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create named element tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_named_element_info",
                "Return model element information of the specified named element (specified by ID).",
                this::getInfo,
                IdDTO.class,
                NamedElementDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_name",
                "Set the name of the specified named element (specified by ID), and return the model element of the named element after it is edited. The name must be a plain, literal Unicode string; do NOT HTML/XML-escape special characters (e.g., pass \"A & B\", not \"A &amp; B\"; \"<x>\", not \"&lt;x&gt;\").",
                this::setName,
                NamedElementWithNameDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_alias1",
                "Set the alias1 of the specified named element (specified by ID), and return the model element of the named element after it is edited.",
                this::setAlias1,
                NamedElementWithAlias1DTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_alias2",
                "Set the alias2 of the specified named element (specified by ID), and return the model element of the named element after it is edited.",
                this::setAlias2,
                NamedElementWithAlias2DTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_definition",
                "Set the definition of the specified named element (specified by ID), and return the model element of the named element after it is edited.",
                this::setDefinition,
                NamedElementWithDefinitionDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_visibility",
                "Set the visibility of the specified named element (specified by ID), and return the model element of the named element after it is edited.",
                this::setVisibility,
                NamedElementWithVisibilityDTO.class,
                NamedElementDTO.class)
        );
    }

    private NamedElementDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get named element information: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.id());

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO setName(McpSyncServerExchange exchange, NamedElementWithNameDTO param) throws Exception {
        log.debug("Set name of named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        txnAstah.run( () -> {
            astahNamedElement.setName(param.name());
        });

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO setAlias1(McpSyncServerExchange exchange, NamedElementWithAlias1DTO param) throws Exception {
        log.debug("Set alias1 of named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        txnAstah.run( () -> {
            astahNamedElement.setAlias1(param.alias1());
        });

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO setAlias2(McpSyncServerExchange exchange, NamedElementWithAlias2DTO param) throws Exception {
        log.debug("Set alias2 of named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        txnAstah.run( () -> {
            astahNamedElement.setAlias2(param.alias2());
        });

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO setDefinition(McpSyncServerExchange exchange, NamedElementWithDefinitionDTO param) throws Exception {
        log.debug("Set definition of named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        txnAstah.run( () -> {
            astahNamedElement.setDefinition(param.definition());
        });

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }

    private NamedElementDTO setVisibility(McpSyncServerExchange exchange, NamedElementWithVisibilityDTO param) throws Exception {
        log.debug("Set visibility of named element: {}", param);

        INamedElement astahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        txnAstah.run( () -> {
            astahNamedElement.setVisibility(param.visibility().astahValue);
        });

        return NamedElementDTOAssembler.toDTO(astahNamedElement);
    }
}
