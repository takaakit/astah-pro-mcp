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
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/ERModelEditor.html
@Slf4j
public class ERModelEditorTool implements ToolProvider {

    private final ERModelEditor erModelEditor;
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public ERModelEditorTool(ERModelEditor erModelEditor, ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.erModelEditor = erModelEditor;
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
            log.error("Failed to create ER model editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "create_er_model_in_project",
                "Create a new ER model in the project, and return the newly created model element of the ER model.",
                this::createERModel,
                NewERModelDTO.class,
                ERModelDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_er_pkg_in_parent_er_pkg",
                "Create a new ER package under the specified parent ER package (specified by ID), and return the newly created model element of the ER package.",
                this::createERPackage,
                NewERPackageInERPackageDTO.class,
                ERPackageDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_er_entity_in_parent_er_pkg",
                "Create a new ER entity under the specified parent ER package (specified by ID), and return the newly created model element of the ER entity.",
                this::createEREntity,
                NewEREntityInERPackageDTO.class,
                EREntityDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_er_attr_in_er_entity",
                "Create a new ER attribute under the specified ER entity (specified by ID), and return the newly created model element of the ER attribute.",
                this::createERAttribute,
                NewERAttributeInEREntityDTO.class,
                ERAttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_er_datatype_in_er_model",
                "Create a new ER datatype in the ER schema of the ER model (specified by ID), and return the newly created model element of the ER datatype.",
                this::createERDatatype,
                NewERDatatypeInERModelDTO.class,
                ERDatatypeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_er_domain_in_er_model",
                "Create a new ER domain in the ER schema of the ER model (specified by ID), and return the newly created model element of the ER domain.",
                this::createERDomainInERModel,
                NewERDomainInERModelDTO.class,
                ERDomainDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_er_domain_in_er_domain",
                "Create a new ER domain in the ER domain (specified by ID), and return the newly created model element of the ER domain.",
                this::createERDomainInERDomain,
                NewERDomainInERDomainDTO.class,
                ERDomainDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_identifying_relationship",
                "Create a new identifying relationship between the specified parent ER entity (specified by ID) and child ER entity (specified by ID), and return the newly created model element of the identifying relationship.",
                this::createIdentifyingRelationship,
                NewIdentifyingRelationshipDTO.class,
                ERRelationshipDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_non_identifying_relationship",
                "Create a new non-identifying relationship between the specified parent ER entity (specified by ID) and child ER entity (specified by ID), and return the newly created model element of the non-identifying relationship.",
                this::createNonIdentifyingRelationship,
                NewNonIdentifyingRelationshipDTO.class,
                ERRelationshipDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_many_to_many_relationship",
                "Create a new many-to-many relationship between the specified parent ER entity (specified by ID) and child ER entity (specified by ID), and return the newly created model element of the relationship.",
                this::createManyToManyRelationship,
                NewManyToManyRelationshipDTO.class,
                ERRelationshipDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_subtype_relationship",
                "Create a new subtype relationship between the specified parent ER entity (specified by ID) and child ER entity (specified by ID), and return the newly created model element of the subtype relationship.",
                this::createSubtypeRelationship,
                NewSubtypeRelationshipDTO.class,
                ERSubtypeRelationshipDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_er_index_of_er_attr",
                "Create a new ER index of the specified ER attribute (specified by ID), and return the newly created model element of the ER index.",
                this::createERIndex,
                NewERIndexDTO.class,
                ERIndexDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "delete_er_model_or_er_elem",
                "Delete the specified ER model (specified by ID) or ER element (specified by ID), and return the deleted ER model or ER element.",
                this::delete,
                IdDTO.class,
                ElementDTO.class)
        );
    }

    private ERModelDTO createERModel(NewERModelDTO param) throws Exception {
        log.debug("Create ER model: {}", param);

        IModel astahProject = projectAccessor.getProject();

        IERModel astahERModel = txnAstah.call( () -> {
            return erModelEditor.createERModel(
                astahProject,
                param.newERModelName());
        });

        return ERModelDTOAssembler.toDTO(astahERModel);
    }

    private ERPackageDTO createERPackage(NewERPackageInERPackageDTO param) throws Exception {
        log.debug("Create ER package: {}", param);

        IERPackage parentERPackage = astahProToolSupport.getERPackage(param.parentERPackageId());

        IERPackage createdERPackage = txnAstah.call( () -> {
            return erModelEditor.createERPackage(
                parentERPackage,
                param.newERPackageName());
        });

        return ERPackageDTOAssembler.toDTO(createdERPackage);
    }

    private EREntityDTO createEREntity(NewEREntityInERPackageDTO param) throws Exception {
        log.debug("Create ER entity: {}", param);

        IERPackage parentERPackage = astahProToolSupport.getERPackage(param.parentERPackageId());

        IEREntity createdEREntity = txnAstah.call( () -> {
            return erModelEditor.createEREntity(
                parentERPackage,
                param.newEREntityLogicalName(),
                param.newEREntityPhysicalName());
        });

        return EREntityDTOAssembler.toDTO(createdEREntity);
    }

    private ERAttributeDTO createERAttribute(NewERAttributeInEREntityDTO param) throws Exception {
        log.debug("Create ER attribute: {}", param);

        IEREntity targetEREntity = astahProToolSupport.getEREntity(param.targetEREntityId());
        IERDatatype erDatatype = astahProToolSupport.getERDatatype(param.erDatatypeId());

        IERAttribute createdERAttribute = txnAstah.call( () -> {
            return erModelEditor.createERAttribute(
                targetEREntity,
                param.newERAttributeLogicalName(),
                param.newERAttributePhysicalName(),
                erDatatype);
        });

        return ERAttributeDTOAssembler.toDTO(createdERAttribute);
    }

    private ERDatatypeDTO createERDatatype(NewERDatatypeInERModelDTO param) throws Exception {
        log.debug("Create ER datatype: {}", param);

        IERModel erModel = astahProToolSupport.getERModel(param.targetERModelId());

        IERDatatype createdERDatatype = txnAstah.call( () -> {
            return erModelEditor.createERDatatype(
                erModel,
                param.newERDatatypeName());
        });

        return ERDatatypeDTOAssembler.toDTO(createdERDatatype);
    }

    private ERDomainDTO createERDomainInERModel(NewERDomainInERModelDTO param) throws Exception {
        log.debug("Create ER domain in ER model: {}", param);

        IERModel erModel = astahProToolSupport.getERModel(param.targetERModelId());
        IERDatatype erDatatype = astahProToolSupport.getERDatatype(param.erDatatypeId());

        IERDomain createdERDomain = txnAstah.call( () -> {
            return erModelEditor.createERDomain(
                erModel,
                null,
                param.newERDomainLogicalName(),
                param.newERDomainPhysicalName(),
                erDatatype);
        });

        return ERDomainDTOAssembler.toDTO(createdERDomain);
    }

    private ERDomainDTO createERDomainInERDomain(NewERDomainInERDomainDTO param) throws Exception {
        log.debug("Create ER domain in ER domain: {}", param);

        IERDomain parentERDomain = astahProToolSupport.getERDomain(param.parentERDomainId());
        IERDatatype erDatatype = astahProToolSupport.getERDatatype(param.erDatatypeId());

        IERDomain createdERDomain = txnAstah.call( () -> {
            return erModelEditor.createERDomain(
                null,
                parentERDomain,
                param.newERDomainLogicalName(),
                param.newERDomainPhysicalName(),
                erDatatype);
        });

        return ERDomainDTOAssembler.toDTO(createdERDomain);
    }

    private ERRelationshipDTO createIdentifyingRelationship(NewIdentifyingRelationshipDTO param) throws Exception {
        log.debug("Create identifying relationship: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());
        IEREntity childEREntity = astahProToolSupport.getEREntity(param.childEREntityId());

        IERRelationship createdERRelationship = txnAstah.call( () -> {
            return erModelEditor.createIdentifyingRelationship(
                parentEREntity,
                childEREntity,
                param.newRelationshipLogicalName(),
                param.newRelationshipPhysicalName());
        });

        return ERRelationshipDTOAssembler.toDTO(createdERRelationship);
    }

    private ERRelationshipDTO createNonIdentifyingRelationship(NewNonIdentifyingRelationshipDTO param) throws Exception {
        log.debug("Create non-identifying relationship: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());
        IEREntity childEREntity = astahProToolSupport.getEREntity(param.childEREntityId());

        IERRelationship createdERRelationship = txnAstah.call( () -> {
            return erModelEditor.createNonIdentifyingRelationship(
                parentEREntity,
                childEREntity,
                param.newRelationshipLogicalName(),
                param.newRelationshipPhysicalName());
        });

        return ERRelationshipDTOAssembler.toDTO(createdERRelationship);
    }

    private ERRelationshipDTO createManyToManyRelationship(NewManyToManyRelationshipDTO param) throws Exception {
        log.debug("Create many-to-many relationship: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());
        IEREntity childEREntity = astahProToolSupport.getEREntity(param.childEREntityId());

        IERRelationship createdERRelationship = txnAstah.call( () -> {
            return erModelEditor.createMultiToMultiRelationship(
                parentEREntity,
                childEREntity,
                param.newRelationshipLogicalName(),
                param.newRelationshipPhysicalName());
        });

        return ERRelationshipDTOAssembler.toDTO(createdERRelationship);
    }

    private ERSubtypeRelationshipDTO createSubtypeRelationship(NewSubtypeRelationshipDTO param) throws Exception {
        log.debug("Create subtype relationship: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());
        IEREntity childEREntity = astahProToolSupport.getEREntity(param.childEREntityId());

        IERSubtypeRelationship createdSubtypeRelationship = txnAstah.call( () -> {
            return erModelEditor.createSubtypeRelationship(
                parentEREntity,
                childEREntity,
                param.newRelationshipLogicalName(),
                param.newRelationshipPhysicalName());
        });

        return ERSubtypeRelationshipDTOAssembler.toDTO(createdSubtypeRelationship);
    }

    private ERIndexDTO createERIndex(NewERIndexDTO param) throws Exception {
        log.debug("Create ER index: {}", param);

        IEREntity parentEREntity = astahProToolSupport.getEREntity(param.parentEREntityId());

        List<IERAttribute> erAttributes = new ArrayList<>();
        for (String attributeId : param.targetERAttributeIds()) {
            erAttributes.add(astahProToolSupport.getERAttribute(attributeId));
        }

        IERIndex createdERIndex = txnAstah.call( () -> {
            return erModelEditor.createERIndex(
                param.newERIndexName(),
                parentEREntity,
                param.unique(),
                param.key(),
                erAttributes.toArray(new IERAttribute[0]));
        });

        return ERIndexDTOAssembler.toDTO(createdERIndex);
    }

    private ElementDTO delete(IdDTO param) throws Exception {
        log.debug("Delete ER model or ER element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        ElementDTO deletedElementDTO = ElementDTOAssembler.toDTO(astahElement);

        txnAstah.run( () -> {
            erModelEditor.delete(astahElement);
        });

        return deletedElementDTO;
    }
}
