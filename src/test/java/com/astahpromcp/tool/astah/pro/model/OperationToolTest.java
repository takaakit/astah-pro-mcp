package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.OperationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class OperationToolTest {

    private ProjectAccessor projectAccessor;
    private OperationTool tool;
    private Method getInfo;
    private Method setAbstract;
    private Method setLeaf;
    private Method setStatic;
    private Method setReturnType;
    private Method setReturnTypeExpression;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/OperationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new OperationTool(
            projectAccessor,
            transactionManager,
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
        IOperation operation = (IOperation) TestSupport.instance().getNamedElement(
            IOperation.class,
            "operation0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(operation.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IOperation operation = (IOperation) TestSupport.instance().getNamedElement(
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
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IOperation operation = (IOperation) TestSupport.instance().getNamedElement(
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
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IOperation operation = (IOperation) TestSupport.instance().getNamedElement(
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
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IOperation operation = (IOperation) TestSupport.instance().getNamedElement(
            IOperation.class,
            "operation0");
        
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
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
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IOperation operation = (IOperation) TestSupport.instance().getNamedElement(
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
        OperationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setReturnTypeExpression,
            tool,
            inputDTO,
            OperationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check return type expression after setting
        assertEquals("long", operation.getReturnTypeExpression());
    }
}
