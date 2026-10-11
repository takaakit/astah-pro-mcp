package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IActivity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivityToolTest {

    private ProjectAccessor projectAccessor;
    private ActivityTool tool;
    private Method getInfo;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/ActivityTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ActivityTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ActivityTool.class,
            "getInfo",
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
        // Get activity
        IActivity activity = (IActivity) TestSupport.instance().getNamedElementByClassAndName(
            IActivity.class,
            "Activity0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(activity.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ActivityDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            ActivityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getInfo_ng_activityBehindDataFlowDiagram() throws Exception {
        IActivity activity = mock(IActivity.class);
        when(activity.getName()).thenReturn("Activity0");
        when(activity.getId()).thenReturn("activity-behind-dfd");
        when(activity.getActivityDiagram()).thenThrow(new ClassCastException(
            "class xR cannot be cast to class com.change_vision.jude.api.inf.model.IActivityDiagram"));

        AstahProToolSupport astahProToolSupport = mock(AstahProToolSupport.class);
        when(astahProToolSupport.getActivity("activity-behind-dfd")).thenReturn(activity);

        ActivityTool toolWithMock = new ActivityTool(
            projectAccessor,
            new TransactionSupport(projectAccessor.getTransactionManager()),
            astahProToolSupport);

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                getInfo,
                toolWithMock,
                new IdDTO("activity-behind-dfd"),
                ActivityDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains(
            "The activity 'Activity0' (ID: activity-behind-dfd) is the one Astah keeps behind a data flow diagram"));
    }
}
