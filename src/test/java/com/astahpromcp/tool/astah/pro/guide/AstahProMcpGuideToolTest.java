package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.common.outputdto.GuideDTO;
import com.astahpromcp.tool.knowledge.document.DocumentType;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        GuideDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getGuide,
            tool,
            inputDTO,
            GuideDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
    }
    
    @Test
    void getGuide_ok_tellsWhenToReadEveryReferenceDocument() throws Exception {
        GuideDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getGuide,
            tool,
            new NoInputDTO(),
            GuideDTO.class);
        
        for (DocumentType type : DocumentType.values()) {
            assertTrue(outputDTO.contents().contains("'" + type.value() + "'"),
                "the guide does not tell when to read " + type.value());
            assertTrue(outputDTO.contents().contains(type.title()),
                "the guide does not tell when to read " + type.value());
            assertTrue(outputDTO.contents().contains(type.usage()),
                "the guide does not tell when to read " + type.value());
        }
    }
}
