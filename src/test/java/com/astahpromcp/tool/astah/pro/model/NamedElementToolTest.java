package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.VisibilityKind;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.*;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class NamedElementToolTest {

    private ProjectAccessor projectAccessor;
    private NamedElementTool tool;
    private Method getInfo;
    private Method setName;
    private Method setAlias1;
    private Method setAlias2;
    private Method setDefinition;
    private Method setVisibility;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/NamedElementToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new NamedElementTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            NamedElementTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setName() method
        setName = TestSupport.getAccessibleMethod(
            NamedElementTool.class,
            "setName",
            McpSyncServerExchange.class,
            NamedElementWithNameDTO.class);

        // setAlias1() method
        setAlias1 = TestSupport.getAccessibleMethod(
            NamedElementTool.class,
            "setAlias1",
            McpSyncServerExchange.class,
            NamedElementWithAlias1DTO.class);

        // setAlias2() method
        setAlias2 = TestSupport.getAccessibleMethod(
            NamedElementTool.class,
            "setAlias2",
            McpSyncServerExchange.class,
            NamedElementWithAlias2DTO.class);

        // setDefinition() method
        setDefinition = TestSupport.getAccessibleMethod(
            NamedElementTool.class,
            "setDefinition",
            McpSyncServerExchange.class,
            NamedElementWithDefinitionDTO.class);

        // setVisibility() method
        setVisibility = TestSupport.getAccessibleMethod(
            NamedElementTool.class,
            "setVisibility",
            McpSyncServerExchange.class,
            NamedElementWithVisibilityDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(namedElement.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(namedElement.getId(), outputDTO.element().id());
        assertEquals("https://www.mlb.com/en", outputDTO.urlHyperlinks().get(0).url());
        assertEquals(".\\data\\sample1.xlsx", outputDTO.filePathHyperlinks().get(0).filePath());
        assertEquals("C:\\data\\sample2.xlsx", outputDTO.filePathHyperlinks().get(1).filePath());
        assertFalse(outputDTO.namedElementHyperlinks().get(0).namedElementId().isEmpty());
    }

    @Test
    void setName_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        NamedElementWithNameDTO inputDTO = new NamedElementWithNameDTO(
            namedElement.getId(),
            "NewFoo");

        // Check name before setting
        assertNotEquals("NewFoo", namedElement.getName());

        // ----------------------------------------
        // Call setName()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setName,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check name after setting
        assertEquals("NewFoo", namedElement.getName());
    }

    @Test
    void setAlias1_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        NamedElementWithAlias1DTO inputDTO = new NamedElementWithAlias1DTO(
            namedElement.getId(),
            "Alias1");

        // Check alias1 before setting
        assertNotEquals("Alias1", namedElement.getAlias1());

        // ----------------------------------------
        // Call setAlias1()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setAlias1,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check alias1 after setting
        assertEquals("Alias1", namedElement.getAlias1());
    }

    @Test
    void setAlias2_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        NamedElementWithAlias2DTO inputDTO = new NamedElementWithAlias2DTO(
            namedElement.getId(),
            "Alias2");

        // Check alias2 before setting
        assertNotEquals("Alias2", namedElement.getAlias2());

        // ----------------------------------------
        // Call setAlias2()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setAlias2,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check alias2 after setting
        assertEquals("Alias2", namedElement.getAlias2());
    }

    @Test
    void setDefinition_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        NamedElementWithDefinitionDTO inputDTO = new NamedElementWithDefinitionDTO(
            namedElement.getId(),
            "This is a test definition");

        // Check definition before setting
        assertNotEquals("This is a test definition", namedElement.getDefinition());

        // ----------------------------------------
        // Call setDefinition()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setDefinition,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check definition after setting
        assertEquals("This is a test definition", namedElement.getDefinition());
    }

    @Test
    void setVisibility_ok() throws Exception {
        // Get named element
        INamedElement namedElement = (INamedElement) TestSupport.instance().getNamedElementByClassAndName(
            INamedElement.class,
            "Foo");
        
        // Create input DTO
        NamedElementWithVisibilityDTO inputDTO = new NamedElementWithVisibilityDTO(
            namedElement.getId(),
            VisibilityKind.PRIVATE);

        // Check visibility before setting
        assertFalse(namedElement.isPrivateVisibility());

        // ----------------------------------------
        // Call setVisibility()
        // ----------------------------------------
        NamedElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setVisibility,
            tool,
            inputDTO,
            NamedElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check visibility after setting
        assertTrue(namedElement.isPrivateVisibility());
    }
}
