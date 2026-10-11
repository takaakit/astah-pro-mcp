package com.astahpromcp.tool.astah.pro.presentation;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.image.ImageCaptureSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithColorDTO;
import com.astahpromcp.tool.astah.pro.presentation.inputdto.PresentationWithLabelDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationTypeListDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.editor.SequenceDiagramEditor;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IComment;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants.Key;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PresentationToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private PresentationTool tool;
    private Method getElement;
    private Method getAllTypes;
    private Method setLabel;
    private Method changeFillColor;
    private Method changeLineColor;
    private Method changeFontColor;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        projectAccessor.open("src/test/resources/modelfile/presentation/PresentationToolTest.asta");
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);
        ImageCaptureSupport imageCaptureSupport = mock(ImageCaptureSupport.class);
        when(imageCaptureSupport.createThumbnailContent(anyString()))
            .thenReturn(McpSchema.ImageContent.builder("", "image/png").build());

        // Tool
        tool = new PresentationTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            imageCaptureSupport);

        // getElement() method
        getElement = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "getElement",
            IdDTO.class);

        // getAllTypes() method
        getAllTypes = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "getAllTypes",
            NoInputDTO.class);

        // setLabel() method
        setLabel = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "setLabel",
            PresentationWithLabelDTO.class);

        // changeFillColor() method
        changeFillColor = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "changeFillColor",
            PresentationWithColorDTO.class);

        // changeLineColor() method
        changeLineColor = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "changeLineColor",
            PresentationWithColorDTO.class);

        // changeFontColor() method
        changeFontColor = TestSupport.getAccessibleMethod(
            PresentationTool.class,
            "changeFontColor",
            PresentationWithColorDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void getElement_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(presentation.getID());

        // ----------------------------------------
        // Call getElement()
        // ----------------------------------------
        ElementDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getElement,
            tool,
            inputDTO,
            ElementDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void getAllTypes_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getAllTypes()
        // ----------------------------------------
        PresentationTypeListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getAllTypes,
            tool,
            inputDTO,
            PresentationTypeListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(PresentationDTO.Type.values().length - 1, outputDTO.value().size());
        assertFalse(outputDTO.value().contains("Unknown"));
    }

    @Test
    void setLabel_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create input DTO
        PresentationWithLabelDTO inputDTO = new PresentationWithLabelDTO(
            presentation.getID(),
            "UpdatedFoo");

        // Check label before setting
        assertNotEquals("UpdatedFoo", presentation.getLabel());

        // ----------------------------------------
        // Call setLabel()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setLabel,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("UpdatedFoo", outputDTO.label());

        // Check presentation after setting
        assertEquals("UpdatedFoo", presentation.getLabel());
    }

    @Test
    void setLabel_ok_noteOnSequenceDiagram() throws Exception {
        // Create note on a new sequence diagram
        SequenceDiagramEditor editor = projectAccessor.getDiagramEditorFactory().getSequenceDiagramEditor();
        INodePresentation note = transactionSupport.call( () -> {
            editor.setDiagram(editor.createSequenceDiagram(projectAccessor.getProject(), "Sequence Diagram0"));
            return editor.createNote("Note", new Point2D.Double(100, 100));
        });

        // Create input DTO
        PresentationWithLabelDTO inputDTO = new PresentationWithLabelDTO(
            note.getID(),
            "UpdatedNote\nSecond line");

        // ----------------------------------------
        // Call setLabel()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setLabel,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(note.getID(), outputDTO.id());
        assertEquals("UpdatedNote\nSecond line", outputDTO.label());

        // Check note after setting
        assertEquals("UpdatedNote\nSecond line", ((IComment) note.getModel()).getBody());
    }

    @Test
    void setLabel_ok_noteOnClassDiagram() throws Exception {
        // Get class diagram
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");

        // Create note on the class diagram
        ClassDiagramEditor editor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
        editor.setDiagram(classDiagram);
        INodePresentation note = transactionSupport.call( () -> {
            return editor.createNote("Note", new Point2D.Double(100, 300));
        });

        // Create input DTO
        PresentationWithLabelDTO inputDTO = new PresentationWithLabelDTO(
            note.getID(),
            "UpdatedNote\nSecond line");

        // ----------------------------------------
        // Call setLabel()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            setLabel,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(note.getID(), outputDTO.id());
        assertEquals("UpdatedNote\nSecond line", outputDTO.label());

        // Check note after setting
        assertEquals("UpdatedNote\nSecond line", ((IComment) note.getModel()).getBody());
    }

    @Test
    void changeFillColor_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            presentation.getID(),
            "#FF0000");

        // Check fill color before setting
        assertNotEquals("#FF0000", presentation.getProperty(Key.FILL_COLOR));

        // ----------------------------------------
        // Call changeFillColor()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            changeFillColor,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("#FF0000", outputDTO.fillColor());

        // Check presentation after setting
        assertEquals("#FF0000", presentation.getProperty(Key.FILL_COLOR));
    }

    @Test
    void changeLineColor_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            presentation.getID(),
            "#00FF00");

        // ----------------------------------------
        // Call changeLineColor()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            changeLineColor,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("#00FF00", outputDTO.lineColor());

        // Check presentation after setting
        assertEquals("#00FF00", presentation.getProperty(Key.LINE_COLOR));
    }

    @Test
    void changeFontColor_ok() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            presentation.getID(),
            "#0000FF");

        // ----------------------------------------
        // Call changeFontColor()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            changeFontColor,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("#0000FF", outputDTO.fontColor());

        // Check presentation after setting
        assertEquals("#0000FF", presentation.getProperty(Key.FONT_COLOR));
    }

    @Test
    void changeFillColor_ok_lowerCase() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            presentation.getID(),
            "#ff0000");

        // ----------------------------------------
        // Call changeFillColor()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDtoAndContents(
            changeFillColor,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals("#FF0000", outputDTO.fillColor());

        // Check presentation after setting
        assertEquals("#FF0000", presentation.getProperty(Key.FILL_COLOR));
    }

    @Test
    void changeColor_ng_invalidColor() throws Exception {
        // Get presentation
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        String fillColorBefore = presentation.getProperty(Key.FILL_COLOR);
        String lineColorBefore = presentation.getProperty(Key.LINE_COLOR);
        String fontColorBefore = presentation.getProperty(Key.FONT_COLOR);

        // Astah accepts "#FFF" as #000FFF and "#00FF0080" as #FF0080 without an error, and rejects the others with an unclear message.
        for (Method changeColor : List.of(changeFillColor, changeLineColor, changeFontColor)) {
            for (String color : Arrays.asList("#FFF", "#00FF0080", "red", "FF0000", " #FFFFFF", "", null)) {
                // Create input DTO
                PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
                    presentation.getID(),
                    color);

                // ----------------------------------------
                // Call changeFillColor(), changeLineColor() or changeFontColor()
                // ----------------------------------------
                Exception exception = assertThrows(Exception.class, () -> {
                    TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                        changeColor,
                        tool,
                        inputDTO,
                        PresentationDTO.class);
                }, changeColor.getName() + "() accepted " + color);

                // Check exception
                assertInstanceOf(IllegalArgumentException.class, exception.getCause());
                assertTrue(exception.getCause().getMessage().contains("#RRGGBB"));
            }
        }

        // Check that no color is changed
        assertEquals(fillColorBefore, presentation.getProperty(Key.FILL_COLOR));
        assertEquals(lineColorBefore, presentation.getProperty(Key.LINE_COLOR));
        assertEquals(fontColorBefore, presentation.getProperty(Key.FONT_COLOR));
    }

    @Test
    void changeFillColor_ng_link() throws Exception {
        // Get class diagram and presentation
        IClassDiagram classDiagram = (IClassDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IClassDiagram.class,
            "Class Diagram0");
        IPresentation presentation = (IPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create note anchor on the class diagram
        ClassDiagramEditor editor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
        editor.setDiagram(classDiagram);
        ILinkPresentation noteAnchor = transactionSupport.call( () -> {
            INodePresentation note = editor.createNote("Note", new Point2D.Double(100, 300));
            return editor.createNoteAnchor(note, presentation);
        });
        String fillColorBefore = noteAnchor.getProperty(Key.FILL_COLOR);

        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            noteAnchor.getID(),
            "#FF0000");

        // ----------------------------------------
        // Call changeFillColor()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                changeFillColor,
                tool,
                inputDTO,
                PresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("has no fill color"));

        // Check that the fill color of the note anchor is not changed
        assertEquals(fillColorBefore, noteAnchor.getProperty(Key.FILL_COLOR));
    }

    @Test
    void changeFillColor_ng_notKept() throws Exception {
        // A message on a communication diagram accepts a fill color without keeping it, but the Astah API cannot create a communication diagram, so a presentation that behaves the same is mocked.
        IPresentation presentation = mock(IPresentation.class);
        when(presentation.getID()).thenReturn("message-id");
        when(presentation.getType()).thenReturn("Message");
        when(presentation.getProperty(Key.FILL_COLOR)).thenReturn("#FFFFFF");

        AstahProToolSupport astahProToolSupport = mock(AstahProToolSupport.class);
        when(astahProToolSupport.getPresentation("message-id")).thenReturn(presentation);

        PresentationTool toolWithMock = new PresentationTool(
            projectAccessor,
            transactionSupport,
            astahProToolSupport,
            mock(ImageCaptureSupport.class));

        // Create input DTO
        PresentationWithColorDTO inputDTO = new PresentationWithColorDTO(
            "message-id",
            "#FF0000");

        // ----------------------------------------
        // Call changeFillColor()
        // ----------------------------------------
        Exception exception = assertThrows(Exception.class, () -> {
            TestSupport.instance().invokeToolMethodReturningDtoAndContents(
                changeFillColor,
                toolWithMock,
                inputDTO,
                PresentationDTO.class);
        });

        // Check exception
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("has no fill color"));
    }
}
