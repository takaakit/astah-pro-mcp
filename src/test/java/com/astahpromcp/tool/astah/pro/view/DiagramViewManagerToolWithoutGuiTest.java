package com.astahpromcp.tool.astah.pro.view;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Checks DiagramViewManagerTool against a mocked diagram view, so that it runs outside the Astah GUI as well.
public class DiagramViewManagerToolWithoutGuiTest {
    
    private ProjectAccessor projectAccessor;
    private TransactionSupport transactionSupport;
    private IDiagramViewManager diagramViewManager;
    private DiagramViewManagerTool tool;
    private Method getCurrentDiagram;
    private Method getSelectedPresentations;
    private Method selectPresentations;
    private Method selectAllPresentations;
    private Method unselectAllPresentations;
    
    @BeforeEach
    void setUp() throws Exception {
        AstahAPI astahApi = AstahAPI.getAstahAPI();
        projectAccessor = astahApi.getProjectAccessor();
        projectAccessor.open("src/test/resources/modelfile/view/DiagramViewManagerToolTest.asta");
        transactionSupport = new TransactionSupport(projectAccessor.getTransactionManager());
        diagramViewManager = mock(IDiagramViewManager.class);
        
        // Tool
        tool = new DiagramViewManagerTool(
            projectAccessor,
            diagramViewManager,
            transactionSupport,
            new AstahProToolSupport(projectAccessor));
        
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
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (projectAccessor != null) {
            projectAccessor.close();
        }
    }
    
    @Test
    void getCurrentDiagram_ok_returnsTheDiagramTheViewShows() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");
        
        when(diagramViewManager.getCurrentDiagram()).thenReturn(diagram);
        
        // ----------------------------------------
        // Call getCurrentDiagram()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getCurrentDiagram,
            tool,
            new NoInputDTO(),
            DiagramDTO.class);
        
        // Check output DTO
        assertEquals(diagram.getId(), outputDTO.namedElement().element().id());
    }
    
    @Test
    void getCurrentDiagram_ng_noDiagramOpen() throws Exception {
        when(diagramViewManager.getCurrentDiagram()).thenReturn(null);
        
        // ----------------------------------------
        // Call getCurrentDiagram()
        // ----------------------------------------
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            getCurrentDiagram,
            tool,
            new NoInputDTO(),
            DiagramDTO.class));
        
        // Check exception
        assertInstanceOf(IllegalStateException.class, thrown.getCause());
    }
    
    @Test
    void getSelectedPresentations_ok_returnsWhatTheViewHasSelectedInItsOrder() throws Exception {
        // Get presentations
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        IPresentation bar = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");
        
        when(diagramViewManager.getSelectedPresentations()).thenReturn(new IPresentation[] { bar, foo });
        
        // ----------------------------------------
        // Call getSelectedPresentations()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            getSelectedPresentations,
            tool,
            new NoInputDTO(),
            PresentationListDTO.class);
        
        // Check output DTO
        assertEquals(List.of(bar.getID(), foo.getID()), idsOf(outputDTO));
    }
    
    @Test
    void selectPresentations_ok_selectsTheGivenPresentationsInTheView() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");
        
        // Get presentations
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        IPresentation bar = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");
        
        when(diagramViewManager.getCurrentDiagram()).thenReturn(diagram);
        // Nothing is selected before the call, and both are selected after it
        when(diagramViewManager.getSelectedPresentations()).thenReturn(new IPresentation[0], new IPresentation[] { foo, bar });
        
        // ----------------------------------------
        // Call selectPresentations()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of(new IdDTO(foo.getID()), new IdDTO(bar.getID()))),
            PresentationListDTO.class);
        
        // Check that the view was asked to select exactly the given presentations, and that the selection was not undone
        ArgumentCaptor<IPresentation[]> selected = ArgumentCaptor.forClass(IPresentation[].class);
        verify(diagramViewManager).select(selected.capture());
        assertEquals(List.of(foo, bar), Arrays.asList(selected.getValue()));
        verify(diagramViewManager, never()).unselectAll();
        
        // Check output DTO
        assertEquals(List.of(foo.getID(), bar.getID()), idsOf(outputDTO));
    }
    
    @Test
    void selectPresentations_ng_restoresTheSelectionWhenTheViewSkipsAPresentation() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");
        
        // Get presentations
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        IPresentation bar = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");
        
        when(diagramViewManager.getCurrentDiagram()).thenReturn(diagram);
        // Bar is selected before the call, and the view selects only Foo of the two given
        when(diagramViewManager.getSelectedPresentations()).thenReturn(new IPresentation[] { bar }, new IPresentation[] { foo });
        
        // ----------------------------------------
        // Call selectPresentations()
        // ----------------------------------------
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of(new IdDTO(foo.getID()), new IdDTO(bar.getID()))),
            PresentationListDTO.class));
        
        // Check that it was rejected naming the presentation the view skipped
        IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        assertTrue(cause.getMessage().contains(bar.getID()), cause.getMessage());
        
        // Check that the selection before the call was put back
        InOrder inOrder = inOrder(diagramViewManager);
        ArgumentCaptor<IPresentation[]> selected = ArgumentCaptor.forClass(IPresentation[].class);
        inOrder.verify(diagramViewManager).select(selected.capture());
        inOrder.verify(diagramViewManager).unselectAll();
        inOrder.verify(diagramViewManager).select(selected.capture());
        assertEquals(List.of(foo, bar), Arrays.asList(selected.getAllValues().get(0)));
        assertEquals(List.of(bar), Arrays.asList(selected.getAllValues().get(1)));
    }
    
    @Test
    void selectPresentations_ng_emptyListTouchesNothing() throws Exception {
        // ----------------------------------------
        // Call selectPresentations()
        // ----------------------------------------
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of()),
            PresentationListDTO.class));
        
        // Check that it was rejected before the view was used at all
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        verifyNoInteractions(diagramViewManager);
    }
    
    @Test
    void selectPresentations_ng_presentationOnAnotherDiagramSelectsNothing() throws Exception {
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
        
        // Get presentation
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        
        when(diagramViewManager.getCurrentDiagram()).thenReturn(diagram);
        
        // ----------------------------------------
        // Call selectPresentations()
        // ----------------------------------------
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> TestSupport.instance().invokeToolMethodReturningDto(
            selectPresentations,
            tool,
            new IdListDTO(List.of(new IdDTO(foo.getID()), new IdDTO(elsewhere.getID()))),
            PresentationListDTO.class));
        
        // Check that the whole list was rejected before anything was selected
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
        verify(diagramViewManager, never()).select(any(IPresentation[].class));
        verify(diagramViewManager, never()).unselectAll();
    }
    
    @Test
    void selectAllPresentations_ok_selectsAllInTheViewAndReturnsWhatItSelected() throws Exception {
        // Get presentations
        IPresentation foo = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Foo");
        IPresentation bar = TestSupport.instance().getPresentationByTypeAndLabel(
            "Class",
            "Bar");
        
        when(diagramViewManager.getSelectedPresentations()).thenReturn(new IPresentation[] { foo, bar });
        
        // ----------------------------------------
        // Call selectAllPresentations()
        // ----------------------------------------
        PresentationListDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            selectAllPresentations,
            tool,
            new NoInputDTO(),
            PresentationListDTO.class);
        
        // Check that the selection was read from the view after it selected all
        InOrder inOrder = inOrder(diagramViewManager);
        inOrder.verify(diagramViewManager).selectAll();
        inOrder.verify(diagramViewManager).getSelectedPresentations();
        
        // Check output DTO
        assertEquals(List.of(foo.getID(), bar.getID()), idsOf(outputDTO));
    }
    
    @Test
    void unselectAllPresentations_ok_unselectsInTheViewAndLeavesTheModelAsItIs() throws Exception {
        // Get diagram
        IDiagram diagram = (IDiagram) TestSupport.instance().getNamedElementByClassAndName(
            IDiagram.class,
            "Class Diagram0");
        
        // Check presentations are on the diagram before unselect all
        Set<String> presentationIdsBefore = presentationIdsOn(diagram);
        assertFalse(presentationIdsBefore.isEmpty());
        
        when(diagramViewManager.getCurrentDiagram()).thenReturn(diagram);
        
        // ----------------------------------------
        // Call unselectAllPresentations()
        // ----------------------------------------
        DiagramDTO outputDTO = TestSupport.instance().invokeToolMethodReturningDto(
            unselectAllPresentations,
            tool,
            new NoInputDTO(),
            DiagramDTO.class);
        
        // Check output DTO
        assertEquals(diagram.getId(), outputDTO.namedElement().element().id());
        
        // Check that the view was asked to unselect, and that the diagram and the project were left as they were
        verify(diagramViewManager).unselectAll();
        assertEquals(presentationIdsBefore, presentationIdsOn(diagram));
        assertFalse(projectAccessor.isProjectModified());
    }
    
    private static List<String> idsOf(PresentationListDTO outputDTO) {
        return outputDTO.value().stream().map(PresentationDTO::id).toList();
    }
    
    private static Set<String> presentationIdsOn(IDiagram diagram) throws Exception {
        return Arrays.stream(diagram.getPresentations()).map(IPresentation::getID).collect(Collectors.toSet());
    }
}
