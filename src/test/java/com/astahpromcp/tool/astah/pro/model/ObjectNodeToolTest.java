package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ObjectNodeWithBaseDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ObjectNodeDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IObjectNode;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectNodeToolTest {

    private ProjectAccessor projectAccessor;
    private ObjectNodeTool tool;
    private Method getInfo;
    private Method setBase;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ObjectNodeTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ObjectNodeTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ObjectNodeTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setBase() method
        setBase = TestSupport.getAccessibleMethod(
            ObjectNodeTool.class,
            "setBase",
            McpSyncServerExchange.class,
            ObjectNodeWithBaseDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get object node
        IObjectNode objectNode = (IObjectNode) TestSupport.instance().getNamedElement(
            IObjectNode.class,
            "Object0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(objectNode.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ObjectNodeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ObjectNodeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setBase_ok() throws Exception {
        // Get object node
        IObjectNode objectNode = (IObjectNode) TestSupport.instance().getNamedElement(
            IObjectNode.class,
            "Object0");
        
        // Get base class
        IClass baseClass = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Foo");
        
        // Create input DTO
        ObjectNodeWithBaseDTO inputDTO = new ObjectNodeWithBaseDTO(
            objectNode.getId(),
            baseClass.getId());

        // Check base before setting
        assertNotEquals(baseClass, objectNode.getBase());

        // ----------------------------------------
        // Call setBase()
        // ----------------------------------------
        ObjectNodeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setBase,
            tool,
            inputDTO,
            ObjectNodeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check base after setting
        assertEquals(baseClass, objectNode.getBase());
    }
}
