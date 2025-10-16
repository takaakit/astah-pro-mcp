package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionOperandWithGuardDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionOperandDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class InteractionOperandToolTest {

    private ProjectAccessor projectAccessor;
    private InteractionOperandTool tool;
    private Method getInfo;
    private Method setGuard;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/InteractionOperandToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new InteractionOperandTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            InteractionOperandTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setGuard() method
        setGuard = TestSupport.getAccessibleMethod(
            InteractionOperandTool.class,
            "setGuard",
            McpSyncServerExchange.class,
            InteractionOperandWithGuardDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get interaction operand - assuming there's an interaction operand in the test model
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElement(
            IInteractionOperand.class,
            "");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(interactionOperand.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        InteractionOperandDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            InteractionOperandDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setGuard_ok() throws Exception {
        // Get interaction operand
        IInteractionOperand interactionOperand = (IInteractionOperand) TestSupport.instance().getNamedElement(
            IInteractionOperand.class,
            "");

        // Create input DTO
        InteractionOperandWithGuardDTO inputDTO = new InteractionOperandWithGuardDTO(
            interactionOperand.getId(),
            "x > 0");

        // Check guard before setting
        assertNotEquals("x > 0", interactionOperand.getGuard());

        // ----------------------------------------
        // Call setGuard()
        // ----------------------------------------
        InteractionOperandDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setGuard,
            tool,
            inputDTO,
            InteractionOperandDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check guard after setting
        assertEquals("x > 0", interactionOperand.getGuard());
    }
}
