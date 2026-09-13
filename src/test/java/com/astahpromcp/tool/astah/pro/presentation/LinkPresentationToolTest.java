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
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.Transient;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

public class LinkPresentationToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private BasicModelEditor basicModelEditor;
    private ClassDiagramEditor classDiagramEditor;
    private LinkPresentationTool tool;
    private Method getInfo;
    private Method setAllPoints;
    private Method setLineStyle;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        basicModelEditor = projectAccessor.getModelEditorFactory().getBasicModelEditor();
        classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
        projectAccessor.open("src/test/resources/modelfile/presentation/LinkPresentationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createSmallImageContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());

        // Tool
        tool = new LinkPresentationTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            imageCaptureSupport);

        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            LinkPresentationTool.class,
            "getInfo",
            IdDTO.class);

        // setAllPoints() method
        setAllPoints = TestSupport.getAccessibleMethod(
            LinkPresentationTool.class,
            "setAllPoints",
            LinkPresentationWithPointsDTO.class);

        // setLineStyle() method
        setLineStyle = TestSupport.getAccessibleMethod(
            LinkPresentationTool.class,
            "setLineStyle",
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
    void getInfo_ok_lineStyle() throws Exception {
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
    void getInfo_ok_lineRightAngleStyle() throws Exception {
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
    void getInfo_ok_curveStyle() throws Exception {
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
    void getInfo_ok_curveRightAngleStyle() throws Exception {
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
    void setLineStyle_ok_lineToCurve() throws Exception {
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
    void setLineStyle_ok_curveToLineRightAngle() throws Exception {
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
    void setLineStyle_ok_lineRightAngleToCurveRightAngle() throws Exception {
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
    void setLineStyle_ok_curveRightAngleToLine() throws Exception {
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

    // Two classes on a class diagram of their own, with a generalization and an association drawn between them.
    private record TwoClassFixture(
        INodePresentation subNode,
        INodePresentation superNode,
        ILinkPresentation generalization,
        ILinkPresentation association) {
    }

    private TwoClassFixture createTwoClassFixture(int superX, int superY, int subX, int subY) throws Exception {
        return transactionSupport.call( () -> {
            IPackage rootPackage = projectAccessor.getProject();
            long stamp = System.nanoTime();

            IClassDiagram diagram = classDiagramEditor.createClassDiagram(rootPackage, "PointOrder" + stamp);
            classDiagramEditor.setDiagram(diagram);

            IClass superClass = basicModelEditor.createClass(rootPackage, "Super" + stamp);
            IClass subClass = basicModelEditor.createClass(rootPackage, "Sub" + stamp);

            INodePresentation superNode = classDiagramEditor.createNodePresentation(
                superClass, new Point2D.Double(superX, superY));
            INodePresentation subNode = classDiagramEditor.createNodePresentation(
                subClass, new Point2D.Double(subX, subY));

            ILinkPresentation generalization = classDiagramEditor.createLinkPresentation(
                basicModelEditor.createGeneralization(subClass, superClass, ""),
                subNode,
                superNode);

            ILinkPresentation association = classDiagramEditor.createLinkPresentation(
                basicModelEditor.createAssociation(subClass, superClass, "", "", ""),
                subNode,
                superNode);

            return new TwoClassFixture(subNode, superNode, generalization, association);
        });
    }

    private static PointIntDTO centreOf(INodePresentation nodePresentation) {
        Rectangle2D rectangle = nodePresentation.getRectangle();
        return new PointIntDTO(
            (int) (rectangle.getX() + rectangle.getWidth() / 2),
            (int) (rectangle.getY() + rectangle.getHeight() / 2));
    }

    // Sets the points through the tool and checks that the link ends up drawn through exactly those points.
    private void setPoints(ILinkPresentation linkPresentation, PointIntDTO... points) throws Exception {
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setAllPoints,
            tool,
            new LinkPresentationWithPointsDTO(linkPresentation.getID(), List.of(points)),
            LinkPresentationDTO.class);

        assertNotNull(outputDTO);

        Point2D[] stored = linkPresentation.getPoints();
        assertEquals(points.length, stored.length);
        assertTrue(
            matches(points, stored, false) || matches(points, stored, true),
            "The stored points follow neither the given order nor its reverse."
                + " given=" + List.of(points) + " stored=" + Arrays.toString(stored));
    }

    private static boolean matches(PointIntDTO[] given, Point2D[] stored, boolean reversed) {
        for (int i = 0; i < given.length; i++) {
            Point2D point = stored[reversed ? stored.length - 1 - i : i];
            if (Math.abs(point.getX() - given[i].x()) > 0.5 || Math.abs(point.getY() - given[i].y()) > 0.5) {
                return false;
            }
        }
        return true;
    }

    private static String rootCauseMessageOf(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        StringBuilder messages = new StringBuilder();
        for (Throwable each = throwable; each != null && each != each.getCause(); each = each.getCause()) {
            messages.append(each.getMessage()).append(' ');
        }
        return messages.toString();
    }

    @Test
    void setAllPoints_ok_generalizationInSourceToTargetOrder() throws Exception {
        TwoClassFixture fixture = createTwoClassFixture(400, 60, 400, 300);

        // The order reported for a generalization: the sub class end first. Astah rejects this order, so the tool has to retry it reversed.
        setPoints(fixture.generalization(),
            centreOf(fixture.subNode()),
            new PointIntDTO(250, 200),
            centreOf(fixture.superNode()));
    }

    @Test
    void setAllPoints_ok_generalizationInReverseOrder() throws Exception {
        TwoClassFixture fixture = createTwoClassFixture(400, 60, 400, 300);

        // The order Astah itself holds for a generalization: the super class end first.
        setPoints(fixture.generalization(),
            centreOf(fixture.superNode()),
            new PointIntDTO(250, 200),
            centreOf(fixture.subNode()));
    }

    @Test
    void setAllPoints_ok_associationInBothOrders() throws Exception {
        TwoClassFixture fixture = createTwoClassFixture(400, 60, 400, 300);

        setPoints(fixture.association(),
            centreOf(fixture.subNode()),
            new PointIntDTO(600, 200),
            centreOf(fixture.superNode()));

        setPoints(fixture.association(),
            centreOf(fixture.superNode()),
            new PointIntDTO(650, 200),
            centreOf(fixture.subNode()));
    }

    @Test
    void setAllPoints_ok_endsWithOverlappingRectangles() throws Exception {
        // Placed so that the two rectangles overlap, which leaves the order undecidable from geometry alone.
        TwoClassFixture fixture = createTwoClassFixture(100, 100, 130, 110);

        setPoints(fixture.generalization(),
            centreOf(fixture.subNode()),
            new PointIntDTO(60, 250),
            centreOf(fixture.superNode()));

        setPoints(fixture.generalization(),
            centreOf(fixture.superNode()),
            new PointIntDTO(80, 260),
            centreOf(fixture.subNode()));
    }

    @Test
    void setAllPoints_ok_selfLink() throws Exception {
        // Both ends of the link are one and the same node presentation.
        ILinkPresentation selfLink = transactionSupport.call( () -> {
            IPackage rootPackage = projectAccessor.getProject();
            long stamp = System.nanoTime();

            IClassDiagram diagram = classDiagramEditor.createClassDiagram(rootPackage, "SelfLink" + stamp);
            classDiagramEditor.setDiagram(diagram);

            IClass selfClass = basicModelEditor.createClass(rootPackage, "Self" + stamp);
            INodePresentation selfNode = classDiagramEditor.createNodePresentation(
                selfClass, new Point2D.Double(500, 300));

            return classDiagramEditor.createLinkPresentation(
                basicModelEditor.createAssociation(selfClass, selfClass, "", "", ""),
                selfNode,
                selfNode);
        });

        assertEquals(selfLink.getSourceEnd().getID(), selfLink.getTargetEnd().getID());

        // Both ends have to sit inside the one rectangle, so they are taken from it.
        Rectangle2D rectangle = ((INodePresentation) selfLink.getSourceEnd()).getRectangle();
        int insideY = (int) (rectangle.getY() + rectangle.getHeight() / 2);
        PointIntDTO left = new PointIntDTO((int) (rectangle.getX() + rectangle.getWidth() * 0.3), insideY);
        PointIntDTO right = new PointIntDTO((int) (rectangle.getX() + rectangle.getWidth() * 0.7), insideY);
        int aboveY = (int) rectangle.getY() - 30;

        // Astah works the ends of a self loop out for itself and moves them along the rectangle, so what it stores is checked against the rectangle rather than against the exact points given.
        setSelfLoopPoints(selfLink, rectangle,
            left, new PointIntDTO(left.x(), aboveY), new PointIntDTO(right.x(), aboveY), right);
        setSelfLoopPoints(selfLink, rectangle,
            right, new PointIntDTO(right.x(), aboveY - 5), new PointIntDTO(left.x(), aboveY - 5), left);
    }

    private void setSelfLoopPoints(ILinkPresentation linkPresentation, Rectangle2D rectangle, PointIntDTO... points) throws Exception {
        LinkPresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setAllPoints,
            tool,
            new LinkPresentationWithPointsDTO(linkPresentation.getID(), List.of(points)),
            LinkPresentationDTO.class);

        assertNotNull(outputDTO);

        Point2D[] stored = linkPresentation.getPoints();
        assertEquals(points.length, stored.length);
        assertTrue(rectangle.contains(stored[0]), "The first point is outside the rectangle: " + stored[0]);
        assertTrue(rectangle.contains(stored[stored.length - 1]), "The last point is outside the rectangle: " + stored[stored.length - 1]);
    }

    @Test
    void setAllPoints_ng_pointsOutsideBothRectangles() throws Exception {
        TwoClassFixture fixture = createTwoClassFixture(400, 60, 400, 300);

        // Neither order can place these inside the end rectangles, so both attempts fail. The message must not name an order, because both of them were tried.
        Exception thrown = assertThrows(Exception.class, () ->
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                setAllPoints,
                tool,
                new LinkPresentationWithPointsDTO(
                    fixture.generalization().getID(),
                    List.of(new PointIntDTO(9000, 9000), new PointIntDTO(9500, 9500))),
                LinkPresentationDTO.class));

        String message = rootCauseMessageOf(thrown);
        assertTrue(message.contains("must be inside the rectangles"), message);
        assertFalse(message.contains("must be ordered from the source end"), message);
    }

    @Test
    void setAllPoints_ok_rejectedAttemptLeavesThePointsUntouched() throws Exception {
        // The retry of the reversed order rests on this: within one transaction, a rejected setAllPoints must leave the presentation as it was, so that the second attempt starts from the original state.
        TwoClassFixture fixture = createTwoClassFixture(400, 60, 400, 300);
        ILinkPresentation generalization = fixture.generalization();

        transactionSupport.run( () -> {
            Point2D[] before = copyOf(generalization.getPoints());

            assertThrows(Exception.class, () -> generalization.setAllPoints(
                new Point2D.Double[] { new Point2D.Double(9000, 9000), new Point2D.Double(9500, 9500) }));

            Point2D[] after = copyOf(generalization.getPoints());

            assertEquals(before.length, after.length);
            for (int i = 0; i < before.length; i++) {
                assertEquals(before[i].getX(), after[i].getX(), 0.0001);
                assertEquals(before[i].getY(), after[i].getY(), 0.0001);
            }
        });
    }

    private static Point2D[] copyOf(Point2D[] points) {
        Point2D[] copy = new Point2D[points.length];
        for (int i = 0; i < points.length; i++) {
            copy[i] = new Point2D.Double(points[i].getX(), points[i].getY());
        }
        return copy;
    }
}
