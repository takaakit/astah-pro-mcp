package com.astahpromcp.tool.astah.pro.review;

import com.astahpromcp.tool.astah.pro.AstahToolProvider;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.ReportDTO;
import com.astahpromcp.tool.astah.pro.TransactionSupport;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.astah.pro.presentation.outputdto.PresentationDTO.Type;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DiagramLayoutLintTool extends AstahToolProvider {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    // The node and link presentation types checked for overlaps on one diagram type.
    private record OverlapTarget(
        NamedElementDTO.Type diagramType,
        Set<Type> nodeTypes,
        Set<Type> linkTypes
    ) {
    }

    // The diagram types supported by overlap detection
    private static final List<OverlapTarget> OVERLAP_TARGETS = List.of(
        new OverlapTarget(
            NamedElementDTO.Type.CLASS_DIAGRAM,
            EnumSet.of(
                Type.CLASS,
                Type.ASSOCIATION_CLASS,
                Type.INSTANCE_SPECIFICATION,
                Type.USE_CASE,
                Type.NOTE),
            EnumSet.of(
                Type.ASSOCIATION,
                Type.GENERALIZATION,
                Type.REALIZATION,
                Type.DEPENDENCY,
                Type.USAGE,
                Type.LINK)),

        new OverlapTarget(
            NamedElementDTO.Type.USE_CASE_DIAGRAM,
            EnumSet.of(
                Type.CLASS,
                Type.USE_CASE,
                Type.NOTE),
            EnumSet.of(
                Type.ASSOCIATION,
                Type.GENERALIZATION,
                Type.DEPENDENCY,
                Type.EXTEND,
                Type.INCLUDE)),

        new OverlapTarget(
            NamedElementDTO.Type.SEQUENCE_DIAGRAM,
            EnumSet.of(
                Type.LIFELINE,
                Type.NOTE),
            EnumSet.of(
                Type.MESSAGE)),

        new OverlapTarget(
            NamedElementDTO.Type.ACTIVITY_DIAGRAM,
            EnumSet.of(
                Type.INITIAL_NODE,
                Type.ACTION,
                Type.FLOW_FINAL_NODE,
                Type.SEND_SIGNAL_ACTION,
                Type.ACCEPT_EVENT_ACTION,
                Type.PROCESS,
                Type.CALL_BEHAVIOR_ACTION,
                Type.ACTIVITY_FINAL,
                Type.DECISION_NODE_AND_MERGE_NODE,
                Type.FORK_NODE,
                Type.JOIN_NODE,
                Type.OBJECT_NODE,
                Type.NOTE),
            EnumSet.of(
                Type.CONTROL_FLOW_OR_OBJECT_FLOW)),

        new OverlapTarget(
            NamedElementDTO.Type.STATE_MACHINE_DIAGRAM,
            EnumSet.of(
                Type.INITIAL_PSEUDOSTATE,
                Type.FINAL_STATE,
                Type.JUNCTION_PSEUDOSTATE,
                Type.CHOICE_PSEUDOSTATE,
                Type.FORK_PSEUDOSTATE,
                Type.JOIN_PSEUDOSTATE,
                Type.NOTE),
            EnumSet.of(
                Type.TRANSITION)),

        new OverlapTarget(
            NamedElementDTO.Type.REQUIREMENT_DIAGRAM,
            EnumSet.of(
                Type.REQUIREMENT,
                Type.TEST_CASE,
                Type.CLASS,
                Type.USE_CASE,
                Type.NOTE),
            EnumSet.of(
                Type.NEST,
                Type.DERIVE_REQT,
                Type.COPY,
                Type.SATISFY,
                Type.VERIFY,
                Type.REFINE,
                Type.TRACE,
                Type.DEPENDENCY)));

    private static final String SUPPORTED_DIAGRAM_TYPE_NAMES = OVERLAP_TARGETS.stream()
        .map(target -> target.diagramType().typeName)
        .collect(Collectors.joining(", "));

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;

    public DiagramLayoutLintTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
    }

    @Override
    protected List<ToolDefinition> createTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "detect_overlap",
                "Detect overlap of presentations in the specified diagram (specified by ID) and return the overlap information. Use this tool when you need to adjust the layout of a diagram. Supported diagram types: " + SUPPORTED_DIAGRAM_TYPE_NAMES + ".",
                this::detectOverlap,
                IdDTO.class,
                ReportDTO.class));
    }

    private ReportDTO detectOverlap(IdDTO param) throws Exception {
        IDiagram diagram = astahProToolSupport.getDiagram(param.id());
        StringBuilder sbContents = new StringBuilder();

        // Retrieve the node/link presentations to be checked for overlap detection
        OverlapTarget overlapTarget = findOverlapTarget(diagram).orElseThrow(() -> new IllegalArgumentException(String.format(
            "The type of this diagram is excluded from overlap detection: %s(%s) is a diagram of type %s. Supported diagram types: %s.",
            diagram.getName(),
            diagram.getId(),
            NamedElementDTO.Type.getCorrespondingType(diagram).typeName,
            SUPPORTED_DIAGRAM_TYPE_NAMES)));
        List<INodePresentation> targetNodePresentations = getNodePresentationsByTypes(diagram, overlapTarget.nodeTypes());
        List<ILinkPresentation> targetLinkPresentations = getLinkPresentationsByTypes(diagram, overlapTarget.linkTypes());

        // Detect overlaps
        sbContents.append(detectOverlapsBetweenRectangles(targetNodePresentations));
        sbContents.append(detectOverlapsBetweenLines(targetLinkPresentations));
        sbContents.append(detectOverlapsBetweenRectangleAndLine(targetNodePresentations, targetLinkPresentations));

        if (sbContents.isEmpty()) {
            sbContents.append("No overlap was detected.");
        }

        return new ReportDTO(sbContents.toString());
    }

    // Find the overlap target for the type of the diagram, if overlap detection supports it
    private static Optional<OverlapTarget> findOverlapTarget(IDiagram diagram) {
        return OVERLAP_TARGETS.stream()
            .filter(target -> target.diagramType().typeClass.isInstance(diagram))
            .findFirst();
    }

    // Retrieve node presentations of the specified types
    private List<INodePresentation> getNodePresentationsByTypes(IDiagram diagram, Set<Type> types) throws Exception {
        List<INodePresentation> nodePresentations = new ArrayList<>();
        for (IPresentation presentation : AstahProToolSupport.getPresentationsOnDiagram(diagram)) {
            if (types.contains(Type.getCorrespondingType(presentation.getType())) && presentation instanceof INodePresentation) {
                nodePresentations.add((INodePresentation)presentation);
            }
        }
        return nodePresentations;
    }

    // Retrieve link presentations of the specified types
    private List<ILinkPresentation> getLinkPresentationsByTypes(IDiagram diagram, Set<Type> types) throws Exception {
        List<ILinkPresentation> linkPresentations = new ArrayList<>();
        for (IPresentation presentation : AstahProToolSupport.getPresentationsOnDiagram(diagram)) {
            if (types.contains(Type.getCorrespondingType(presentation.getType())) && presentation instanceof ILinkPresentation) {
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
                if (isEndOrItsAncestor(nodePresentation, sourceEnd) || isEndOrItsAncestor(nodePresentation, targetEnd)) {
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

    // Whether the given node presentation is an end of the link, or encloses that end.
    // Comparing against the ends alone is not enough on a sequence diagram: the end of a message there is the activation the message leaves or starts, never the lifeline that activation sits on, while the nodes checked against are the lifelines.
    private static boolean isEndOrItsAncestor(INodePresentation nodePresentation, IPresentation end) {
        IPresentation current = end;
        while (current != null) {
            if (nodePresentation.equals(current)) {
                return true;
            }
            current = current instanceof INodePresentation node ? node.getParent() : null;
        }
        return false;
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
