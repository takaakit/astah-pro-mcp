package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithCardinalityDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithChildVerbPhraseDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithERIndexDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithLogicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithParentRequiredDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithParentVerbPhraseDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ERRelationshipWithPhysicalNameDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ERRelationshipDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IERIndex;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ERRelationshipToolTest {

    private ProjectAccessor projectAccessor;
    private ERRelationshipTool tool;
    private Method getInfo;
    private Method setCardinality;
    private Method setERIndex;
    private Method setLogicalName;
    private Method setPhysicalName;
    private Method setParentRequired;
    private Method setVerbPhraseParent;
    private Method setVerbPhraseChild;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/ERRelationshipToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new ERRelationshipTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            ERRelationshipTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setCardinality() method
        setCardinality = TestSupport.getAccessibleMethod(
            ERRelationshipTool.class,
            "setCardinality",
            McpSyncServerExchange.class,
            ERRelationshipWithCardinalityDTO.class);

        // setERIndex() method
        setERIndex = TestSupport.getAccessibleMethod(
            ERRelationshipTool.class,
            "setERIndex",
            McpSyncServerExchange.class,
            ERRelationshipWithERIndexDTO.class);

        // setLogicalName() method
        setLogicalName = TestSupport.getAccessibleMethod(
            ERRelationshipTool.class,
            "setLogicalName",
            McpSyncServerExchange.class,
            ERRelationshipWithLogicalNameDTO.class);

        // setPhysicalName() method
        setPhysicalName = TestSupport.getAccessibleMethod(
            ERRelationshipTool.class,
            "setPhysicalName",
            McpSyncServerExchange.class,
            ERRelationshipWithPhysicalNameDTO.class);

        // setParentRequired() method
        setParentRequired = TestSupport.getAccessibleMethod(
            ERRelationshipTool.class,
            "setParentRequired",
            McpSyncServerExchange.class,
            ERRelationshipWithParentRequiredDTO.class);

        // setVerbPhraseParent() method
        setVerbPhraseParent = TestSupport.getAccessibleMethod(
            ERRelationshipTool.class,
            "setVerbPhraseParent",
            McpSyncServerExchange.class,
            ERRelationshipWithParentVerbPhraseDTO.class);

        // setVerbPhraseChild() method
        setVerbPhraseChild = TestSupport.getAccessibleMethod(
            ERRelationshipTool.class,
            "setVerbPhraseChild",
            McpSyncServerExchange.class,
            ERRelationshipWithChildVerbPhraseDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get ER relationship
        IERRelationship erRelationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(erRelationship.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erRelationship.getId(), outputDTO.namedElement().element().id());
        assertEquals(erRelationship.getName(), outputDTO.namedElement().name());
    }

    @Test
    void setCardinality_ok() throws Exception {
        // Get ER relationship
        IERRelationship erRelationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship0");

        // Create input DTO
        ERRelationshipWithCardinalityDTO inputDTO = new ERRelationshipWithCardinalityDTO(
            erRelationship.getId(),
            "1");

        // ----------------------------------------
        // Call setCardinality()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setCardinality,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("1", erRelationship.getCardinality());
    }

    @Test
    void setERIndex_ok() throws Exception {
        // Get ER relationship
        IERRelationship erRelationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship0");

        // Get ER index
        IERIndex erIndex = (IERIndex) TestSupport.instance().getNamedElement(
            IERIndex.class,
            "Index0");

        // Create input DTO
        ERRelationshipWithERIndexDTO inputDTO = new ERRelationshipWithERIndexDTO(
            erRelationship.getId(),
            erIndex.getId());

        // ----------------------------------------
        // Call setERIndex()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setERIndex,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(erIndex.getId(), erRelationship.getERIndex().getId());
    }

    @Test
    void setLogicalName_ok() throws Exception {
        // Get ER relationship
        IERRelationship erRelationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship0");

        // Create input DTO
        ERRelationshipWithLogicalNameDTO inputDTO = new ERRelationshipWithLogicalNameDTO(
            erRelationship.getId(),
            "LogicalName_777");

        // ----------------------------------------
        // Call setLogicalName()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setLogicalName,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("LogicalName_777", erRelationship.getLogicalName());
    }

    @Test
    void setPhysicalName_ok() throws Exception {
        // Get ER relationship
        IERRelationship erRelationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship0");

        // Create input DTO
        ERRelationshipWithPhysicalNameDTO inputDTO = new ERRelationshipWithPhysicalNameDTO(
            erRelationship.getId(),
            "physical_name_777");

        // ----------------------------------------
        // Call setPhysicalName()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setPhysicalName,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("physical_name_777", erRelationship.getPhysicalName());
    }

    @Test
    void setParentRequired_ok() throws Exception {
        // Get ER relationship
        IERRelationship erRelationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship1");
        boolean nextValue = !erRelationship.isParentRequired();

        // Create input DTO
        ERRelationshipWithParentRequiredDTO inputDTO = new ERRelationshipWithParentRequiredDTO(
            erRelationship.getId(),
            nextValue);

        // ----------------------------------------
        // Call setParentRequired()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setParentRequired,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nextValue, erRelationship.isParentRequired());
    }

    @Test
    void setVerbPhraseParent_ok() throws Exception {
        // Get ER relationship
        IERRelationship erRelationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship0");

        // Create input DTO
        ERRelationshipWithParentVerbPhraseDTO inputDTO = new ERRelationshipWithParentVerbPhraseDTO(
            erRelationship.getId(),
            "ParentVerb_777");

        // ----------------------------------------
        // Call setVerbPhraseParent()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setVerbPhraseParent,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("ParentVerb_777", erRelationship.getVerbPhraseParent());
    }

    @Test
    void setVerbPhraseChild_ok() throws Exception {
        // Get ER relationship
        IERRelationship erRelationship = (IERRelationship) TestSupport.instance().getNamedElement(
            IERRelationship.class,
            "Relationship0");

        // Create input DTO
        ERRelationshipWithChildVerbPhraseDTO inputDTO = new ERRelationshipWithChildVerbPhraseDTO(
            erRelationship.getId(),
            "ChildVerb_777");

        // ----------------------------------------
        // Call setVerbPhraseChild()
        // ----------------------------------------
        ERRelationshipDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setVerbPhraseChild,
            tool,
            inputDTO,
            ERRelationshipDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("ChildVerb_777", erRelationship.getVerbPhraseChild());
    }
}
