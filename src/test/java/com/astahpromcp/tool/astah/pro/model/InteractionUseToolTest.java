package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionUseWithArgumentDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.InteractionUseWithSequenceDiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.InteractionUseDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IInteractionUse;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class InteractionUseToolTest {

    private ProjectAccessor projectAccessor;
    private InteractionUseTool tool;
    private Method getInfo;
    private Method setArgument;
    private Method setSequenceDiagram;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/InteractionUseToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new InteractionUseTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            InteractionUseTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setArgument() method
        setArgument = TestSupport.getAccessibleMethod(
            InteractionUseTool.class,
            "setArgument",
            McpSyncServerExchange.class,
            InteractionUseWithArgumentDTO.class);

        // setSequenceDiagram() method
        setSequenceDiagram = TestSupport.getAccessibleMethod(
            InteractionUseTool.class,
            "setSequenceDiagram",
            McpSyncServerExchange.class,
            InteractionUseWithSequenceDiagramDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get interaction use - assuming there's an interaction use in the test model
        IInteractionUse interactionUse = (IInteractionUse) TestSupport.instance().getNamedElement(
            IInteractionUse.class,
            "");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(interactionUse.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        InteractionUseDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            InteractionUseDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setArgument_ok() throws Exception {
        // Get interaction use
        IInteractionUse interactionUse = (IInteractionUse) TestSupport.instance().getNamedElement(
            IInteractionUse.class,
            "");

        // Create input DTO
        InteractionUseWithArgumentDTO inputDTO = new InteractionUseWithArgumentDTO(
            interactionUse.getId(),
            "test argument");

        // Check argument before setting
        assertNotEquals("test argument", interactionUse.getArgument());

        // ----------------------------------------
        // Call setArgument()
        // ----------------------------------------
        InteractionUseDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setArgument,
            tool,
            inputDTO,
            InteractionUseDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check argument after setting
        assertEquals("test argument", interactionUse.getArgument());
    }

    @Test
    void setSequenceDiagram_ok() throws Exception {
        // Get interaction use
        IInteractionUse interactionUse = (IInteractionUse) TestSupport.instance().getNamedElement(
            IInteractionUse.class,
            "");
        
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElement(
            ISequenceDiagram.class,
            "Sequence Diagram1");

        // Create input DTO
        InteractionUseWithSequenceDiagramDTO inputDTO = new InteractionUseWithSequenceDiagramDTO(
            interactionUse.getId(),
            sequenceDiagram.getId());

        // Check sequence diagram before setting
        assertNotEquals(sequenceDiagram, interactionUse.getSequenceDiagram());

        // ----------------------------------------
        // Call setSequenceDiagram()
        // ----------------------------------------
        InteractionUseDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setSequenceDiagram,
            tool,
            inputDTO,
            InteractionUseDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check sequence diagram after setting
        assertEquals(sequenceDiagram, interactionUse.getSequenceDiagram());
    }
}
