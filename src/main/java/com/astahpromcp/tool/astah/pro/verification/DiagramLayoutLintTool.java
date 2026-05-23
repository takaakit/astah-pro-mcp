package com.astahpromcp.tool.astah.pro.verification;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.ReportDTO;
import com.change_vision.jude.api.inf.editor.ITransactionManager;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DiagramLayoutLintTool implements ToolProvider {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final ProjectAccessor projectAccessor;
    private final ITransactionManager transactionManager;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public DiagramLayoutLintTool(ProjectAccessor projectAccessor, ITransactionManager transactionManager, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
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
            log.error("Failed to create diagram layout lint tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "detect_overlap",
                "Detect overlap of presentations in the specified diagram (specified by ID) and return the overlap information. Use this tool when you need to adjust the layout of a diagram.",
                this::detectOverlap,
                IdDTO.class,
                ReportDTO.class));
    }

    private List<ToolDefinition> createEditTools() {
        return List.of();
    }

    private ReportDTO detectOverlap(McpSyncServerExchange exchange, IdDTO param) throws Exception {
        log.debug("Detect overlap: {}", param);

        IDiagram diagram = astahProToolSupport.getDiagram(param.id());
        StringBuilder sbContent = new StringBuilder();

        // Retrieve the node/link presentations to be checked for overlap detection
        List<INodePresentation> targetNodePresentations;
        List<ILinkPresentation> targetLinkPresentations;
        if (diagram instanceof IClassDiagram) {
            targetNodePresentations = getNodePresentationsByTypeNames(diagram, List.of(
                "Class",
                "AssociationClass",
                "InstanceSpecification",
                "UseCase",
                "Note"));

            targetLinkPresentations = getLinkPresentationsByTypeNames(diagram, List.of(
                "Association",
                "Generalization",
                "Realization",
                "Dependency",
                "Usage",
                "Link"));

        } else if (diagram instanceof IUseCaseDiagram) {
            targetNodePresentations = getNodePresentationsByTypeNames(diagram, List.of(
                "Class",
                "UseCase",
                "Note"));

            targetLinkPresentations = getLinkPresentationsByTypeNames(diagram, List.of(
                "Association",
                "Generalization",
                "Dependency",
                "Extend",
                "Include"));

        } else if (diagram instanceof ISequenceDiagram) {
            targetNodePresentations = getNodePresentationsByTypeNames(diagram, List.of(
                "Lifeline",
                "Note"));

            targetLinkPresentations = getLinkPresentationsByTypeNames(diagram, List.of(
                "Message"));

        } else if (diagram instanceof IActivityDiagram) {
            targetNodePresentations = getNodePresentationsByTypeNames(diagram, List.of(
                "InitialNode",
                "Action",
                "Flow Final Node",
                "SendSignalAction",
                "AcceptEventAction",
                "Process",
                "CallBehaviorAction",
                "ActivityFinal",
                "Decision Node & Merge Node",
                "ForkNode",
                "JoinNode",
                "ObjectNode",
                "Note"));

            targetLinkPresentations = getLinkPresentationsByTypeNames(diagram, List.of(
                "ControlFlow/ObjectFlow"));

        } else if (diagram instanceof IStateMachineDiagram) {
            targetNodePresentations = getNodePresentationsByTypeNames(diagram, List.of(
                "InitialPseudostate",
                "FinalState",
                "JunctionPseudostate",
                "ChoicePseudostate",
                "ForkPseudostate",
                "JoinPseudostate",
                "Note"));

            targetLinkPresentations = getLinkPresentationsByTypeNames(diagram, List.of(
                "Transition"));

        } else {
            throw new IllegalArgumentException("The type of this diagram is excluded from overlap detection: " + diagram.getName() + "(" + diagram.getId() + ")");
        }

        // Detect overlaps
        sbContent.append(detectOverlapsBetweenRectangles(targetNodePresentations));
        sbContent.append(detectOverlapsBetweenLines(targetLinkPresentations));
        sbContent.append(detectOverlapsBetweenRectangleAndLine(targetNodePresentations, targetLinkPresentations));

        return new ReportDTO(sbContent.toString());
    }

    // Retrieve node presentations with the specified type names
    private List<INodePresentation> getNodePresentationsByTypeNames(IDiagram diagram, List<String> typeNames) throws Exception {
        List<INodePresentation> nodePresentations = new ArrayList<>();
        for (IPresentation presentation : diagram.getPresentations()) {
            if (typeNames.contains(presentation.getType()) && presentation instanceof INodePresentation) {
                nodePresentations.add((INodePresentation)presentation);
            }
        }
        return nodePresentations;
    }

    // Retrieve link presentations with the specified type names
    private List<ILinkPresentation> getLinkPresentationsByTypeNames(IDiagram diagram, List<String> typeNames) throws Exception {
        List<ILinkPresentation> linkPresentations = new ArrayList<>();
        for (IPresentation presentation : diagram.getPresentations()) {
            if (typeNames.contains(presentation.getType()) && presentation instanceof ILinkPresentation) {
                linkPresentations.add((ILinkPresentation)presentation);
            }
        }
        return linkPresentations;
    }

    // Detect overlaps between rectangles
    private StringBuilder detectOverlapsBetweenRectangles(List<INodePresentation> nodePresentations) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodePresentations.size() - 1; i++) {
            for (int j = i + 1; j < nodePresentations.size(); j++) {
                INodePresentation nodePresentationA = nodePresentations.get(i);
                INodePresentation nodePresentationB = nodePresentations.get(j);
                
                Polygon polygonA = toPolygon(nodePresentationA.getRectangle());
                Polygon polygonB = toPolygon(nodePresentationB.getRectangle());

                // Detect overlap
                if (polygonA.intersection(polygonB).getArea() > 0) {
                    sb.append(formatOverlap(nodePresentationA, nodePresentationB)).append("\n");
                }
            }
        }
        return sb;
    }

    // Detect overlaps between rectangles and lines
    private StringBuilder detectOverlapsBetweenRectangleAndLine(List<INodePresentation> nodePresentations, List<ILinkPresentation> linkPresentations) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (ILinkPresentation linkPresentation : linkPresentations) {
            LineString lineString = toLineString(linkPresentation.getPoints());
            if (lineString == null) {
                continue;
            }

            IPresentation sourceEnd = linkPresentation.getSourceEnd();
            IPresentation targetEnd = linkPresentation.getTargetEnd();
            for (INodePresentation nodePresentation : nodePresentations) {
                // Skip the link's own endpoints, since the link legitimately touches them.
                if (nodePresentation.equals(sourceEnd) || nodePresentation.equals(targetEnd)) {
                    continue;
                }

                Polygon polygon = toPolygon(nodePresentation.getRectangle());

                // Detect overlap
                Geometry intersection = lineString.intersection(polygon);
                if (intersection.getDimension() >= 1 && intersection.getLength() > 0) {
                    sb.append(formatOverlap(linkPresentation, nodePresentation)).append("\n");
                }
            }
        }
        return sb;
    }

    // Detect overlaps between lines
    private StringBuilder detectOverlapsBetweenLines(List<ILinkPresentation> linkPresentations) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < linkPresentations.size() - 1; i++) {
            for (int j = i + 1; j < linkPresentations.size(); j++) {
                ILinkPresentation linkPresentationA = linkPresentations.get(i);
                ILinkPresentation linkPresentationB = linkPresentations.get(j);
                
                LineString lineA = toLineString(linkPresentationA.getPoints());
                LineString lineB = toLineString(linkPresentationB.getPoints());
                if (lineA == null || lineB == null) {
                    continue;
                }

                // Detect overlap
                //   Treat as overlapping when they mostly overlap.
                final double EPS = 4.0;
                double intersectionLength = lineB.intersection(lineA.buffer(EPS)).getLength();
                if ((int) (intersectionLength - (lineA.getLength() - EPS)) >= 0
                        || (int) (intersectionLength - (lineB.getLength() - EPS)) >= 0) {
                    sb.append(formatOverlap(linkPresentationA, linkPresentationB)).append("\n");
                }
            }
        }
        return sb;
    }

    private static Polygon toPolygon(Rectangle2D rectangle) {
        double x = rectangle.getX();
        double y = rectangle.getY();
        double w = rectangle.getWidth();
        double h = rectangle.getHeight();
        return GEOMETRY_FACTORY.createPolygon(new Coordinate[] {
            new Coordinate(x, y),
            new Coordinate(x + w, y),
            new Coordinate(x + w, y + h),
            new Coordinate(x, y + h),
            new Coordinate(x, y)
        });
    }

    private static LineString toLineString(Point2D[] points) {
        if (points == null || points.length < 2) {
            return null;
        }
        Coordinate[] coordinates = new Coordinate[points.length];
        for (int i = 0; i < points.length; i++) {
            coordinates[i] = new Coordinate(points[i].getX(), points[i].getY());
        }
        return GEOMETRY_FACTORY.createLineString(coordinates);
    }

    private static String formatOverlap(IPresentation a, IPresentation b) {
        return String.format("[%s] %s (id=%s) overlaps with [%s] %s (id=%s)",
            a.getType(), labelOf(a), a.getID(),
            b.getType(), labelOf(b), b.getID());
    }

    private static String labelOf(IPresentation presentation) {
        String label = presentation.getLabel();
        return (label == null || label.isEmpty()) ? "(unnamed)" : label;
    }
}
