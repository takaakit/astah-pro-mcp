package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.PointIntDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.LinkPresentationWithPointsDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LinkPresentationToolTest {

    private ProjectAccessor projectAccessor;
    private LinkPresentationTool tool;
    private Method getInfo;
    private Method setAllPoints;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/presentation/LinkPresentationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new LinkPresentationTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            LinkPresentationTool.class,
            "getInfo",
            McpSyncServerExchange.class,
            IdDTO.class);

        // setAllPoints() method
        setAllPoints = TestSupport.getAccessibleMethod(
            LinkPresentationTool.class,
            "setAllPoints",
            McpSyncServerExchange.class,
            LinkPresentationWithPointsDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getInfo_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentation(
            "Association",
            "");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(linkPresentation.getID());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            getInfo,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void setAllPoints_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentation(
            "Association",
            "");

        // Get start and end points
        Point2D[] points = linkPresentation.getAllPoints();
        PointIntDTO startPoint = new PointIntDTO((int)points[0].getX(), (int)points[0].getY());
        PointIntDTO endPoint = new PointIntDTO((int)points[points.length - 1].getX(), (int)points[points.length - 1].getY());
        
        // Create input DTO
        LinkPresentationWithPointsDTO inputDTO = new LinkPresentationWithPointsDTO(
            linkPresentation.getID(),
            List.of(
                startPoint,
                new PointIntDTO(10, 20),
                new PointIntDTO(30, 40),
                new PointIntDTO(50, 60),
                endPoint
            ));
        
        // Check points before setting
        assertNotEquals(5, linkPresentation.getPoints().length);

        // ----------------------------------------
        // Call setAllPoints()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethod(
            setAllPoints,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check points after setting
        assertEquals(5, linkPresentation.getPoints().length);
    }
}
