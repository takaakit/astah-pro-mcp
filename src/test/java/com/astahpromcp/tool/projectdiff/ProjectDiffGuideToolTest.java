package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.astah.pro.common.outputdto.GuideDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectDiffGuideToolTest {

    @Test
    void getGuide_ok_fillsInEverySetting() throws Exception {
        Method getGuide = ProjectDiffGuideTool.class.getDeclaredMethod("getGuide", NoInputDTO.class);
        getGuide.setAccessible(true);

        GuideDTO guide = (GuideDTO) getGuide.invoke(new ProjectDiffGuideTool(), new NoInputDTO());

        assertFalse(Pattern.compile("\\{[A-Z_]+}").matcher(guide.contents()).find(), "no setting is left unfilled");
        assertTrue(guide.contents().contains(McpServerConfig.PROJECT_DIFF_MAX_RESPONSE_VALUE_CHARS + " characters"));
        assertTrue(guide.contents().contains(McpServerConfig.PROJECT_DIFF_LAST_RESORT_VALUE_CHARS + " characters"));
    }
}
