package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithActiveDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithInvariantDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithLeafDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IConstraint;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class ClassToolTest {

    private ProjectAccessor projectAccessor;
    private ClassTool tool;
    private Method getInfo;
    private Method setAbstract;
    private Method setActive;
    private Method setLeaf;
    private Method addInvariant;
    private Method removeInvariant;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        BasicModelEditor basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/ClassToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ClassTool(
            basicModelEditor,
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setAbstract() method
        setAbstract = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "setAbstract",
            McpSyncServerExchange.class,
            ClassWithAbstractDTO.class);

        // setActive() method
        setActive = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "setActive",
            McpSyncServerExchange.class,
            ClassWithActiveDTO.class);

        // setLeaf() method
        setLeaf = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "setLeaf",
            McpSyncServerExchange.class,
            ClassWithLeafDTO.class);

        // addInvariant() method
        addInvariant = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "addInvariant",
            McpSyncServerExchange.class,
            ClassWithInvariantDTO.class);

        // removeInvariant() method
        removeInvariant = TestSupport.getAccessibleMethod(
            ClassTool.class,
            "removeInvariant",
            McpSyncServerExchange.class,
            ClassWithInvariantDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(clazz.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setAbstract_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        ClassWithAbstractDTO inputDTO = new ClassWithAbstractDTO(
            clazz.getId(),
            true);

        // Check abstract before setting
        assertFalse(clazz.isAbstract());

        // ----------------------------------------
        // Call setAbstract()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setAbstract,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check abstract after setting
        assertTrue(clazz.isAbstract());
    }

    @Test
    void setActive_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        ClassWithActiveDTO inputDTO = new ClassWithActiveDTO(
            clazz.getId(),
            true);

        // Check active before setting
        assertFalse(clazz.isActive());

        // ----------------------------------------
        // Call setActive()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setActive,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check active after setting
        assertTrue(clazz.isActive());
    }

    @Test
    void setLeaf_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");
        
        // Create input DTO
        ClassWithLeafDTO inputDTO = new ClassWithLeafDTO(
            clazz.getId(),
            true);

        // Check leaf before setting
        assertFalse(clazz.isLeaf());

        // ----------------------------------------
        // Call setLeaf()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setLeaf,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check leaf after setting
        assertTrue(clazz.isLeaf());
    }

    @Test
    void addInvariant_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Create input DTO
        ClassWithInvariantDTO inputDTO = new ClassWithInvariantDTO(
            clazz.getId(),
            "x > 0");

        // Check invariants before adding
        int beforeCount = clazz.getConstraints().length;

        // ----------------------------------------
        // Call addInvariant()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            addInvariant,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check invariants after adding
        IConstraint[] constraints = clazz.getConstraints();
        assertEquals(beforeCount + 1, constraints.length);
        assertEquals("x > 0", constraints[constraints.length - 1].getName());
    }

    @Test
    void removeInvariant_ok() throws Exception {
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElementByClassAndName(
            IClass.class,
            "Foo");

        // Add an invariant first
        ClassWithInvariantDTO addDTO = new ClassWithInvariantDTO(
            clazz.getId(),
            "x > 0");
        TestSupport.instance().invokeToolMethodReturningDto(
            addInvariant,
            tool,
            addDTO,
            ClassDTO.class);

        // Check invariants before removing
        int beforeCount = clazz.getConstraints().length;
        assertTrue(beforeCount > 0);

        // Create input DTO
        ClassWithInvariantDTO inputDTO = new ClassWithInvariantDTO(
            clazz.getId(),
            "x > 0");

        // ----------------------------------------
        // Call removeInvariant()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            removeInvariant,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check invariants after removing
        IConstraint[] constraints = clazz.getConstraints();
        assertEquals(beforeCount - 1, constraints.length);
        for (IConstraint constraint : constraints) {
            assertNotEquals("x > 0", constraint.getName());
        }
    }
}
