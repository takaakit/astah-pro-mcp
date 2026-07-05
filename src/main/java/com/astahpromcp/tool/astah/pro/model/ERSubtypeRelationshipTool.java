package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERSubtypeRelationshipWithConclusiveDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERSubtypeRelationshipWithDiscriminatorAttrDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERSubtypeRelationshipWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERSubtypeRelationshipWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERSubtypeRelationshipDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERSubtypeRelationshipDTOAssembler;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERSubtypeRelationship;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERSubtypeRelationship.html
@Slf4j
public class ERSubtypeRelationshipTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERSubtypeRelationshipTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER subtype relationship tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "get_er_subtype_relationship_info",
                "Return model element information about the specified ER subtype relationship (specified by ID).",
                this::getInfo,
                IdDTO.class,
                ERSubtypeRelationshipDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "set_conclusive_of_er_subtype_relationship",
                "Set the conclusive of the specified ER subtype relationship (specified by ID), and return the model element of the ER subtype relationship after it is set.",
                this::setConclusive,
                ERSubtypeRelationshipWithConclusiveDTO.class,
                ERSubtypeRelationshipDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_discriminator_attr_of_er_subtype_relationship",
                "Set the discriminator attribute of the specified ER subtype relationship (specified by ID), and return the model element of the ER subtype relationship after it is set.",
                this::setDiscriminatorAttr,
                ERSubtypeRelationshipWithDiscriminatorAttrDTO.class,
                ERSubtypeRelationshipDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_logical_name_of_er_subtype_relationship",
                "Set the logical name of the specified ER subtype relationship (specified by ID), and return the model element of the ER subtype relationship after it is set.",
                this::setLogicalName,
                ERSubtypeRelationshipWithLogicalNameDTO.class,
                ERSubtypeRelationshipDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "set_physical_name_of_er_subtype_relationship",
                "Set the physical name of the specified ER subtype relationship (specified by ID), and return the model element of the ER subtype relationship after it is set.",
                this::setPhysicalName,
                ERSubtypeRelationshipWithPhysicalNameDTO.class,
                ERSubtypeRelationshipDTO.class)
        );
    }

    private ERSubtypeRelationshipDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get ER subtype relationship information: {}", param);

        IERSubtypeRelationship astahERSubtypeRelationship = astahProToolSupport.getERSubtypeRelationship(param.id());

        return ERSubtypeRelationshipDTOAssembler.toDTO(astahERSubtypeRelationship);
    }

    private ERSubtypeRelationshipDTO setConclusive(McpSyncServerExchange exchange, ERSubtypeRelationshipWithConclusiveDTO param) throws Exception {
        log.debug("Set conclusive of ER subtype relationship: {}", param);

        IERSubtypeRelationship astahERSubtypeRelationship = astahProToolSupport.getERSubtypeRelationship(param.targetERSubtypeRelationshipId());

        txnAstah.run( () -> {
            astahERSubtypeRelationship.setConclusive(param.isConclusive());
        });

        return ERSubtypeRelationshipDTOAssembler.toDTO(astahERSubtypeRelationship);
    }

    private ERSubtypeRelationshipDTO setDiscriminatorAttr(McpSyncServerExchange exchange, ERSubtypeRelationshipWithDiscriminatorAttrDTO param) throws Exception {
        log.debug("Set discriminator attribute of ER subtype relationship: {}", param);

        IERSubtypeRelationship astahERSubtypeRelationship = astahProToolSupport.getERSubtypeRelationship(param.targetERSubtypeRelationshipId());
        IERAttribute astahERAttribute = astahProToolSupport.getERAttribute(param.erDiscriminatorAttributeId());

        txnAstah.run( () -> {
            astahERSubtypeRelationship.setDiscriminatorAttribute(astahERAttribute);
        });

        return ERSubtypeRelationshipDTOAssembler.toDTO(astahERSubtypeRelationship);
    }

    private ERSubtypeRelationshipDTO setLogicalName(McpSyncServerExchange exchange, ERSubtypeRelationshipWithLogicalNameDTO param) throws Exception {
        log.debug("Set logical name of ER subtype relationship: {}", param);

        IERSubtypeRelationship astahERSubtypeRelationship = astahProToolSupport.getERSubtypeRelationship(param.targetERSubtypeRelationshipId());

        txnAstah.run( () -> {
            astahERSubtypeRelationship.setLogicalName(param.logicalName());
        });

        return ERSubtypeRelationshipDTOAssembler.toDTO(astahERSubtypeRelationship);
    }

    private ERSubtypeRelationshipDTO setPhysicalName(McpSyncServerExchange exchange, ERSubtypeRelationshipWithPhysicalNameDTO param) throws Exception {
        log.debug("Set physical name of ER subtype relationship: {}", param);

        IERSubtypeRelationship astahERSubtypeRelationship = astahProToolSupport.getERSubtypeRelationship(param.targetERSubtypeRelationshipId());

        txnAstah.run( () -> {
            astahERSubtypeRelationship.setPhysicalName(param.physicalName());
        });

        return ERSubtypeRelationshipDTOAssembler.toDTO(astahERSubtypeRelationship);
    }
}
