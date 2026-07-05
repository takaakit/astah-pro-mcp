package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.*;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/editor/BasicModelEditor.html
@Slf4j
public class BasicModelEditorTool implements ToolProvider {

    private final BasicModelEditor basicModelEditor;
    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public BasicModelEditorTool(BasicModelEditor basicModelEditor, ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.basicModelEditor = basicModelEditor;
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
            log.error("Failed to create basic model editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "change_parent",
                "Change the parent named element (specified by ID) on the specified named element (specified by ID), and return the changed model element. For example, this tool is used when you want to change the package structure.",
                this::changeParent,
                NamedElementWithParentDTO.class,
                NamedElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_artifact",
                "Create a new artifact under the specified parent package (specified by ID), and return the newly created model element of the artifact.",
                this::createArtifact,
                NewArtifactInPackageDTO.class,
                ArtifactDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_pkg_in_parent_pkg",
                "Create a new package under the specified parent package (specified by ID), and return the newly created model element of the package. Note that this tool cannot create a root package (i.e., a project).",
                this::createPackageInParentPackage,
                NewPackageInPackageDTO.class,
                PackageDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_class_in_parent_pkg",
                "Create a new class under the specified parent package (specified by ID), and return the newly created model element of the class.",
                this::createClassInParentPackage,
                NewClassInPackageDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_class_in_parent_class",
                "Create a new class under the specified parent class (specified by ID), and return the newly created model element of the class.",
                this::createClassInParentClass,
                NewClassInClassDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_enum_in_parent_pkg",
                "Create a new enumeration under the specified parent package (specified by ID), and return the newly created model element of the enumeration.",
                this::createEnumerationInParentPackage,
                NewEnumerationInPackageDTO.class,
                EnumerationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_interface_in_parent_pkg",
                "Create a new interface under the specified parent package (specified by ID), and return the newly created model element of the interface.",
                this::createInterfaceInParentPackage,
                NewInterfaceInPackageDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_interface_in_parent_class",
                "Create a new interface under the specified parent class (specified by ID), and return the newly created model element of the interface.",
                this::createInterfaceInParentClass,
                NewInterfaceInClassDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_component",
                "Create a new component under the specified parent package (specified by ID), and return the newly created model element of the component.",
                this::createComponent,
                NewComponentInPackageDTO.class,
                ComponentDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_port",
                "Create a new port under the specified class (specified by ID), and return the newly created model element of the port.",
                this::createPort,
                NewPortInClassDTO.class,
                PortDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_attr",
                "Create a new attribute under the specified class (specified by ID), and return the newly created model element of the attribute. Since the attribute type is set as 'int', if the attribute type is not 'int', use other tool function to set the attribute type.",
                this::createAttribute,
                NewAttributeInClassDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_enum_literal",
                "Create a new enumeration literal under the specified enumeration (specified by ID), and return the newly created model element of the enumeration literal.",
                this::createEnumerationLiteral,
                NewEnumerationLiteralInEnumerationDTO.class,
                EnumerationLiteralDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_ope",
                "Create a new operation under the specified class (specified by ID), and return the newly created model element of the operation. Since the operation return type is set to 'void', if the operation return type is not 'void', use other tool function to set the operation return type.",
                this::createOperation,
                NewOperationInClassDTO.class,
                OperationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_param",
                "Create a new parameter to the specified operation (specified by ID), and return the newly created model element of the parameter. Since the parameter type is set as 'int', if the parameter type is not 'int', use other tool function to set the parameter type.",
                this::createParameter,
                NewParameterToOperationDTO.class,
                ParameterDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_asso",
                "Create a new association between the specified class (specified by ID) and the another specified class (specified by ID), and return the newly created model element of the association. The association name and the association end A and B role names are set to empty strings.",
                this::createAssociation,
                NewAssociationDTO.class,
                AssociationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_asso_class",
                "Create a new association and association class between the specified class (specified by ID) and the another specified class (specified by ID), and return the newly created model element of the association. The association end A and B role names are set to empty strings.",
                this::createAssociationClass,
                NewAssociationClassDTO.class,
                AssociationClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_dep",
                "Create a new dependency between the specified source named element (specified by ID) and the specified target named element (specified by ID), and return the newly created model element of the dependency.",
                this::createDependency,
                NewDependencyDTO.class,
                DependencyDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_gen",
                "Create a new generalization between the specified sub class (specified by ID) and the specified super class (specified by ID), and return the newly created model element of the generalization.",
                this::createGeneralization,
                NewGeneralizationDTO.class,
                GeneralizationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_real",
                "Create a new realization between the specified client (specified by ID) and the specified supplier (specified by ID), and return the newly created model element of the realization.",
                this::createRealization,
                NewRealizationDTO.class,
                RealizationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_usage",
                "Create a new usage between the specified client (specified by ID) and the specified supplier (specified by ID), and return the newly created model element of the usage.",
                this::createUsage,
                NewUsageDTO.class,
                UsageDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_qualifier",
                "Create a new qualifier (type and name) to the specified association end (specified by ID), and return the newly created model element of the qualifier. Limitation: Because an ID of the qualifier type is required, a qualifier of a primitive type cannot be created.",
                this::createQualifier,
                NewQualifierToAssociationEndDTO.class,
                AttributeDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_tagged_val",
                "Create a new tagged value (name and value) to the specified element (specified by ID), and return the element after it is edited.",
                this::createTaggedValue,
                NewTaggedValueToElementDTO.class,
                ElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_template_param",
                "Create a template parameter of the specified type (specified by ID) to the specified class (specified by ID), and return the model element of the class after it is edited.",
                this::createTemplateParameter,
                NewTemplateParameterToClassDTO.class,
                ClassDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "delete_elem",
                "Delete the specified element (specified by ID), and return the deleted element. Note that deleting an element also deletes all corresponding presentations.",
                this::deleteElement,
                IdDTO.class,
                ElementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_req_in_parent_pkg",
                "Create a new requirement under the specified parent package (specified by ID), and return the newly created model element of the requirement.",
                this::createRequirementInParentPackage,
                NewRequirementInPackageDTO.class,
                RequirementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_req_in_parent_req",
                "Create a new requirement under the specified parent requirement (specified by ID), and return the newly created model element of the requirement.",
                this::createRequirementInParentRequirement,
                NewRequirementInRequirementDTO.class,
                RequirementDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_test_case_in_parent_pkg",
                "Create a new test case under the specified parent package (specified by ID), and return the newly created model element of the test case.",
                this::createTestCaseInParentPackage,
                NewTestCaseInPackageDTO.class,
                TestCaseDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_test_case_in_parent_test_case",
                "Create a new test case under the specified parent test case (specified by ID), and return the newly created model element of the test case.",
                this::createTestCaseInParentTestCase,
                NewTestCaseInTestCaseDTO.class,
                TestCaseDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_copy_dep",
                "Create a copy dependency from the specified source requirement (specified by ID) to the specified target requirement (specified by ID), and return the newly created model element of the dependency.",
                this::createCopyDependency,
                NewCopyDependencyDTO.class,
                DependencyDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_derive_req_dep",
                "Create a DeriveReqt dependency from the specified source requirement (specified by ID) to the specified target requirement (specified by ID), and return the newly created model element of the dependency.",
                this::createDeriveReqtDependency,
                NewDeriveReqtDependencyDTO.class,
                DependencyDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_refine_dep",
                "Create a refine dependency from the specified source named element (specified by ID) to the specified target requirement (specified by ID), and return the newly created model element of the dependency.",
                this::createRefineDependency,
                NewRefineDependencyDTO.class,
                DependencyDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_satisfy_dep",
                "Create a satisfy dependency from the specified source named element (specified by ID) to the specified target requirement (specified by ID), and return the newly created model element of the dependency.",
                this::createSatisfyDependency,
                NewSatisfyDependencyDTO.class,
                DependencyDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_trace_dep",
                "Create a trace dependency from the specified source named element (specified by ID) to the specified target named element (specified by ID), and return the newly created model element of the dependency.",
                this::createTraceDependency,
                NewTraceDependencyDTO.class,
                DependencyDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_verify_dep",
                "Create a verify dependency from the specified source test case (specified by ID) to the specified target requirement (specified by ID), and return the newly created model element of the dependency.",
                this::createVerifyDependency,
                NewVerifyDependencyDTO.class,
                DependencyDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "create_constraint",
                "Create a constraint to the specified named element (specified by ID), and return the newly created model element of the constraint.",
                this::createConstraint,
                NewConstraintDTO.class,
                ConstraintDTO.class)
        );
    }

    private NamedElementDTO changeParent(McpSyncServerExchange exchange, NamedElementWithParentDTO param) throws Exception {
        log.debug("Change parent: {}", param);

        INamedElement astahTargetNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementid());
        INamedElement newParentAstahNamedElement = astahProToolSupport.getNamedElement(param.newParentNamedElementId());

        txnAstah.run( () -> {
            basicModelEditor.changeParent(newParentAstahNamedElement, astahTargetNamedElement);
        });

        return NamedElementDTOAssembler.toDTO(astahTargetNamedElement);
    }

    private ArtifactDTO createArtifact(McpSyncServerExchange exchange, NewArtifactInPackageDTO param) throws Exception {
        log.debug("Create artifact in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IArtifact createdAstahArtifact = txnAstah.call( () -> {
            return basicModelEditor.createArtifact(parentAstahPackage, param.newArtifactName());
        });

        return ArtifactDTOAssembler.toDTO(createdAstahArtifact);
    }

    private PackageDTO createPackageInParentPackage(McpSyncServerExchange exchange, NewPackageInPackageDTO param) throws Exception {
        log.debug("Create package in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IPackage createdAstahPackage = txnAstah.call( () -> {
            return basicModelEditor.createPackage(parentAstahPackage, param.newPackageName());
        });

        return PackageDTOAssembler.toDTO(createdAstahPackage);
    }

    private ClassDTO createClassInParentPackage(McpSyncServerExchange exchange, NewClassInPackageDTO param) throws Exception {
        log.debug("Create class in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IClass createdAstahClass = txnAstah.call( () -> {
            return basicModelEditor.createClass(parentAstahPackage, param.newClassName());
        });

        return ClassDTOAssembler.toDTO(createdAstahClass);
    }

    private ClassDTO createClassInParentClass(McpSyncServerExchange exchange, NewClassInClassDTO param) throws Exception {
        log.debug("Create class in parent class: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        IClass createdAstahClass = txnAstah.call( () -> {
            return basicModelEditor.createClass(parentAstahClass, param.newClassName());
        });

        return ClassDTOAssembler.toDTO(createdAstahClass);
    }

    private EnumerationDTO createEnumerationInParentPackage(McpSyncServerExchange exchange, NewEnumerationInPackageDTO param) throws Exception {
        log.debug("Create enumeration in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IEnumeration createdAstahEnumeration = txnAstah.call( () -> {
            return basicModelEditor.createEnumeration(parentAstahPackage, param.newEnumerationName());
        });

        return EnumerationDTOAssembler.toDTO(createdAstahEnumeration);
    }

    private ClassDTO createInterfaceInParentPackage(McpSyncServerExchange exchange, NewInterfaceInPackageDTO param) throws Exception {
        log.debug("Create interface in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IClass createdAstahInterface = txnAstah.call( () -> {
            return basicModelEditor.createInterface(parentAstahPackage, param.newInterfaceName());
        });

        return ClassDTOAssembler.toDTO(createdAstahInterface);
    }

    private ClassDTO createInterfaceInParentClass(McpSyncServerExchange exchange, NewInterfaceInClassDTO param) throws Exception {
        log.debug("Create interface in parent class: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        IClass createdAstahInterface = txnAstah.call( () -> {
            return basicModelEditor.createInterface(parentAstahClass, param.newInterfaceName());
        });

        return ClassDTOAssembler.toDTO(createdAstahInterface);
    }

    private ComponentDTO createComponent(McpSyncServerExchange exchange, NewComponentInPackageDTO param) throws Exception {
        log.debug("Create component in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IComponent createdAstahComponent = txnAstah.call( () -> {
            return basicModelEditor.createComponent(parentAstahPackage, param.newComponentName());
        });

        return ComponentDTOAssembler.toDTO(createdAstahComponent);
    }

    private PortDTO createPort(McpSyncServerExchange exchange, NewPortInClassDTO param) throws Exception {
        log.debug("Create port: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        IPort createdAstahPort = txnAstah.call( () -> {
            return basicModelEditor.createPort(parentAstahClass, param.newPortName());
        });

        return PortDTOAssembler.toDTO(createdAstahPort);
    }

    private AttributeDTO createAttribute(McpSyncServerExchange exchange, NewAttributeInClassDTO param) throws Exception {
        log.debug("Create attribute: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        IAttribute createdAstahAttribute = txnAstah.call( () -> {
            return basicModelEditor.createAttribute(parentAstahClass, param.newAttributeName(), "int");
        });

        return AttributeDTOAssembler.toDTO(createdAstahAttribute);
    }

    private EnumerationLiteralDTO createEnumerationLiteral(McpSyncServerExchange exchange, NewEnumerationLiteralInEnumerationDTO param) throws Exception {
        log.debug("Create enumeration literal: {}", param);

        IEnumeration astahEnumeration = astahProToolSupport.getEnumeration(param.parentEnumerationId());

        IEnumerationLiteral createdAstahEnumerationLiteral = txnAstah.call( () -> {
            return basicModelEditor.createEnumerationLiteral(astahEnumeration, param.newEnumerationLiteralName());
        });

        return EnumerationLiteralDTOAssembler.toDTO(createdAstahEnumerationLiteral);
    }

    private OperationDTO createOperation(McpSyncServerExchange exchange, NewOperationInClassDTO param) throws Exception {
        log.debug("Create operation: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        IOperation createdAstahOperation = txnAstah.call( () -> {
            return basicModelEditor.createOperation(parentAstahClass, param.newOperationName(), "void");
        });

        return OperationDTOAssembler.toDTO(createdAstahOperation);
    }

    private ParameterDTO createParameter(McpSyncServerExchange exchange, NewParameterToOperationDTO param) throws Exception {
        log.debug("Create parameter: {}", param);

        IOperation astahTargetOperation = astahProToolSupport.getOperation(param.targetOperationId());

        IParameter createdAstahParameter = txnAstah.call( () -> {
            return basicModelEditor.createParameter(
                astahTargetOperation,
                param.newParameterName(),
                "int");
        });

        return ParameterDTOAssembler.toDTO(createdAstahParameter);
    }

    private AssociationDTO createAssociation(McpSyncServerExchange exchange, NewAssociationDTO param) throws Exception {
        log.debug("Create association: {}", param);

        IClass astahSourceClass = astahProToolSupport.getClass(param.sourceClassId());
        IClass astahTargetClass = astahProToolSupport.getClass(param.targetClassId());

        IAssociation createdAstahAssociation = txnAstah.call( () -> {
            IAssociation association = basicModelEditor.createAssociation(
                astahSourceClass,
                astahTargetClass,
                "",
                "",
                "");

            association.getMemberEnds()[0].setNavigability(param.sourceNavigability().astahValue);
            association.getMemberEnds()[1].setNavigability(param.targetNavigability().astahValue);
            association.getMemberEnds()[0].setAggregationKind(param.sourceAggregationKind().astahValue);
            association.getMemberEnds()[1].setAggregationKind(param.targetAggregationKind().astahValue);
            return association;
        });

        return AssociationDTOAssembler.toDTO(createdAstahAssociation);
    }

    private AssociationClassDTO createAssociationClass(McpSyncServerExchange exchange, NewAssociationClassDTO param) throws Exception {
        log.debug("Create association class: {}", param);

        IClass astahSourceClass = astahProToolSupport.getClass(param.sourceClassId());
        IClass astahTargetClass = astahProToolSupport.getClass(param.targetClassId());

        IAssociationClass createdAstahAssociationClass = txnAstah.call( () -> {
            return basicModelEditor.createAssociationClass(
                astahSourceClass,
                astahTargetClass,
                param.newAssociationClassName(),
                "",
                "");
        });

        return AssociationClassDTOAssembler.toDTO(createdAstahAssociationClass);
    }

    private DependencyDTO createDependency(McpSyncServerExchange exchange, NewDependencyDTO param) throws Exception {
        log.debug("Create dependency: {}", param);

        INamedElement sourceAstahNamedElement = astahProToolSupport.getNamedElement(param.sourceNamedElementId());
        INamedElement targetAstahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        // Note: In the API implementation, unlike in the API document, the source and target dependency arguments are reversed.
        IDependency createdAstahDependency = txnAstah.call( () -> {
            return basicModelEditor.createDependency(
                targetAstahNamedElement,
                sourceAstahNamedElement,
                "");
        });

        return DependencyDTOAssembler.toDTO(createdAstahDependency);
    }

    private GeneralizationDTO createGeneralization(McpSyncServerExchange exchange, NewGeneralizationDTO param) throws Exception {
        log.debug("Create generalization: {}", param);

        IClass astahSubClass = astahProToolSupport.getClass(param.subClassId());
        IClass astahSuperClass = astahProToolSupport.getClass(param.superClassId());

        IGeneralization createdAstahGeneralization = txnAstah.call( () -> {
            return basicModelEditor.createGeneralization(
                astahSubClass,
                astahSuperClass,
                "");
        });

        return GeneralizationDTOAssembler.toDTO(createdAstahGeneralization);
    }

    private RealizationDTO createRealization(McpSyncServerExchange exchange, NewRealizationDTO param) throws Exception {
        log.debug("Create realization: {}", param);

        IClass astahClientClass = astahProToolSupport.getClass(param.clientClassId());
        IClass astahSupplierClass = astahProToolSupport.getClass(param.supplierClassId());

        IRealization createdAstahRealization = txnAstah.call( () -> {
            return basicModelEditor.createRealization(
                astahClientClass,
                astahSupplierClass,
                "");
        });

        return RealizationDTOAssembler.toDTO(createdAstahRealization);
    }

    private UsageDTO createUsage(McpSyncServerExchange exchange, NewUsageDTO param) throws Exception {
        log.debug("Create usage: {}", param);

        IClass astahClientClass = astahProToolSupport.getClass(param.clientClassId());
        IClass astahSupplierClass = astahProToolSupport.getClass(param.supplierClassId());

        IUsage createdAstahUsage = txnAstah.call( () -> {
            return basicModelEditor.createUsage(
                astahClientClass,
                astahSupplierClass,
                "");
        });

        return UsageDTOAssembler.toDTO(createdAstahUsage);
    }

    private AttributeDTO createQualifier(McpSyncServerExchange exchange, NewQualifierToAssociationEndDTO param) throws Exception {
        log.debug("Create qualifier: {}", param);

        IAttribute astahAssociationEnd = astahProToolSupport.getAttribute(param.targetAssociationEndId());
        IClass astahType = astahProToolSupport.getClass(param.qualifierTypeId());

        // Create an attribute on the owner class to serve as the qualifier
        IAttribute createdAstahQualifier = txnAstah.call( () -> {
            return basicModelEditor.createQualifier(
                astahAssociationEnd,
                param.newQualifierName(),
                astahType);
        });

        return AttributeDTOAssembler.toDTO(createdAstahQualifier);
    }

    private ElementDTO createTaggedValue(McpSyncServerExchange exchange, NewTaggedValueToElementDTO param) throws Exception {
        log.debug("Create tagged value: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());

        txnAstah.run( () -> {
            basicModelEditor.createTaggedValue(
                astahElement,
                param.newKeyOfTaggedValue(),
                param.newValueOfTaggedValue());
        });

        return ElementDTOAssembler.toDTO(astahElement);
    }

    private ClassDTO createTemplateParameter(McpSyncServerExchange exchange, NewTemplateParameterToClassDTO param) throws Exception {
        log.debug("Create template parameter: {}", param);

        IClass astahTargetClass = astahProToolSupport.getClass(param.targetClassId());
        IClass astahType = astahProToolSupport.getClass(param.templateParameterTypeId());

        txnAstah.run( () -> {
            basicModelEditor.createTemplateParameter(
                astahTargetClass,
                param.newTemplateParameterName(),
                astahType,
                "");
        });

        return ClassDTOAssembler.toDTO(astahTargetClass);
    }

    private ElementDTO deleteElement(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Delete element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        ElementDTO deletedElementDTO = ElementDTOAssembler.toDTO(astahElement);

        txnAstah.run( () -> {
            basicModelEditor.delete(astahElement);
        });

        return deletedElementDTO;
    }

    private RequirementDTO createRequirementInParentPackage(McpSyncServerExchange exchange, NewRequirementInPackageDTO param) throws Exception {
        log.debug("Create requirement in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        IRequirement createdAstahRequirement = txnAstah.call( () -> {
            return basicModelEditor.createRequirement(
                parentAstahPackage,
                param.newRequirementName());
        });

        return RequirementDTOAssembler.toDTO(createdAstahRequirement);
    }

    private RequirementDTO createRequirementInParentRequirement(McpSyncServerExchange exchange, NewRequirementInRequirementDTO param) throws Exception {
        log.debug("Create requirement in parent requirement: {}", param);

        IRequirement parentAstahRequirement = astahProToolSupport.getRequirement(param.parentRequirementId());

        IRequirement createdAstahRequirement = txnAstah.call( () -> {
            return basicModelEditor.createRequirement(
                parentAstahRequirement,
                param.newRequirementName());
        });

        return RequirementDTOAssembler.toDTO(createdAstahRequirement);
    }

    private TestCaseDTO createTestCaseInParentPackage(McpSyncServerExchange exchange, NewTestCaseInPackageDTO param) throws Exception {
        log.debug("Create test case in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        ITestCase createdAstahTestCase = txnAstah.call( () -> {
            return basicModelEditor.createTestCase(
                parentAstahPackage,
                param.newTestCaseName());
        });


        return TestCaseDTOAssembler.toDTO(createdAstahTestCase);
    }

    private TestCaseDTO createTestCaseInParentTestCase(McpSyncServerExchange exchange, NewTestCaseInTestCaseDTO param) throws Exception {
        log.debug("Create test case in parent test case: {}", param);

        ITestCase parentAstahTestCase = astahProToolSupport.getTestCase(param.parentTestCaseId());

        ITestCase createdAstahTestCase = txnAstah.call( () -> {
            return basicModelEditor.createTestCase(
                parentAstahTestCase,
                param.newTestCaseName());
        });

        return TestCaseDTOAssembler.toDTO(createdAstahTestCase);
    }

    private DependencyDTO createCopyDependency(McpSyncServerExchange exchange, NewCopyDependencyDTO param) throws Exception {
        log.debug("Create copy dependency: {}", param);

        IRequirement sourceAstahRequirement = astahProToolSupport.getRequirement(param.sourceRequirementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        // Note: In the API implementation, unlike in the API document, the source and target dependency arguments are reversed.
        IDependency createdAstahDependency = txnAstah.call( () -> {
            IDependency dependency = basicModelEditor.createDependency(
                targetAstahRequirement,
                sourceAstahRequirement,
                "");
            dependency.addStereotype("copy");
            return dependency;
        });

        return DependencyDTOAssembler.toDTO(createdAstahDependency);
    }

    private DependencyDTO createDeriveReqtDependency(McpSyncServerExchange exchange, NewDeriveReqtDependencyDTO param) throws Exception {
        log.debug("Create derive reqt dependency: {}", param);

        IRequirement sourceAstahRequirement = astahProToolSupport.getRequirement(param.sourceRequirementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        // Note: In the API implementation, unlike in the API document, the source and target dependency arguments are reversed.
        IDependency createdAstahDependency = txnAstah.call( () -> {
            IDependency dependency = basicModelEditor.createDependency(
                targetAstahRequirement,
                sourceAstahRequirement,
                "");
            dependency.addStereotype("deriveReqt");
            return dependency;
        });

        return DependencyDTOAssembler.toDTO(createdAstahDependency);
    }

    private DependencyDTO createRefineDependency(McpSyncServerExchange exchange, NewRefineDependencyDTO param) throws Exception {
        log.debug("Create refine dependency: {}", param);

        INamedElement sourceAstahNamedElement = astahProToolSupport.getNamedElement(param.sourceNamedElementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        // Note: In the API implementation, unlike in the API document, the source and target dependency arguments are reversed.
        IDependency createdAstahDependency = txnAstah.call( () -> {
            IDependency dependency = basicModelEditor.createDependency(
                targetAstahRequirement,
                sourceAstahNamedElement,
                "");
            dependency.addStereotype("refine");
            return dependency;
        });

        return DependencyDTOAssembler.toDTO(createdAstahDependency);
    }

    private DependencyDTO createSatisfyDependency(McpSyncServerExchange exchange, NewSatisfyDependencyDTO param) throws Exception {
        log.debug("Create satisfy dependency: {}", param);

        INamedElement sourceAstahNamedElement = astahProToolSupport.getNamedElement(param.sourceNamedElementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        // Note: In the API implementation, unlike in the API document, the source and target dependency arguments are reversed.
        IDependency createdAstahDependency = txnAstah.call( () -> {
            IDependency dependency = basicModelEditor.createDependency(
                targetAstahRequirement,
                sourceAstahNamedElement,
                "");
            dependency.addStereotype("satisfy");
            return dependency;
        });

        return DependencyDTOAssembler.toDTO(createdAstahDependency);
    }

    private DependencyDTO createTraceDependency(McpSyncServerExchange exchange, NewTraceDependencyDTO param) throws Exception {
        log.debug("Create trace dependency: {}", param);

        INamedElement sourceAstahNamedElement = astahProToolSupport.getNamedElement(param.sourceNamedElementId());
        INamedElement targetAstahNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        // Note: In the API implementation, unlike in the API document, the source and target dependency arguments are reversed.
        IDependency createdAstahDependency = txnAstah.call( () -> {
            IDependency dependency = basicModelEditor.createDependency(
                targetAstahNamedElement,
                sourceAstahNamedElement,
                "");
            dependency.addStereotype("trace");
            return dependency;
        });

        return DependencyDTOAssembler.toDTO(createdAstahDependency);
    }

    private DependencyDTO createVerifyDependency(McpSyncServerExchange exchange, NewVerifyDependencyDTO param) throws Exception {
        log.debug("Create verify dependency: {}", param);

        ITestCase sourceAstahTestCase = astahProToolSupport.getTestCase(param.sourceTestCaseId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        // Note: In the API implementation, unlike in the API document, the source and target dependency arguments are reversed.
        IDependency createdAstahDependency = txnAstah.call( () -> {
            IDependency dependency = basicModelEditor.createDependency(
                targetAstahRequirement,
                sourceAstahTestCase,
                "");
            dependency.addStereotype("verify");
            return dependency;
        });

        return DependencyDTOAssembler.toDTO(createdAstahDependency);
    }

    private ConstraintDTO createConstraint(McpSyncServerExchange exchange, NewConstraintDTO param) throws Exception {
        log.debug("Create constraint: {}", param);

        INamedElement astahTargetNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        IConstraint createdAstahConstraint = txnAstah.call( () -> {
            return basicModelEditor.createConstraint(
                astahTargetNamedElement,
                param.newConstraintName());
        });

        return ConstraintDTOAssembler.toDTO(createdAstahConstraint);
    }
}
