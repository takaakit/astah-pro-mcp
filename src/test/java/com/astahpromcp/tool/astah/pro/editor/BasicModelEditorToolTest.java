package com.astahpromcp.tool.astah.pro.editor;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.Transient;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

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
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        BasicModelEditor basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new BasicModelEditorTool(
            basicModelEditor,
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // changeParent() method
        changeParent = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "changeParent",
            NamedElementWithParentDTO.class);

        // createPackageInParentPackage() method
        createPackageInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createPackageInParentPackage",
            NewPackageInPackageDTO.class);

        // createClassInParentPackage() method
        createClassInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createClassInParentPackage",
            NewClassInPackageDTO.class);

        // createClassInParentClass() method
        createClassInParentClass = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createClassInParentClass",
            NewClassInClassDTO.class);

        // createEnumerationInParentPackage() method
        createEnumerationInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createEnumerationInParentPackage",
            NewEnumerationInPackageDTO.class);

        // createInterfaceInParentPackage() method
        createInterfaceInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createInterfaceInParentPackage",
            NewInterfaceInPackageDTO.class);

        // createInterfaceInParentClass() method
        createInterfaceInParentClass = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createInterfaceInParentClass",
            NewInterfaceInClassDTO.class);

        // createAttribute() method
        createAttribute = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createAttribute",
            NewAttributeInClassDTO.class);

        // createEnumerationLiteral() method
        createEnumerationLiteral = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createEnumerationLiteral",
            NewEnumerationLiteralInEnumerationDTO.class);

        // createOperation() method
        createOperation = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createOperation",
            NewOperationInClassDTO.class);

        // createParameter() method
        createParameter = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createParameter",
            NewParameterToOperationDTO.class);

        // createAssociation() method
        createAssociation = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createAssociation",
            NewAssociationDTO.class);

        // createAssociationClass() method
        createAssociationClass = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createAssociationClass",
            NewAssociationClassDTO.class);

        // createDependency() method
        createDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createDependency",
            NewDependencyDTO.class);

        // createGeneralization() method
        createGeneralization = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createGeneralization",
            NewGeneralizationDTO.class);

        // createRealization() method
        createRealization = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createRealization",
            NewRealizationDTO.class);

        // createUsage() method
        createUsage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createUsage",
            NewUsageDTO.class);

        // createQualifier() method
        createQualifier = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createQualifier",
            NewQualifierToAssociationEndDTO.class);

        // createTaggedValue() method
        createTaggedValue = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTaggedValue",
            NewTaggedValueToElementDTO.class);

        // createTemplateParameter() method
        createTemplateParameter = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTemplateParameter",
            NewTemplateParameterToClassDTO.class);

        // deleteElement() method
        deleteElement = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "deleteElement",
            IdDTO.class);

        // createRequirementInParentPackage() method
        createRequirementInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createRequirementInParentPackage",
            NewRequirementInPackageDTO.class);

        // createRequirementInParentRequirement() method
        createRequirementInParentRequirement = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createRequirementInParentRequirement",
            NewRequirementInRequirementDTO.class);

        // createTestCaseInParentPackage() method
        createTestCaseInParentPackage = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTestCaseInParentPackage",
            NewTestCaseInPackageDTO.class);

        // createTestCaseInParentTestCase() method
        createTestCaseInParentTestCase = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTestCaseInParentTestCase",
            NewTestCaseInTestCaseDTO.class);

        // createCopyDependency() method
        createCopyDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createCopyDependency",
            NewCopyDependencyDTO.class);

        // createDeriveReqtDependency() method
        createDeriveReqtDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createDeriveReqtDependency",
            NewDeriveReqtDependencyDTO.class);

        // createRefineDependency() method
        createRefineDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createRefineDependency",
            NewRefineDependencyDTO.class);

        // createSatisfyDependency() method
        createSatisfyDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createSatisfyDependency",
            NewSatisfyDependencyDTO.class);

        // createTraceDependency() method
        createTraceDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createTraceDependency",
            NewTraceDependencyDTO.class);

        // createVerifyDependency() method
        createVerifyDependency = TestSupport.getAccessibleMethod(
            BasicModelEditorTool.class,
            "createVerifyDependency",
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
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement newParentElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Bar");
        
        // Create input DTO
        NamedElementWithParentDTO inputDTO = new NamedElementWithParentDTO(
            targetElement.getId(),
            newParentElement.getId());
        
        // ----------------------------------------
        // Call changeParent()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewPackageInPackageDTO inputDTO = new NewPackageInPackageDTO(
            parentPackage.getId(),
            "TestPackage");
        
        // ----------------------------------------
        // Call createPackageInParentPackage()
        // ----------------------------------------
        PackageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");

        // Create input DTO
        NewClassInPackageDTO inputDTO = new NewClassInPackageDTO(
            parentPackage.getId(),
            "TestClass");
        
        // ----------------------------------------
        // Call createClassInParentPackage()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IClass parentClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewClassInClassDTO inputDTO = new NewClassInClassDTO(
            parentClass.getId(),
            "TestInnerClass");
        
        // ----------------------------------------
        // Call createClassInParentClass()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewEnumerationInPackageDTO inputDTO = new NewEnumerationInPackageDTO(
            parentPackage.getId(),
            "TestEnum");
        
        // ----------------------------------------
        // Call createEnumerationInParentPackage()
        // ----------------------------------------
        EnumerationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewInterfaceInPackageDTO inputDTO = new NewInterfaceInPackageDTO(
            parentPackage.getId(),
            "TestInterface");
        
        // ----------------------------------------
        // Call createInterfaceInParentPackage()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IClass parentClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewInterfaceInClassDTO inputDTO = new NewInterfaceInClassDTO(
            parentClass.getId(),
            "TestInnerInterface");
        
        // ----------------------------------------
        // Call createInterfaceInParentClass()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IClass parentClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewAttributeInClassDTO inputDTO = new NewAttributeInClassDTO(
            parentClass.getId(),
            "testAttribute");
        
        // ----------------------------------------
        // Call createAttribute()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IEnumeration parentEnumeration = (IEnumeration) TestSupport.instance().getNamedElementByClassAndName(
            IEnumeration.class,
            "Enumeration0");
        
        // Create input DTO
        NewEnumerationLiteralInEnumerationDTO inputDTO = new NewEnumerationLiteralInEnumerationDTO(
            parentEnumeration.getId(),
            "TEST_LITERAL");
        
        // ----------------------------------------
        // Call createEnumerationLiteral()
        // ----------------------------------------
        EnumerationLiteralDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IClass parentClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        NewOperationInClassDTO inputDTO = new NewOperationInClassDTO(
            parentClass.getId(),
            "testOperation");
        
        // ----------------------------------------
        // Call createOperation()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IOperation targetOperation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");
        
        // Create input DTO
        NewParameterToOperationDTO inputDTO = new NewParameterToOperationDTO(
            targetOperation.getId(),
            "testParameter");
        
        // ----------------------------------------
        // Call createParameter()
        // ----------------------------------------
        ParameterDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IClass sourceClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        IClass targetClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewAssociationDTO inputDTO = new NewAssociationDTO(
            sourceClass.getId(),
            targetClass.getId(),
            com.astahpromcp.tool.astah.pro.common.NavigabilityKind.UNSPECIFIED,
            com.astahpromcp.tool.astah.pro.common.NavigabilityKind.NAVIGABLE,
            com.astahpromcp.tool.astah.pro.common.AggregationKind.COMPOSITE,
            com.astahpromcp.tool.astah.pro.common.AggregationKind.NONE);
        
        // ----------------------------------------
        // Call createAssociation()
        // ----------------------------------------
        AssociationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createAssociation,
            tool,
            inputDTO,
            AssociationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        
        // Get created association
        IAssociation createdAssociation = (IAssociation) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Get created association ends
        IAttribute createdAssociationEndA = createdAssociation.getMemberEnds()[0];
        IAttribute createdAssociationEndB = createdAssociation.getMemberEnds()[1];
        
        // Check with the created association
        assertEquals(com.astahpromcp.tool.astah.pro.common.NavigabilityKind.UNSPECIFIED.astahValue, createdAssociationEndA.getNavigability());
        assertEquals(false, createdAssociationEndA.isAggregate());
        assertEquals(true, createdAssociationEndA.isComposite());
        assertEquals(com.astahpromcp.tool.astah.pro.common.NavigabilityKind.NAVIGABLE.astahValue, createdAssociationEndB.getNavigability());
        assertEquals(false, createdAssociationEndB.isAggregate());
        assertEquals(false, createdAssociationEndB.isComposite());
    }

    @Test
    void createAssociationClass_ok() throws Exception {
        // Get classes
        IClass sourceClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        IClass targetClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
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
        AssociationClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Bar");
        
        // Create input DTO
        NewDependencyDTO inputDTO = new NewDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created dependency
        IDependency createdDependency = (IDependency) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created dependency
        assertEquals(sourceElement.getId(), createdDependency.getClient().getId());
        assertEquals(targetElement.getId(), createdDependency.getSupplier().getId());
    }

    @Test
    void createGeneralization_ok() throws Exception {
        // Get classes
        IClass subClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        IClass superClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewGeneralizationDTO inputDTO = new NewGeneralizationDTO(
            subClass.getId(),
            superClass.getId());
        
        // ----------------------------------------
        // Call createGeneralization()
        // ----------------------------------------
        GeneralizationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createGeneralization,
            tool,
            inputDTO,
            GeneralizationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created generalization
        IGeneralization createdGeneralization = (IGeneralization) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created generalization
        assertEquals(subClass.getId(), createdGeneralization.getSubType().getId());
        assertEquals(superClass.getId(), createdGeneralization.getSuperType().getId());
    }

    @Test
    void createRealization_ok() throws Exception {
        // Get classes
        IClass clientClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        IClass supplierClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewRealizationDTO inputDTO = new NewRealizationDTO(
            clientClass.getId(),
            supplierClass.getId());
        
        // ----------------------------------------
        // Call createRealization()
        // ----------------------------------------
        RealizationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createRealization,
            tool,
            inputDTO,
            RealizationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created realization
        IRealization createdRealization = (IRealization) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created realization
        assertEquals(clientClass.getId(), createdRealization.getClient().getId());
        assertEquals(supplierClass.getId(), createdRealization.getSupplier().getId());
    }

    @Test
    void createUsage_ok() throws Exception {
        // Get classes
        IClass clientClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        IClass supplierClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Bar");
        
        // Create input DTO
        NewUsageDTO inputDTO = new NewUsageDTO(
            clientClass.getId(),
            supplierClass.getId());
        
        // ----------------------------------------
        // Call createUsage()
        // ----------------------------------------
        UsageDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createUsage,
            tool,
            inputDTO,
            UsageDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created usage
        IUsage createdUsage = (IUsage) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created usage
        assertEquals(clientClass.getId(), createdUsage.getClient().getId());
        assertEquals(supplierClass.getId(), createdUsage.getSupplier().getId());
    }

    @Test
    void createQualifier_ok() throws Exception {
        // Get association end
        IAttribute associationEnd = (IAttribute) TestSupport.instance().getNamedElementByClassAndName(
            IAttribute.class,
            "bar");

        // Get qualifier type
        IClass qualifierType = (IClass) TestSupport.instance().getNamedElementByClassAndName(
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
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        INamedElement namedElement = TestSupport.instance().getNamedElementByClassAndName(
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
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IClass targetClass = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Get template parameter type
        IClass templateParameterType = (IClass) TestSupport.instance().getNamedElementByClassAndName(
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
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        INamedElement namedElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());
        
        // ----------------------------------------
        // Call deleteElement()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewRequirementInPackageDTO inputDTO = new NewRequirementInPackageDTO(
            parentPackage.getId(),
            "TestRequirement");
        
        // ----------------------------------------
        // Call createRequirementInParentPackage()
        // ----------------------------------------
        RequirementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IRequirement parentRequirement = (IRequirement) TestSupport.instance().getNamedElementByClassAndName(
            IRequirement.class,
            "Requirement0");
        
        // Create input DTO
        NewRequirementInRequirementDTO inputDTO = new NewRequirementInRequirementDTO(
            parentRequirement.getId(),
            "TestSubRequirement");
        
        // ----------------------------------------
        // Call createRequirementInParentRequirement()
        // ----------------------------------------
        RequirementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        IPackage parentPackage = (IPackage) TestSupport.instance().getNamedElementByClassAndName(
            IPackage.class,
            "subPackage");
        
        // Create input DTO
        NewTestCaseInPackageDTO inputDTO = new NewTestCaseInPackageDTO(
            parentPackage.getId(),
            "TestTestCase");
        
        // ----------------------------------------
        // Call createTestCaseInParentPackage()
        // ----------------------------------------
        TestCaseDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        ITestCase parentTestCase = (ITestCase) TestSupport.instance().getNamedElementByClassAndName(
            ITestCase.class,
            "TestCase0");
        
        // Create input DTO
        NewTestCaseInTestCaseDTO inputDTO = new NewTestCaseInTestCaseDTO(
            parentTestCase.getId(),
            "TestSubTestCase");
        
        // ----------------------------------------
        // Call createTestCaseInParentTestCase()
        // ----------------------------------------
        TestCaseDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewCopyDependencyDTO inputDTO = new NewCopyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createCopyDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createCopyDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created dependency
        IDependency createdDependency = (IDependency) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created dependency
        assertEquals(sourceElement.getId(), createdDependency.getClient().getId());
        assertEquals(targetElement.getId(), createdDependency.getSupplier().getId());
        assertEquals("copy", createdDependency.getStereotypes()[0]);
    }

    @Test
    void createCopyDependency_ng_1() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement1");

        // Create input DTO
        NewCopyDependencyDTO inputDTO = new NewCopyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createCopyDependency()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createCopyDependency,
                tool,
                inputDTO,
                DependencyDTO.class);
        });
    }

    @Test
    void createCopyDependency_ng_2() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Bar");

        // Create input DTO
        NewCopyDependencyDTO inputDTO = new NewCopyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createCopyDependency()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createCopyDependency,
                tool,
                inputDTO,
                DependencyDTO.class);
        });
    }

    @Test
    void createDeriveReqtDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewDeriveReqtDependencyDTO inputDTO = new NewDeriveReqtDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createDeriveReqtDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createDeriveReqtDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created dependency
        IDependency createdDependency = (IDependency) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created dependency
        assertEquals(sourceElement.getId(), createdDependency.getClient().getId());
        assertEquals(targetElement.getId(), createdDependency.getSupplier().getId());
        assertEquals("deriveReqt", createdDependency.getStereotypes()[0]);
    }

    @Test
    void createDeriveReqtDependency_ng_1() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement1");

        // Create input DTO
        NewDeriveReqtDependencyDTO inputDTO = new NewDeriveReqtDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createDeriveReqtDependency()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createDeriveReqtDependency,
                tool,
                inputDTO,
                DependencyDTO.class);
        });
    }

    @Test
    void createDeriveReqtDependency_ng_2() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Bar");

        // Create input DTO
        NewDeriveReqtDependencyDTO inputDTO = new NewDeriveReqtDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createDeriveReqtDependency()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createDeriveReqtDependency,
                tool,
                inputDTO,
                DependencyDTO.class);
        });
    }

    @Test
    void createRefineDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewRefineDependencyDTO inputDTO = new NewRefineDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createRefineDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createRefineDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created dependency
        IDependency createdDependency = (IDependency) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created dependency
        assertEquals(sourceElement.getId(), createdDependency.getClient().getId());
        assertEquals(targetElement.getId(), createdDependency.getSupplier().getId());
        assertEquals("refine", createdDependency.getStereotypes()[0]);
    }

    @Test
    void createRefineDependency_ng() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Bar");

        // Create input DTO
        NewRefineDependencyDTO inputDTO = new NewRefineDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createRefineDependency()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createRefineDependency,
                tool,
                inputDTO,
                DependencyDTO.class);
        });
    }

    @Test
    void createSatisfyDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement1");
        
        // Create input DTO
        NewSatisfyDependencyDTO inputDTO = new NewSatisfyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createSatisfyDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createSatisfyDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created dependency
        IDependency createdDependency = (IDependency) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created dependency
        assertEquals(sourceElement.getId(), createdDependency.getClient().getId());
        assertEquals(targetElement.getId(), createdDependency.getSupplier().getId());
        assertEquals("satisfy", createdDependency.getStereotypes()[0]);
    }

    @Test
    void createSatisfyDependency_ng() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Bar");

        // Create input DTO
        NewSatisfyDependencyDTO inputDTO = new NewSatisfyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createSatisfyDependency()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createSatisfyDependency,
                tool,
                inputDTO,
                DependencyDTO.class);
        });
    }

    @Test
    void createTraceDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Bar");
        
        // Create input DTO
        NewTraceDependencyDTO inputDTO = new NewTraceDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createTraceDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createTraceDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created dependency
        IDependency createdDependency = (IDependency) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created dependency
        assertEquals(sourceElement.getId(), createdDependency.getClient().getId());
        assertEquals(targetElement.getId(), createdDependency.getSupplier().getId());
        assertEquals("trace", createdDependency.getStereotypes()[0]);
    }

    @Test
    void createVerifyDependency_ok() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "TestCase0");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement0");
        
        // Create input DTO
        NewVerifyDependencyDTO inputDTO = new NewVerifyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createVerifyDependency()
        // ----------------------------------------
        DependencyDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            createVerifyDependency,
            tool,
            inputDTO,
            DependencyDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);

        // Get created dependency
        IDependency createdDependency = (IDependency) TestSupport.instance().getNamedElementById(
            outputDTO.namedElement().element().id());
        
        // Check with the created dependency
        assertEquals(sourceElement.getId(), createdDependency.getClient().getId());
        assertEquals(targetElement.getId(), createdDependency.getSupplier().getId());
        assertEquals("verify", createdDependency.getStereotypes()[0]);
    }

    @Test
    void createVerifyDependency_ng_1() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement0");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Requirement1");

        // Create input DTO
        NewVerifyDependencyDTO inputDTO = new NewVerifyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createVerifyDependency()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createVerifyDependency,
                tool,
                inputDTO,
                DependencyDTO.class);
        });
    }

    @Test
    void createVerifyDependency_ng_2() throws Exception {
        // Get named elements
        INamedElement sourceElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "TestCase0");
        INamedElement targetElement = TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "TestCase1");

        // Create input DTO
        NewVerifyDependencyDTO inputDTO = new NewVerifyDependencyDTO(
            sourceElement.getId(),
            targetElement.getId());
        
        // ----------------------------------------
        // Call createVerifyDependency()
        // ----------------------------------------
        assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                createVerifyDependency,
                tool,
                inputDTO,
                DependencyDTO.class);
        });
    }
}
