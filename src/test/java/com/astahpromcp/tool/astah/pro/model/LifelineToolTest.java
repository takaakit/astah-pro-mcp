package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LifelineWithBaseClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LifelineDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class LifelineToolTest {

    private ProjectAccessor projectAccessor;
    private LifelineTool tool;
    private Method getInfo;
    private Method setBaseClass;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/LifelineToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new LifelineTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            LifelineTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setBaseClass() method
        setBaseClass = TestSupport.getAccessibleMethod(
            LifelineTool.class,
            "setBaseClass",
            McpSyncServerExchange.class,
            LifelineWithBaseClassDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get lifeline - assuming there's a lifeline in the test model
        ILifeline lifeline = (ILifeline) TestSupport.instance().getNamedElement(
            ILifeline.class,
            "foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(lifeline.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LifelineDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            LifelineDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setBaseClass_ok() throws Exception {
        // Get lifeline
        ILifeline lifeline = (ILifeline) TestSupport.instance().getNamedElement(
            ILifeline.class,
            "foo");
        
        // Get class
        IClass clazz = (IClass) TestSupport.instance().getNamedElement(
            IClass.class,
            "Bar");

        // Create input DTO
        LifelineWithBaseClassDTO inputDTO = new LifelineWithBaseClassDTO(
            lifeline.getId(),
            clazz.getId());

        // Check base class before setting
        assertNotEquals(clazz, lifeline.getBase());

        // ----------------------------------------
        // Call setBaseClass()
        // ----------------------------------------
        LifelineDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setBaseClass,
            tool,
            inputDTO,
            LifelineDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check base class after setting
        assertEquals(clazz, lifeline.getBase());
    }
}
