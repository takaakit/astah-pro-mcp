package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class OperationToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private OperationTool tool;
    private Method getInfo;
    private Method setAbstract;
    private Method setLeaf;
    private Method setStatic;
    private Method setReturnType;
    private Method setReturnTypeExpression;
    private Method addPrecondition;
    private Method addPostcondition;
    private Method setBodyCondition;
    private Method removePrecondition;
    private Method removePostcondition;
    private Method removeBodyCondition;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/OperationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new OperationTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "getInfo",
            IdDTO.class);

        // setAbstract() method
        setAbstract = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setAbstract",
            OperationWithAbstractDTO.class);

        // setLeaf() method
        setLeaf = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setLeaf",
            OperationWithLeafDTO.class);

        // setStatic() method
        setStatic = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setStatic",
            OperationWithStaticDTO.class);

        // setReturnType() method
        setReturnType = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setReturnType",
            OperationWithReturnTypeDTO.class);

        // setReturnTypeExpression() method
        setReturnTypeExpression = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setReturnTypeExpression",
            OperationWithReturnTypeExpressionDTO.class);

        // addPrecondition() method
        addPrecondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "addPrecondition",
            OperationWithPreconditionDTO.class);

        // addPostcondition() method
        addPostcondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "addPostcondition",
            OperationWithPostconditionDTO.class);

        // setBodyCondition() method
        setBodyCondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setBodyCondition",
            OperationWithBodyConditionDTO.class);

        // removePrecondition() method
        removePrecondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "removePrecondition",
            OperationWithPreconditionDTO.class);

        // removePostcondition() method
        removePostcondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "removePostcondition",
            OperationWithPostconditionDTO.class);

        // removeBodyCondition() method
        removeBodyCondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "removeBodyCondition",
            IdDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(operation.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setAbstract_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        OperationWithAbstractDTO inputDTO = new OperationWithAbstractDTO(
            operation.getId(),
            true);

        // Check abstract before setting
        assertFalse(operation.isAbstract());

        // ----------------------------------------
        // Call setAbstract()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setAbstract,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check abstract after setting
        assertTrue(operation.isAbstract());
    }

    @Test
    void setLeaf_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        OperationWithLeafDTO inputDTO = new OperationWithLeafDTO(
            operation.getId(),
            true);

        // Check leaf before setting
        assertFalse(operation.isLeaf());

        // ----------------------------------------
        // Call setLeaf()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setLeaf,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check leaf after setting
        assertTrue(operation.isLeaf());
    }

    @Test
    void setStatic_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        OperationWithStaticDTO inputDTO = new OperationWithStaticDTO(
            operation.getId(),
            true);

        // Check static before setting
        assertFalse(operation.isStatic());

        // ----------------------------------------
        // Call setStatic()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setStatic,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check static after setting
        assertTrue(operation.isStatic());
    }

    @Test
    void setReturnType_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Bar");

        // Create input DTO
        OperationWithReturnTypeDTO inputDTO = new OperationWithReturnTypeDTO(
            operation.getId(),
            clazz.getId());

        // Check return type before setting
        assertNotEquals(clazz.getId(), operation.getReturnType().getId());

        // ----------------------------------------
        // Call setReturnType()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setReturnType,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check return type after setting
        assertEquals(clazz.getId(), operation.getReturnType().getId());
    }

    @Test
    void setReturnTypeExpression_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        OperationWithReturnTypeExpressionDTO inputDTO = new OperationWithReturnTypeExpressionDTO(
            operation.getId(),
            "long");

        assertNotEquals("long", operation.getReturnTypeExpression());

        // ----------------------------------------
        // Call setReturnTypeExpression()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setReturnTypeExpression,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check return type expression after setting
        assertEquals("long", operation.getReturnTypeExpression());
    }

    @Test
    void addPrecondition_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        OperationWithPreconditionDTO inputDTO = new OperationWithPreconditionDTO(
            operation.getId(),
            "x > 0");

        // Check preconditions before adding
        int beforeCount = operation.getPreConditions().length;

        // ----------------------------------------
        // Call addPrecondition()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addPrecondition,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check preconditions after adding
        String[] preconditions = operation.getPreConditions();
        assertEquals(beforeCount + 1, preconditions.length);
        assertEquals("x > 0", preconditions[preconditions.length - 1]);
    }

    @Test
    void addPostcondition_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        OperationWithPostconditionDTO inputDTO = new OperationWithPostconditionDTO(
            operation.getId(),
            "result > 0");

        // Check postconditions before adding
        int beforeCount = operation.getPostConditions().length;

        // ----------------------------------------
        // Call addPostcondition()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addPostcondition,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check postconditions after adding
        String[] postconditions = operation.getPostConditions();
        assertEquals(beforeCount + 1, postconditions.length);
        assertEquals("result > 0", postconditions[postconditions.length - 1]);
    }

    @Test
    void setBodyCondition_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Create input DTO
        OperationWithBodyConditionDTO inputDTO = new OperationWithBodyConditionDTO(
            operation.getId(),
            "result = x * 2");

        // Check body condition before setting
        assertNotEquals("result = x * 2", operation.getBodyCondition());

        // ----------------------------------------
        // Call setBodyCondition()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setBodyCondition,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check body condition after setting
        assertEquals("result = x * 2", operation.getBodyCondition());
    }

    @Test
    void removePrecondition_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Add a precondition first
        OperationWithPreconditionDTO addDTO = new OperationWithPreconditionDTO(
            operation.getId(),
            "x > 0");
        TestSupport.instance().invokeToolMethodReturningDto(
            addPrecondition,
            tool,
            addDTO,
            OperationDTO.class);

        // Check preconditions before removing
        int beforeCount = operation.getPreConditions().length;
        assertTrue(beforeCount > 0);

        // Create input DTO
        OperationWithPreconditionDTO inputDTO = new OperationWithPreconditionDTO(
            operation.getId(),
            "x > 0");

        // ----------------------------------------
        // Call removePrecondition()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removePrecondition,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check preconditions after removing
        String[] preconditions = operation.getPreConditions();
        assertEquals(beforeCount - 1, preconditions.length);
        for (String precondition : preconditions) {
            assertNotEquals("x > 0", precondition);
        }
    }

    @Test
    void removePostcondition_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Add a postcondition first
        OperationWithPostconditionDTO addDTO = new OperationWithPostconditionDTO(
            operation.getId(),
            "result > 0");
        TestSupport.instance().invokeToolMethodReturningDto(
            addPostcondition,
            tool,
            addDTO,
            OperationDTO.class);

        // Check postconditions before removing
        int beforeCount = operation.getPostConditions().length;
        assertTrue(beforeCount > 0);

        // Create input DTO
        OperationWithPostconditionDTO inputDTO = new OperationWithPostconditionDTO(
            operation.getId(),
            "result > 0");

        // ----------------------------------------
        // Call removePostcondition()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removePostcondition,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check postconditions after removing
        String[] postconditions = operation.getPostConditions();
        assertEquals(beforeCount - 1, postconditions.length);
        for (String postcondition : postconditions) {
            assertNotEquals("result > 0", postcondition);
        }
    }

    @Test
    void removeBodyCondition_ok() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Set a body condition first
        OperationWithBodyConditionDTO setDTO = new OperationWithBodyConditionDTO(
            operation.getId(),
            "result = x * 2");
        TestSupport.instance().invokeToolMethodReturningDto(
            setBodyCondition,
            tool,
            setDTO,
            OperationDTO.class);
        assertEquals("result = x * 2", operation.getBodyCondition());

        // Create input DTO
        IdDTO inputDTO = new IdDTO(operation.getId());

        // ----------------------------------------
        // Call removeBodyCondition()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeBodyCondition,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check body condition after removing
        assertEquals("", operation.getBodyCondition());
    }

    @Test
    void addPrecondition_ok_stripsSurroundingWhitespace() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // ----------------------------------------
        // Call addPrecondition()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            addPrecondition,
            tool,
            new OperationWithPreconditionDTO(operation.getId(), "  x > 0\n  y > 0 \n"),
            OperationDTO.class);

        // Check that the surrounding whitespace is removed and the inner whitespace is kept
        String[] preconditions = operation.getPreConditions();
        assertEquals("x > 0\n  y > 0", preconditions[preconditions.length - 1]);
    }

    @Test
    void addPrecondition_ng_alreadyHas() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Add a precondition first
        TestSupport.instance().invokeToolMethodReturningDto(
            addPrecondition,
            tool,
            new OperationWithPreconditionDTO(operation.getId(), "x > 0"),
            OperationDTO.class);
        String[] preconditionsBefore = operation.getPreConditions();

        // ----------------------------------------
        // Call addPrecondition()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addPrecondition,
                tool,
                new OperationWithPreconditionDTO(operation.getId(), " x > 0 "),
                OperationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("already has the precondition 'x > 0'"));
        assertTrue(exception.getCause().getMessage().contains("Existing preconditions: " + Arrays.asList(preconditionsBefore)));

        // Check that the precondition is not duplicated
        assertArrayEquals(preconditionsBefore, operation.getPreConditions());
    }

    @Test
    void addPrecondition_ng_blank() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");
        String[] preconditionsBefore = operation.getPreConditions();

        // ----------------------------------------
        // Call addPrecondition()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addPrecondition,
                tool,
                new OperationWithPreconditionDTO(operation.getId(), "   "),
                OperationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("The precondition must not be empty or blank.", exception.getCause().getMessage());

        // Check that nothing was added
        assertArrayEquals(preconditionsBefore, operation.getPreConditions());
    }

    @Test
    void addPostcondition_ng_alreadyHas() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Add a postcondition first
        TestSupport.instance().invokeToolMethodReturningDto(
            addPostcondition,
            tool,
            new OperationWithPostconditionDTO(operation.getId(), "result > 0"),
            OperationDTO.class);
        String[] postconditionsBefore = operation.getPostConditions();

        // ----------------------------------------
        // Call addPostcondition()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                addPostcondition,
                tool,
                new OperationWithPostconditionDTO(operation.getId(), "result > 0\n"),
                OperationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("already has the postcondition 'result > 0'"));

        // Check that the postcondition is not duplicated
        assertArrayEquals(postconditionsBefore, operation.getPostConditions());
    }

    @Test
    void removePrecondition_ok_removesEveryMatchAsStored() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");
        int beforeCount = operation.getPreConditions().length;

        // Add duplicates directly, as they may have been added before the tool rejected them or stripped them
        transactionSupport.run(() -> {
            operation.addPreCondition("x > 0");
            operation.addPreCondition(" x > 0 ");
        });
        assertEquals(beforeCount + 2, operation.getPreConditions().length);

        // ----------------------------------------
        // Call removePrecondition()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            removePrecondition,
            tool,
            new OperationWithPreconditionDTO(operation.getId(), "x > 0"),
            OperationDTO.class);

        // Check that both are removed
        String[] preconditions = operation.getPreConditions();
        assertEquals(beforeCount, preconditions.length);
        for (String precondition : preconditions) {
            assertNotEquals("x > 0", precondition.strip());
        }
    }

    @Test
    void removePrecondition_ng_notFound() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");

        // Add a precondition first
        TestSupport.instance().invokeToolMethodReturningDto(
            addPrecondition,
            tool,
            new OperationWithPreconditionDTO(operation.getId(), "x >= 0"),
            OperationDTO.class);
        String[] preconditionsBefore = operation.getPreConditions();

        // ----------------------------------------
        // Call removePrecondition()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                removePrecondition,
                tool,
                new OperationWithPreconditionDTO(operation.getId(), "x > 0"),
                OperationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("has no precondition 'x > 0'"));
        assertTrue(exception.getCause().getMessage().contains("Existing preconditions: " + Arrays.asList(preconditionsBefore)));

        // Check that nothing was removed
        assertArrayEquals(preconditionsBefore, operation.getPreConditions());
    }

    @Test
    void removePostcondition_ok_removesEveryMatchAsStored() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");
        int beforeCount = operation.getPostConditions().length;

        // Add duplicates directly, as they may have been added before the tool rejected them or stripped them
        transactionSupport.run(() -> {
            operation.addPostCondition("result > 0");
            operation.addPostCondition("result > 0\n");
        });
        assertEquals(beforeCount + 2, operation.getPostConditions().length);

        // ----------------------------------------
        // Call removePostcondition()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            removePostcondition,
            tool,
            new OperationWithPostconditionDTO(operation.getId(), "result > 0"),
            OperationDTO.class);

        // Check that both are removed
        String[] postconditions = operation.getPostConditions();
        assertEquals(beforeCount, postconditions.length);
        for (String postcondition : postconditions) {
            assertNotEquals("result > 0", postcondition.strip());
        }
    }

    @Test
    void removePostcondition_ng_notFound() throws Exception {
        // Get operation
        IOperation operation = (IOperation) TestSupport.instance().getNamedElementByClassAndName(
            IOperation.class,
            "operation0");
        String[] postconditionsBefore = operation.getPostConditions();

        // ----------------------------------------
        // Call removePostcondition()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                removePostcondition,
                tool,
                new OperationWithPostconditionDTO(operation.getId(), "result > 0"),
                OperationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("has no postcondition 'result > 0'"));

        // Check that nothing was removed
        assertArrayEquals(postconditionsBefore, operation.getPostConditions());
    }
}
