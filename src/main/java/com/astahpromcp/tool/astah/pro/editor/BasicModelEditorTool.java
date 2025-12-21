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
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BasicModelEditorTool implements ToolProvider {

    private final BasicModelEditor basicModelEditor;
    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public BasicModelEditorTool(BasicModelEditor basicModelEditor, ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.basicModelEditor = basicModelEditor;
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
            log.error("Failed to create basic model editor tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                ToolSupport.definition(
                        "change_parent",
                        "Change the parent named element (specified by ID) on the specified named element (specified by ID), and return the newly created package information. For example, this tool is used when you want to change the package structure.",
                        this::changeParent,
                        NamedElementWithParentDTO.class,
                        NamedElementDTO.class),

                ToolSupport.definition(
                        "create_pkg_in_parent_pkg",
                        "Create a new package under the specified parent package (specified by ID), and return the newly created package information. Note that this tool cannot create a root package (i.e., a project).",
                        this::createPackageInParentPackage,
                        NewPackageInPackageDTO.class,
                        PackageDTO.class),

                ToolSupport.definition(
                        "create_class_in_parent_pkg",
                        "Create a new class under the specified parent package (specified by ID), and return the newly created class information.",
                        this::createClassInParentPackage,
                        NewClassInPackageDTO.class,
                        ClassDTO.class),

                ToolSupport.definition(
                        "create_class_in_parent_class",
                        "Create a new class under the specified parent class (specified by ID), and return the newly created class information.",
                        this::createClassInParentClass,
                        NewClassInClassDTO.class,
                        ClassDTO.class),

                ToolSupport.definition(
                        "create_enum_in_parent_pkg",
                        "Create a new enumeration under the specified parent package (specified by ID), and return the newly created enumeration information.",
                        this::createEnumerationInParentPackage,
                        NewEnumerationInPackageDTO.class,
                        EnumerationDTO.class),

                ToolSupport.definition(
                        "create_interface_in_parent_pkg",
                        "Create a new interface under the specified parent package (specified by ID), and return the newly created interface information.",
                        this::createInterfaceInParentPackage,
                        NewInterfaceInPackageDTO.class,
                        ClassDTO.class),

                ToolSupport.definition(
                        "create_interface_in_parent_class",
                        "Create a new interface under the specified parent class (specified by ID), and return the newly created interface information.",
                        this::createInterfaceInParentClass,
                        NewInterfaceInClassDTO.class,
                        ClassDTO.class),

                ToolSupport.definition(
                        "create_attr",
                        "Create a new attribute under the specified class (specified by ID), and return the newly created attribute information. Since the attribute type is set as 'int', if the attribute type is not 'int', use other tool function to set the attribute type.",
                        this::createAttribute,
                        NewAttributeInClassDTO.class,
                        AttributeDTO.class),

                ToolSupport.definition(
                        "create_enum_literal",
                        "Create a new enumeration literal under the specified enumeration (specified by ID), and return the newly created enumeration literal information.",
                        this::createEnumerationLiteral,
                        NewEnumerationLiteralInEnumerationDTO.class,
                        EnumerationLiteralDTO.class),

                ToolSupport.definition(
                        "create_ope",
                        "Create a new operation under the specified class (specified by ID), and return the newly created operation information. Since the operation return type is set to 'void', if the operation return type is not 'void', use other tool function to set the operation return type.",
                        this::createOperation,
                        NewOperationInClassDTO.class,
                        OperationDTO.class),

                ToolSupport.definition(
                        "create_param",
                        "Create a new parameter to the specified operation (specified by ID), and return the newly created parameter information. Since the parameter type is set as 'int', if the parameter type is not 'int', use other tool function to set the parameter type.",
                        this::createParameter,
                        NewParameterToOperationDTO.class,
                        ParameterDTO.class),

                ToolSupport.definition(
                        "create_asso",
                        "Create a new association between the specified class (specified by ID) and the another specified class (specified by ID), and return the newly created association information. The association name and the association end A and B role names are set to empty strings.",
                        this::createAssociation,
                        NewAssociationDTO.class,
                        AssociationDTO.class),

                ToolSupport.definition(
                        "create_asso_class",
                        "Create a new association and association class between the specified class (specified by ID) and the another specified class (specified by ID), and return the newly created association information. The association end A and B role names are set to empty strings.",
                        this::createAssociationClass,
                        NewAssociationClassDTO.class,
                        AssociationClassDTO.class),

                ToolSupport.definition(
                        "create_dep",
                        "Create a new dependency between the specified source named element (specified by ID) and the specified target named element (specified by ID), and return the newly created dependency information.",
                        this::createDependency,
                        NewDependencyDTO.class,
                        DependencyDTO.class),

                ToolSupport.definition(
                        "create_gen",
                        "Create a new generalization between the specified sub class (specified by ID) and the specified super class (specified by ID), and return the newly created generalization information.",
                        this::createGeneralization,
                        NewGeneralizationDTO.class,
                        GeneralizationDTO.class),

                ToolSupport.definition(
                        "create_real",
                        "Create a new realization between the specified client (specified by ID) and the specified supplier (specified by ID), and return the newly created realization information.",
                        this::createRealization,
                        NewRealizationDTO.class,
                        RealizationDTO.class),

                ToolSupport.definition(
                        "create_usage",
                        "Create a new usage between the specified client (specified by ID) and the specified supplier (specified by ID), and return the newly created usage information.",
                        this::createUsage,
                        NewUsageDTO.class,
                        UsageDTO.class),

                ToolSupport.definition(
                        "create_qualifier",
                        "Create a new qualifier (type and name) to the specified association end (specified by ID), and return the newly created qualifier information. Limitation: Because an ID of the qualifier type is required, a qualifier of a primitive type cannot be created.",
                        this::createQualifier,
                        NewQualifierToAssociationEndDTO.class,
                        AttributeDTO.class),

                ToolSupport.definition(
                        "create_tagged_val",
                        "Create a new tagged value (name and value) to the specified element (specified by ID), and return the element information after it is edited.",
                        this::createTaggedValue,
                        NewTaggedValueToElementDTO.class,
                        ElementDTO.class),

                ToolSupport.definition(
                        "create_template_param",
                        "Create a template parameter of the specified type (specified by ID) to the specified class (specified by ID), and return the class information after it is edited.",
                        this::createTemplateParameter,
                        NewTemplateParameterToClassDTO.class,
                        ClassDTO.class),

                ToolSupport.definition(
                        "delete_elem",
                        "Delete the specified element (specified by ID), and return the deleted element information.",
                        this::deleteElement,
                        IdDTO.class,
                        ElementDTO.class),

                ToolSupport.definition(
                        "create_req_in_parent_pkg",
                        "Create a new requirement under the specified parent package (specified by ID), and return the newly created requirement information.",
                        this::createRequirementInParentPackage,
                        NewRequirementInPackageDTO.class,
                        RequirementDTO.class),

                ToolSupport.definition(
                        "create_req_in_parent_req",
                        "Create a new requirement under the specified parent requirement (specified by ID), and return the newly created requirement information.",
                        this::createRequirementInParentRequirement,
                        NewRequirementInRequirementDTO.class,
                        RequirementDTO.class),

                ToolSupport.definition(
                        "create_test_case_in_parent_pkg",
                        "Create a new test case under the specified parent package (specified by ID), and return the newly created test case information.",
                        this::createTestCaseInParentPackage,
                        NewTestCaseInPackageDTO.class,
                        TestCaseDTO.class),

                ToolSupport.definition(
                        "create_test_case_in_parent_test_case",
                        "Create a new test case under the specified parent test case (specified by ID), and return the newly created test case information.",
                        this::createTestCaseInParentTestCase,
                        NewTestCaseInTestCaseDTO.class,
                        TestCaseDTO.class),

                ToolSupport.definition(
                        "create_copy_dep",
                        "Create a copy dependency from the specified source requirement (specified by ID) to the specified target requirement (specified by ID), and return the newly created dependency information.",
                        this::createCopyDependency,
                        NewCopyDependencyDTO.class,
                        DependencyDTO.class),

                ToolSupport.definition(
                        "create_derive_req_dep",
                        "Create a DeriveReqt dependency from the specified source requirement (specified by ID) to the specified target requirement (specified by ID), and return the newly created dependency information.",
                        this::createDeriveReqtDependency,
                        NewDeriveReqtDependencyDTO.class,
                        DependencyDTO.class),

                ToolSupport.definition(
                        "create_refine_dep",
                        "Create a refine dependency from the specified source named element (specified by ID) to the specified target requirement (specified by ID), and return the newly created dependency information.",
                        this::createRefineDependency,
                        NewRefineDependencyDTO.class,
                        DependencyDTO.class),

                ToolSupport.definition(
                        "create_satisfy_dep",
                        "Create a satisfy dependency from the specified source named element (specified by ID) to the specified target requirement (specified by ID), and return the newly created dependency information.",
                        this::createSatisfyDependency,
                        NewSatisfyDependencyDTO.class,
                        DependencyDTO.class),

                ToolSupport.definition(
                        "create_trace_dep",
                        "Create a trace dependency from the specified source requirement (specified by ID) to the specified target requirement (specified by ID), and return the newly created dependency information.",
                        this::createTraceDependency,
                        NewTraceDependencyDTO.class,
                        DependencyDTO.class),

                ToolSupport.definition(
                        "create_verify_dep",
                        "Create a verify dependency from the specified source test case (specified by ID) to the specified target requirement (specified by ID), and return the newly created dependency information.",
                        this::createVerifyDependency,
                        NewVerifyDependencyDTO.class,
                        DependencyDTO.class)
        );
    }

    private NamedElementDTO changeParent(McpSyncServerExchange exchange, NamedElementWithParentDTO param) throws Exception {
        log.debug("Change parent: {}", param);

        INamedElement astahTargetNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementid());
        INamedElement newParentAstahNamedElement = astahProToolSupport.getNamedElement(param.newParentNamedElementId());

        try {
            transactionManager.beginTransaction();
            basicModelEditor.changeParent(newParentAstahNamedElement, astahTargetNamedElement);
            transactionManager.endTransaction();

            return NamedElementDTOAssembler.toDTO(astahTargetNamedElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private PackageDTO createPackageInParentPackage(McpSyncServerExchange exchange, NewPackageInPackageDTO param) throws Exception {
        log.debug("Create package in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IPackage createdAstahPackage = basicModelEditor.createPackage(parentAstahPackage, param.newPackageName());
            transactionManager.endTransaction();

            return PackageDTOAssembler.toDTO(createdAstahPackage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ClassDTO createClassInParentPackage(McpSyncServerExchange exchange, NewClassInPackageDTO param) throws Exception {
        log.debug("Create class in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IClass createdAstahClass = basicModelEditor.createClass(parentAstahPackage, param.newClassName());
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(createdAstahClass);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ClassDTO createClassInParentClass(McpSyncServerExchange exchange, NewClassInClassDTO param) throws Exception {
        log.debug("Create class in parent class: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        try {
            transactionManager.beginTransaction();
            IClass createdAstahClass = basicModelEditor.createClass(parentAstahClass, param.newClassName());
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(createdAstahClass);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private EnumerationDTO createEnumerationInParentPackage(McpSyncServerExchange exchange, NewEnumerationInPackageDTO param) throws Exception {
        log.debug("Create enumeration in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IEnumeration createdAstahEnumeration = basicModelEditor.createEnumeration(parentAstahPackage, param.newEnumerationName());
            transactionManager.endTransaction();

            return EnumerationDTOAssembler.toDTO(createdAstahEnumeration);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ClassDTO createInterfaceInParentPackage(McpSyncServerExchange exchange, NewInterfaceInPackageDTO param) throws Exception {
        log.debug("Create interface in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IClass createdAstahInterface = basicModelEditor.createInterface(parentAstahPackage, param.newInterfaceName());
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(createdAstahInterface);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ClassDTO createInterfaceInParentClass(McpSyncServerExchange exchange, NewInterfaceInClassDTO param) throws Exception {
        log.debug("Create interface in parent class: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        try {
            transactionManager.beginTransaction();
            IClass createdAstahInterface = basicModelEditor.createInterface(parentAstahClass, param.newInterfaceName());
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(createdAstahInterface);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO createAttribute(McpSyncServerExchange exchange, NewAttributeInClassDTO param) throws Exception {
        log.debug("Create attribute: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        try {
            transactionManager.beginTransaction();
            IAttribute createdAstahAttribute = basicModelEditor.createAttribute(parentAstahClass, param.newAttributeName(), "int");
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(createdAstahAttribute);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private EnumerationLiteralDTO createEnumerationLiteral(McpSyncServerExchange exchange, NewEnumerationLiteralInEnumerationDTO param) throws Exception {
        log.debug("Create enumeration literal: {}", param);

        IEnumeration astahEnumeration = astahProToolSupport.getEnumeration(param.parentEnumerationId());

        try {
            transactionManager.beginTransaction();
            IEnumerationLiteral createdAstahEnumerationLiteral = basicModelEditor.createEnumerationLiteral(astahEnumeration, param.newEnumerationLiteralName());
            transactionManager.endTransaction();

            return EnumerationLiteralDTOAssembler.toDTO(createdAstahEnumerationLiteral);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private OperationDTO createOperation(McpSyncServerExchange exchange, NewOperationInClassDTO param) throws Exception {
        log.debug("Create operation: {}", param);

        IClass parentAstahClass = astahProToolSupport.getClass(param.parentClassId());

        try {
            transactionManager.beginTransaction();
            IOperation createdAstahOperation = basicModelEditor.createOperation(parentAstahClass, param.newOperationName(), "void");
            transactionManager.endTransaction();

            return OperationDTOAssembler.toDTO(createdAstahOperation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ParameterDTO createParameter(McpSyncServerExchange exchange, NewParameterToOperationDTO param) throws Exception {
        log.debug("Create parameter: {}", param);

        IOperation astahTargetOperation = astahProToolSupport.getOperation(param.targetOperationId());

        try {
            transactionManager.beginTransaction();
            IParameter createdAstahParameter = basicModelEditor.createParameter(
                astahTargetOperation,
                param.newParameterName(),
                "int");
            transactionManager.endTransaction();

            return ParameterDTOAssembler.toDTO(createdAstahParameter);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AssociationDTO createAssociation(McpSyncServerExchange exchange, NewAssociationDTO param) throws Exception {
        log.debug("Create association: {}", param);

        IClass astahSourceClass = astahProToolSupport.getClass(param.sourceClassId());
        IClass astahTargetClass = astahProToolSupport.getClass(param.targetClassId());

        try {
            transactionManager.beginTransaction();
            IAssociation createdAstahAssociation = basicModelEditor.createAssociation(
                astahSourceClass,
                astahTargetClass,
                "",
                "",
                "");

            createdAstahAssociation.getMemberEnds()[0].setNavigability(param.sourceNavigability().toAstahValue());
            createdAstahAssociation.getMemberEnds()[1].setNavigability(param.targetNavigability().toAstahValue());
            createdAstahAssociation.getMemberEnds()[0].setAggregationKind(param.sourceAggregationKind().toAstahValue());
            createdAstahAssociation.getMemberEnds()[1].setAggregationKind(param.targetAggregationKind().toAstahValue());
            transactionManager.endTransaction();

            return AssociationDTOAssembler.toDTO(createdAstahAssociation);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AssociationClassDTO createAssociationClass(McpSyncServerExchange exchange, NewAssociationClassDTO param) throws Exception {
        log.debug("Create association class: {}", param);

        IClass astahSourceClass = astahProToolSupport.getClass(param.sourceClassId());
        IClass astahTargetClass = astahProToolSupport.getClass(param.targetClassId());

        try {
            transactionManager.beginTransaction();
            IAssociationClass createdAstahAssociationClass = basicModelEditor.createAssociationClass(
                astahSourceClass,
                astahTargetClass,
                param.newAssociationClassName(),
                "",
                "");
            transactionManager.endTransaction();

            return AssociationClassDTOAssembler.toDTO(createdAstahAssociationClass);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private DependencyDTO createDependency(McpSyncServerExchange exchange, NewDependencyDTO param) throws Exception {
        log.debug("Create dependency: {}", param);

        INamedElement astahSourceNamedElement = astahProToolSupport.getNamedElement(param.sourceNamedElementId());
        INamedElement astahTargetNamedElement = astahProToolSupport.getNamedElement(param.targetNamedElementId());

        try {
            transactionManager.beginTransaction();
            IDependency createdAstahDependency = basicModelEditor.createDependency(
                astahSourceNamedElement,
                astahTargetNamedElement,
                "");
            transactionManager.endTransaction();

            return DependencyDTOAssembler.toDTO(createdAstahDependency);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private GeneralizationDTO createGeneralization(McpSyncServerExchange exchange, NewGeneralizationDTO param) throws Exception {
        log.debug("Create generalization: {}", param);

        IClass astahSubClass = astahProToolSupport.getClass(param.subClassId());
        IClass astahSuperClass = astahProToolSupport.getClass(param.superClassId());

        try {
            transactionManager.beginTransaction();
            IGeneralization createdAstahGeneralization = basicModelEditor.createGeneralization(
                astahSubClass,
                astahSuperClass,
                "");
            transactionManager.endTransaction();

            return GeneralizationDTOAssembler.toDTO(createdAstahGeneralization);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private RealizationDTO createRealization(McpSyncServerExchange exchange, NewRealizationDTO param) throws Exception {
        log.debug("Create realization: {}", param);

        IClass astahClientClass = astahProToolSupport.getClass(param.clientClassId());
        IClass astahSupplierClass = astahProToolSupport.getClass(param.supplierClassId());

        try {
            transactionManager.beginTransaction();
            IRealization createdAstahRealization = basicModelEditor.createRealization(
                astahClientClass,
                astahSupplierClass,
                "");
            transactionManager.endTransaction();

            return RealizationDTOAssembler.toDTO(createdAstahRealization);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private UsageDTO createUsage(McpSyncServerExchange exchange, NewUsageDTO param) throws Exception {
        log.debug("Create usage: {}", param);

        IClass astahClientClass = astahProToolSupport.getClass(param.clientClassId());
        IClass astahSupplierClass = astahProToolSupport.getClass(param.supplierClassId());

        try {
            transactionManager.beginTransaction();
            IUsage createdAstahUsage = basicModelEditor.createUsage(
                astahClientClass,
                astahSupplierClass,
                "");
            transactionManager.endTransaction();

            return UsageDTOAssembler.toDTO(createdAstahUsage);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private AttributeDTO createQualifier(McpSyncServerExchange exchange, NewQualifierToAssociationEndDTO param) throws Exception {
        log.debug("Create qualifier: {}", param);

        IAttribute astahAssociationEnd = astahProToolSupport.getAttribute(param.targetAssociationEndId());
        IClass astahType = astahProToolSupport.getClass(param.qualifierTypeId());

        try {
            transactionManager.beginTransaction();
            // Create an attribute on the owner class to serve as the qualifier
            IAttribute createdAstahQualifier = basicModelEditor.createQualifier(
                astahAssociationEnd,
                param.newQualifierName(),
                astahType);
            transactionManager.endTransaction();

            return AttributeDTOAssembler.toDTO(createdAstahQualifier);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ElementDTO createTaggedValue(McpSyncServerExchange exchange, NewTaggedValueToElementDTO param) throws Exception {
        log.debug("Create tagged value: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.targetElementId());

        try {
            transactionManager.beginTransaction();
            basicModelEditor.createTaggedValue(
                astahElement,
                param.newKeyOfTaggedValue(),
                param.newValueOfTaggedValue());
            transactionManager.endTransaction();

            return ElementDTOAssembler.toDTO(astahElement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ClassDTO createTemplateParameter(McpSyncServerExchange exchange, NewTemplateParameterToClassDTO param) throws Exception {
        log.debug("Create template parameter: {}", param);

        IClass astahTargetClass = astahProToolSupport.getClass(param.targetClassId());
        IClass astahType = astahProToolSupport.getClass(param.templateParameterTypeId());

        try {
            transactionManager.beginTransaction();
            basicModelEditor.createTemplateParameter(
                astahTargetClass,
                param.newTemplateParameterName(),
                astahType,
                "");
            transactionManager.endTransaction();

            return ClassDTOAssembler.toDTO(astahTargetClass);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private ElementDTO deleteElement(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Delete element: {}", param);

        IElement astahElement = astahProToolSupport.getElement(param.id());

        ElementDTO deletedElementDTO = ElementDTOAssembler.toDTO(astahElement);

        try {
            transactionManager.beginTransaction();
            basicModelEditor.delete(astahElement);
            transactionManager.endTransaction();

            return deletedElementDTO;

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private RequirementDTO createRequirementInParentPackage(McpSyncServerExchange exchange, NewRequirementInPackageDTO param) throws Exception {
        log.debug("Create requirement in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            IRequirement createdAstahRequirement = basicModelEditor.createRequirement(
                parentAstahPackage,
                param.newRequirementName());
            transactionManager.endTransaction();

            return RequirementDTOAssembler.toDTO(createdAstahRequirement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private RequirementDTO createRequirementInParentRequirement(McpSyncServerExchange exchange, NewRequirementInRequirementDTO param) throws Exception {
        log.debug("Create requirement in parent requirement: {}", param);

        IRequirement parentAstahRequirement = astahProToolSupport.getRequirement(param.parentRequirementId());

        try {
            transactionManager.beginTransaction();
            IRequirement createdAstahRequirement = basicModelEditor.createRequirement(
                parentAstahRequirement,
                param.newRequirementName());
            transactionManager.endTransaction();

            return RequirementDTOAssembler.toDTO(createdAstahRequirement);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private TestCaseDTO createTestCaseInParentPackage(McpSyncServerExchange exchange, NewTestCaseInPackageDTO param) throws Exception {
        log.debug("Create test case in parent package: {}", param);

        IPackage parentAstahPackage = astahProToolSupport.getPackage(param.parentPackageId());

        try {
            transactionManager.beginTransaction();
            ITestCase createdAstahTestCase = basicModelEditor.createTestCase(
                parentAstahPackage,
                param.newTestCaseName());
            transactionManager.endTransaction();


            return TestCaseDTOAssembler.toDTO(createdAstahTestCase);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private TestCaseDTO createTestCaseInParentTestCase(McpSyncServerExchange exchange, NewTestCaseInTestCaseDTO param) throws Exception {
        log.debug("Create test case in parent test case: {}", param);

        ITestCase parentAstahTestCase = astahProToolSupport.getTestCase(param.parentTestCaseId());

        try {
            transactionManager.beginTransaction();
            ITestCase createdAstahTestCase = basicModelEditor.createTestCase(
                parentAstahTestCase,
                param.newTestCaseName());
            transactionManager.endTransaction();

            return TestCaseDTOAssembler.toDTO(createdAstahTestCase);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private DependencyDTO createCopyDependency(McpSyncServerExchange exchange, NewCopyDependencyDTO param) throws Exception {
        log.debug("Create copy dependency: {}", param);

        IRequirement sourceAstahRequirement = astahProToolSupport.getRequirement(param.sourceRequirementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        try {
            transactionManager.beginTransaction();
            IDependency createdAstahDependency = basicModelEditor.createCopyDependency(
                sourceAstahRequirement,
                targetAstahRequirement,
                param.newCopyDependencyName());
            transactionManager.endTransaction();

            return DependencyDTOAssembler.toDTO(createdAstahDependency);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private DependencyDTO createDeriveReqtDependency(McpSyncServerExchange exchange, NewDeriveReqtDependencyDTO param) throws Exception {
        log.debug("Create derive reqt dependency: {}", param);

        IRequirement sourceAstahRequirement = astahProToolSupport.getRequirement(param.sourceRequirementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        try {
            transactionManager.beginTransaction();
            IDependency createdAstahDependency = basicModelEditor.createDeriveReqtDependency(
                sourceAstahRequirement,
                targetAstahRequirement,
                param.newDeriveReqtDependencyName());
            transactionManager.endTransaction();

            return DependencyDTOAssembler.toDTO(createdAstahDependency);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private DependencyDTO createRefineDependency(McpSyncServerExchange exchange, NewRefineDependencyDTO param) throws Exception {
        log.debug("Create refine dependency: {}", param);

        IRequirement sourceAstahRequirement = astahProToolSupport.getRequirement(param.sourceRequirementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        try {
            transactionManager.beginTransaction();
            IDependency createdAstahDependency = basicModelEditor.createRefineDependency(
                sourceAstahRequirement,
                targetAstahRequirement,
                param.newRefineDependencyName());
            transactionManager.endTransaction();

            return DependencyDTOAssembler.toDTO(createdAstahDependency);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private DependencyDTO createSatisfyDependency(McpSyncServerExchange exchange, NewSatisfyDependencyDTO param) throws Exception {
        log.debug("Create satisfy dependency: {}", param);

        IRequirement sourceAstahRequirement = astahProToolSupport.getRequirement(param.sourceRequirementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        try {
            transactionManager.beginTransaction();
            IDependency createdAstahDependency = basicModelEditor.createSatisfyDependency(
                sourceAstahRequirement,
                targetAstahRequirement,
                param.newSatisfyDependencyName());
            transactionManager.endTransaction();

            return DependencyDTOAssembler.toDTO(createdAstahDependency);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private DependencyDTO createTraceDependency(McpSyncServerExchange exchange, NewTraceDependencyDTO param) throws Exception {
        log.debug("Create trace dependency: {}", param);

        IRequirement sourceAstahRequirement = astahProToolSupport.getRequirement(param.sourceRequirementId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        try {
            transactionManager.beginTransaction();
            IDependency createdAstahDependency = basicModelEditor.createTraceDependency(
                sourceAstahRequirement,
                targetAstahRequirement,
                param.newTraceDependencyName());
            transactionManager.endTransaction();

            return DependencyDTOAssembler.toDTO(createdAstahDependency);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }

    private DependencyDTO createVerifyDependency(McpSyncServerExchange exchange, NewVerifyDependencyDTO param) throws Exception {
        log.debug("Create verify dependency: {}", param);

        ITestCase sourceAstahTestCase = astahProToolSupport.getTestCase(param.sourceTestCaseId());
        IRequirement targetAstahRequirement = astahProToolSupport.getRequirement(param.targetRequirementId());

        try {
            transactionManager.beginTransaction();
            IDependency createdAstahDependency = basicModelEditor.createVerifyDependency(
                sourceAstahTestCase,
                targetAstahRequirement,
                param.newVerifyDependencyName());
            transactionManager.endTransaction();

            return DependencyDTOAssembler.toDTO(createdAstahDependency);

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
    }
}
