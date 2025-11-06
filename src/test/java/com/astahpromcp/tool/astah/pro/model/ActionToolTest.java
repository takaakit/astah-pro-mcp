package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ActionWithCallingActivityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActionDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IAction;
import com.change_vision.jude.api.inf.model.IActivity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ActionToolTest {

    private ProjectAccessor projectAccessor;
    private ActionTool tool;
    private Method getInfo;
    private Method setCallingActivity;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ActionTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ActionTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ActionTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setCallingActivity() method
        setCallingActivity = TestSupport.getAccessibleMethod(
            ActionTool.class,
            "setCallingActivity",
            McpSyncServerExchange.class,
            ActionWithCallingActivityDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get action
        IAction action = (IAction) TestSupport.instance().getNamedElement(
            IAction.class,
            "Action0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(action.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ActionDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ActionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setCallingActivity_ok() throws Exception {
        // Get action
        IAction action = (IAction) TestSupport.instance().getNamedElement(
            IAction.class,
            "CallBehaviorAction0");

        // Get activity
        IActivity activity = (IActivity) TestSupport.instance().getNamedElement(
            IActivity.class,
            "Activity1");

        // Create input DTO
        ActionWithCallingActivityDTO inputDTO = new ActionWithCallingActivityDTO(
            action.getId(),
            activity.getId());

        // Check calling activity before setting
        assertNull(action.getCallingActivity());

        // ----------------------------------------
        // Call setCallingActivity()
        // ----------------------------------------
        ActionDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setCallingActivity,
            tool,
            inputDTO,
            ActionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check calling activity after setting
        assertNotNull(action.getCallingActivity());
        assertEquals(activity.getId(), action.getCallingActivity().getId());
    }
}
