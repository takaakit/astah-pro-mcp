package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class BasicModelEditorToolTest {

    private ProjectAccessor projectAccessor;
    private BasicModelEditorTool tool;
    private Method changeParent;
    private Method createPackageInParentPackage;
    private Method createClassInParentPackage;
    private Method createClassInParentClass;
    private Method createEnumerationInParentPackage;
    private Method createInterfaceInParentPackage;
    private Method createInterfaceInParentClass;
    private Method createAttribute;
    private Method createEnumerationLiteral;
    private Method createOperation;
    private Method createParameter;
    private Method createAssociation;
    private Method createAssociationClass;
    private Method createDependency;
    private Method createGeneralization;
    private Method createRealization;
    private Method createUsage;
    private Method createQualifier;
    private Method createTaggedValue;
    private Method createTemplateParameter;
    private Method deleteElement;
    private Method createRequirementInParentPackage;
    private Method createRequirementInParentRequirement;
    private Method createTestCaseInParentPackage;
    private Method createTestCaseInParentTestCase;
    private Method createCopyDependency;
    private Method createDeriveReqtDependency;
    private Method createRefineDependency;
    private Method createSatisfyDependency;
    private Method createTraceDependency;
    private Method createVerifyDependency;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.open("src/test/resources/modelfile/editor/BasicModelEditorToolTest.asta");
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        BasicModelEditor basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new BasicModelEditorTool(
            basicModelEditor,
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // changeParent() method
        changeParent = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "changeParent",
            McpSyncServerExchange.class,
            NamedElementWithParentDTO.class);

        // createPackageInParentPackage() method
        createPackageInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createPackageInParentPackage",
            McpSyncServerExchange.class,
            NewPackageInPackageDTO.class);

        // createClassInParentPackage() method
        createClassInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createClassInParentPackage",
            McpSyncServerExchange.class,
            NewClassInPackageDTO.class);

        // createClassInParentClass() method
        createClassInParentClass = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createClassInParentClass",
            McpSyncServerExchange.class,
            NewClassInClassDTO.class);

        // createEnumerationInParentPackage() method
        createEnumerationInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createEnumerationInParentPackage",
            McpSyncServerExchange.class,
            NewEnumerationInPackageDTO.class);

        // createInterfaceInParentPackage() method
        createInterfaceInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createInterfaceInParentPackage",
            McpSyncServerExchange.class,
            NewInterfaceInPackageDTO.class);

        // createInterfaceInParentClass() method
        createInterfaceInParentClass = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createInterfaceInParentClass",
            McpSyncServerExchange.class,
            NewInterfaceInClassDTO.class);

        // createAttribute() method
        createAttribute = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createAttribute",
            McpSyncServerExchange.class,
            NewAttributeInClassDTO.class);

        // createEnumerationLiteral() method
        createEnumerationLiteral = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createEnumerationLiteral",
            McpSyncServerExchange.class,
            NewEnumerationLiteralInEnumerationDTO.class);

        // createOperation() method
        createOperation = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createOperation",
            McpSyncServerExchange.class,
            NewOperationInClassDTO.class);

        // createParameter() method
        createParameter = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createParameter",
            McpSyncServerExchange.class,
            NewParameterToOperationDTO.class);

        // createAssociation() method
        createAssociation = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createAssociation",
            McpSyncServerExchange.class,
            NewAssociationDTO.class);

        // createAssociationClass() method
        createAssociationClass = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createAssociationClass",
            McpSyncServerExchange.class,
            NewAssociationClassDTO.class);

        // createDependency() method
        createDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createDependency",
            McpSyncServerExchange.class,
            NewDependencyDTO.class);

        // createGeneralization() method
        createGeneralization = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createGeneralization",
            McpSyncServerExchange.class,
            NewGeneralizationDTO.class);

        // createRealization() method
        createRealization = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createRealization",
            McpSyncServerExchange.class,
            NewRealizationDTO.class);

        // createUsage() method
        createUsage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createUsage",
            McpSyncServerExchange.class,
            NewUsageDTO.class);

        // createQualifier() method
        createQualifier = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createQualifier",
            McpSyncServerExchange.class,
            NewQualifierToAssociationEndDTO.class);

        // createTaggedValue() method
        createTaggedValue = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTaggedValue",
            McpSyncServerExchange.class,
            NewTaggedValueToElementDTO.class);

        // createTemplateParameter() method
        createTemplateParameter = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTemplateParameter",
            McpSyncServerExchange.class,
            NewTemplateParameterToClassDTO.class);

        // deleteElement() method
        deleteElement = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "deleteElement",
            McpSyncServerExchange.class,
            IdDTO.class);

        // createRequirementInParentPackage() method
        createRequirementInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createRequirementInParentPackage",
            McpSyncServerExchange.class,
            NewRequirementInPackageDTO.class);

        // createRequirementInParentRequirement() method
        createRequirementInParentRequirement = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createRequirementInParentRequirement",
            McpSyncServerExchange.class,
            NewRequirementInRequirementDTO.class);

        // createTestCaseInParentPackage() method
        createTestCaseInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTestCaseInParentPackage",
            McpSyncServerExchange.class,
            NewTestCaseInPackageDTO.class);

        // createTestCaseInParentTestCase() method
        createTestCaseInParentTestCase = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTestCaseInParentTestCase",
            McpSyncServerExchange.class,
            NewTestCaseInTestCaseDTO.class);

        // createCopyDependency() method
        createCopyDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createCopyDependency",
            McpSyncServerExchange.class,
            NewCopyDependencyDTO.class);

        // createDeriveReqtDependency() method
        createDeriveReqtDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createDeriveReqtDependency",
            McpSyncServerExchange.class,
            NewDeriveReqtDependencyDTO.class);

        // createRefineDependency() method
        createRefineDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createRefineDependency",
            McpSyncServerExchange.class,
            NewRefineDependencyDTO.class);

        // createSatisfyDependency() method
        createSatisfyDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createSatisfyDependency",
            McpSyncServerExchange.class,
            NewSatisfyDependencyDTO.class);

        // createTraceDependency() method
        createTraceDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTraceDependency",
            McpSyncServerExchange.class,
            NewTraceDependencyDTO.class);

        // createVerifyDependency() method
        createVerifyDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createVerifyDependency",
            McpSyncServerExchange.class,
            NewVerifyDependencyDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void changeParent_ok() throws Exception {
        // Get named elements
        INamedElement targetElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Foo");
        INamedElement newParentElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Bar");
        
        // Create input DTO
        NamedElementWithParentDTO inputDTO = new NamedElementWithParentDTO(
            targetElement.getId(),
            newParentElement.getId());
        
        // ----------------------------------------
        // Call changeParent()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            changeParent,
            tool,
            inputDTO,
            NamedElementDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createPackageInParentPackage_ok() throws Exception {
        // Get package
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewPackageInPackageDTO inputDTO = new NewPackageInPackageDTO(
            parentPackage.getId(),
            "TestPackage");
        
        // ----------------------------------------
        // Call createPackageInParentPackage()
        // ----------------------------------------
        PackageDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createPackageInParentPackage,
            tool,
            inputDTO,
            PackageDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createClassInParentPackage_ok() throws Exception {
        // Get package
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");

        // Create input DTO
        NewClassInPackageDTO inputDTO = new NewClassInPackageDTO(
            parentPackage.getId(),
            "TestClass");
        
        // ----------------------------------------
        // Call createClassInParentPackage()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createClassInParentPackage,
            tool,
            inputDTO,
            ClassDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createClassInParentClass_ok() throws Exception {
        // Get class
        IClass parentClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewClassInClassDTO inputDTO = new NewClassInClassDTO(
            parentClass.getId(),
            "TestInnerClass");
        
        // ----------------------------------------
        // Call createClassInParentClass()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createClassInParentClass,
            tool,
            inputDTO,
            ClassDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createEnumerationInParentPackage_ok() throws Exception {
        // Get package
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewEnumerationInPackageDTO inputDTO = new NewEnumerationInPackageDTO(
            parentPackage.getId(),
            "TestEnum");
        
        // ----------------------------------------
        // Call createEnumerationInParentPackage()
        // ----------------------------------------
        EnumerationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createEnumerationInParentPackage,
            tool,
            inputDTO,
            EnumerationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInterfaceInParentPackage_ok() throws Exception {
        // Get package
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewInterfaceInPackageDTO inputDTO = new NewInterfaceInPackageDTO(
            parentPackage.getId(),
            "TestInterface");
        
        // ----------------------------------------
        // Call createInterfaceInParentPackage()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createInterfaceInParentPackage,
            tool,
            inputDTO,
            ClassDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createInterfaceInParentClass_ok() throws Exception {
        // Get class
        IClass parentClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewInterfaceInClassDTO inputDTO = new NewInterfaceInClassDTO(
            parentClass.getId(),
            "TestInnerInterface");
        
        // ----------------------------------------
        // Call createInterfaceInParentClass()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createInterfaceInParentClass,
            tool,
            inputDTO,
            ClassDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createAttribute_ok() throws Exception {
        // Get class
        IClass parentClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewAttributeInClassDTO inputDTO = new NewAttributeInClassDTO(
            parentClass.getId(),
            "testAttribute");
        
        // ----------------------------------------
        // Call createAttribute()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createAttribute,
            tool,
            inputDTO,
            AttributeDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createEnumerationLiteral_ok() throws Exception {
        // Get enumeration
        IEnumeration parentEnumeration = (IEnumeration) TestSupport.instance().getNamedElement(
            IEnumeration.class,
            "Enumeration0");
        
        // Create input DTO
        NewEnumerationLiteralInEnumerationDTO inputDTO = new NewEnumerationLiteralInEnumerationDTO(
            parentEnumeration.getId(),
            "TEST_LITERAL");
        
        // ----------------------------------------
        // Call createEnumerationLiteral()
        // ----------------------------------------
        EnumerationLiteralDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createEnumerationLiteral,
            tool,
            inputDTO,
            EnumerationLiteralDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createOperation_ok() throws Exception {
        // Get class
        IClass parentClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewOperationInClassDTO inputDTO = new NewOperationInClassDTO(
            parentClass.getId(),
            "testOperation");
        
        // ----------------------------------------
        // Call createOperation()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createOperation,
            tool,
            inputDTO,
            OperationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createParameter_ok() throws Exception {
        // Get operation
        IOperation targetOperation = (IOperation) TestSupport.instance().getNamedElement(
            IOperation.class,
            "operation0");
        
        // Create input DTO
        NewParameterToOperationDTO inputDTO = new NewParameterToOperationDTO(
            targetOperation.getId(),
            "testParameter");
        
        // ----------------------------------------
        // Call createParameter()
        // ----------------------------------------
        ParameterDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createParameter,
            tool,
            inputDTO,
            ParameterDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createAssociation_ok() throws Exception {
        // Get classes
        IClass sourceClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        IClass targetClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewAssociationDTO inputDTO = new NewAssociationDTO(
            sourceClass.getId(),
            targetClass.getId(),
            com.astahpromcp.tool.astah.pro.common.NavigabilityKind.non_navigable,
            com.astahpromcp.tool.astah.pro.common.NavigabilityKind.navigable,
            com.astahpromcp.tool.astah.pro.common.AggregationKind.aggregate,
            com.astahpromcp.tool.astah.pro.common.AggregationKind.none);
        
        // ----------------------------------------
        // Call createAssociation()
        // ----------------------------------------
        AssociationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createAssociation,
            tool,
            inputDTO,
            AssociationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createAssociationClass_ok() throws Exception {
        // Get classes
        IClass sourceClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        IClass targetClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewAssociationClassDTO inputDTO = new NewAssociationClassDTO(
            sourceClass.getId(),
            targetClass.getId(),
            "TestAssociationClass");
        
        // ----------------------------------------
        // Call createAssociationClass()
        // ----------------------------------------
        AssociationClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createAssociationClass,
            tool,
            inputDTO,
            AssociationClassDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Bar");
        
        // Create input DTO
        NewDependencyDTO inputDTO = new NewDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createGeneralization_ok() throws Exception {
        // Get classes
        IClass subClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        IClass superClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewGeneralizationDTO inputDTO = new NewGeneralizationDTO(
            subClass.getId(),
            superClass.getId());
        
        // ----------------------------------------
        // Call createGeneralization()
        // ----------------------------------------
        GeneralizationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createGeneralization,
            tool,
            inputDTO,
            GeneralizationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createRealization_ok() throws Exception {
        // Get classes
        IClass clientClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        IClass supplierClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewRealizationDTO inputDTO = new NewRealizationDTO(
            clientClass.getId(),
            supplierClass.getId());
        
        // ----------------------------------------
        // Call createRealization()
        // ----------------------------------------
        RealizationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createRealization,
            tool,
            inputDTO,
            RealizationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createUsage_ok() throws Exception {
        // Get classes
        IClass clientClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        IClass supplierClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewUsageDTO inputDTO = new NewUsageDTO(
            clientClass.getId(),
            supplierClass.getId());
        
        // ----------------------------------------
        // Call createUsage()
        // ----------------------------------------
        UsageDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createUsage,
            tool,
            inputDTO,
            UsageDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createQualifier_ok() throws Exception {
        // Get association end
        IAttribute associationEnd = (IAttribute) TestSupport.instance().getNamedElement(
            IAttribute.class,
            "bar");

        // Get qualifier type
        IClass qualifierType = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Integer");
        
        // Create input DTO
        NewQualifierToAssociationEndDTO inputDTO = new NewQualifierToAssociationEndDTO(
            associationEnd.getId(),
            qualifierType.getId(),
            "testQualifier");
        
        // ----------------------------------------
        // Call createQualifier()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createQualifier,
            tool,
            inputDTO,
            AttributeDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTaggedValue_ok() throws Exception {
        // Get element
        INamedElement namedElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        NewTaggedValueToElementDTO inputDTO = new NewTaggedValueToElementDTO(
            namedElement.getId(),
            "testKey",
            "testValue");
        
        // Check tagged value before setting
        assertNotEquals("testValue", namedElement.getTaggedValue("testKey"));
        
        // ----------------------------------------
        // Call createTaggedValue()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTaggedValue,
            tool,
            inputDTO,
            ElementDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Check tagged value after setting
        assertEquals("testValue", namedElement.getTaggedValue("testKey"));
    }

    @Test
    void createTemplateParameter_ok() throws Exception {
        // Get class
        IClass targetClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Get template parameter type
        IClass templateParameterType = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "T");
        
        // Create input DTO
        NewTemplateParameterToClassDTO inputDTO = new NewTemplateParameterToClassDTO(
            targetClass.getId(),
            templateParameterType.getId(),
            "templateParameter");
        
        // ----------------------------------------
        // Call createTemplateParameter()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTemplateParameter,
            tool,
            inputDTO,
            ClassDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void deleteElement_ok() throws Exception {
        // Get element
        INamedElement namedElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());
        
        // ----------------------------------------
        // Call deleteElement()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            deleteElement,
            tool,
            inputDTO,
            ElementDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createRequirementInParentPackage_ok() throws Exception {
        // Get package
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewRequirementInPackageDTO inputDTO = new NewRequirementInPackageDTO(
            parentPackage.getId(),
            "TestRequirement");
        
        // ----------------------------------------
        // Call createRequirementInParentPackage()
        // ----------------------------------------
        RequirementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createRequirementInParentPackage,
            tool,
            inputDTO,
            RequirementDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createRequirementInParentRequirement_ok() throws Exception {
        // Get requirement
        IRequirement parentRequirement = (IRequirement) TestSupport.instance().getNamedElement(
            IRequirement.class,
            "Requirement0");
        
        // Create input DTO
        NewRequirementInRequirementDTO inputDTO = new NewRequirementInRequirementDTO(
            parentRequirement.getId(),
            "TestSubRequirement");
        
        // ----------------------------------------
        // Call createRequirementInParentRequirement()
        // ----------------------------------------
        RequirementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createRequirementInParentRequirement,
            tool,
            inputDTO,
            RequirementDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTestCaseInParentPackage_ok() throws Exception {
        // Get package
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElement(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewTestCaseInPackageDTO inputDTO = new NewTestCaseInPackageDTO(
            parentPackage.getId(),
            "TestTestCase");
        
        // ----------------------------------------
        // Call createTestCaseInParentPackage()
        // ----------------------------------------
        TestCaseDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTestCaseInParentPackage,
            tool,
            inputDTO,
            TestCaseDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTestCaseInParentTestCase_ok() throws Exception {
        // Get test case
        ITestCase parentTestCase = (ITestCase) TestSupport.instance().getNamedElement(
            ITestCase.class,
            "TestCase0");
        
        // Create input DTO
        NewTestCaseInTestCaseDTO inputDTO = new NewTestCaseInTestCaseDTO(
            parentTestCase.getId(),
            "TestSubTestCase");
        
        // ----------------------------------------
        // Call createTestCaseInParentTestCase()
        // ----------------------------------------
        TestCaseDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTestCaseInParentTestCase,
            tool,
            inputDTO,
            TestCaseDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createCopyDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewCopyDependencyDTO inputDTO = new NewCopyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId(),
            "TestCopyDependency");
        
        // ----------------------------------------
        // Call createCopyDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createCopyDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createDeriveReqtDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewDeriveReqtDependencyDTO inputDTO = new NewDeriveReqtDependencyDTO(
            sourceElement.getId(),
            targetElement.getId(),
            "TestDeriveReqtDependency");
        
        // ----------------------------------------
        // Call createDeriveReqtDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createDeriveReqtDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createRefineDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewRefineDependencyDTO inputDTO = new NewRefineDependencyDTO(
            sourceElement.getId(),
            targetElement.getId(),
            "TestRefineDependency");
        
        // ----------------------------------------
        // Call createRefineDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createRefineDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createSatisfyDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewSatisfyDependencyDTO inputDTO = new NewSatisfyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId(),
            "TestSatisfyDependency");
        
        // ----------------------------------------
        // Call createSatisfyDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createSatisfyDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createTraceDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewTraceDependencyDTO inputDTO = new NewTraceDependencyDTO(
            sourceElement.getId(),
            targetElement.getId(),
            "TestTraceDependency");
        
        // ----------------------------------------
        // Call createTraceDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createTraceDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void createVerifyDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "TestCase0");
        INamedElement targetElement = TestSupport.instance().getNamedElement(
            INamedElement.class,
            "Requirement0");
        
        // Create input DTO
        NewVerifyDependencyDTO inputDTO = new NewVerifyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId(),
            "TestVerifyDependency");
        
        // ----------------------------------------
        // Call createVerifyDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethod(
            createVerifyDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }
}
