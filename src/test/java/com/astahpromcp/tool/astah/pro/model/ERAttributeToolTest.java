package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERAttributeWithDefaultValueDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERAttributeWithERDatatypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERAttributeWithERDomainDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERAttributeWithLengthPrecisionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERAttributeWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERAttributeWithNotNullDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERAttributeWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERAttributeWithPrimaryKeyDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERAttributeDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERDatatype;
import com.change_vision.jude.api.inf.model.IERDomain;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ERAttributeToolTest {

    private ProjectAccessor projectAccessor;
    private ERAttributeTool tool;
    private Method getInfo;
    private Method setDatatype;
    private Method setDomain;
    private Method setDefaultValue;
    private Method setLengthPrecision;
    private Method setLogicalName;
    private Method setPhysicalName;
    private Method setPrimaryKey;
    private Method setNotNull;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ERAttributeToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERAttributeTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setDatatype() method
        setDatatype = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "setDatatype",
            McpSyncServerExchange.class,
            ERAttributeWithERDatatypeDTO.class);

        // setDomain() method
        setDomain = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "setDomain",
            McpSyncServerExchange.class,
            ERAttributeWithERDomainDTO.class);

        // setDefaultValue() method
        setDefaultValue = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "setDefaultValue",
            McpSyncServerExchange.class,
            ERAttributeWithDefaultValueDTO.class);

        // setLengthPrecision() method
        setLengthPrecision = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "setLengthPrecision",
            McpSyncServerExchange.class,
            ERAttributeWithLengthPrecisionDTO.class);

        // setLogicalName() method
        setLogicalName = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "setLogicalName",
            McpSyncServerExchange.class,
            ERAttributeWithLogicalNameDTO.class);

        // setPhysicalName() method
        setPhysicalName = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "setPhysicalName",
            McpSyncServerExchange.class,
            ERAttributeWithPhysicalNameDTO.class);

        // setPrimaryKey() method
        setPrimaryKey = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "setPrimaryKey",
            McpSyncServerExchange.class,
            ERAttributeWithPrimaryKeyDTO.class);

        // setNotNull() method
        setNotNull = TestSupport.getAccessibleMethod(
            ERAttributeTool.class,
            "setNotNull",
            McpSyncServerExchange.class,
            ERAttributeWithNotNullDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erAttribute.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erAttribute.getId(), outputDTO.namedElement().element().id());
        assertEquals(erAttribute.getName(), outputDTO.namedElement().name());
    }

    @Test
    void setDatatype_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        ERAttributeWithERDatatypeDTO inputDTO = new ERAttributeWithERDatatypeDTO(
            erAttribute.getId(),
            erDatatype.getId());

        // ----------------------------------------
        // Call setDatatype()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setDatatype,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erDatatype.getId(), erAttribute.getDatatype().getId());
    }

    @Test
    void setDomain_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Create input DTO
        ERAttributeWithERDomainDTO inputDTO = new ERAttributeWithERDomainDTO(
            erAttribute.getId(),
            erDomain.getId());

        // ----------------------------------------
        // Call setDomain()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setDomain,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erDomain.getId(), erAttribute.getDomain().getId());
    }

    @Test
    void setDefaultValue_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Create input DTO
        ERAttributeWithDefaultValueDTO inputDTO = new ERAttributeWithDefaultValueDTO(
            erAttribute.getId(),
            "DEFAULT_777");

        // ----------------------------------------
        // Call setDefaultValue()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setDefaultValue,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("DEFAULT_777", erAttribute.getDefaultValue());
    }

    @Test
    void setLengthPrecision_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Create input DTO
        ERAttributeWithLengthPrecisionDTO inputDTO = new ERAttributeWithLengthPrecisionDTO(
            erAttribute.getId(),
            "12,3");

        // ----------------------------------------
        // Call setLengthPrecision()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLengthPrecision,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("12,3", erAttribute.getLengthPrecision());
    }

    @Test
    void setLogicalName_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Create input DTO
        ERAttributeWithLogicalNameDTO inputDTO = new ERAttributeWithLogicalNameDTO(
            erAttribute.getId(),
            "LogicalName_777");

        // ----------------------------------------
        // Call setLogicalName()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLogicalName,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("LogicalName_777", erAttribute.getLogicalName());
    }

    @Test
    void setPhysicalName_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Create input DTO
        ERAttributeWithPhysicalNameDTO inputDTO = new ERAttributeWithPhysicalNameDTO(
            erAttribute.getId(),
            "physical_name_777");

        // ----------------------------------------
        // Call setPhysicalName()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setPhysicalName,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("physical_name_777", erAttribute.getPhysicalName());
    }

    @Test
    void setPrimaryKey_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");
        boolean nextValue = !erAttribute.isPrimaryKey();

        // Create input DTO
        ERAttributeWithPrimaryKeyDTO inputDTO = new ERAttributeWithPrimaryKeyDTO(
            erAttribute.getId(),
            nextValue);

        // ----------------------------------------
        // Call setPrimaryKey()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setPrimaryKey,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nextValue, erAttribute.isPrimaryKey());
    }

    @Test
    void setNotNull_ok() throws Exception {
        // Get ER attribute
        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");
        boolean nextValue = !erAttribute.isNotNull();

        // Create input DTO
        ERAttributeWithNotNullDTO inputDTO = new ERAttributeWithNotNullDTO(
            erAttribute.getId(),
            nextValue);

        // ----------------------------------------
        // Call setNotNull()
        // ----------------------------------------
        ERAttributeDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNotNull,
            tool,
            inputDTO,
            ERAttributeDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nextValue, erAttribute.isNotNull());
    }

}
