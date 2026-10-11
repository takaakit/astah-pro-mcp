package com.astahpromcp.tool.astah.pro.view;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.EdtSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdListDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.DiagramDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.assembler.DiagramDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.assembler.PresentationDTOAssembler;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationListDTO;
import com.astahpromcp.tool.astah.pro.view.inputdto.PresentationWithHighlightColorDTO;
import com.astahpromcp.tool.astah.pro.view.outputdto.DiagramWithZoomFactorDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/latest/api/en/doc/javadoc/com/change_vision/jude/api/inf/view/IDiagramViewManager.html
@Slf4j
public class DiagramViewManagerTool extends AstahToolProvider {

    private static final String FIT_WINDOW_TOOLBAR_BUTTON_NAME = "managementview.tool_button.drop_down_fit_window";
    private static final String FIT_WINDOW_ACTION_COMMAND = "FitWindow%both";

    private static final String BRING_TO_FRONT_MENU_ITEM_NAME = "managementview.menu.edit.arrange_depth.front";
    private static final String SEND_TO_BACK_MENU_ITEM_NAME = "managementview.menu.edit.arrange_depth.back";

    private static final double MIN_ZOOM_FACTOR = 0.05;
    private static final double MAX_ZOOM_FACTOR = 4.0;

    private final ProjectAccessor projectAccessor;
    private final IDiagramViewManager diagramViewManager;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public DiagramViewManagerTool(ProjectAccessor projectAccessor, IDiagramViewManager diagramViewManager, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.diagramViewManager = diagramViewManager;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "open_dgm",
                "Open the specified diagram (specified by ID) in Diagram Editor. The diagram is shown in the front if the diagram has already been open. And return the model element of the opened diagram.",
                this::openDiagram,
                IdDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "close_dgm",
                "Close the specified diagram (specified by ID) in Diagram Editor. And return the model element of the closed diagram.",
                this::closeDiagram,
                IdDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "select_prsts",
                "Select the specified presentations (specified by ID) in current diagram, replacing the current selection, and return the selected presentations. Every presentation must be on the current diagram, so open its diagram first.",
                this::selectPresentations,
                IdListDTO.class,
                PresentationListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "select_all_prsts",
                "Select all presentations in current diagram, and return the selected presentations.",
                this::selectAllPresentations,
                NoInputDTO.class,
                PresentationListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "unselect_all_prsts",
                "Unselect all presentations in current diagram, and return the model element of the current diagram.",
                this::unselectAllPresentations,
                NoInputDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "zoom",
                "Set the zoom factor of current diagram to the specified absolute value, and return the model element of the zoomed diagram with the zoom factor actually applied. The zoom factor must be between 0.05 and 4.0 (1.0 is 100%); a value outside this range is rejected with an error. The value is not relative to the current zoom factor. Astah rounds a value within the range to its own zoom steps, so the applied zoom factor can differ slightly from the specified one (e.g., 1.25 may become 1.26); check it in the result.",
                this::zoom,
                com.astahpromcp.tool.astah.pro.view.inputdto.ZoomFactorDTO.class,
                DiagramWithZoomFactorDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "zoom_fit",
                "Zoom fit in current diagram, and return the model element of the zoomed diagram.",
                this::zoomFit,
                NoInputDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "center_prst_in_dgm",
                "Show the specified presentation (specified by ID) in the center of Diagram Editor and select it, and return the presentation. The diagram that contains the presentation is opened and becomes the current diagram.",
                this::centerPresentationInDiagram,
                IdDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_current_dgm",
                "Return the model element of the currently selected diagram in Diagram Editor.",
                this::getCurrentDiagram,
                NoInputDTO.class,
                DiagramDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_selected_prst",
                "Return the currently selected presentations in Diagram Editor.",
                this::getSelectedPresentations,
                NoInputDTO.class,
                PresentationListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_zoom_factor",
                "Return the zoom factor of the current diagram (0.05 - 4.0, where 1.0 is 100%). Return 0.0 if the diagram is not opened.",
                this::getZoomFactor,
                NoInputDTO.class,
                com.astahpromcp.tool.astah.pro.view.outputdto.ZoomFactorDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "get_highlighted_prsts_within_dgm",
                "Get the highlighted presentations within the specified diagram (specified by ID), and return the highlighted presentations information. Highlights exist only while the diagram is open, so an empty list is returned if the diagram is not open.",
                this::getHighlightedPresentationsWithinDiagram,
                IdDTO.class,
                PresentationListDTO.class),


            /* To leverage the AI's layout capabilities, do not register the automatic layout tool.
            ToolSupport.definition(
                "auto_layout",
                "Layout all presentations in the currently selected diagram automatically, and return the updated diagram information. Note that the diagram to be laid out must be open in the foreground.",
                this::autoLayout,
                NoInputDTO.class,
                DiagramDTO.class),
            */

            ToolSupport.toolDefinitionReturningDto(
                "highlight_prst",
                "Temporarily highlight the specified presentation (specified by ID) in the specified color (in the format #FFFFFF), and return the highlighted presentation. This highlight is temporary and is rendered only while the diagram is open. When you reopen the diagram, the highlight disappears.",
                this::highlightPresentation,
                PresentationWithHighlightColorDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "unhighlight_prst",
                "Unhighlight the specified presentation (specified by ID), and return the unhighlighted presentation.",
                this::unhighlightPresentation,
                IdDTO.class,
                PresentationDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "bring_prsts_to_front",
                "Bring the specified node/link presentations (specified by ID) to front in current diagram, and return the presentations. If a presentation is hidden behind another presentation and cannot be seen, use this tool to adjust the Z-order and resolve the issue.",
                this::bringPresentationsToFront,
                IdListDTO.class,
                PresentationListDTO.class),

            ToolSupport.toolDefinitionReturningDto(
                "send_prsts_to_back",
                "Send the specified node/link presentations (specified by ID) to back in current diagram, and return the presentations. If a presentation is hidden behind another presentation and cannot be seen, use this tool to adjust the Z-order and resolve the issue.",
                this::sendPresentationsToBack,
                IdListDTO.class,
                PresentationListDTO.class)
        );
    }

    private DiagramDTO openDiagram(IdDTO param) throws Exception {
        IDiagram diagram = astahProToolSupport.getDiagram(param.id());

        try {
            diagramViewManager.open(diagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open diagram.", e);
        }

        return DiagramDTOAssembler.toDTO(diagram);
    }

    private DiagramDTO closeDiagram(IdDTO param) throws Exception {
        IDiagram diagram = astahProToolSupport.getDiagram(param.id());

        try {
            diagramViewManager.close(diagram);
        } catch (Exception e) {
            throw new RuntimeException("Failed to close diagram.", e);
        }

        return DiagramDTOAssembler.toDTO(diagram);
    }

    // Return the diagram currently open in Diagram Editor
    private IDiagram requireCurrentDiagram() {
        IDiagram currentDiagram;
        try {
            currentDiagram = diagramViewManager.getCurrentDiagram();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current diagram.", e);
        }

        if (currentDiagram == null) {
            throw new IllegalStateException(
                    "No diagram is currently open in the Diagram Editor. Open a diagram first (e.g., with the 'open_dgm' tool).");
        }

        return currentDiagram;
    }

    private DiagramDTO getCurrentDiagram(NoInputDTO param) throws Exception {
        return DiagramDTOAssembler.toDTO(requireCurrentDiagram());
    }

    private PresentationListDTO getSelectedPresentations(NoInputDTO param) throws Exception {
        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : diagramViewManager.getSelectedPresentations()) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }

        return new PresentationListDTO(presentationDTOs);
    }

    private PresentationListDTO selectPresentations(IdListDTO param) throws Exception {
        if (param.value() == null || param.value().isEmpty()) {
            throw new IllegalArgumentException("Specify at least one presentation ID.");
        }

        IDiagram currentDiagram = requireCurrentDiagram();

        List<IPresentation> presentations = new ArrayList<>();
        for (String id : param.value().stream().map(IdDTO::id).toList()) {
            IPresentation presentation = astahProToolSupport.getPresentation(id);
            requireOnDiagram(presentation, currentDiagram);
            presentations.add(presentation);
        }

        IPresentation[] previousSelection = diagramViewManager.getSelectedPresentations();
        try {
            diagramViewManager.select(presentations.toArray(IPresentation[]::new));
        } catch (Exception e) {
            throw new RuntimeException("Failed to select presentations.", e);
        }

        // select() silently skips a presentation it cannot select, so check the selection took
        List<IPresentation> selectedPresentations = Arrays.asList(diagramViewManager.getSelectedPresentations());
        List<IPresentation> unselectedPresentations = presentations.stream()
                .filter(presentation -> !selectedPresentations.contains(presentation))
                .distinct()
                .toList();
        if (!unselectedPresentations.isEmpty()) {
            diagramViewManager.unselectAll();
            if (previousSelection.length > 0) {
                diagramViewManager.select(previousSelection);
            }
            throw new IllegalArgumentException(String.format(
                    "Presentations cannot be selected: %s. The selection was left unchanged.",
                    unselectedPresentations.stream()
                            .map(presentation -> String.format("(ID: %s, type: %s)", presentation.getID(), presentation.getType()))
                            .collect(Collectors.joining(", "))));
        }

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : selectedPresentations) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }

        return new PresentationListDTO(presentationDTOs);
    }

    private PresentationListDTO selectAllPresentations(NoInputDTO param) throws Exception {
        try {
            diagramViewManager.selectAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to select all presentations.", e);
        }

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : diagramViewManager.getSelectedPresentations()) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }

        return new PresentationListDTO(presentationDTOs);
    }

    private DiagramDTO unselectAllPresentations(NoInputDTO param) throws Exception {
        try {
            diagramViewManager.unselectAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to unselect all presentations.", e);
        }

        return DiagramDTOAssembler.toDTO(requireCurrentDiagram());
    }

    private PresentationDTO centerPresentationInDiagram(IdDTO param) throws Exception {
        IPresentation presentation = astahProToolSupport.getPresentation(param.id());

        try {
            diagramViewManager.showInDiagramEditor(presentation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to center presentation in diagram.", e);
        }

        return PresentationDTOAssembler.toDTO(presentation);
    }

    private DiagramDTO autoLayout(NoInputDTO param) throws Exception {
        log.debug("Auto layout: {}", param);

        IDiagram currentDiagram = requireCurrentDiagram();

        txnAstah.run( () -> {
            diagramViewManager.layoutAll();
        });


        return DiagramDTOAssembler.toDTO(currentDiagram);
    }

    private com.astahpromcp.tool.astah.pro.view.outputdto.ZoomFactorDTO getZoomFactor(NoInputDTO param) throws Exception {
        return new com.astahpromcp.tool.astah.pro.view.outputdto.ZoomFactorDTO(diagramViewManager.getZoomFactor());
    }

    private DiagramWithZoomFactorDTO zoom(com.astahpromcp.tool.astah.pro.view.inputdto.ZoomFactorDTO param) throws Exception {
        double zoomFactor = param.zoomFactorValue();
        // Negated so that NaN is rejected too
        if (!(zoomFactor >= MIN_ZOOM_FACTOR && zoomFactor <= MAX_ZOOM_FACTOR)) {
            throw new IllegalArgumentException(String.format(
                    "The zoom factor must be between %s and %s (1.0 is 100%%). It is absolute, not relative to the current zoom factor: %s",
                    MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR, zoomFactor));
        }
        IDiagram currentDiagram = requireCurrentDiagram();

        try {
            diagramViewManager.zoom(zoomFactor, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to zoom.", e);
        }

        // Astah rounds the value to its own zoom steps without telling, so return the zoom factor it actually applied
        return new DiagramWithZoomFactorDTO(
                DiagramDTOAssembler.toDTO(currentDiagram),
                diagramViewManager.getZoomFactor());
    }

    private DiagramDTO zoomFit(NoInputDTO param) throws Exception {
        IDiagram currentDiagram = requireCurrentDiagram();

        JFrame mainFrame = projectAccessor.getViewManager().getMainFrame();
        if (mainFrame == null) {
            throw new RuntimeException("Main window is unavailable.");
        }

        // Find and click in one EDT task
        EdtSupport.call(() -> {
            Component fitControl = findComponentByName(mainFrame, FIT_WINDOW_TOOLBAR_BUTTON_NAME);
            if (fitControl == null) {
                throw new RuntimeException("Fit window button not found (name=" + FIT_WINDOW_TOOLBAR_BUTTON_NAME + ").");
            }
            resolveClickableFitButton(fitControl).doClick();
            return null;
        });

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
        
        if (root instanceof JMenu menu) {
            Component found = findComponentByName(menu.getPopupMenu(), name);
            if (found != null) {
                return found;
            }
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
        
        if (root instanceof JMenu menu) {
            AbstractButton found = findAbstractButtonByActionCommand(menu.getPopupMenu(), actionCommand);
            if (found != null) {
                return found;
            }
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

    private PresentationDTO highlightPresentation(PresentationWithHighlightColorDTO param) throws Exception {
        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.presentationId());
        Color highlightColor = Color.decode(AstahProToolSupport.requireRrggbbColor(param.highlightColor()));

        if (astahPresentation instanceof ILinkPresentation) {
            diagramViewManager.setViewProperty(astahPresentation, IDiagramViewManager.LINE_COLOR, highlightColor);
        } else {
            diagramViewManager.setViewProperty(astahPresentation, IDiagramViewManager.BACKGROUND_COLOR, highlightColor);
        }

        return PresentationDTOAssembler.toDTO(astahPresentation);
    }

    private PresentationDTO unhighlightPresentation(IdDTO param) throws Exception {
        IPresentation astahPresentation = astahProToolSupport.getPresentation(param.id());

        diagramViewManager.clearAllViewProperties(astahPresentation);

        return PresentationDTOAssembler.toDTO(astahPresentation);
    }

    private PresentationListDTO getHighlightedPresentationsWithinDiagram(IdDTO param) throws Exception {
        IDiagram diagram = astahProToolSupport.getDiagram(param.id());

        // Highlights are discarded when the diagram is closed
        if (!Arrays.asList(diagramViewManager.getOpenDiagrams()).contains(diagram)) {
            return new PresentationListDTO(List.of());
        }

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : AstahProToolSupport.getPresentationsOnDiagram(diagram)) {
            // The cells of a CRUD and a generalization group reject view properties, so they can never be highlighted
            boolean highlighted;
            try {
                highlighted = diagramViewManager.getViewProperty(presentation, IDiagramViewManager.BORDER_COLOR) != null
                    || diagramViewManager.getViewProperty(presentation, IDiagramViewManager.LINE_COLOR) != null
                    || diagramViewManager.getViewProperty(presentation, IDiagramViewManager.BACKGROUND_COLOR) != null;
            } catch (InvalidUsingException e) {
                continue;
            }

            if (highlighted) {
                presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
            }
        }

        return new PresentationListDTO(presentationDTOs);
    }

    private PresentationListDTO bringPresentationsToFront(IdListDTO param) throws Exception {
        return arrangeDepthOfPresentations(param, BRING_TO_FRONT_MENU_ITEM_NAME);
    }

    private PresentationListDTO sendPresentationsToBack(IdListDTO param) throws Exception {
        return arrangeDepthOfPresentations(param, SEND_TO_BACK_MENU_ITEM_NAME);
    }

    private PresentationListDTO arrangeDepthOfPresentations(IdListDTO param, String menuItemName) throws Exception {
        IDiagram currentDiagram = requireCurrentDiagram();

        List<IPresentation> presentations = new ArrayList<>();
        for (String id : param.value().stream().map(IdDTO::id).toList()) {
            IPresentation presentation = astahProToolSupport.getPresentation(id);
            requireOnDiagram(presentation, currentDiagram);
            presentations.add(presentation);
        }

        for (IPresentation presentation : presentations) {
            if (!selectOnly(presentation)) {
                throw new IllegalArgumentException(String.format(
                        "Presentation (ID: %s, type: %s) cannot be selected, so its Z-order cannot be changed.",
                        presentation.getID(), presentation.getType()));
            }
        }

        // Sort Z-order
        if (BRING_TO_FRONT_MENU_ITEM_NAME.equals(menuItemName)) {
            presentations.sort((a, b) -> Integer.compare(b.getDepth(), a.getDepth()));
        } else {
            presentations.sort((a, b) -> Integer.compare(a.getDepth(), b.getDepth()));
        }

        for (IPresentation presentation : presentations) {
            // The arrange depth menu items stay enabled with nothing selected, so a click without a selection would do nothing
            if (!selectOnly(presentation)) {
                throw new IllegalStateException(String.format("Failed to select presentation (ID: %s).", presentation.getID()));
            }
            clickArrangeDepthMenuItem(menuItemName);
        }

        List<PresentationDTO> presentationDTOs = new ArrayList<>();
        for (IPresentation presentation : presentations) {
            presentationDTOs.add(PresentationDTOAssembler.toDTO(presentation));
        }

        return new PresentationListDTO(presentationDTOs);
    }

    private static void requireOnDiagram(IPresentation presentation, IDiagram diagram) {
        IDiagram actualDiagram = presentation.getDiagram();
        if (actualDiagram != null && diagram.getId().equals(actualDiagram.getId())) {
            return;
        }

        throw new IllegalArgumentException(String.format(
                "Presentation (ID: %s) is not on the current diagram '%s'%s. Open its diagram first (e.g., with the 'open_dgm' tool).",
                presentation.getID(), diagram.getName(),
                actualDiagram == null ? "" : " but on '" + actualDiagram.getName() + "'"));
    }

    // Select only the given presentation, and return whether the selection took
    private boolean selectOnly(IPresentation presentation) {
        try {
            diagramViewManager.unselectAll();
            diagramViewManager.select(new IPresentation[]{presentation});
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to select presentation (ID: %s).", presentation.getID()), e);
        }

        return Arrays.asList(diagramViewManager.getSelectedPresentations()).contains(presentation);
    }

    private void clickArrangeDepthMenuItem(String menuItemName) throws Exception {
        JFrame mainFrame = projectAccessor.getViewManager().getMainFrame();
        if (mainFrame == null) {
            throw new RuntimeException("Main window is unavailable.");
        }

        // Find and click in one EDT task
        EdtSupport.call(() -> {
            // The Edit menu-bar item is stable and always present (unlike the toolbar dropdown,
            // whose lazily-built popup items are not reachable until shown).
            Component component = findComponentByName(mainFrame, menuItemName);
            if (!(component instanceof AbstractButton item)) {
                throw new RuntimeException("Arrange depth menu item not found (name=" + menuItemName + ").");
            }
            if (!item.isEnabled()) {
                throw new RuntimeException("Arrange depth menu item is disabled (name=" + menuItemName + ").");
            }
            item.doClick();
            return null;
        });
    }

}
