package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.CommunicationDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ICommunicationDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommunicationDiagramToolTest {

    private ProjectAccessor projectAccessor;
    private CommunicationDiagramTool tool;
    private Method getInfo;
    private Method getInteraction;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/CommunicationDiagramToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new CommunicationDiagramTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            CommunicationDiagramTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // getInteraction() method
        getInteraction = TestSupport.getAccessibleMethod(
            CommunicationDiagramTool.class,
            "getInteraction",
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
        // Get communication diagram
        ICommunicationDiagram communicationDiagram = (ICommunicationDiagram) TestSupport.instance().getNamedElement(
            ICommunicationDiagram.class,
            "Communication Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(communicationDiagram.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        CommunicationDiagramDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            CommunicationDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getInteraction_ok() throws Exception {
        // Get communication diagram
        ICommunicationDiagram communicationDiagram = (ICommunicationDiagram) TestSupport.instance().getNamedElement(
            ICommunicationDiagram.class,
            "Communication Diagram0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(communicationDiagram.getId());

        // ----------------------------------------
        // Call getInteraction()
        // ----------------------------------------
        InteractionDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInteraction,
            tool,
            inputDTO,
            InteractionDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
