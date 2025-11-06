package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.ParameterWithTypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ParameterWithTypeExpressionDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ParameterDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IParameter;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ParameterToolTest {

    private ProjectAccessor projectAccessor;
    private ParameterTool tool;
    private Method setType;
    private Method setTypeExpression;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ParameterToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ParameterTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // setType() method
        setType = TestSupport.getAccessibleMethod(
            ParameterTool.class,
            "setType",
            McpSyncServerExchange.class,
            ParameterWithTypeDTO.class);

        // setTypeExpression() method
        setTypeExpression = TestSupport.getAccessibleMethod(
            ParameterTool.class,
            "setTypeExpression",
            McpSyncServerExchange.class,
            ParameterWithTypeExpressionDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void setType_ok() throws Exception {
        // Get parameter
        IParameter parameter = (IParameter) TestSupport.instance().getNamedElement(
            IParameter.class,
            "param0");
        
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Bar");
        
        // Create input DTO
        ParameterWithTypeDTO inputDTO = new ParameterWithTypeDTO(
            parameter.getId(),
            clazz.getId());

        // Check type before setting
        assertNotEquals(clazz.getId(), parameter.getType().getId());

        // ----------------------------------------
        // Call setType()
        // ----------------------------------------
        ParameterDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setType,
            tool,
            inputDTO,
            ParameterDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check type after setting
        assertEquals(clazz.getId(), parameter.getType().getId());
    }

    @Test
    void setTypeExpression_ok() throws Exception {
        // Get parameter
        IParameter parameter = (IParameter) TestSupport.instance().getNamedElement(
            IParameter.class,
            "param1");
        
        // Create input DTO
        ParameterWithTypeExpressionDTO inputDTO = new ParameterWithTypeExpressionDTO(
            parameter.getId(),
            "long");

        // Check type expression before setting
        assertNotEquals("long", parameter.getTypeExpression());

        // ----------------------------------------
        // Call setTypeExpression()
        // ----------------------------------------
        ParameterDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setTypeExpression,
            tool,
            inputDTO,
            ParameterDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check type expression after setting
        assertEquals("long", parameter.getTypeExpression());
    }
}
