package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.EREntityWithTypeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.EREntityDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IEREntity;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class EREntityToolTest {

    private ProjectAccessor projectAccessor;
    private EREntityTool tool;
    private Method getInfo;
    private Method setLogicalName;
    private Method setPhysicalName;
    private Method setType;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/EREntityToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new EREntityTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            EREntityTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setLogicalName() method
        setLogicalName = TestSupport.getAccessibleMethod(
            EREntityTool.class,
            "setLogicalName",
            McpSyncServerExchange.class,
            EREntityWithLogicalNameDTO.class);

        // setPhysicalName() method
        setPhysicalName = TestSupport.getAccessibleMethod(
            EREntityTool.class,
            "setPhysicalName",
            McpSyncServerExchange.class,
            EREntityWithPhysicalNameDTO.class);

        // setType() method
        setType = TestSupport.getAccessibleMethod(
            EREntityTool.class,
            "setType",
            McpSyncServerExchange.class,
            EREntityWithTypeDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erEntity.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erEntity.getId(), outputDTO.namedElement().element().id());
        assertEquals(erEntity.getName(), outputDTO.namedElement().name());
    }

    @Test
    void setLogicalName_ok() throws Exception {
        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        EREntityWithLogicalNameDTO inputDTO = new EREntityWithLogicalNameDTO(
            erEntity.getId(),
            "LogicalName_777");

        // ----------------------------------------
        // Call setLogicalName()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLogicalName,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("LogicalName_777", erEntity.getLogicalName());
    }

    @Test
    void setPhysicalName_ok() throws Exception {
        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        EREntityWithPhysicalNameDTO inputDTO = new EREntityWithPhysicalNameDTO(
            erEntity.getId(),
            "physical_name_777");

        // ----------------------------------------
        // Call setPhysicalName()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setPhysicalName,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("physical_name_777", erEntity.getPhysicalName());
    }

    @Test
    void setType_ok() throws Exception {
        // Get ER entity
        IEREntity erEntity = (IEREntity) TestSupport.instance().getNamedElement(
            IEREntity.class,
            "Entity0");

        // Create input DTO
        EREntityWithTypeDTO inputDTO = new EREntityWithTypeDTO(
            erEntity.getId(),
            "Event");

        // ----------------------------------------
        // Call setType()
        // ----------------------------------------
        EREntityDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setType,
            tool,
            inputDTO,
            EREntityDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Event", erEntity.getType());
    }
}
