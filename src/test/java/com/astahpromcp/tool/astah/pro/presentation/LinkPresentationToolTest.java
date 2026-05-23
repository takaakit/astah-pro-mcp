package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.PointIntDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.LinkPresentationWithLineStyleDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.LinkPresentationWithPointsDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.LinkPresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.beans.Transient;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LinkPresentationToolTest {

    private ProjectAccessor projectAccessor;
    private LinkPresentationTool tool;
    private Method getInfo;
    private Method setAllPoints;
    private Method setLineStyle;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        ITransactionManager transactionManager = projectAccessor.getTransactionManager();
        projectAccessor.open("src/test/resources/modelfile/presentation/LinkPresentationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createImageContent(anyString(), any()))
            .thenReturn(new McpSchema.ImageContent(null, "", "image/png"));

        // Tool
        tool = new LinkPresentationTool(
            projectAccessor,
            transactionManager,
            astahProToolSupport,
            imageCaptureSupport,
            true);

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

        // setLineStyle() method
        setLineStyle = TestSupport.getAccessibleMethod(
            LinkPresentationTool.class,
            "setLineStyle",
            McpSyncServerExchange.class,
            LinkPresentationWithLineStyleDTO.class);
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
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Association",
            "");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(linkPresentation.getID());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(linkPresentation.getID(), outputDTO.presentation().id());
    }

    @Test
    void getInfo_lineStyle_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Dependency",
            "line style");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(linkPresentation.getID());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            LinkPresentationDTO.class);
            
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(LineStyleKind.LINE, outputDTO.lineStyle());

        // Check link presentation
        assertEquals(LineStyleKind.LINE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));
    }

    @Test
    void getInfo_lineRightAngleStyle_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Dependency",
            "line right angle style");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(linkPresentation.getID());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            LinkPresentationDTO.class);
            
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(LineStyleKind.LINE_RIGHT_ANGLE, outputDTO.lineStyle());

        // Check link presentation
        assertEquals(LineStyleKind.LINE_RIGHT_ANGLE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));
    }

    @Test
    void getInfo_curveStyle_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Dependency",
            "curve style");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(linkPresentation.getID());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(LineStyleKind.CURVE, outputDTO.lineStyle());

        // Check link presentation
        assertEquals(LineStyleKind.CURVE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));
    }

    @Test
    void getInfo_curveRightAngleStyle_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Dependency",
            "curve right angle style");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(linkPresentation.getID());

        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            LinkPresentationDTO.class);
            
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(LineStyleKind.CURVE_RIGHT_ANGLE, outputDTO.lineStyle());

        // Check link presentation
        assertEquals(LineStyleKind.CURVE_RIGHT_ANGLE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));
    }

    @Test
    void setAllPoints_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
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
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setAllPoints,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check points after setting
        assertEquals(5, linkPresentation.getPoints().length);
    }

    @Test
    void setLineStyle_lineToCurve_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Dependency",
            "line style");

        // Create input DTO
        LinkPresentationWithLineStyleDTO inputDTO = new LinkPresentationWithLineStyleDTO(
            linkPresentation.getID(),
            LineStyleKind.CURVE);

        // Check line style before setting
        assertEquals(LineStyleKind.LINE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));

        // ----------------------------------------
        // Call setLineStyle()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setLineStyle,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(LineStyleKind.CURVE, outputDTO.lineStyle());

        // Check link presentation
        assertEquals(LineStyleKind.CURVE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));
    }

    @Test
    void setLineStyle_curveToLineRightAngle_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Dependency",
            "curve style");

        // Create input DTO
        LinkPresentationWithLineStyleDTO inputDTO = new LinkPresentationWithLineStyleDTO(
            linkPresentation.getID(),
            LineStyleKind.LINE_RIGHT_ANGLE);

        // Check line style before setting
        assertEquals(LineStyleKind.CURVE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));

        // ----------------------------------------
        // Call setLineStyle()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setLineStyle,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(LineStyleKind.LINE_RIGHT_ANGLE, outputDTO.lineStyle());

        // Check link presentation
        assertEquals(LineStyleKind.LINE_RIGHT_ANGLE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));
    }

    @Test
    void setLineStyle_lineRightAngleToCurveRightAngle_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Dependency",
            "line right angle style");

        // Create input DTO
        LinkPresentationWithLineStyleDTO inputDTO = new LinkPresentationWithLineStyleDTO(
            linkPresentation.getID(),
            LineStyleKind.CURVE_RIGHT_ANGLE);

        // Check line style before setting
        assertEquals(LineStyleKind.LINE_RIGHT_ANGLE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));

        // ----------------------------------------
        // Call setLineStyle()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setLineStyle,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(LineStyleKind.CURVE_RIGHT_ANGLE, outputDTO.lineStyle());

        // Check link presentation
        assertEquals(LineStyleKind.CURVE_RIGHT_ANGLE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));
    }

    @Test
    void setLineStyle_curveRightAngleToLine_ok() throws Exception {
        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Dependency",
            "curve right angle style");

        // Create input DTO
        LinkPresentationWithLineStyleDTO inputDTO = new LinkPresentationWithLineStyleDTO(
            linkPresentation.getID(),
            LineStyleKind.LINE);

        // Check line style before setting
        assertEquals(LineStyleKind.CURVE_RIGHT_ANGLE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));

        // ----------------------------------------
        // Call setLineStyle()
        // ----------------------------------------
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setLineStyle,
            tool,
            inputDTO,
            LinkPresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(LineStyleKind.LINE, outputDTO.lineStyle());

        // Check link presentation
        assertEquals(LineStyleKind.LINE.astahValue, linkPresentation.getProperty(Key.LINE_SHAPE));
    }
}
