package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StateMachineDiagramGuideToolTest {

    private StateMachineDiagramGuideTool tool;
    private Method getGuide;

    @BeforeEach
    void setUp() throws Exception {
        // Tool
        tool = new StateMachineDiagramGuideTool();

        // getGuide() method
        getGuide = TestSupport.getAccessibleMethod(
            StateMachineDiagramGuideTool.class,
            "getGuide",
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
        GuideDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getGuide,
            tool,
            inputDTO,
            GuideDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }
}
