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
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

// Tools definition for the following Astah API.
//   https://members.change-vision.com/javadoc/astah-api/11_0_0/api/en/doc/javadoc/com/change_vision/jude/api/inf/view/IDiagramViewManager.html
@Slf4j
public class DiagramViewManagerTool implements ToolProvider {

    private static final double FIT_EPS = 1e-3;
    private static final double FIT_PADDING = 0.95;
    private static final double MIN_ZOOM = 0.05;
    private static final double MAX_ZOOM = 4.0;

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
                ToolSupport.definition(
                        "auto_layout",
                        "Layout all presentations in the currently selected diagram automatically, and return the updated diagram information. Note that the diagram to be laid out must be open in the foreground.",
                        this::autoLayout,
                        NoInputDTO.class,
                        DiagramDTO.class),

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
        log.debug("Zoom fit using presentation bounds: {}", param);

        IDiagram currentDiagram;
        try {
            currentDiagram = diagramViewManager.getCurrentDiagram();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get the current diagram.");
        }

        IPresentation[] presentations = currentDiagram.getPresentations();

        // If no presentations are found, reset the zoom to 1.0.
        if (presentations == null || presentations.length == 0) {
            log.debug("No presentations found in the diagram. Resetting zoom to 1.0.");
            diagramViewManager.zoom(1.0, true);
            return DiagramDTOAssembler.toDTO(currentDiagram);
        }

        // Compute the bounds of the presentations.
        Rectangle2D contentBounds = computePresentationsBounds(currentDiagram, presentations);
        if (contentBounds == null || contentBounds.isEmpty()) {
            log.warn("Unable to calculate presentation bounds. Aborting zoom fit.");
            return DiagramDTOAssembler.toDTO(currentDiagram);
        }

        // Get the bounds of the diagram editor.
        Rectangle2D viewDeviceBounds = diagramViewManager.getCurrentDiagramEditorBoundsRect();
        if (viewDeviceBounds == null || viewDeviceBounds.getWidth() <= FIT_EPS || viewDeviceBounds.getHeight() <= FIT_EPS) {
            log.warn("Diagram editor bounds are unavailable or too small. Aborting zoom fit.");
            return DiagramDTOAssembler.toDTO(currentDiagram);
        }

        // Compute the target zoom.
        double targetZoom = computeTargetZoom(viewDeviceBounds, contentBounds);
        diagramViewManager.zoom(targetZoom, true);

        // Get the bounds of the diagram editor after zooming.
        Rectangle2D updatedViewDeviceBounds = diagramViewManager.getCurrentDiagramEditorBoundsRect();
        Point2D viewCenterWorld = toWorldCenter(updatedViewDeviceBounds);
        if (viewCenterWorld != null) {
            // Compute the delta to pan the diagram.
            double deltaX = contentBounds.getCenterX() - viewCenterWorld.getX();
            double deltaY = contentBounds.getCenterY() - viewCenterWorld.getY();
            if (Math.abs(deltaX) > FIT_EPS || Math.abs(deltaY) > FIT_EPS) {
                diagramViewManager.pan(deltaX, deltaY);
            }
        } else {
            log.warn("Failed to convert view center to world coordinates. Skipping pan.");
        }

        return DiagramDTOAssembler.toDTO(currentDiagram);
    }

    // Compute the bounds of the presentations.
    private Rectangle2D computePresentationsBounds(IDiagram diagram, IPresentation[] presentations) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (IPresentation presentation : presentations) {
            if (presentation == null) {
                continue;
            }

            if (presentation instanceof INodePresentation) {
                INodePresentation nodePresentation = (INodePresentation) presentation;
                Rectangle2D rectangle = nodePresentation.getRectangle();
                if (rectangle == null) {
                    continue;
                }

                minX = Math.min(minX, rectangle.getMinX());
                minY = Math.min(minY, rectangle.getMinY());
                maxX = Math.max(maxX, rectangle.getMaxX());
                maxY = Math.max(maxY, rectangle.getMaxY());

            } else if (presentation instanceof ILinkPresentation) {
                ILinkPresentation linkPresentation = (ILinkPresentation) presentation;
                Point2D[] points = linkPresentation.getAllPoints();
                if (points == null || points.length == 0) {
                    continue;
                }

                for (Point2D point : points) {
                    if (point == null) {
                        continue;
                    }
                    minX = Math.min(minX, point.getX());
                    minY = Math.min(minY, point.getY());
                    maxX = Math.max(maxX, point.getX());
                    maxY = Math.max(maxY, point.getY());
                }
            }
        }

        Rectangle2D boundsFromPresentations = null;

        if (minX != Double.POSITIVE_INFINITY && minY != Double.POSITIVE_INFINITY
                && maxX != Double.NEGATIVE_INFINITY && maxY != Double.NEGATIVE_INFINITY) {
            double width = maxX - minX;
            double height = maxY - minY;

            double adjustedMinX = minX;
            double adjustedMinY = minY;
            double adjustedMaxX = maxX;
            double adjustedMaxY = maxY;

            if (width < FIT_EPS) {
                double half = FIT_EPS / 2.0;
                double centerX = (minX + maxX) / 2.0;
                adjustedMinX = centerX - half;
                adjustedMaxX = centerX + half;
            }

            if (height < FIT_EPS) {
                double half = FIT_EPS / 2.0;
                double centerY = (minY + maxY) / 2.0;
                adjustedMinY = centerY - half;
                adjustedMaxY = centerY + half;
            }

            boundsFromPresentations = new Rectangle2D.Double(
                    adjustedMinX,
                    adjustedMinY,
                    adjustedMaxX - adjustedMinX,
                    adjustedMaxY - adjustedMinY);
        }

        Rectangle2D diagramBounds = null;
        try {
            diagramBounds = diagram.getBoundRect();
        } catch (Exception e) {
            log.warn("Failed to obtain diagram bound rectangle.", e);
        }

        if (diagramBounds != null && !diagramBounds.isEmpty()) {
            if (boundsFromPresentations != null) {
                boundsFromPresentations = boundsFromPresentations.createUnion(diagramBounds);
            } else {
                boundsFromPresentations = (Rectangle2D) diagramBounds.clone();
            }
        }

        return boundsFromPresentations;
    }

    // Compute the target zoom.
    private double computeTargetZoom(Rectangle2D viewDeviceBounds, Rectangle2D contentBounds) {
        double widthRatio = viewDeviceBounds.getWidth() / Math.max(contentBounds.getWidth(), FIT_EPS);
        double heightRatio = viewDeviceBounds.getHeight() / Math.max(contentBounds.getHeight(), FIT_EPS);
        double zoom = Math.min(widthRatio, heightRatio) * FIT_PADDING;

        if (!Double.isFinite(zoom) || zoom <= 0.0) {
            double currentZoom = diagramViewManager.getZoomFactor();
            double fallbackZoom = currentZoom > 0.0 ? currentZoom : 1.0;
            return clampZoom(fallbackZoom);
        }

        return clampZoom(zoom);
    }

    // Clamp the zoom to the minimum and maximum values.
    private double clampZoom(double zoom) {
        return Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, zoom));
    }

    // Convert the device bounds to the world center.
    private Point2D toWorldCenter(Rectangle2D deviceBounds) {
        if (deviceBounds == null) {
            return null;
        }

        try {
            Point2D topLeft = diagramViewManager.toWorldCoord(
                    (int) Math.round(deviceBounds.getX()),
                    (int) Math.round(deviceBounds.getY()));
            Point2D bottomRight = diagramViewManager.toWorldCoord(
                    (int) Math.round(deviceBounds.getX() + deviceBounds.getWidth()),
                    (int) Math.round(deviceBounds.getY() + deviceBounds.getHeight()));

            if (topLeft == null || bottomRight == null) {
                return null;
            }

            return new Point2D.Double(
                    (topLeft.getX() + bottomRight.getX()) / 2.0,
                    (topLeft.getY() + bottomRight.getY()) / 2.0);

        } catch (Exception e) {
            log.warn("Failed to convert device coordinates to world coordinates.", e);
            return null;
        }
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
