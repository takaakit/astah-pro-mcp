package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDiagramDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ActivityDiagramToolTest {

    private ProjectAccessor projectAccessor;
    private ActivityDiagramTool tool;
    private Method getInfo;
    private Method getActivity;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ActivityDiagramTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ActivityDiagramTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ActivityDiagramTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // getActivity() method
        getActivity = TestSupport.getAccessibleMethod(
            ActivityDiagramTool.class,
            "getActivity",
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
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(activityDiagram.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ActivityDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ActivityDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getActivity_ok() throws Exception {
        // Get activity diagram
        IActivityDiagram activityDiagram = (IActivityDiagram) TestSupport.instance().getNamedElement(
            IActivityDiagram.class,
            "Activity Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(activityDiagram.getId());

        // ----------------------------------------
        // Call getActivity()
        // ----------------------------------------
        ActivityDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getActivity,
            tool,
            inputDTO,
            ActivityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
