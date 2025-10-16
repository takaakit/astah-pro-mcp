package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithStereotypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithTaggedValueDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ElementWithTypeModifierDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ElementToolTest {

    private ProjectAccessor projectAccessor;
    private ElementTool tool;
    private Method addStereotype;
    private Method removeStereotype;
    private Method setTypeModifier;
    private Method changeTaggedValue;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ElementToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ElementTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // addStereotype() method
        addStereotype = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "addStereotype",
            McpSyncServerExchange.class,
            ElementWithStereotypeDTO.class);

        // removeStereotype() method
        removeStereotype = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "removeStereotype",
            McpSyncServerExchange.class,
            ElementWithStereotypeDTO.class);

        // setTypeModifier() method
        setTypeModifier = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "setTypeModifier",
            McpSyncServerExchange.class,
            ElementWithTypeModifierDTO.class);

        // changeTaggedValue() method
        changeTaggedValue = TestSupport.getAccessibleMethod(
            ElementTool.class,
            "changeTaggedValue",
            McpSyncServerExchange.class,
            ElementWithTaggedValueDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void addStereotype_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "testStereotype");

        // Check stereotype before adding
        assertFalse(Arrays.asList(clazz.getStereotypes()).contains("testStereotype"));

        // ----------------------------------------
        // Call addStereotype()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            addStereotype,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check stereotype after adding
        assertTrue(Arrays.asList(clazz.getStereotypes()).contains("testStereotype"));
    }

    @Test
    void removeStereotype_ok() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");

        // Create input DTO
        ElementWithStereotypeDTO inputDTO = new ElementWithStereotypeDTO(
            clazz.getId(),
            "singleton");
        
        // Check stereotype before removing
        assertTrue(Arrays.asList(clazz.getStereotypes()).contains("singleton"));

        // ----------------------------------------
        // Call removeStereotype()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            removeStereotype,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check stereotype after removing
        assertFalse(Arrays.asList(clazz.getStereotypes()).contains("singleton"));
    }

    @Test
    void setTypeModifier_ok() throws Exception {
        // Get element
        IAttribute attribute = (IAttribute) TestSupport.instance().getNamedElement(
            IAttribute.class,
            "sum");
        
        // Create input DTO
        ElementWithTypeModifierDTO inputDTO = new ElementWithTypeModifierDTO(
            attribute.getId(),
            "*");

        // Check type modifier before setting
        assertNotEquals("*", attribute.getTypeModifier());

        // ----------------------------------------
        // Call setTypeModifier()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setTypeModifier,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check type modifier after setting
        assertEquals("*", attribute.getTypeModifier());
    }

    @Test
    void changeTaggedValue_ok() throws Exception {
        // Get element
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        ElementWithTaggedValueDTO inputDTO = new ElementWithTaggedValueDTO(
            clazz.getId(),
            "author",
            "John Doe");

        // Check tagged value before setting
        assertNotEquals("John Doe", clazz.getTaggedValue("author"));

        // ----------------------------------------
        // Call changeTaggedValue()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            changeTaggedValue,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        assertEquals("John Doe", clazz.getTaggedValue("author"));
    }
}
