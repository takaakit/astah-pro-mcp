package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AstahScriptGuideToolTest {

    private AstahScriptGuideTool tool;
    private Method getGuide;

    @BeforeEach
    void setUp() throws Exception {
        // Tool
        tool = new AstahScriptGuideTool();

        // getGuide() method
        getGuide = TestSupport.getAccessibleMethod(
            AstahScriptGuideTool.class,
            "getGuide",
            NoInputDTO.class);
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

        // Check the output DTO
        assertNotNull(outputDTO);
        assertFalse(outputDTO.contents().isBlank());
    }
}
