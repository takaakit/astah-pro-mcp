package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateMachineDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IStateMachine;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StateMachineToolTest {

    private ProjectAccessor projectAccessor;
    private StateMachineTool tool;
    private Method getInfo;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/StateMachineToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new StateMachineTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // Methods
        getInfo = TestSupport.getAccessibleMethod(
            StateMachineTool.class,
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
    void getInfo() throws Exception {
        // Get state machine
        IStateMachine stateMachine = (IStateMachine) TestSupport.instance().getNamedElement(
            IStateMachine.class,
            "StateMachine0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(stateMachine.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        StateMachineDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            StateMachineDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
