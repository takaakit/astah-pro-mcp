package com.astahpromcp.tool.astah.pro.view;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.view.inputdto.PresentationWithHighlightColorDTO;
import com.astahpromcp.tool.astah.pro.view.inputdto.ZoomFactorDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
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

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

@Disabled("DiagramViewManager is not available in non-plugin")
public class DiagramViewManagerToolTest {

    private ProjectAccessor projectAccessor;
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

    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.open("src/test/resources/modelfile/view/DiagramViewManagerToolTest.asta");
        TransactionSupport transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        diagramViewManager = astahApi.getViewManager().getDiagramViewManager();
        AstahProToolSupport astahProToolSupport = new AstahProToolSupport(projectAccessor);

        // Tool
        tool = new DiagramViewManagerTool(
            projectAccessor,
            diagramViewManager,
            transactionSupport,
            astahProToolSupport,
            true);

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
        assertEquals(outputDTO.namedElement().element().id(), diagram.getId());
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
        assertEquals(outputDTO.value().size(), 1);
        assertEquals(outputDTO.value().get(0).id(), presentation.getID());
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
        assertEquals(outputDTO.value().size(), diagram.getPresentations().length);
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
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            zoom,
            tool,
            inputDTO,
            DiagramDTO.class);

        // Check output DTO
        assertNotNull(outputDTO);

        // Check zoom factor after zoom
        assertEquals(1.5, diagramViewManager.getZoomFactor(), 0.1);
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
        assertEquals(outputDTO.id(), nodePresentation.getID());
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
        assertEquals(outputDTO.id(), linkPresentation.getID());
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
        assertEquals(outputDTO.id(), nodePresentation.getID());

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

}
