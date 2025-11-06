package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.RequirementWithIdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.RequirementWithTextDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.RequirementDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IRequirement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class RequirementToolTest {

    private ProjectAccessor projectAccessor;
    private RequirementTool tool;
    private Method getInfo;
    private Method setRequirementId;
    private Method setRequirementText;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/RequirementToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new RequirementTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            RequirementTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setRequirementId() method
        setRequirementId = TestSupport.getAccessibleMethod(
            RequirementTool.class,
            "setRequirementId",
            McpSyncServerExchange.class,
            RequirementWithIdDTO.class);

        // setRequirementText() method
        setRequirementText = TestSupport.getAccessibleMethod(
            RequirementTool.class,
            "setRequirementText",
            McpSyncServerExchange.class,
            RequirementWithTextDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get requirement
        IRequirement requirement = (IRequirement) TestSupport.instance().getNamedElement(
            IRequirement.class,
            "Requirement0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(requirement.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        RequirementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            RequirementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setRequirementId_ok() throws Exception {
        // Get requirement
        IRequirement requirement = (IRequirement) TestSupport.instance().getNamedElement(
            IRequirement.class,
            "Requirement0");
        
        // Create input DTO
        RequirementWithIdDTO inputDTO = new RequirementWithIdDTO(
            requirement.getId(),
            "REQ-001");

        // Check requirement ID before setting
        assertNotEquals("REQ-001", requirement.getRequirementID());

        // ----------------------------------------
        // Call setRequirementId()
        // ----------------------------------------
        RequirementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setRequirementId,
            tool,
            inputDTO,
            RequirementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check requirement ID after setting
        assertEquals("REQ-001", requirement.getRequirementID());
    }

    @Test
    void setRequirementText_ok() throws Exception {
        // Get requirement
        IRequirement requirement = (IRequirement) TestSupport.instance().getNamedElement(
            IRequirement.class,
            "Requirement0");
        
        // Create input DTO
        RequirementWithTextDTO inputDTO = new RequirementWithTextDTO(
            requirement.getId(),
            "This is a test requirement text");

        // Check requirement text before setting
        assertNotEquals("This is a test requirement text", requirement.getRequirementText());

        // ----------------------------------------
        // Call setRequirementText()
        // ----------------------------------------
        RequirementDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setRequirementText,
            tool,
            inputDTO,
            RequirementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check requirement text after setting
        assertEquals("This is a test requirement text", requirement.getRequirementText());
    }
}
