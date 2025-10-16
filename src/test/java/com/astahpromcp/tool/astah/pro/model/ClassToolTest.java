package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithActiveDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithLeafDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ClassToolTest {

    private ProjectAccessor projectAccessor;
    private ClassTool tool;
    private Method getInfo;
    private Method setAbstract;
    private Method setActive;
    private Method setLeaf;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ClassToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ClassTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

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
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(clazz.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
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
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
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
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
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
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
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
        ClassDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLeaf,
            tool,
            inputDTO,
            ClassDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check leaf after setting
        assertTrue(clazz.isLeaf());
    }
}
