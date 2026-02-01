package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithERAttributeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithKeyDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERIndexWithUniqueDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERIndexDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERIndex;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ERIndexToolTest {

    private ProjectAccessor projectAccessor;
    private ERIndexTool tool;
    private Method getInfo;
    private Method addERAttribute;
    private Method removeERAttribute;
    private Method setKey;
    private Method setUnique;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ERIndexToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERIndexTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ERIndexTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // addERAttribute() method
        addERAttribute = TestSupport.getAccessibleMethod(
            ERIndexTool.class,
            "addERAttribute",
            McpSyncServerExchange.class,
            ERIndexWithERAttributeDTO.class);

        // removeERAttribute() method
        removeERAttribute = TestSupport.getAccessibleMethod(
            ERIndexTool.class,
            "removeERAttribute",
            McpSyncServerExchange.class,
            ERIndexWithERAttributeDTO.class);

        // setKey() method
        setKey = TestSupport.getAccessibleMethod(
            ERIndexTool.class,
            "setKey",
            McpSyncServerExchange.class,
            ERIndexWithKeyDTO.class);

        // setUnique() method
        setUnique = TestSupport.getAccessibleMethod(
            ERIndexTool.class,
            "setUnique",
            McpSyncServerExchange.class,
            ERIndexWithUniqueDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER index
        IERIndex erIndex = (IERIndex) TestSupport.instance().getNamedElement(
            IERIndex.class,
            "Index0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erIndex.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ERIndexDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ERIndexDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erIndex.getId(), outputDTO.namedElement().element().id());
        assertEquals(erIndex.getName(), outputDTO.namedElement().name());
    }

    @Test
    void addERAttribute_ok() throws Exception {
        // Get ER index
        IERIndex erIndex = (IERIndex) TestSupport.instance().getNamedElement(
            IERIndex.class,
            "Index0");

        // Get ER attribute
        IERAttribute targetAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute1");

        // Create input DTO
        ERIndexWithERAttributeDTO inputDTO = new ERIndexWithERAttributeDTO(
            erIndex.getId(),
            targetAttribute.getId());

        // ----------------------------------------
        // Call addERAttribute()
        // ----------------------------------------
        ERIndexDTO outputDTO = TestSupport.instance().invokeToolMethod(
            addERAttribute,
            tool,
            inputDTO,
            ERIndexDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(2, erIndex.getERAttributes().length);
    }

    @Test
    void removeERAttribute_ok() throws Exception {
        // Get ER index
        IERIndex erIndex = (IERIndex) TestSupport.instance().getNamedElement(
            IERIndex.class,
            "Index0");
        
        // Get ER attribute
        IERAttribute targetAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Create input DTO
        ERIndexWithERAttributeDTO inputDTO = new ERIndexWithERAttributeDTO(
            erIndex.getId(),
            targetAttribute.getId());

        // ----------------------------------------
        // Call removeERAttribute()
        // ----------------------------------------
        ERIndexDTO outputDTO = TestSupport.instance().invokeToolMethod(
            removeERAttribute,
            tool,
            inputDTO,
            ERIndexDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(0, erIndex.getERAttributes().length);
    }

    @Test
    void setKey_ok() throws Exception {
        // Get ER index
        IERIndex erIndex = (IERIndex) TestSupport.instance().getNamedElement(
            IERIndex.class,
            "Index0");
        boolean nextValue = !erIndex.isKey();

        // Create input DTO
        ERIndexWithKeyDTO inputDTO = new ERIndexWithKeyDTO(
            erIndex.getId(),
            nextValue);

        // ----------------------------------------
        // Call setKey()
        // ----------------------------------------
        ERIndexDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setKey,
            tool,
            inputDTO,
            ERIndexDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nextValue, erIndex.isKey());
    }

    @Test
    void setUnique_ok() throws Exception {
        // Get ER index
        IERIndex erIndex = (IERIndex) TestSupport.instance().getNamedElement(
            IERIndex.class,
            "Index0");
        boolean nextValue = !erIndex.isUnique();

        // Create input DTO
        ERIndexWithUniqueDTO inputDTO = new ERIndexWithUniqueDTO(
            erIndex.getId(),
            nextValue);

        // ----------------------------------------
        // Call setUnique()
        // ----------------------------------------
        ERIndexDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setUnique,
            tool,
            inputDTO,
            ERIndexDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nextValue, erIndex.isUnique());
    }
}
