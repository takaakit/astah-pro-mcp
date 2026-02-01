package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERSubtypeRelationshipWithConclusiveDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERSubtypeRelationshipWithDiscriminatorAttrDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERSubtypeRelationshipWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERSubtypeRelationshipWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERSubtypeRelationshipDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERAttribute;
import com.change_vision.jude.api.inf.model.IERSubtypeRelationship;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ERSubtypeRelationshipToolTest {

    private ProjectAccessor projectAccessor;
    private ERSubtypeRelationshipTool tool;
    private Method getInfo;
    private Method setConclusive;
    private Method setDiscriminatorAttr;
    private Method setLogicalName;
    private Method setPhysicalName;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ERSubtypeRelationshipToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERSubtypeRelationshipTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ERSubtypeRelationshipTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setConclusive() method
        setConclusive = TestSupport.getAccessibleMethod(
            ERSubtypeRelationshipTool.class,
            "setConclusive",
            McpSyncServerExchange.class,
            ERSubtypeRelationshipWithConclusiveDTO.class);

        // setDiscriminatorAttr() method
        setDiscriminatorAttr = TestSupport.getAccessibleMethod(
            ERSubtypeRelationshipTool.class,
            "setDiscriminatorAttr",
            McpSyncServerExchange.class,
            ERSubtypeRelationshipWithDiscriminatorAttrDTO.class);

        // setLogicalName() method
        setLogicalName = TestSupport.getAccessibleMethod(
            ERSubtypeRelationshipTool.class,
            "setLogicalName",
            McpSyncServerExchange.class,
            ERSubtypeRelationshipWithLogicalNameDTO.class);

        // setPhysicalName() method
        setPhysicalName = TestSupport.getAccessibleMethod(
            ERSubtypeRelationshipTool.class,
            "setPhysicalName",
            McpSyncServerExchange.class,
            ERSubtypeRelationshipWithPhysicalNameDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER subtype relationship
        IERSubtypeRelationship erSubtypeRelationship = (IERSubtypeRelationship) TestSupport.instance().getNamedElement(
            IERSubtypeRelationship.class,
            "Subtype0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erSubtypeRelationship.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ERSubtypeRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ERSubtypeRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erSubtypeRelationship.getId(), outputDTO.namedElement().element().id());
        assertEquals(erSubtypeRelationship.getName(), outputDTO.namedElement().name());
    }

    @Test
    void setConclusive_ok() throws Exception {
        // Get ER subtype relationship
        IERSubtypeRelationship erSubtypeRelationship = (IERSubtypeRelationship) TestSupport.instance().getNamedElement(
            IERSubtypeRelationship.class,
            "Subtype0");
        boolean nextValue = !erSubtypeRelationship.isConclusive();

        // Create input DTO
        ERSubtypeRelationshipWithConclusiveDTO inputDTO = new ERSubtypeRelationshipWithConclusiveDTO(
            erSubtypeRelationship.getId(),
            nextValue);

        // ----------------------------------------
        // Call setConclusive()
        // ----------------------------------------
        ERSubtypeRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setConclusive,
            tool,
            inputDTO,
            ERSubtypeRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nextValue, erSubtypeRelationship.isConclusive());
    }

    @Test
    void setDiscriminatorAttr_ok() throws Exception {
        // Get ER subtype relationship
        IERSubtypeRelationship erSubtypeRelationship = (IERSubtypeRelationship) TestSupport.instance().getNamedElement(
            IERSubtypeRelationship.class,
            "Subtype0");

        IERAttribute erAttribute = (IERAttribute) TestSupport.instance().getNamedElement(
            IERAttribute.class,
            "Attribute0");

        // Create input DTO
        ERSubtypeRelationshipWithDiscriminatorAttrDTO inputDTO = new ERSubtypeRelationshipWithDiscriminatorAttrDTO(
            erSubtypeRelationship.getId(),
            erAttribute.getId());

        // ----------------------------------------
        // Call setDiscriminatorAttr()
        // ----------------------------------------
        ERSubtypeRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setDiscriminatorAttr,
            tool,
            inputDTO,
            ERSubtypeRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertNotNull(erSubtypeRelationship.getDiscriminatorAttribute());
        assertEquals(erAttribute.getId(), erSubtypeRelationship.getDiscriminatorAttribute().getId());
    }

    @Test
    void setLogicalName_ok() throws Exception {
        // Get ER subtype relationship
        IERSubtypeRelationship erSubtypeRelationship = (IERSubtypeRelationship) TestSupport.instance().getNamedElement(
            IERSubtypeRelationship.class,
            "Subtype0");

        // Create input DTO
        ERSubtypeRelationshipWithLogicalNameDTO inputDTO = new ERSubtypeRelationshipWithLogicalNameDTO(
            erSubtypeRelationship.getId(),
            "LogicalName_777");

        // ----------------------------------------
        // Call setLogicalName()
        // ----------------------------------------
        ERSubtypeRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLogicalName,
            tool,
            inputDTO,
            ERSubtypeRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("LogicalName_777", erSubtypeRelationship.getLogicalName());
    }

    @Test
    void setPhysicalName_ok() throws Exception {
        // Get ER subtype relationship
        IERSubtypeRelationship erSubtypeRelationship = (IERSubtypeRelationship) TestSupport.instance().getNamedElement(
            IERSubtypeRelationship.class,
            "Subtype0");

        // Create input DTO
        ERSubtypeRelationshipWithPhysicalNameDTO inputDTO = new ERSubtypeRelationshipWithPhysicalNameDTO(
            erSubtypeRelationship.getId(),
            "physical_name_777");

        // ----------------------------------------
        // Call setPhysicalName()
        // ----------------------------------------
        ERSubtypeRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setPhysicalName,
            tool,
            inputDTO,
            ERSubtypeRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("physical_name_777", erSubtypeRelationship.getPhysicalName());
    }

}
