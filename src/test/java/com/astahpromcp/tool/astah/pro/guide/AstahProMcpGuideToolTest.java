package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.guide.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AstahProMcpGuideToolTest {

    private ProjectAccessor projectAccessor;
    private AstahProMcpGuideTool tool;
    private Method getGuide;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.open("src/test/resources/modelfile/guide/AstahProMcpGuideToolTest.asta");

        // Tool
        tool = new AstahProMcpGuideTool(projectAccessor);

        // getGuide() method
        getGuide = TestSupport.getAccessibleMethod(
            AstahProMcpGuideTool.class,
            "getGuide",
            McpSyncServerExchange.class,
            NoInputDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getGuide_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getGuide()
        // ----------------------------------------
        GuideDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getGuide,
            tool,
            inputDTO,
            GuideDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
