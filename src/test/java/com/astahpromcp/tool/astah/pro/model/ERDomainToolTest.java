package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithDefaultValueDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithERDatatypeDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithLengthPrecisionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithNotNullDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithParentERDomainDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERDomainWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERDomainDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERDatatype;
import com.change_vision.jude.api.inf.model.IERDomain;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ERDomainToolTest {

    private ProjectAccessor projectAccessor;
    private ERDomainTool tool;
    private Method getInfo;
    private Method setDatatype;
    private Method setDefaultValue;
    private Method setLengthPrecision;
    private Method setLogicalName;
    private Method setPhysicalName;
    private Method setNotNull;
    private Method setParentERDomain;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ERDomainToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERDomainTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ERDomainTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setDatatype() method
        setDatatype = TestSupport.getAccessibleMethod(
            ERDomainTool.class,
            "setDatatype",
            McpSyncServerExchange.class,
            ERDomainWithERDatatypeDTO.class);

        // setDefaultValue() method
        setDefaultValue = TestSupport.getAccessibleMethod(
            ERDomainTool.class,
            "setDefaultValue",
            McpSyncServerExchange.class,
            ERDomainWithDefaultValueDTO.class);

        // setLengthPrecision() method
        setLengthPrecision = TestSupport.getAccessibleMethod(
            ERDomainTool.class,
            "setLengthPrecision",
            McpSyncServerExchange.class,
            ERDomainWithLengthPrecisionDTO.class);

        // setLogicalName() method
        setLogicalName = TestSupport.getAccessibleMethod(
            ERDomainTool.class,
            "setLogicalName",
            McpSyncServerExchange.class,
            ERDomainWithLogicalNameDTO.class);

        // setPhysicalName() method
        setPhysicalName = TestSupport.getAccessibleMethod(
            ERDomainTool.class,
            "setPhysicalName",
            McpSyncServerExchange.class,
            ERDomainWithPhysicalNameDTO.class);

        // setNotNull() method
        setNotNull = TestSupport.getAccessibleMethod(
            ERDomainTool.class,
            "setNotNull",
            McpSyncServerExchange.class,
            ERDomainWithNotNullDTO.class);

        // setParentERDomain() method
        setParentERDomain = TestSupport.getAccessibleMethod(
            ERDomainTool.class,
            "setParentERDomain",
            McpSyncServerExchange.class,
            ERDomainWithParentERDomainDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erDomain.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erDomain.getId(), outputDTO.namedElement().element().id());
        assertEquals(erDomain.getName(), outputDTO.namedElement().name());
    }

    @Test
    void setDatatype_ok() throws Exception {
        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Get ER datatype
        IERDatatype erDatatype = (IERDatatype) TestSupport.instance().getNamedElement(
            IERDatatype.class,
            "INT");

        // Create input DTO
        ERDomainWithERDatatypeDTO inputDTO = new ERDomainWithERDatatypeDTO(
            erDomain.getId(),
            erDatatype.getId());

        // ----------------------------------------
        // Call setDatatype()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setDatatype,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erDatatype.getName(), erDomain.getDatatypeName());
    }

    @Test
    void setDefaultValue_ok() throws Exception {
        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Create input DTO
        ERDomainWithDefaultValueDTO inputDTO = new ERDomainWithDefaultValueDTO(
            erDomain.getId(),
            "DEFAULT_777");

        // ----------------------------------------
        // Call setDefaultValue()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setDefaultValue,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("DEFAULT_777", erDomain.getDefaultValue());
    }

    @Test
    void setLengthPrecision_ok() throws Exception {
        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Create input DTO
        ERDomainWithLengthPrecisionDTO inputDTO = new ERDomainWithLengthPrecisionDTO(
            erDomain.getId(),
            "12,3");

        // ----------------------------------------
        // Call setLengthPrecision()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLengthPrecision,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("12,3", erDomain.getLengthPrecision());
    }

    @Test
    void setLogicalName_ok() throws Exception {
        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Create input DTO
        ERDomainWithLogicalNameDTO inputDTO = new ERDomainWithLogicalNameDTO(
            erDomain.getId(),
            "LogicalName_777");

        // ----------------------------------------
        // Call setLogicalName()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLogicalName,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("LogicalName_777", erDomain.getLogicalName());
    }

    @Test
    void setPhysicalName_ok() throws Exception {
        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Create input DTO
        ERDomainWithPhysicalNameDTO inputDTO = new ERDomainWithPhysicalNameDTO(
            erDomain.getId(),
            "physical_name_777");

        // ----------------------------------------
        // Call setPhysicalName()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setPhysicalName,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("physical_name_777", erDomain.getPhysicalName());
    }

    @Test
    void setNotNull_ok() throws Exception {
        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");
        boolean nextValue = !erDomain.isNotNull();

        // Create input DTO
        ERDomainWithNotNullDTO inputDTO = new ERDomainWithNotNullDTO(
            erDomain.getId(),
            nextValue);

        // ----------------------------------------
        // Call setNotNull()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNotNull,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nextValue, erDomain.isNotNull());
    }

    @Test
    void setParentERDomain_ok() throws Exception {
        // Get ER domain
        IERDomain erDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain0");

        // Get parent ER domain
        IERDomain parentDomain = (IERDomain) TestSupport.instance().getNamedElement(
            IERDomain.class,
            "Domain1");

        // Create input DTO
        ERDomainWithParentERDomainDTO inputDTO = new ERDomainWithParentERDomainDTO(
            erDomain.getId(),
            parentDomain.getId());

        // ----------------------------------------
        // Call setParentERDomain()
        // ----------------------------------------
        ERDomainDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setParentERDomain,
            tool,
            inputDTO,
            ERDomainDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(parentDomain.getChildren()[0].getId(), erDomain.getId());
    }
}
