package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.editor.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.*;
import com.change_vision.jude.api.inf.editor.ERModelEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/ERModelEditor.html
@Slf4j
public class ERModelEditorTool implements ToolProvider {

    private final ERModelEditor erModelEditor;
    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERModelEditorTool(ERModelEditor erModelEditor, ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.erModelEditor = erModelEditor;
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
            log.error("Failed to create ER model editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "create_er_model_in_project",
                        "Create a new ER model in the project, and return the newly created ER model information.",
                        this::createERModel,
                        NewERModelDTO.class,
                        ERModelDTO.class),

                ToolSupport.definition(
                        "create_er_pkg_in_parent_er_pkg",
                        "Create a new ER package under the specified parent ER package (specified by ID), and return the newly created ER package information.",
                        this::createERPackage,
                        NewERPackageInERPackageDTO.class,
                        ERPackageDTO.class),

                ToolSupport.definition(
                        "create_er_entity_in_parent_er_pkg",
                        "Create a new ER entity under the specified parent ER package (specified by ID), and return the newly created ER entity information.",
                        this::createEREntity,
                        NewEREntityInERPackageDTO.class,
                        EREntityDTO.class),

                ToolSupport.definition(
                        "create_er_attr_in_er_entity",
                        "Create a new ER attribute under the specified ER entity (specified by ID), and return the newly created ER attribute information.",
                        this::createERAttribute,
                        NewERAttributeInEREntityDTO.class,
                        ERAttributeDTO.class),

                ToolSupport.definition(
                        "create_er_datatype_in_er_model",
                        "Create a new ER datatype in the ER schema of the ER model (specified by ID), and return the newly created ER datatype information.",
                        this::createERDatatype,
                        NewERDatatypeInERModelDTO.class,
                        ERDatatypeDTO.class),

                ToolSupport.definition(
                        "create_er_domain_in_er_model",
                        "Create a new ER domain in the ER schema of the ER model (specified by ID), and return the newly created ER domain information.",
                        this::createERDomainInERModel,
                        NewERDomainInERModelDTO.class,
                        ERDomainDTO.class),

                ToolSupport.definition(
                        "create_er_domain_in_er_domain",
                        "Create a new ER domain in the ER domain (specified by ID), and return the newly created ER domain information.",
                        this::createERDomainInERDomain,
                        NewERDomainInERDomainDTO.class,
                        ERDomainDTO.class),

                ToolSupport.definition(
                        "create_identifying_relationship",
                        "Create a new identifying relationship between the specified parent ER entity (specified by ID) and child ER entity (specified by ID), and return the newly created identifying relationship information.",
                        this::createIdentifyingRelationship,
                        NewIdentifyingRelationshipDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "create_non_identifying_relationship",
                        "Create a new non-identifying relationship between the specified parent ER entity (specified by ID) and child ER entity (specified by ID), and return the newly created non-identifying relationship information.",
                        this::createNonIdentifyingRelationship,
                        NewNonIdentifyingRelationshipDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "create_many_to_many_relationship",
                        "Create a new many-to-many relationship between the specified parent ER entity (specified by ID) and child ER entity (specified by ID), and return the newly created many-to-many relationship information.",
                        this::createManyToManyRelationship,
                        NewManyToManyRelationshipDTO.class,
                        ERRelationshipDTO.class),

                ToolSupport.definition(
                        "create_subtype_relationship",
                        "Create a new subtype relationship between the specified parent ER entity (specified by ID) and child ER entity (specified by ID), and return the newly created subtype relationship information.",
                        this::createSubtypeRelationship,
                        NewSubtypeRelationshipDTO.class,
                        ERSubtypeRelationshipDTO.class),

                ToolSupport.definition(
                        "create_er_index_of_er_attr",
                        "Create a new ER index of the specified ER attribute (specified by ID), and return the newly created ER index information.",
                        this::createERIndex,
                        NewERIndexDTO.class,
                        ERIndexDTO.class),

                ToolSupport.definition(
                        "delete_er_model_or_er_elem",
                        "Delete the specified ER model (specified by ID) or ER element (specified by ID), and return the deleted ER model or ER element information.",
                        this::delete,
                        IdDTO.class,
                        ElementDTO.class)
        );
    }

    private ERModelDTO createERModel(McpSyncServerExchange exchange, NewERModelDTO param) throws Exception {
        log.debug("Create ER model: {}", param);

        IModel astahProject = projectAccessor.getProject();

        try {
            transactionManager.beginTransaction();
            IERModel astahERModel = erModelEditor.createERModel(
                astahProject,
                param.newERModelName());
            transactionManager.endTransaction();

            return ERModelDTOAssembler.toDTO(astahERModel);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERPackageDTO createERPackage(McpSyncServerExchange exchange, NewERPackageInERPackageDTO param) throws Exception {
        log.debug("Create ER package: {}", param);

        IERPackage parentERPackage = astahProToolSupport.getERPackage(param.parentERPackageId());

        try {
            transactionManager.beginTransaction();
            IERPackage createdERPackage = erModelEditor.createERPackage(
                parentERPackage,
                param.newERPackageName());
            transactionManager.endTransaction();

            return ERPackageDTOAssembler.toDTO(createdERPackage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private EREntityDTO createEREntity(McpSyncServerExchange exchange, NewEREntityInERPackageDTO param) throws Exception {
        log.debug("Create ER entity: {}", param);

        IERPackage parentERPackage = astahProToolSupport.getERPackage(param.parentERPackageId());

        try {
            transactionManager.beginTransaction();
            IEREntity createdEREntity = erModelEditor.createEREntity(
                parentERPackage,
                param.newEREntityLogicalName(),
                param.newEREntityPhysicalName());
            transactionManager.endTransaction();

            return EREntityDTOAssembler.toDTO(createdEREntity);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERAttributeDTO createERAttribute(McpSyncServerExchange exchange, NewERAttributeInEREntityDTO param) throws Exception {
        log.debug("Create ER attribute: {}", param);

        IEREntity targetEREntity = astahProToolSupport.getEREntity(param.targetEREntityId());
        IERDatatype erDatatype = astahProToolSupport.getERDatatype(param.erDatatypeId());

        try {
            transactionManager.beginTransaction();
            IERAttribute createdERAttribute = erModelEditor.createERAttribute(
                targetEREntity,
                param.newERAttributeLogicalName(),
                param.newERAttributePhysicalName(),
                erDatatype);
            transactionManager.endTransaction();

            return ERAttributeDTOAssembler.toDTO(createdERAttribute);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDatatypeDTO createERDatatype(McpSyncServerExchange exchange, NewERDatatypeInERModelDTO param) throws Exception {
        log.debug("Create ER datatype: {}", param);

        IERModel erModel = astahProToolSupport.getERModel(param.targetERModelId());

        try {
            transactionManager.beginTransaction();
            IERDatatype createdERDatatype = erModelEditor.createERDatatype(
                erModel,
                param.newERDatatypeName());
            transactionManager.endTransaction();

            return ERDatatypeDTOAssembler.toDTO(createdERDatatype);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDomainDTO createERDomainInERModel(McpSyncServerExchange exchange, NewERDomainInERModelDTO param) throws Exception {
        log.debug("Create ER domain in ER model: {}", param);

        IERModel erModel = astahProToolSupport.getERModel(param.targetERModelId());
        IERDatatype erDatatype = astahProToolSupport.getERDatatype(param.erDatatypeId());

        try {
            transactionManager.beginTransaction();
            IERDomain createdERDomain = erModelEditor.createERDomain(
                erModel,
                null,
                param.newERDomainLogicalName(),
                param.newERDomainPhysicalName(),
                erDatatype);
            transactionManager.endTransaction();

            return ERDomainDTOAssembler.toDTO(createdERDomain);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERDomainDTO createERDomainInERDomain(McpSyncServerExchange exchange, NewERDomainInERDomainDTO param) throws Exception {
        log.debug("Create ER domain in ER domain: {}", param);

        IERDomain parentERDomain = astahProToolSupport.getERDomain(param.parentERDomainId());
        IERDatatype erDatatype = astahProToolSupport.getERDatatype(param.erDatatypeId());

        try {
            transactionManager.beginTransaction();
            IERDomain createdERDomain = erModelEditor.createERDomain(
                null,
                parentERDomain,
                param.newERDomainLogicalName(),
                param.newERDomainPhysicalName(),
                erDatatype);
            transactionManager.endTransaction();

            return ERDomainDTOAssembler.toDTO(createdERDomain);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO createIdentifyingRelationship(McpSyncServerExchange exchange, NewIdentifyingRelationshipDTO param) throws Exception {
        log.debug("Create identifying relationship: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());
        IEREntity childEREntity = astahProToolSupport.getEREntity(param.childEREntityId());

        try {
            transactionManager.beginTransaction();
            IERRelationship createdERRelationship = erModelEditor.createIdentifyingRelationship(
                parentEREntity,
                childEREntity,
                param.newRelationshipLogicalName(),
                param.newRelationshipPhysicalName());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(createdERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO createNonIdentifyingRelationship(McpSyncServerExchange exchange, NewNonIdentifyingRelationshipDTO param) throws Exception {
        log.debug("Create non-identifying relationship: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());
        IEREntity childEREntity = astahProToolSupport.getEREntity(param.childEREntityId());

        try {
            transactionManager.beginTransaction();
            IERRelationship createdERRelationship = erModelEditor.createNonIdentifyingRelationship(
                parentEREntity,
                childEREntity,
                param.newRelationshipLogicalName(),
                param.newRelationshipPhysicalName());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(createdERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERRelationshipDTO createManyToManyRelationship(McpSyncServerExchange exchange, NewManyToManyRelationshipDTO param) throws Exception {
        log.debug("Create many-to-many relationship: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());
        IEREntity childEREntity = astahProToolSupport.getEREntity(param.childEREntityId());

        try {
            transactionManager.beginTransaction();
            IERRelationship createdERRelationship = erModelEditor.createMultiToMultiRelationship(
                parentEREntity,
                childEREntity,
                param.newRelationshipLogicalName(),
                param.newRelationshipPhysicalName());
            transactionManager.endTransaction();

            return ERRelationshipDTOAssembler.toDTO(createdERRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERSubtypeRelationshipDTO createSubtypeRelationship(McpSyncServerExchange exchange, NewSubtypeRelationshipDTO param) throws Exception {
        log.debug("Create subtype relationship: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());
        IEREntity childEREntity = astahProToolSupport.getEREntity(param.childEREntityId());

        try {
            transactionManager.beginTransaction();
            IERSubtypeRelationship createdSubtypeRelationship = erModelEditor.createSubtypeRelationship(
                parentEREntity,
                childEREntity,
                param.newRelationshipLogicalName(),
                param.newRelationshipPhysicalName());
            transactionManager.endTransaction();

            return ERSubtypeRelationshipDTOAssembler.toDTO(createdSubtypeRelationship);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ERIndexDTO createERIndex(McpSyncServerExchange exchange, NewERIndexDTO param) throws Exception {
        log.debug("Create ER index: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());

        List<IERAttribute> erAttributes = new ArrayList<>();
        for (String attributeId : param.targetERAttributeIds()) {
            erAttributes.add(astahProToolSupport.getERAttribute(attributeId));
        }

        try {
            transactionManager.beginTransaction();
            IERIndex createdERIndex = erModelEditor.createERIndex(
                param.newERIndexName(),
                parentEREntity,
                param.unique(),
                param.key(),
                erAttributes.toArray(new IERAttribute[0]));
            transactionManager.endTransaction();

            return ERIndexDTOAssembler.toDTO(createdERIndex);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ElementDTO delete(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Delete ER model or ER element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        ElementDTO deletedElementDTO = ElementDTOAssembler.toDTO(astahElement);

        try {
            transactionManager.beginTransaction();
            erModelEditor.delete(astahElement);
            transactionManager.endTransaction();

            return deletedElementDTO;

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
