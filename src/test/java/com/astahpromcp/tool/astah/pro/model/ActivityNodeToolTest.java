package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActivityNodeDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IActivityNode;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ActivityNodeToolTest {

    private ProjectAccessor projectAccessor;
    private ActivityNodeTool tool;
    private Method getInfo;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ActivityNodeTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ActivityNodeTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ActivityNodeTool.class,
            "getInfo",
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
        // Get activity node
        IActivityNode activityNode = (IActivityNode) TestSupport.instance().getNamedElement(
            IActivityNode.class,
            "Object0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(activityNode.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ActivityNodeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ActivityNodeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
