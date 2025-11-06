package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class AttributeToolTest {

    private ProjectAccessor projectAccessor;
    private AttributeTool tool;
    private Method getInfo;
    private Method setInitialValue;
    private Method setLower;
    private Method setMultiplicityByInt;
    private Method setMultiplicityByString;
    private Method setStatic;
    private Method setType;
    private Method setTypeExpression;
    private Method setUpper;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/AttributeToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new AttributeTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            AttributeTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setInitialValue() method
        setInitialValue = TestSupport.getAccessibleMethod(
            AttributeTool.class,
            "setInitialValue",
            McpSyncServerExchange.class,
            AttributeWithInitialValueDTO.class);

        // setStatic() method
        setStatic = TestSupport.getAccessibleMethod(
            AttributeTool.class,
            "setStatic",
            McpSyncServerExchange.class,
            AttributeWithStaticDTO.class);

        // setType() method
        setType = TestSupport.getAccessibleMethod(
            AttributeTool.class,
            "setType",
            McpSyncServerExchange.class,
            AttributeWithTypeDTO.class);

        // setTypeExpression() method
        setTypeExpression = TestSupport.getAccessibleMethod(
            AttributeTool.class,
            "setTypeExpression",
            McpSyncServerExchange.class,
            AttributeWithTypeExpressionDTO.class);

        // setMultiplicityByInt() method
        setMultiplicityByInt = TestSupport.getAccessibleMethod(
            AttributeTool.class,
            "setMultiplicityByInt",
            McpSyncServerExchange.class,
            AttributeWithIntMultiplicityDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElement(
            IAttribute.class,
            "sum");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(attribute.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(attribute.getId(), outputDTO.namedElement().element().id());
        assertEquals(attribute.getName(), outputDTO.namedElement().name());
    }

    @Test
    void setInitialValue_ok() throws Exception {
        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElement(
            IAttribute.class,
            "sum");
        
        // Create input DTO
        AttributeWithInitialValueDTO inputDTO = new AttributeWithInitialValueDTO(
            attribute.getId(),
            "777");

        // Check initial value before setting
        assertNotEquals("777", attribute.getInitialValue());

        // ----------------------------------------
        // Call setInitialValue()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setInitialValue,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check initial value after setting
        assertEquals("777", attribute.getInitialValue());
    }

    @Test
    void setStatic_ok() throws Exception {
        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElement(
            IAttribute.class,
            "sum");
        
        // Create input DTO
        AttributeWithStaticDTO inputDTO = new AttributeWithStaticDTO(
            attribute.getId(),
            true);

        // Check static before setting
        assertFalse(attribute.isStatic());

        // ----------------------------------------
        // Call setStatic()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setStatic,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check static after setting
        assertTrue(attribute.isStatic());
    }

    @Test
    void setType_ok() throws Exception {
        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElement(
            IAttribute.class,
            "sum");
        
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "String");
        
        // Create input DTO
        AttributeWithTypeDTO inputDTO = new AttributeWithTypeDTO(
            attribute.getId(),
            clazz.getId());

        // Check type before setting
        assertNotEquals(clazz.getId(), attribute.getType().getId());

        // ----------------------------------------
        // Call setType()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setType,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check type after setting
        assertEquals(clazz.getId(), attribute.getType().getId());
    }

    @Test
    void setTypeExpression_ok() throws Exception {
        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElement(
            IAttribute.class,
            "sum");
        
        // Create input DTO
        AttributeWithTypeExpressionDTO inputDTO = new AttributeWithTypeExpressionDTO(
            attribute.getId(),
            "long");

        // Check type expression before setting
        assertNotEquals("long", attribute.getTypeExpression());

        // ----------------------------------------
        // Call setTypeExpression()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setTypeExpression,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check type expression after setting
        assertEquals("long", attribute.getTypeExpression());
    }

    @Test
    void setMultiplicityByInt_ok() throws Exception {
        // Get attribute
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElement(
            IAttribute.class,
            "sum");
        
        // Create input DTO
        AttributeWithIntMultiplicityDTO inputDTO = new AttributeWithIntMultiplicityDTO(
            attribute.getId(),
            2,
            5);

        // Check multiplicity before setting
        assertNotEquals(2, attribute.getMultiplicity()[0].getLower());
        assertNotEquals(5, attribute.getMultiplicity()[0].getUpper());

        // ----------------------------------------
        // Call setMultiplicityByInt()
        // ----------------------------------------
        AttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setMultiplicityByInt,
            tool,
            inputDTO,
            AttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check multiplicity after setting
        assertEquals(2, attribute.getMultiplicity()[0].getLower());
        assertEquals(5, attribute.getMultiplicity()[0].getUpper());
    }
}
