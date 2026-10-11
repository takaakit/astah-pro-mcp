package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.NavigabilityKind;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithAggregationDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithCompositionDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.LinkEndWithNavigabilityDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.LinkEndDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class LinkEndToolTest {

    private ProjectAccessor projectAccessor;
    private LinkEndTool tool;
    private Method getInfo;
    private Method setAggregation;
    private Method setComposition;
    private Method setNavigability;

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
            astahProToolSupport);

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

        // setNavigability() method
        setNavigability = TestSupport.getAccessibleMethod(
            LinkEndTool.class,
            "setNavigability",
            LinkEndWithNavigabilityDTO.class);
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
        assertEquals(linkEnd.getNavigability(), outputDTO.navigability());
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
    void setNavigability_ok() throws Exception {
        // Get link end
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElementByClassAndName(
            ILinkEnd.class,
            "fooLinkEnd");

        // Create input DTO
        LinkEndWithNavigabilityDTO inputDTO = new LinkEndWithNavigabilityDTO(
            linkEnd.getId(),
            NavigabilityKind.NAVIGABLE);

        // Check navigability before setting
        assertNotEquals("Navigable", linkEnd.getNavigability());

        // ----------------------------------------
        // Call setNavigability()
        // ----------------------------------------
        LinkEndDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setNavigability,
            tool,
            inputDTO,
            LinkEndDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("Navigable", outputDTO.navigability());

        // Check navigability after setting
        assertEquals("Navigable", linkEnd.getNavigability());
    }

    @Test
    void setNavigability_unspecifiedAfterNonNavigable_ok() throws Exception {
        // Get link end
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElementByClassAndName(
            ILinkEnd.class,
            "fooLinkEnd");

        // ----------------------------------------
        // Call setNavigability()
        // ----------------------------------------
        LinkEndDTO nonNavigableOutputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setNavigability,
            tool,
            new LinkEndWithNavigabilityDTO(linkEnd.getId(), NavigabilityKind.NON_NAVIGABLE),
            LinkEndDTO.class);

        // Check that non_navigable is told apart from unspecified
        assertEquals("Non_Navigable", nonNavigableOutputDTO.navigability());
        assertEquals("Non_Navigable", linkEnd.getNavigability());

        // ----------------------------------------
        // Call setNavigability()
        // ----------------------------------------
        LinkEndDTO unspecifiedOutputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            setNavigability,
            tool,
            new LinkEndWithNavigabilityDTO(linkEnd.getId(), NavigabilityKind.UNSPECIFIED),
            LinkEndDTO.class);

        // Check that the navigability is cleared
        assertEquals("Unspecified", unspecifiedOutputDTO.navigability());
        assertEquals("Unspecified", linkEnd.getNavigability());
    }

    @Test
    void setNavigability_nullNavigabilityKind_ng() throws Exception {
        // Get link end
        ILinkEnd linkEnd = (ILinkEnd) TestSupport.instance().getNamedElementByClassAndName(
            ILinkEnd.class,
            "fooLinkEnd");
        String navigabilityBefore = linkEnd.getNavigability();

        // Create input DTO
        LinkEndWithNavigabilityDTO inputDTO = new LinkEndWithNavigabilityDTO(
            linkEnd.getId(),
            null);

        // ----------------------------------------
        // Call setNavigability()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDto(
                setNavigability,
                tool,
                inputDTO,
                LinkEndDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("navigabilityKind must be one of"));

        // Check that the navigability did not change
        assertEquals(navigabilityBefore, linkEnd.getNavigability());
    }
}
