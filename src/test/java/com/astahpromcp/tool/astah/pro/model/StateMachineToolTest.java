package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateMachineDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IStateMachine;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class StateMachineToolTest {

    private ProjectAccessor projectAccessor;
    private StateMachineTool tool;
    private Method getInfo;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/StateMachineToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new StateMachineTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // Methods
        getInfo = TestSupport.getAccessibleMethod(
            StateMachineTool.class,
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
        // Get state machine
        IStateMachine stateMachine = (IStateMachine) TestSupport.instance().getNamedElementByClassAndName(
            IStateMachine.class,
            "StateMachine0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(stateMachine.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        StateMachineDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            StateMachineDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
