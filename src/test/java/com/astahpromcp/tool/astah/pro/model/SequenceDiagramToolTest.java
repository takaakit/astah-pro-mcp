package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.SequenceDiagramDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class SequenceDiagramToolTest {

    private ProjectAccessor projectAccessor;
    private SequenceDiagramTool tool;
    private Method getInfo;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/SequenceDiagramToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new SequenceDiagramTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            SequenceDiagramTool.class,
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
        // Get sequence diagram
        ISequenceDiagram sequenceDiagram = (ISequenceDiagram) TestSupport.instance().getNamedElementByClassAndName(
            ISequenceDiagram.class,
            "Sequence Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(sequenceDiagram.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        SequenceDiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            SequenceDiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(sequenceDiagram.getId(), outputDTO.diagram().namedElement().element().id());
        assertEquals(sequenceDiagram.getInteraction().getId(), outputDTO.interaction().id());
    }
}
