package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithAggregationDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithCompositionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithNavigationDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkEndDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class LinkEndToolTest {

    private ProjectAccessor projectAccessor;
    private LinkEndTool tool;
    private Method getInfo;
    private Method setAggregation;
    private Method setComposition;
    private Method setNavigation;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/model/LinkEndToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new LinkEndTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            true);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            LinkEndTool.class,
            "getInfo",
            IdDTO.class);

        // setAggregation() method
        setAggregation = TestSupport.getAccessibleMethod(
            LinkEndTool.class,
            "setAggregation",
            LinkEndWithAggregationDTO.class);

        // setComposition() method
        setComposition = TestSupport.getAccessibleMethod(
            LinkEndTool.class,
            "setComposition",
            LinkEndWithCompositionDTO.class);

        // setNavigation() method
        setNavigation = TestSupport.getAccessibleMethod(
            LinkEndTool.class,
            "setNavigation",
            LinkEndWithNavigationDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get link end
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElementByClassAndName(
            ILinkEnd.class,
            "fooLinkEnd");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(linkEnd.getId());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LinkEndDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            LinkEndDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(linkEnd.getId(), outputDTO.namedElement().element().id());
    }

    @Test
    void setAggregation_ok() throws Exception {
        // Get link end
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElementByClassAndName(
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
        LinkEndDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElementByClassAndName(
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
        LinkEndDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElementByClassAndName(
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
        LinkEndDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
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
