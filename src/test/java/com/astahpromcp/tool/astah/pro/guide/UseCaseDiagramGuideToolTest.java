package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.guide.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UseCaseDiagramGuideToolTest {

    private UseCaseDiagramGuideTool tool;
    private Method getGuide;

    @BeforeEach
    void setUp() throws Exception {
        // Tool
        tool = new UseCaseDiagramGuideTool();

        // getGuide() method
        getGuide = TestSupport.getAccessibleMethod(
            UseCaseDiagramGuideTool.class,
            "getGuide",
            McpSyncServerExchange.class,
            NoInputDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Cleanup if needed
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
