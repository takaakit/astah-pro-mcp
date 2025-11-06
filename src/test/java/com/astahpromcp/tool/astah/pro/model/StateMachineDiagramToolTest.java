package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.StateMachineDiagramDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StateMachineDiagramToolTest {

    private ProjectAccessor projectAccessor;
    private StateMachineDiagramTool tool;
    private Method getInfo;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/StateMachineDiagramToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new StateMachineDiagramTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // Methods
        getInfo = TestSupport.getAccessibleMethod(
            StateMachineDiagramTool.class,
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
        // Get state machine diagram
        IStateMachineDiagram stateMachineDiagram = (IStateMachineDiagram) TestSupport.instance().getNamedElement(
            IStateMachineDiagram.class,
            "Statemachine Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(stateMachineDiagram.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        StateMachineDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            StateMachineDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
