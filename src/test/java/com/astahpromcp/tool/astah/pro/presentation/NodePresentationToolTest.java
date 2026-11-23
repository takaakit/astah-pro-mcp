package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithHeightDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithLocationDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.NodePresentationWithWidthDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.NodePresentationDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.editor.UseCaseDiagramEditor;
import com.change_vision.jude.api.inf.editor.UseCaseModelEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Value;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodePresentationToolTest {
    
    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private NodePresentationTool tool;
    private Method getInfo;
    private Method getNodePresentationRectangle;
    private Method setNodePresentationLocation;
    private Method setNodePresentationWidth;
    private Method setNodePresentationHeight;
    
    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/presentation/NodePresentationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createThumbnailContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());
        
        // Tool
        tool = new NodePresentationTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            imageCaptureSupport);
        
        // getInfo() method
        getInfo = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "getInfo",
            IdDTO.class);
        
        // getNodePresentationRectangle() method
        getNodePresentationRectangle = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "getNodePresentationRectangle",
            IdDTO.class);
        
        // setNodePresentationLocation() method
        setNodePresentationLocation = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "setNodePresentationLocation",
            NodePresentationWithLocationDTO.class);
        
        // setNodePresentationWidth() method
        setNodePresentationWidth = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "setNodePresentationWidth",
            NodePresentationWithWidthDTO.class);
        
        // setNodePresentationHeight() method
        setNodePresentationHeight = TestSupport.getAccessibleMethod(
            NodePresentationTool.class,
            "setNodePresentationHeight",
            NodePresentationWithHeightDTO.class);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }
    
    @Test
    void getInfo_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(nodePresentation.getID());
        
        // ----------------------------------------
        // Call getInfo()
        // ----------------------------------------
        NodePresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getInfo,
            tool,
            inputDTO,
            NodePresentationDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nodePresentation.getID(), outputDTO.presentation().id());
        assertEquals("https://www.mlb.com/en", outputDTO.urlHyperlinks().get(0).url());
        assertEquals(".\\data\\sample1.xlsx", outputDTO.filePathHyperlinks().get(0).filePath());
        assertEquals("C:\\data\\sample2.xlsx", outputDTO.filePathHyperlinks().get(1).filePath());
        assertFalse(outputDTO.namedElementHyperlinks().get(0).namedElementId().isEmpty());
    }
    
    @Test
    void getNodePresentationRectangle_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        
        // Create input DTO
        IdDTO inputDTO = new IdDTO(nodePresentation.getID());
        
        // ----------------------------------------
        // Call getNodePresentationRectangle()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getNodePresentationRectangle,
            tool,
            inputDTO,
            RectangleDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        assertTrue(outputDTO.width() > 0);
        assertTrue(outputDTO.height() > 0);
    }
    
    @Test
    void setNodePresentationLocation_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");
        
        // Create input DTO
        NodePresentationWithLocationDTO inputDTO = new NodePresentationWithLocationDTO(
            nodePresentation.getID(),
            100,
            200);
        
        // Check points before setting
        assertNotEquals(100, nodePresentation.getLocation().getX(), 0.1);
        assertNotEquals(200, nodePresentation.getLocation().getY(), 0.1);
        
        // ----------------------------------------
        // Call setNodePresentationLocation()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setNodePresentationLocation,
            tool,
            inputDTO,
            RectangleDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(100, outputDTO.x(), 0.1);
        assertEquals(200, outputDTO.y(), 0.1);
        
        // Check points after setting
        assertEquals(100, nodePresentation.getLocation().getX(), 0.1);
        assertEquals(200, nodePresentation.getLocation().getY(), 0.1);
    }
    
    @Test
    void setNodePresentationWidth_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");
        
        // Create input DTO
        NodePresentationWithWidthDTO inputDTO = new NodePresentationWithWidthDTO(
            nodePresentation.getID(),
            150);
        
        // Check width before setting
        assertNotEquals(150, nodePresentation.getWidth(), 0.1);
        
        // ----------------------------------------
        // Call setNodePresentationWidth()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setNodePresentationWidth,
            tool,
            inputDTO,
            RectangleDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(150, outputDTO.width(), 0.1);
        
        // Check width after setting
        assertEquals(150, nodePresentation.getWidth(), 0.1);
    }
    
    @Test
    void setNodePresentationHeight_ok() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "State",
            "Quux");
        
        // Create input DTO
        NodePresentationWithHeightDTO inputDTO = new NodePresentationWithHeightDTO(
            nodePresentation.getID(),
            100);
        
        // Check height before setting
        assertNotEquals(100, nodePresentation.getHeight(), 0.1);
        
        // ----------------------------------------
        // Call setNodePresentationHeight()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setNodePresentationHeight,
            tool,
            inputDTO,
            RectangleDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(100, outputDTO.height(), 0.1);
        
        // Check height after setting
        assertEquals(100, nodePresentation.getHeight(), 0.1);
    }
    
    @Test
    void setNodePresentationLocationWidthHeight_ng_combinedFragment() throws Exception {
        // A combined fragment cannot be moved or resized through the Astah API
        SequenceDiagramEditor sequenceDiagramEditor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
        INodePresentation combinedFragment = transactionSupport.call(() -> {
            ISequenceDiagram diagram = sequenceDiagramEditor.createSequenceDiagram(projectAccessor.getProject(), "CombinedFragmentTarget");
            sequenceDiagramEditor.setDiagram(diagram);
            sequenceDiagramEditor.createLifeline("a", 50);
            sequenceDiagramEditor.createLifeline("b", 250);
            return sequenceDiagramEditor.createCombinedFragment("", "opt", new Point2D.Double(20, 100), 380, 100);
        });
        Rectangle2D rectangleBefore = combinedFragment.getRectangle();
        
        String id = combinedFragment.getID();
        Object[][] calls = {
            { setNodePresentationLocation, new NodePresentationWithLocationDTO(id, 40, 120) },
            { setNodePresentationWidth, new NodePresentationWithWidthDTO(id, 500) },
            { setNodePresentationHeight, new NodePresentationWithHeightDTO(id, 150) }
        };
        
        for (Object[] call : calls) {
            // ----------------------------------------
            // Call the tool method
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    (Method) call[0],
                    tool,
                    call[1],
                    RectangleDTO.class);
            });
            
            // Check that the exception tells how to change the combined fragment instead
            assertInstanceOf(InvalidEditingException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("set_height_of_interaction_operand"));
            assertTrue(exception.getCause().getMessage().contains("dgm_guide"));
        }
        
        // Check that the combined fragment is left as it was
        assertEquals(rectangleBefore, combinedFragment.getRectangle());
    }
    
    @Test
    void setNodePresentationLocation_ng_otherPresentationKeepsGeneralHint() throws Exception {
        // A frame cannot be moved through the Astah API either, but it is not a combined fragment
        INodePresentation frame = createFrameOnSequenceDiagram();
        
        // ----------------------------------------
        // Call setNodePresentationLocation()
        // ----------------------------------------
        NodePresentationWithLocationDTO inputDTO = new NodePresentationWithLocationDTO(frame.getID(), 40, 120);
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                setNodePresentationLocation,
                tool,
                inputDTO,
                RectangleDTO.class);
        });
        
        // Check that the exception keeps the general hint
        assertInstanceOf(InvalidEditingException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("delete it and create it again"));
        assertFalse(exception.getCause().getMessage().contains("set_height_of_interaction_operand"));
    }
    
    @Test
    void setNodePresentationWidthHeight_ng_unsupportedElement() throws Exception {
        // A frame cannot be resized through the Astah API either, but it is not a combined fragment
        INodePresentation frame = createFrameOnSequenceDiagram();
        Rectangle2D rectangleBefore = frame.getRectangle();
        
        String id = frame.getID();
        Object[][] calls = {
            { setNodePresentationWidth, new NodePresentationWithWidthDTO(id, (int) rectangleBefore.getWidth() + 100) },
            { setNodePresentationHeight, new NodePresentationWithHeightDTO(id, (int) rectangleBefore.getHeight() + 100) }
        };
        
        for (Object[] call : calls) {
            // ----------------------------------------
            // Call the tool method
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    (Method) call[0],
                    tool,
                    call[1],
                    RectangleDTO.class);
            });
            
            // Check that the exception keeps the key Astah gave
            InvalidEditingException cause = assertInstanceOf(InvalidEditingException.class, exception.getCause());
            assertEquals(InvalidEditingException.UNSUPPORTED_ELEMENT_ERROR_KEY, cause.getKey());
            assertFalse(cause.getMessage().contains("set_height_of_interaction_operand"));
        }
        
        // Check that the frame is left as it was
        assertEquals(rectangleBefore, frame.getRectangle());
    }
    
    @Test
    void setNodePresentationWidth_ok_icon() throws Exception {
        // Get node presentation drawn as an icon
        INodePresentation nodePresentation = createActorOnUseCaseDiagram();
        assertEquals(Value.NOTATION_TYPE_ICON, nodePresentation.getProperty(Key.NOTATION_TYPE));
        double ratio = nodePresentation.getHeight() / nodePresentation.getWidth();
        int newWidth = (int) Math.round(nodePresentation.getWidth() * 1.5);
        
        // Create input DTO
        NodePresentationWithWidthDTO inputDTO = new NodePresentationWithWidthDTO(
            nodePresentation.getID(),
            newWidth);
        
        // Check height before setting
        assertNotEquals(newWidth * ratio, nodePresentation.getHeight(), 0.1);
        
        // ----------------------------------------
        // Call setNodePresentationWidth()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setNodePresentationWidth,
            tool,
            inputDTO,
            RectangleDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(newWidth, outputDTO.width(), 0.1);
        assertEquals(newWidth * ratio, outputDTO.height(), 0.1);
        
        // Check that the height follows the width by the same ratio
        assertEquals(newWidth, nodePresentation.getWidth(), 0.1);
        assertEquals(newWidth * ratio, nodePresentation.getHeight(), 0.1);
    }
    
    @Test
    void setNodePresentationHeight_ok_icon() throws Exception {
        // Get node presentation drawn as an icon
        INodePresentation nodePresentation = createActorOnUseCaseDiagram();
        assertEquals(Value.NOTATION_TYPE_ICON, nodePresentation.getProperty(Key.NOTATION_TYPE));
        double ratio = nodePresentation.getWidth() / nodePresentation.getHeight();
        int newHeight = (int) Math.round(nodePresentation.getHeight() * 1.5);
        
        // Create input DTO
        NodePresentationWithHeightDTO inputDTO = new NodePresentationWithHeightDTO(
            nodePresentation.getID(),
            newHeight);
        
        // Check width before setting
        assertNotEquals(newHeight * ratio, nodePresentation.getWidth(), 0.1);
        
        // ----------------------------------------
        // Call setNodePresentationHeight()
        // ----------------------------------------
        RectangleDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setNodePresentationHeight,
            tool,
            inputDTO,
            RectangleDTO.class);
        
        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(newHeight, outputDTO.height(), 0.1);
        assertEquals(newHeight * ratio, outputDTO.width(), 0.1);
        
        // Check that the width follows the height by the same ratio
        assertEquals(newHeight, nodePresentation.getHeight(), 0.1);
        assertEquals(newHeight * ratio, nodePresentation.getWidth(), 0.1);
    }
    
    @Test
    void setNodePresentationWidth_ng_iconBelowMinimum() throws Exception {
        // Get node presentation drawn as an icon
        INodePresentation nodePresentation = createActorOnUseCaseDiagram();
        Rectangle2D rectangleBefore = nodePresentation.getRectangle();
        
        // Create input DTO
        NodePresentationWithWidthDTO inputDTO = new NodePresentationWithWidthDTO(
            nodePresentation.getID(),
            5);
        
        // ----------------------------------------
        // Call setNodePresentationWidth()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                setNodePresentationWidth,
                tool,
                inputDTO,
                RectangleDTO.class);
        });
        
        // Check that the exception says that the width and the height were to change together
        assertInstanceOf(InvalidEditingException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("drawn as an icon"));
        
        // Check that the icon is left as it was
        assertEquals(rectangleBefore, nodePresentation.getRectangle());
    }
    
    @Test
    void setNodePresentationWidth_ng_iconLifeline() throws Exception {
        // Get lifeline drawn as an icon
        INodePresentation lifeline = createActorLifelineOnSequenceDiagram();
        assertEquals(Value.NOTATION_TYPE_ICON, lifeline.getProperty(Key.NOTATION_TYPE));
        Rectangle2D rectangleBefore = lifeline.getRectangle();
        
        // Create input DTO
        NodePresentationWithWidthDTO inputDTO = new NodePresentationWithWidthDTO(
            lifeline.getID(),
            200);
        
        // ----------------------------------------
        // Call setNodePresentationWidth()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                setNodePresentationWidth,
                tool,
                inputDTO,
                RectangleDTO.class);
        });
        
        // Check that the exception says why, and that the name does not need a wider icon
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("drawn as an icon"));
        assertTrue(exception.getCause().getMessage().contains("outside its rectangle"));
        
        // Check that the icon is not stretched
        assertEquals(rectangleBefore, lifeline.getRectangle());
    }
    
    @Test
    void setNodePresentationHeight_ng_iconLifeline() throws Exception {
        // Get lifeline drawn as an icon
        INodePresentation lifeline = createActorLifelineOnSequenceDiagram();
        assertEquals(Value.NOTATION_TYPE_ICON, lifeline.getProperty(Key.NOTATION_TYPE));
        Rectangle2D rectangleBefore = lifeline.getRectangle();
        
        // Create input DTO
        NodePresentationWithHeightDTO inputDTO = new NodePresentationWithHeightDTO(
            lifeline.getID(),
            50);
        
        // ----------------------------------------
        // Call setNodePresentationHeight()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                setNodePresentationHeight,
                tool,
                inputDTO,
                RectangleDTO.class);
        });
        
        // Check that the exception says why
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("drawn as an icon"));
        
        // Check that the icon is left as it was
        assertEquals(rectangleBefore, lifeline.getRectangle());
    }
    
    private INodePresentation createActorOnUseCaseDiagram() throws Exception {
        UseCaseModelEditor useCaseModelEditor = projectAccessor.getModelEditorFactory().getUseCaseModelEditor();
        UseCaseDiagramEditor useCaseDiagramEditor = projectAccessor.getDiagramEditorFactory().getUseCaseDiagramEditor();
        return transactionSupport.call(() -> {
            IClass actor = useCaseModelEditor.createActor(projectAccessor.getProject(), "Visitor");
            IUseCaseDiagram diagram = useCaseDiagramEditor.createUseCaseDiagram(projectAccessor.getProject(), "IconTarget");
            useCaseDiagramEditor.setDiagram(diagram);
            return useCaseDiagramEditor.createNodePresentation(actor, new Point2D.Double(100, 100));
        });
    }
    
    private INodePresentation createActorLifelineOnSequenceDiagram() throws Exception {
        UseCaseModelEditor useCaseModelEditor = projectAccessor.getModelEditorFactory().getUseCaseModelEditor();
        SequenceDiagramEditor sequenceDiagramEditor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
        return transactionSupport.call(() -> {
            IClass actor = useCaseModelEditor.createActor(projectAccessor.getProject(), "Customer");
            ISequenceDiagram diagram = sequenceDiagramEditor.createSequenceDiagram(projectAccessor.getProject(), "IconLifelineTarget");
            sequenceDiagramEditor.setDiagram(diagram);
            INodePresentation lifeline = sequenceDiagramEditor.createLifeline("customer", 50);
            ((ILifeline) lifeline.getModel()).setBase(actor);
            return lifeline;
        });
    }
    
    private INodePresentation createFrameOnSequenceDiagram() throws Exception {
        SequenceDiagramEditor sequenceDiagramEditor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
        ISequenceDiagram diagram = transactionSupport.call(() ->
            sequenceDiagramEditor.createSequenceDiagram(projectAccessor.getProject(), "FrameTarget"));
        INodePresentation frame = null;
        for (IPresentation presentation : diagram.getPresentations()) {
            if ("Frame".equals(presentation.getType())) {
                frame = (INodePresentation) presentation;
            }
        }
        assertNotNull(frame);
        return frame;
    }
    
    @Test
    void setNodePresentationWidthHeight_ng_notPositive() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        
        String id = nodePresentation.getID();
        Object[][] calls = {
            { setNodePresentationWidth, new NodePresentationWithWidthDTO(id, 0) },
            { setNodePresentationHeight, new NodePresentationWithHeightDTO(id, -10) }
        };
        
        for (Object[] call : calls) {
            // ----------------------------------------
            // Call the tool method
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    (Method) call[0],
                    tool,
                    call[1],
                    RectangleDTO.class);
            });
            
            // Check that the value is rejected before Astah is asked
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("must be greater than 0"));
        }
    }
    
    @Test
    void setNodePresentationWidthHeight_ng_belowMinimum() throws Exception {
        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        Rectangle2D rectangleBefore = nodePresentation.getRectangle();
        
        String id = nodePresentation.getID();
        Object[][] calls = {
            { setNodePresentationWidth, new NodePresentationWithWidthDTO(id, 5) },
            { setNodePresentationHeight, new NodePresentationWithHeightDTO(id, 5) }
        };
        
        for (Object[] call : calls) {
            // ----------------------------------------
            // Call the tool method
            // ----------------------------------------
            Exception exception = assertThrows(Exception.class, () -> {
                TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                    (Method) call[0],
                    tool,
                    call[1],
                    RectangleDTO.class);
            });
            
            // Check that the exception points at the minimum size rather than at recreating the class
            assertInstanceOf(InvalidEditingException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("below the minimum"));
            assertFalse(exception.getCause().getMessage().contains("delete it and create it again"));
        }
        
        // Check that the class is left as it was
        assertEquals(rectangleBefore, nodePresentation.getRectangle());
    }
}
