package com.astahpromcp.tool.astah.pro.view;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.astahpromcp.tool.astah.pro.view.inputdto.PresentationWithHighlightColorDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/view/IDiagramViewManager.html
@Slf4j
public class DiagramViewManagerTool implements ToolProvider {

    private static final String FIT_WINDOW_TOOLBAR_BUTTON_NAME = "managementview.tool_button.drop_down_fit_window";
    private static final String FIT_WINDOW_ACTION_COMMAND = "FitWindow%both";

    private final ProjectAccessor projectAccessor;
    private final IDiagramViewManager diagramViewManager;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public DiagramViewManagerTool(ProjectAccessor projectAccessor, IDiagramViewManager diagramViewManager, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.diagramViewManager = diagramViewManager;
        this.transactionManager = transactionManager;
        this.astahProToolSupport = astahProToolSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create diagram view manager tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
                ToolSupport.definition(
                        "open_dgm",
                        "Open the specified diagram (specified by ID) in Diagram Editor. The diagram is shown in the front if the diagram has already been open. And return the opened diagram information.",
                        this::openDiagram,
                        IdDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "close_dgm",
                        "Close the specified diagram (specified by ID) in Diagram Editor. And return the closed diagram information.",
                        this::closeDiagram,
                        IdDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "select_prsts",
                        "Select the specified presentations (specified by ID) in current diagram, and return the selected presentations information.",
                        this::selectPresentations,
                        IdListDTO.class,
                        PresentationListDTO.class),

                ToolSupport.definition(
                        "select_all_prsts",
                        "Select all presentations in current diagram, and return the selected presentations information.",
                        this::selectAllPresentations,
                        NoInputDTO.class,
                        PresentationListDTO.class),

                ToolSupport.definition(
                        "unselect_all_prsts",
                        "Unselect all presentations in current diagram, and return the current diagram information.",
                        this::unselectAllPresentations,
                        NoInputDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "zoom",
                        "Zoom in current diagram, and return the zoomed diagram information.",
                        this::zoom,
                        com.astahpromcp.tool.astah.pro.view.inputdto.ZoomFactorDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "zoom_fit",
                        "Zoom fit in current diagram, and return the zoomed diagram information.",
                        this::zoomFit,
                        NoInputDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "center_prst_in_dgm",
                        "Center the specified presentations (specified by ID) in current diagram, and return the centered presentation information.",
                        this::centerPresentationInDiagram,
                        IdDTO.class,
                        PresentationDTO.class),

                ToolSupport.definition(
                        "get_current_dgm",
                        "Return the information of the currently selected diagram in Diagram Editor.",
                        this::getCurrentDiagram,
                        NoInputDTO.class,
                        DiagramDTO.class),

                ToolSupport.definition(
                        "get_selected_prst",
                        "Return the information of the currently selected presentations in Diagram Editor.",
                        this::getSelectedPresentations,
                        NoInputDTO.class,
                        PresentationListDTO.class),

                ToolSupport.definition(
                        "get_zoom_factor",
                        "Return the zoom factor (4.0 - 0.05) of the current diagram. Return 0.0 if the diagram is not opened.",
                        this::getZoomFactor,
                        NoInputDTO.class,
                        com.astahpromcp.tool.astah.pro.view.outputdto.ZoomFactorDTO.class),

                ToolSupport.definition(
                        "get_highlighted_prsts_within_dgm",
                        "Get the highlighted presentations within the specified diagram (specified by ID), and return the highlighted presentations information.",
                        this::getHighlightedPresentationsWithinDiagram,
                        IdDTO.class,
                        PresentationListDTO.class)
        );
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
                /* To leverage the AI's layout capabilities, do not register the automatic layout tool.
                ToolSupport.definition(
                        "auto_layout",
                        "Layout all presentations in the currently selected diagram automatically, and return the updated diagram information. Note that the diagram to be laid out must be open in the foreground.",
                        this::autoLayout,
                        NoInputDTO.class,
                        DiagramDTO.class),
                */

                ToolSupport.definition(
                        "highlight_prst",
                        "Temporarily highlight the specified presentation (specified by ID) in the specified color (in the format #FFFFFF), and return the highlighted presentation information. This highlight is temporary and is rendered only while the diagram is open. When you reopen the diagram, the highlight disappears.",
                        this::highlightPresentation,
                        PresentationWithHighlightColorDTO.class,
                        PresentationDTO.class),

                ToolSupport.definition(
                        "unhighlight_prst",
                        "Unhighlight the specified presentation (specified by ID), and return the unhighlighted presentation information.",
                        this::unhighlightPresentation,
                        IdDTO.class,
                        PresentationDTO.class)
        );
    }

    private DiagramDTO openDiagram(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Open diagram: {}", param);

        IDiagram diagram = astahProToolSupport.getDiagram(param.id());

        try {
            diagramViewManager.open(diagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open diagram.");
        }

        return DiagramDTOAssembler.toDTO(diagram);
    }

    private DiagramDTO closeDiagram(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Close diagram: {}", param);

        IDiagram diagram = astahProToolSupport.getDiagram(param.id());

        try {
            diagramViewManager.close(diagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to close diagram.");
        }

        return DiagramDTOAssembler.toDTO(diagram);
    }

    private DiagramDTO getCurrentDiagram(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get current diagram: {}", param);

        IDiagram currentAstahDiagram;
        try {
            currentAstahDiagram = diagramViewManager.getCurrentDiagram();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current diagram.");
        }

        return DiagramDTOAssembler.toDTO(currentAstahDiagram);
    }

    private PresentationListDTO getSelectedPresentations(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get selected presentations: {}", param);

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : diagramViewManager.getSelectedPresentations()) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }

        return new PresentationListDTO(presentationDTOs);
    }

    private PresentationListDTO selectPresentations(McpSyncServerExchange exchange, IdListDTO param) throws Exception {
        log.debug("Select presentations: {}", param);

        List<IPresentation> presentations = new ArrayList<>();
        for (String id : param.value().stream().map(IdDTO::id).toList()) {
            IPresentation presentation = astahProToolSupport.getPresentation(id);
            presentations.add(presentation);
        }

        try {
            diagramViewManager.select(presentations.toArray(IPresentation[]::new));
        } catch (Exception e) {
            throw new RuntimeException("Failed to select presentations.");
        }

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : presentations) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }

        return new PresentationListDTO(presentationDTOs);
    }

    private PresentationListDTO selectAllPresentations(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Select all presentations: {}", param);

        try {
            diagramViewManager.selectAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to select all presentations.");
        }

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : diagramViewManager.getSelectedPresentations()) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }

        return new PresentationListDTO(presentationDTOs);
    }

    private DiagramDTO unselectAllPresentations(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Unselect all presentations: {}", param);

        try {
            diagramViewManager.unselectAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to unselect all presentations.");
        }

        return DiagramDTOAssembler.toDTO(diagramViewManager.getCurrentDiagram());
    }

    private PresentationDTO centerPresentationInDiagram(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Center presentation in diagram: {}", param);

        IPresentation presentation = astahProToolSupport.getPresentation(param.id());

        try {
            diagramViewManager.showInDiagramEditor(presentation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to center presentation in diagram.");
        }

        return PresentationDTOAssembler.toDTO(presentation);
    }

    private DiagramDTO autoLayout(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Auto layout: {}", param);

        IDiagram currentDiagram;
        try {
            currentDiagram = diagramViewManager.getCurrentDiagram();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current diagram.");
        }

        try {
            transactionManager.beginTransaction();
            diagramViewManager.layoutAll();
            transactionManager.endTransaction();
            
        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }

        return DiagramDTOAssembler.toDTO(currentDiagram);
    }

    private com.astahpromcp.tool.astah.pro.view.outputdto.ZoomFactorDTO getZoomFactor(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Get zoom factor: {}", param);

        return new com.astahpromcp.tool.astah.pro.view.outputdto.ZoomFactorDTO(diagramViewManager.getZoomFactor());
    }

    private DiagramDTO zoom(McpSyncServerExchange exchange, com.astahpromcp.tool.astah.pro.view.inputdto.ZoomFactorDTO param) throws Exception {
        log.debug("Zoom: {}", param);

        try {
            diagramViewManager.zoom(param.zoomFactorValue(), false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to zoom.");
        }

        return DiagramDTOAssembler.toDTO(diagramViewManager.getCurrentDiagram());
    }

    private DiagramDTO zoomFit(McpSyncServerExchange exchange, NoInputDTO param) throws Exception {
        log.debug("Zoom fit: {}", param);

        IDiagram currentDiagram;
        try {
            currentDiagram = diagramViewManager.getCurrentDiagram();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current diagram.");
        }

        JFrame mainFrame = AstahAPI.getAstahAPI().getProjectAccessor().getViewManager().getMainFrame();
        if (mainFrame == null) {
            throw new RuntimeException("Main window is unavailable.");
        }

        Component fitControl = findComponentByName(mainFrame, FIT_WINDOW_TOOLBAR_BUTTON_NAME);
        if (fitControl == null) {
            throw new RuntimeException("Fit window button not found (name=" + FIT_WINDOW_TOOLBAR_BUTTON_NAME + ").");
        }

        AbstractButton button = resolveClickableFitButton(fitControl);
        Runnable click = () -> button.doClick();
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                click.run();
            } else {
                SwingUtilities.invokeAndWait(click);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while clicking fit window.", e);
            
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw new RuntimeException("Failed to click fit window button.", cause != null ? cause : e);
        }

        return DiagramDTOAssembler.toDTO(currentDiagram);
    }

    private static AbstractButton resolveClickableFitButton(Component fitControl) {
        if (fitControl instanceof AbstractButton ab) {
            return ab;
        }
        AbstractButton byAction = findAbstractButtonByActionCommand(fitControl, FIT_WINDOW_ACTION_COMMAND);
        if (byAction != null) {
            return byAction;
        }
        throw new RuntimeException(
                "Fit window control is not clickable (expected AbstractButton or descendant with actionCommand "
                        + FIT_WINDOW_ACTION_COMMAND
                        + ").");
    }

    private static Component findComponentByName(Component root, String name) {
        if (root == null || name == null) {
            return null;
        }
        if (name.equals(root.getName())) {
            return root;
        }
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                Component found = findComponentByName(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static AbstractButton findAbstractButtonByActionCommand(Component root, String actionCommand) {
        if (root instanceof AbstractButton ab && actionCommand.equals(ab.getActionCommand())) {
            return ab;
        }
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                AbstractButton found = findAbstractButtonByActionCommand(child, actionCommand);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private PresentationDTO highlightPresentation(McpSyncServerExchange exchange, PresentationWithHighlightColorDTO param) throws Exception {
        log.debug("Highlight presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());

        try {
            transactionManager.beginTransaction();
            if (astahPresentation instanceof INodePresentation) {
                diagramViewManager.setViewProperty(astahPresentation, IDiagramViewManager.BACKGROUND_COLOR, Color.decode(param.highlightColor()));
            } else if (astahPresentation instanceof ILinkPresentation) {
                diagramViewManager.setViewProperty(astahPresentation, IDiagramViewManager.LINE_COLOR, Color.decode(param.highlightColor()));
            } else {
                diagramViewManager.setViewProperty(astahPresentation, IDiagramViewManager.BACKGROUND_COLOR, Color.decode(param.highlightColor()));
            }
            transactionManager.endTransaction();

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }

        return PresentationDTOAssembler.toDTO(astahPresentation);
    }

    private PresentationDTO unhighlightPresentation(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Unhighlight presentation: {}", param);

        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.id());

        try {
            transactionManager.beginTransaction();
            diagramViewManager.clearAllViewProperties(astahPresentation);
            transactionManager.endTransaction();

        } catch (Exception e) {
            transactionManager.abortTransaction();
            throw e;
        }
        
        return PresentationDTOAssembler.toDTO(astahPresentation);
    }

    private PresentationListDTO getHighlightedPresentationsWithinDiagram(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Get highlighted presentations within diagram: {}", param);

        IDiagram diagram = astahProToolSupport.getDiagram(param.id());
        
        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : diagram.getPresentations()) {
            if (diagramViewManager.getViewProperty(presentation, IDiagramViewManager.BORDER_COLOR) != null
                || diagramViewManager.getViewProperty(presentation, IDiagramViewManager.LINE_COLOR) != null
                || diagramViewManager.getViewProperty(presentation, IDiagramViewManager.BACKGROUND_COLOR) != null) {
                presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
            }
        }

        return new PresentationListDTO(presentationDTOs);
    }
}
