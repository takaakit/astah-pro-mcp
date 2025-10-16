package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithAggregationDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithCompositionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithNavigationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkEndDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class LinkEndToolTest {

    private ProjectAccessor projectAccessor;
    private LinkEndTool tool;
    private Method setAggregation;
    private Method setComposition;
    private Method setNavigation;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/model/LinkEndToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new LinkEndTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // setAggregation() method
        setAggregation = TestSupport.getAccessibleMethod(
            LinkEndTool.class,
            "setAggregation",
            McpSyncServerExchange.class,
            LinkEndWithAggregationDTO.class);

        // setComposition() method
        setComposition = TestSupport.getAccessibleMethod(
            LinkEndTool.class,
            "setComposition",
            McpSyncServerExchange.class,
            LinkEndWithCompositionDTO.class);

        // setNavigation() method
        setNavigation = TestSupport.getAccessibleMethod(
            LinkEndTool.class,
            "setNavigation",
            McpSyncServerExchange.class,
            LinkEndWithNavigationDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void setAggregation_ok() throws Exception {
        // Get link end
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElement(
            ILinkEnd.class,
            "fooLinkEnd");
        
        // Create input DTO
        LinkEndWithAggregationDTO inputDTO = new LinkEndWithAggregationDTO(
            linkEnd.getId(),
            true);

        // Check aggregation before setting
        assertFalse(linkEnd.isAggregate());

        // ----------------------------------------
        // Call setAggregation()
        // ----------------------------------------
        LinkEndDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setAggregation,
            tool,
            inputDTO,
            LinkEndDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check aggregation after setting
        assertTrue(linkEnd.isAggregate());
    }

    @Test
    void setComposition_ok() throws Exception {
        // Get link end
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElement(
            ILinkEnd.class,
            "fooLinkEnd");
        
        // Create input DTO
        LinkEndWithCompositionDTO inputDTO = new LinkEndWithCompositionDTO(
            linkEnd.getId(),
            true);

        // Check composition before setting
        assertFalse(linkEnd.isComposite());

        // ----------------------------------------
        // Call setComposition()
        // ----------------------------------------
        LinkEndDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setComposition,
            tool,
            inputDTO,
            LinkEndDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check composition after setting
        assertTrue(linkEnd.isComposite());
    }

    @Test
    void setNavigation_ok() throws Exception {
        // Get link end
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElement(
            ILinkEnd.class,
            "fooLinkEnd");
        
        // Create input DTO
        LinkEndWithNavigationDTO inputDTO = new LinkEndWithNavigationDTO(
            linkEnd.getId(),
            true);

        // Check navigation before setting
        assertNotEquals("Navigable", linkEnd.getNavigability());

        // ----------------------------------------
        // Call setNavigation()
        // ----------------------------------------
        LinkEndDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setNavigation,
            tool,
            inputDTO,
            LinkEndDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check navigation after setting
        assertEquals("Navigable", linkEnd.getNavigability());
    }
}
