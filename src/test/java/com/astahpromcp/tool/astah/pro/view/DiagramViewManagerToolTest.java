package com.astahpromcp.tool.astah.pro.view;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.astahpromcp.tool.astah.pro.view.inputdto.PresentationWithHighlightColorDTO;
import com.astahpromcp.tool.astah.pro.view.inputdto.ZoomFactorDTO;
import com.astahpromcp.tool.astah.pro.view.outputdto.DiagramWithZoomFactorDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("DiagramViewManager is not available in non-plugin")
public class DiagramViewManagerToolTest {

    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private IDiagramViewManager diagramViewManager;
    private DiagramViewManagerTool tool;
    private Method openDiagram;
    private Method closeDiagram;
    private Method getCurrentDiagram;
    private Method getSelectedPresentations;
    private Method selectPresentations;
    private Method selectAllPresentations;
    private Method unselectAllPresentations;
    private Method centerPresentationInDiagram;
    private Method autoLayout;
    private Method zoom;
    private Method zoomFit;
    private Method highlightPresentation;
    private Method unhighlightPresentation;
    private Method getHighlightedPresentationsWithinDiagram;
    private Method bringPresentationsToFront;
    private Method sendPresentationsToBack;

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.open("src/test/resources/modelfile/view/DiagramViewManagerToolTest.asta");
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        diagramViewManager = astahApi.getViewManager().getDiagramViewManager();
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new DiagramViewManagerTool(
            projectAccessor,
            diagramViewManager,
            transactionSupport,
            astahProToolSupport);

        // openDiagram() method
        openDiagram = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "openDiagram",
            IdDTO.class);

        // closeDiagram() method
        closeDiagram = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "closeDiagram",
            IdDTO.class);

        // getCurrentDiagram() method
        getCurrentDiagram = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "getCurrentDiagram",
            NoInputDTO.class);

        // getSelectedPresentations() method
        getSelectedPresentations = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "getSelectedPresentations",
            NoInputDTO.class);

        // selectPresentations() method
        selectPresentations = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "selectPresentations",
            IdListDTO.class);

        // selectAllPresentations() method
        selectAllPresentations = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "selectAllPresentations",
            NoInputDTO.class);

        // unselectAllPresentations() method
        unselectAllPresentations = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "unselectAllPresentations",
            NoInputDTO.class);

        // centerPresentationInDiagram() method
        centerPresentationInDiagram = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "centerPresentationInDiagram",
            IdDTO.class);

        // autoLayout() method
        autoLayout = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "autoLayout",
            NoInputDTO.class);

        // zoom() method
        zoom = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "zoom",
            ZoomFactorDTO.class);

        // zoomFit() method
        zoomFit = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "zoomFit",
            NoInputDTO.class);

        // highlightPresentation() method
        highlightPresentation = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "highlightPresentation",
            PresentationWithHighlightColorDTO.class);

        // unhighlightPresentation() method
        unhighlightPresentation = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "unhighlightPresentation",
            IdDTO.class);

        // getHighlightedPresentationsWithinDiagram() method
        getHighlightedPresentationsWithinDiagram = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "getHighlightedPresentationsWithinDiagram",
            IdDTO.class);

        // bringPresentationsToFront() method
        bringPresentationsToFront = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "bringPresentationsToFront",
            IdListDTO.class);

        // sendPresentationsToBack() method
        sendPresentationsToBack = TestSupport.getAccessibleMethod(
            DiagramViewManagerTool.class,
            "sendPresentationsToBack",
            IdListDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }

    @Test
    void openDiagram_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // Close diagram
        diagramViewManager.close(diagram);

        // Check diagram is opened before open
        assertNotEquals(diagramViewManager.getCurrentDiagram(), diagram);

        // ----------------------------------------
        // Call openDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            openDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check diagram is opened after open
        assertEquals(diagramViewManager.getCurrentDiagram(), diagram);
    }

    @Test
    void closeDiagram_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // Open diagram
        diagramViewManager.open(diagram);

        // Check diagram is opened before close
        assertEquals(diagramViewManager.getCurrentDiagram(), diagram);

        // ----------------------------------------
        // Call closeDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            closeDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check diagram is closed after close
        assertNotEquals(diagramViewManager.getCurrentDiagram(), diagram);
    }

    @Test
    void getCurrentDiagram_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // Open diagram
        diagramViewManager.open(diagram);

        // ----------------------------------------
        // Call getCurrentDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getCurrentDiagram,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(diagram.getId(), outputDTO.namedElement().element().id());
    }

    @Test
    void getSelectedPresentations_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // Open diagram
        diagramViewManager.open(diagram);

        // Select presentation
        IPresentation presentation = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        diagramViewManager.select(presentation);

        // ----------------------------------------
        // Call getSelectedPresentations()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getSelectedPresentations,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(1, outputDTO.value().size());
        assertEquals(presentation.getID(), outputDTO.value().get(0).id());
    }

    @Test
    void selectPresentations_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get presentations
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        IPresentation bar = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // ----------------------------------------
        // Call selectPresentations()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of(new IdDTO(foo.getID()), new IdDTO(bar.getID()))),
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(
            Set.of(foo.getID(), bar.getID()),
            outputDTO.value().stream().map(PresentationDTO::id).collect(Collectors.toSet()));

        // Check both were selected
        assertEquals(Set.of(foo, bar), new HashSet<>(Arrays.asList(diagramViewManager.getSelectedPresentations())));
    }

    @Test
    void selectPresentations_ok_replacesSelection() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get presentations
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        IPresentation bar = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // Select Foo
        diagramViewManager.select(foo);

        // ----------------------------------------
        // Call selectPresentations() for Bar
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of(new IdDTO(bar.getID()))),
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(1, outputDTO.value().size());
        assertEquals(bar.getID(), outputDTO.value().get(0).id());

        // Check Bar replaced Foo in the selection
        assertEquals(List.of(bar), Arrays.asList(diagramViewManager.getSelectedPresentations()));
    }

    @Test
    void selectPresentations_ng_noDiagramOpen() throws Exception {
        // Get presentation
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Close every diagram
        for (IDiagram openDiagram : diagramViewManager.getOpenDiagrams()) {
            diagramViewManager.close(openDiagram);
        }

        // ----------------------------------------
        // Call selectPresentations()
        // ----------------------------------------
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of(new IdDTO(foo.getID()))),
            PresentationListDTO.class));

        // Check it was rejected because no diagram is open
        assertInstanceOf(IllegalStateException.class, thrown.getCause());
    }

    // select() silently skips a presentation that is not on the current diagram, so the whole list must be rejected before anything is selected
    @Test
    void selectPresentations_ng_listMixedWithPresentationOnAnotherDiagram() throws Exception {
        // Draw a class on another diagram
        INodePresentation elsewhere = transactionSupport.call(() -> {
            IClass clazz = projectAccessor.getModelEditorFactory().getBasicModelEditor().createClass(projectAccessor.getProject(), "Elsewhere");
            ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
            classDiagramEditor.setDiagram(classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "Another Class Diagram"));
            return classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(0, 0));
        });

        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get presentations
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        IPresentation bar = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // Select Foo
        diagramViewManager.select(foo);

        // ----------------------------------------
        // Call selectPresentations()
        // ----------------------------------------
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of(new IdDTO(bar.getID()), new IdDTO(elsewhere.getID()))),
            PresentationListDTO.class));

        // Check it was rejected as an invalid input that names the diagram the presentation is on
        IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(cause.getMessage().contains("'Another Class Diagram'"), cause.getMessage());

        // Check the selection was left unchanged
        assertEquals(List.of(foo), Arrays.asList(diagramViewManager.getSelectedPresentations()));
    }

    @Test
    void selectPresentations_ng_emptyList() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Select Foo
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        diagramViewManager.select(foo);

        // ----------------------------------------
        // Call selectPresentations()
        // ----------------------------------------
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of()),
            PresentationListDTO.class));

        // Check it was rejected with a pointer to the tool that clears the selection
        IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(cause.getMessage().contains("'unselect_all_prsts'"), cause.getMessage());

        // Check the selection was left unchanged
        assertEquals(List.of(foo), Arrays.asList(diagramViewManager.getSelectedPresentations()));
    }

    @Test
    void selectAllPresentations_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // Open diagram
        diagramViewManager.open(diagram);

        // Unselect all presentations
        diagramViewManager.unselectAll();

        // ----------------------------------------
        // Call selectAllPresentations()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            selectAllPresentations,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(diagram.getPresentations().length, outputDTO.value().size());
    }

    @Test
    void unselectAllPresentations_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // Open diagram
        diagramViewManager.open(diagram);

        // Select all presentations
        diagramViewManager.selectAll();

        // Check presentations are selected before unselect all
        assertNotEquals(0, diagram.getPresentations().length);

        // ----------------------------------------
        // Call unselectAllPresentations()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            unselectAllPresentations,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check presentations are unselected after unselect all
        assertEquals(0, diagram.getPresentations().length);
    }

    @Test
    void autoLayout_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // Open diagram
        diagramViewManager.open(diagram);

        // ----------------------------------------
        // Call autoLayout()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            autoLayout,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
    }

    @Test
    void zoom_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        ZoomFactorDTO inputDTO = new ZoomFactorDTO(1.5);

        // Open diagram
        diagramViewManager.open(diagram);

        // Check zoom factor before zoom
        assertNotEquals(1.5, diagramViewManager.getZoomFactor(), 0.1);

        // ----------------------------------------
        // Call zoom()
        // ----------------------------------------
        DiagramWithZoomFactorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            zoom,
            tool,
            inputDTO,
            DiagramWithZoomFactorDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(diagram.getId(), outputDTO.diagram().namedElement().element().id());

        // Check zoom factor after zoom
        assertEquals(1.5, diagramViewManager.getZoomFactor(), 0.1);
        assertEquals(diagramViewManager.getZoomFactor(), outputDTO.zoomFactorValue());
    }

    @Test
    void zoom_ok_boundary() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        for (double zoomFactor : new double[] { 0.05, 4.0 }) {
            // ----------------------------------------
            // Call zoom()
            // ----------------------------------------
            DiagramWithZoomFactorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
                zoom,
                tool,
                new ZoomFactorDTO(zoomFactor),
                DiagramWithZoomFactorDTO.class);

            // Check that both ends of the range are accepted as they are
            assertEquals(zoomFactor, outputDTO.zoomFactorValue(), "zoom() did not apply " + zoomFactor);
            assertEquals(zoomFactor, diagramViewManager.getZoomFactor());
        }
    }

    @Test
    void zoom_ok_roundedByAstah() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // ----------------------------------------
        // Call zoom()
        // ----------------------------------------
        // Astah rounds a value within the range to its own zoom steps
        DiagramWithZoomFactorDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            zoom,
            tool,
            new ZoomFactorDTO(1.25),
            DiagramWithZoomFactorDTO.class);

        // Check that the zoom factor Astah applied is returned, not the specified one
        assertEquals(diagramViewManager.getZoomFactor(), outputDTO.zoomFactorValue());
    }

    @Test
    void zoom_ng_outOfRange() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);
        double zoomFactorBefore = diagramViewManager.getZoomFactor();

        for (double zoomFactor : new double[] { 0.0, -1.0, 0.049, 4.01, 10.0, Double.NaN }) {
            // ----------------------------------------
            // Call zoom()
            // ----------------------------------------
            InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
                zoom,
                tool,
                new ZoomFactorDTO(zoomFactor),
                DiagramWithZoomFactorDTO.class), "zoom() accepted " + zoomFactor);

            // Check exception
            IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
            assertTrue(cause.getMessage().contains("between 0.05 and 4.0"), cause.getMessage());
        }

        // Check that the zoom factor is left as it was
        assertEquals(zoomFactorBefore, diagramViewManager.getZoomFactor());
    }

    @Test
    void zoomFit_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // Open diagram
        diagramViewManager.open(diagram);

        // ----------------------------------------
        // Call zoomFit()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            zoomFit,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(diagram.getId(), outputDTO.namedElement().element().id());
    }

    @Test
    void highlightPresentation_ok_nodePresentation() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Create input DTO
        PresentationWithHighlightColorDTO inputDTO = new PresentationWithHighlightColorDTO(
            nodePresentation.getID(),
            "#FF0000");

        // ----------------------------------------
        // Call highlightPresentation()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            highlightPresentation,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nodePresentation.getID(), outputDTO.id());
    }

    @Test
    void highlightPresentation_ok_linkPresentation() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get link presentation
        ILinkPresentation linkPresentation = (ILinkPresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Association",
            "has");

        // Create input DTO
        PresentationWithHighlightColorDTO inputDTO = new PresentationWithHighlightColorDTO(
            linkPresentation.getID(),
            "#00FF00");

        // ----------------------------------------
        // Call highlightPresentation()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            highlightPresentation,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(linkPresentation.getID(), outputDTO.id());
    }

    @Test
    void highlightPresentation_ng_invalidColor() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        for (String color : Arrays.asList("#FFF", "#00FF0080", "red", "FF0000", "0xFF0000", "16711680", "0777", " #FFFFFF", "", null)) {
            // Create input DTO
            PresentationWithHighlightColorDTO inputDTO = new PresentationWithHighlightColorDTO(
                nodePresentation.getID(),
                color);

            // ----------------------------------------
            // Call highlightPresentation()
            // ----------------------------------------
            InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
                highlightPresentation,
                tool,
                inputDTO,
                PresentationDTO.class), "highlightPresentation() accepted " + color);

            // Check exception
            IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
            assertTrue(cause.getMessage().contains("#RRGGBB"), cause.getMessage());
        }

        // Check that the presentation is not highlighted
        assertNull(diagramViewManager.getViewProperty(nodePresentation, IDiagramViewManager.BACKGROUND_COLOR));
    }

    @Test
    void highlightPresentation_ok_doesNotModifyProject() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Check that the project is not modified before highlighting
        assertFalse(projectAccessor.isProjectModified());

        // ----------------------------------------
        // Call highlightPresentation() and unhighlightPresentation()
        // ----------------------------------------
        TestSupport.instance().invokeToolMethodReturningDto(
            highlightPresentation,
            tool,
            new PresentationWithHighlightColorDTO(nodePresentation.getID(), "#FF0000"),
            PresentationDTO.class);

        // Check that a highlight, which is only drawn on the open diagram, does not mark the project modified
        assertNotNull(diagramViewManager.getViewProperty(nodePresentation, IDiagramViewManager.BACKGROUND_COLOR));
        assertFalse(projectAccessor.isProjectModified());

        TestSupport.instance().invokeToolMethodReturningDto(
            unhighlightPresentation,
            tool,
            new IdDTO(nodePresentation.getID()),
            PresentationDTO.class);

        // Check that unhighlighting does not mark the project modified either
        assertNull(diagramViewManager.getViewProperty(nodePresentation, IDiagramViewManager.BACKGROUND_COLOR));
        assertFalse(projectAccessor.isProjectModified());
    }

    @Test
    void unhighlightPresentation_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get node presentation
        INodePresentation nodePresentation = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");

        // Highlight presentation first
        PresentationWithHighlightColorDTO highlightInputDTO = new PresentationWithHighlightColorDTO(
            nodePresentation.getID(),
            "#FF0000");
        TestSupport.instance().invokeToolMethodReturningDto(
            highlightPresentation,
            tool,
            highlightInputDTO,
            PresentationDTO.class);

        // Check presentation is highlighted before unhighlight
        assertNotNull(diagramViewManager.getViewProperty(nodePresentation, IDiagramViewManager.BACKGROUND_COLOR));

        // Create input DTO for unhighlight
        IdDTO inputDTO = new IdDTO(nodePresentation.getID());

        // ----------------------------------------
        // Call unhighlightPresentation()
        // ----------------------------------------
        PresentationDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            unhighlightPresentation,
            tool,
            inputDTO,
            PresentationDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(nodePresentation.getID(), outputDTO.id());

        // Check presentation is unhighlighted after unhighlight
        assertNull(diagramViewManager.getViewProperty(nodePresentation, IDiagramViewManager.BACKGROUND_COLOR));
    }

    @Test
    void getHighlightedPresentationsWithinDiagram_ok() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get presentations
        INodePresentation nodePresentation1 = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        INodePresentation nodePresentation2 = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // Highlight presentations
        PresentationWithHighlightColorDTO highlightInputDTO1 = new PresentationWithHighlightColorDTO(
            nodePresentation1.getID(),
            "#FF0000");
        TestSupport.instance().invokeToolMethodReturningDto(
            highlightPresentation,
            tool,
            highlightInputDTO1,
            PresentationDTO.class);

        PresentationWithHighlightColorDTO highlightInputDTO2 = new PresentationWithHighlightColorDTO(
            nodePresentation2.getID(),
            "#00FF00");
        TestSupport.instance().invokeToolMethodReturningDto(
            highlightPresentation,
            tool,
            highlightInputDTO2,
            PresentationDTO.class);

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getHighlightedPresentationsWithinDiagram()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getHighlightedPresentationsWithinDiagram,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(2, outputDTO.value().size());
    }

    @Test
    void getHighlightedPresentationsWithinDiagram_ok_diagramNotOpen() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Close diagram, which discards its highlights
        diagramViewManager.close(diagram);

        // Create input DTO
        IdDTO inputDTO = new IdDTO(diagram.getId());

        // ----------------------------------------
        // Call getHighlightedPresentationsWithinDiagram()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getHighlightedPresentationsWithinDiagram,
            tool,
            inputDTO,
            PresentationListDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);
        assertEquals(0, outputDTO.value().size());
    }

    @Test
    void bringPresentationsToFront_ok() throws Exception {
        assertArrangeDepthSwapsOrder(bringPresentationsToFront);
    }

    @Test
    void sendPresentationsToBack_ok() throws Exception {
        assertArrangeDepthSwapsOrder(sendPresentationsToBack);
    }

    // Moving Foo and then Bar to the same end must reverse their relative depth, whichever way getDepth() counts
    private void assertArrangeDepthSwapsOrder(Method arrangeDepth) throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get presentations
        INodePresentation foo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        INodePresentation bar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");

        // ----------------------------------------
        // Call the arrange depth tool for Foo, then for Bar
        // ----------------------------------------
        PresentationListDTO fooOutputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            arrangeDepth,
            tool,
            new IdListDTO(List.of(new IdDTO(foo.getID()))),
            PresentationListDTO.class);
        int orderAfterFoo = Integer.signum(Integer.compare(foo.getDepth(), bar.getDepth()));

        PresentationListDTO barOutputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            arrangeDepth,
            tool,
            new IdListDTO(List.of(new IdDTO(bar.getID()))),
            PresentationListDTO.class);
        int orderAfterBar = Integer.signum(Integer.compare(foo.getDepth(), bar.getDepth()));

        // Check output DTOs
        assertEquals(foo.getID(), fooOutputDTO.value().get(0).id());
        assertEquals(bar.getID(), barOutputDTO.value().get(0).id());

        // Check the relative depth was reversed
        assertNotEquals(0, orderAfterFoo);
        assertEquals(-orderAfterFoo, orderAfterBar);
    }

    @Test
    void bringPresentationsToFront_ng_presentationOnAnotherDiagram() throws Exception {
        assertArrangeDepthRejectsPresentationOnAnotherDiagram(bringPresentationsToFront, false);
    }

    @Test
    void bringPresentationsToFront_ng_listMixedWithPresentationOnAnotherDiagram() throws Exception {
        assertArrangeDepthRejectsPresentationOnAnotherDiagram(bringPresentationsToFront, true);
    }

    @Test
    void sendPresentationsToBack_ng_listMixedWithPresentationOnAnotherDiagram() throws Exception {
        assertArrangeDepthRejectsPresentationOnAnotherDiagram(sendPresentationsToBack, true);
    }

    // A presentation that is not on the current diagram cannot be selected, and the arrange depth menu items stay enabled
    // without a selection, so it must be rejected as an invalid input before any depth changes
    private void assertArrangeDepthRejectsPresentationOnAnotherDiagram(Method arrangeDepth, boolean mixWithCurrentDiagram) throws Exception {
        // Draw a class on another diagram
        INodePresentation elsewhere = transactionSupport.call(() -> {
            IClass clazz = projectAccessor.getModelEditorFactory().getBasicModelEditor().createClass(projectAccessor.getProject(), "Elsewhere");
            ClassDiagramEditor classDiagramEditor = projectAccessor.getDiagramEditorFactory().getClassDiagramEditor();
            classDiagramEditor.setDiagram(classDiagramEditor.createClassDiagram(projectAccessor.getProject(), "Another Class Diagram"));
            return classDiagramEditor.createNodePresentation(clazz, new Point2D.Double(0, 0));
        });

        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");

        // Open diagram
        diagramViewManager.open(diagram);

        // Get presentations
        INodePresentation foo = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        INodePresentation bar = (INodePresentation) TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");
        int fooDepth = foo.getDepth();
        int barDepth = bar.getDepth();
        int elsewhereDepth = elsewhere.getDepth();

        List<IdDTO> ids = new ArrayList<>();
        if (mixWithCurrentDiagram) {
            ids.add(new IdDTO(foo.getID()));
            ids.add(new IdDTO(bar.getID()));
        }
        ids.add(new IdDTO(elsewhere.getID()));

        // ----------------------------------------
        // Call the arrange depth tool
        // ----------------------------------------
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            arrangeDepth,
            tool,
            new IdListDTO(ids),
            PresentationListDTO.class));

        // Check it was rejected as an invalid input that names the diagram the presentation is on
        IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(cause.getMessage().contains("'Another Class Diagram'"), cause.getMessage());

        // Check no depth changed
        assertEquals(fooDepth, foo.getDepth());
        assertEquals(barDepth, bar.getDepth());
        assertEquals(elsewhereDepth, elsewhere.getDepth());
    }

}
