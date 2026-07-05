package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class OperationToolTest {

    private ProjectAccessor projectAccessor;
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
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/OperationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new OperationTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setAbstract() method
        setAbstract = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setAbstract",
            McpSyncServerExchange.class,
            OperationWithAbstractDTO.class);

        // setLeaf() method
        setLeaf = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setLeaf",
            McpSyncServerExchange.class,
            OperationWithLeafDTO.class);

        // setStatic() method
        setStatic = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setStatic",
            McpSyncServerExchange.class,
            OperationWithStaticDTO.class);

        // setReturnType() method
        setReturnType = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setReturnType",
            McpSyncServerExchange.class,
            OperationWithReturnTypeDTO.class);

        // setReturnTypeExpression() method
        setReturnTypeExpression = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setReturnTypeExpression",
            McpSyncServerExchange.class,
            OperationWithReturnTypeExpressionDTO.class);

        // addPrecondition() method
        addPrecondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "addPrecondition",
            McpSyncServerExchange.class,
            OperationWithPreconditionDTO.class);

        // addPostcondition() method
        addPostcondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "addPostcondition",
            McpSyncServerExchange.class,
            OperationWithPostconditionDTO.class);

        // setBodyCondition() method
        setBodyCondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "setBodyCondition",
            McpSyncServerExchange.class,
            OperationWithBodyConditionDTO.class);

        // removePrecondition() method
        removePrecondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "removePrecondition",
            McpSyncServerExchange.class,
            OperationWithPreconditionDTO.class);

        // removePostcondition() method
        removePostcondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "removePostcondition",
            McpSyncServerExchange.class,
            OperationWithPostconditionDTO.class);

        // removeBodyCondition() method
        removeBodyCondition = TestSupport.getAccessibleMethod(
            OperationTool.class,
            "removeBodyCondition",
            McpSyncServerExchange.class,
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
}
