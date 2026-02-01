package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithCardinalityDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithChildVerbPhraseDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithERIndexDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithParentRequiredDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithParentVerbPhraseDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERRelationshipDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.ERRelationshipDTOAssembler;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERIndex;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/model/IERRelationship.html
@Slf4j
public class ERRelationshipTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERRelationshipTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
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
            log.error("Failed to create ER relationship tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "get_er_relationship_info",
                        "Return detailed information about the specified ER relationship (specified by ID).",
                        this::getInfo,
                        IdDTO.class,
                        ERRelationshipDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "set_cardinality_of_er_relationship",
                        "Set the cardinality (specified by string) of the specified ER relationship (specified by ID), and return the ER relationship information after it is set.",
                        this::setCardinality,
                        ERRelationshipWithCardinalityDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "set_er_index_of_er_relationship",
                        "Set the ER index (specified by ID) to the specified ER relationship (specified by ID), and return the ER relationship information after it is set.",
                        this::setERIndex,
                        ERRelationshipWithERIndexDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "set_logical_name_of_er_relationship",
                        "Set the logical name (specified by string) to the specified ER relationship (specified by ID), and return the ER relationship information after it is set.",
                        this::setLogicalName,
                        ERRelationshipWithLogicalNameDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "set_physical_name_of_er_relationship",
                        "Set the physical name (specified by string) to the specified ER relationship (specified by ID), and return the ER relationship information after it is set.",
                        this::setPhysicalName,
                        ERRelationshipWithPhysicalNameDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "set_parent_required_of_er_relationship",
                        "Set the parent required (specified by boolean) to the specified ER relationship (specified by ID), and return the ER relationship information after it is set.",
                        this::setParentRequired,
                        ERRelationshipWithParentRequiredDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "set_parent_verb_phrase_of_er_relationship",
                        "Set the parent verb phrase (specified by string) to the specified ER relationship (specified by ID), and return the ER relationship information after it is set.",
                        this::setVerbPhraseParent,
                        ERRelationshipWithParentVerbPhraseDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "set_child_verb_phrase_of_er_relationship",
                        "Set the child verb phrase (specified by string) to the specified ER relationship (specified by ID), and return the ER relationship information after it is set.",
                        this::setVerbPhraseChild,
                        ERRelationshipWithChildVerbPhraseDTO.class,
                        ERRelationshipDTO.class)
        );
    }

    private ERRelationshipDTO getInfo(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get ER relationship information: {}", param);

        IERRelationship astahERRelationship = astahProToolSupport.getERRelationship(param.id());

        return ERRelationshipDTOAssembler.toDTO(astahERRelationship);
    }

    private ERRelationshipDTO setCardinality(McpSyncServerExchange exchange, ERRelationshipWithCardinalityDTO param) throws Exception {
        log.debug("Set cardinality of ER relationship: {}", param);

        IERRelationship astahERRelationship = astahProToolSupport.getERRelationship(param.targetERRelationshipId());

        try {
            transactionManager.beginTransaction();
            astahERRelationship.setCardinality(param.cardinality());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(astahERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO setERIndex(McpSyncServerExchange exchange, ERRelationshipWithERIndexDTO param) throws Exception {
        log.debug("Set ER index of ER relationship: {}", param);

        IERRelationship astahERRelationship = astahProToolSupport.getERRelationship(param.targetERRelationshipId());
        IERIndex astahERIndex = astahProToolSupport.getERIndex(param.erIndexId());

        try {
            transactionManager.beginTransaction();
            astahERRelationship.setERIndex(astahERIndex);
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(astahERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO setLogicalName(McpSyncServerExchange exchange, ERRelationshipWithLogicalNameDTO param) throws Exception {
        log.debug("Set logical name of ER relationship: {}", param);

        IERRelationship astahERRelationship = astahProToolSupport.getERRelationship(param.targetERRelationshipId());

        try {
            transactionManager.beginTransaction();
            astahERRelationship.setLogicalName(param.logicalName());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(astahERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO setPhysicalName(McpSyncServerExchange exchange, ERRelationshipWithPhysicalNameDTO param) throws Exception {
        log.debug("Set physical name of ER relationship: {}", param);

        IERRelationship astahERRelationship = astahProToolSupport.getERRelationship(param.targetERRelationshipId());

        try {
            transactionManager.beginTransaction();
            astahERRelationship.setPhysicalName(param.physicalName());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(astahERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO setParentRequired(McpSyncServerExchange exchange, ERRelationshipWithParentRequiredDTO param) throws Exception {
        log.debug("Set parent required of ER relationship: {}", param);

        IERRelationship astahERRelationship = astahProToolSupport.getERRelationship(param.targetERRelationshipId());

        try {
            transactionManager.beginTransaction();
            astahERRelationship.setParentRequired(param.isParentRequired());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(astahERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO setVerbPhraseParent(McpSyncServerExchange exchange, ERRelationshipWithParentVerbPhraseDTO param) throws Exception {
        log.debug("Set parent verb phrase of ER relationship: {}", param);

        IERRelationship astahERRelationship = astahProToolSupport.getERRelationship(param.targetERRelationshipId());

        try {
            transactionManager.beginTransaction();
            astahERRelationship.setVerbPhraseParent(param.parentVerbPhrase());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(astahERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO setVerbPhraseChild(McpSyncServerExchange exchange, ERRelationshipWithChildVerbPhraseDTO param) throws Exception {
        log.debug("Set child verb phrase of ER relationship: {}", param);

        IERRelationship astahERRelationship = astahProToolSupport.getERRelationship(param.targetERRelationshipId());

        try {
            transactionManager.beginTransaction();
            astahERRelationship.setVerbPhraseChild(param.childVerbPhrase());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(astahERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
