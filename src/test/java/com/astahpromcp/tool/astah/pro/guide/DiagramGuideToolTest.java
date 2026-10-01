package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.DiagramType;
import com.astahpromcp.tool.astah.pro.common.inputdto.DiagramTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DiagramGuideToolTest {

    private DiagramGuideTool tool;
    private Method getGuide;

    @BeforeEach
    void setUp() throws Exception {
        // Tool
        tool = new DiagramGuideTool();

        // getGuide() method
        getGuide = TestSupport.getAccessibleMethod(
            DiagramGuideTool.class,
            "getGuide",
            DiagramTypeDTO.class);
    }

    @Test
    void getGuide_ok_returnsAGuideForEveryDiagramType() throws Exception {
        for (DiagramType diagramType : DiagramType.values()) {
            // ----------------------------------------
            // Call getGuide()
            // ----------------------------------------
            GuideDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
                getGuide,
                tool,
                new DiagramTypeDTO(diagramType),
                GuideDTO.class);

            // Check output DTO
            assertNotNull(outputDTO, diagramType.name());
            assertFalse(outputDTO.contents().isBlank(), diagramType.name());
        }
    }
}
