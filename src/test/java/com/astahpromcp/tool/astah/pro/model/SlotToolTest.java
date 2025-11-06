package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.SlotWithValueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.SlotDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ISlot;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class SlotToolTest {

    private ProjectAccessor projectAccessor;
    private SlotTool tool;
    private Method getInfo;
    private Method setValue;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/SlotToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new SlotTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            SlotTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setValue() method
        setValue = TestSupport.getAccessibleMethod(
            SlotTool.class,
            "setValue",
            McpSyncServerExchange.class,
            SlotWithValueDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get slot
        ISlot slot = (ISlot) TestSupport.instance().getNamedElement(
            ISlot.class,
            "attribute0");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(slot.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        SlotDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            SlotDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setValue_ok() throws Exception {
        // Get slot
        ISlot slot = (ISlot) TestSupport.instance().getNamedElement(
            ISlot.class,
            "attribute0");
        
        // Create input DTO
        SlotWithValueDTO inputDTO = new SlotWithValueDTO(
            slot.getId(),
            "new value");

        // Check value before setting
        assertNotEquals("new value", slot.getValue());

        // ----------------------------------------
        // Call setValue()
        // ----------------------------------------
        SlotDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setValue,
            tool,
            inputDTO,
            SlotDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check value after setting
        assertEquals("new value", slot.getValue());
    }
}
