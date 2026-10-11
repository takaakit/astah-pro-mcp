package com.astahpromcp.tool.astah.pro.model;

import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.change_vision.jude.api.inf.model.ICombinedFragment;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IInteractionOperand;
import com.change_vision.jude.api.inf.model.IInteractionUse;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyUtil;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// What a sequence diagram draws in and around its combined fragments, read from the presentations alone.
//
// Astah keeps its own record of what each interaction operand holds (IInteractionOperand.getLifelines() and
// getMessages()), but that record does not always follow the drawing. Setting the length of the last operand through
// PresentationPropertyUtil.createOperandLengthKey() empties it for every operand of that combined fragment, and the
// messages already inside a combined fragment are not recorded as held by a new combined fragment drawn around them.
// The drawing is what a reader of the diagram sees, so this class works the contents out from it, by the rules Astah
// follows itself whenever it does record them:
//   * a lifeline is covered by an operand when the centre of its head lies within the width of the combined fragment
//     and its head starts above the bottom of the operand;
//   * a message is inside an operand when both of its ends lie within the width of the combined fragment and it
//     starts at or below the top of the operand and above its bottom; a message inside nested combined fragments
//     belongs to the innermost operand only.
public final class SequenceDiagramLayout {

    // The area an operand takes up: the full width of its combined fragment, from its top down to the top of the next operand.
    public record OperandArea(
        IInteractionOperand operand,
        double left,
        double right,
        double top,
        double bottom
    ) {

        public double height() {
            return bottom - top;
        }

        private boolean holds(MessageLine line) {
            return left <= line.left() && line.right() <= right && top <= line.y() && line.y() < bottom;
        }

        private double size() {
            return (right - left) * (bottom - top);
        }
    }

    // A drawn element at the Y coordinate where it lies across the diagram, for telling what a change of height would cut through or reach.
    public record Mark(
        double y,
        String description
    ) {
    }

    // What the diagram draws in an operand, and how high the operand is drawn. Both lists are empty and the height is 0 for an operand that is not drawn.
    public record DrawnContents(
        List<ILifeline> lifelines,
        List<IMessage> messages,
        double height
    ) {

        public static DrawnContents none() {
            return new DrawnContents(List.of(), List.of(), 0);
        }
    }

    private record MessageLine(
        IMessage message,
        double y,
        double left,
        double right
    ) {
    }

    private record LifelineHead(
        ILifeline lifeline,
        double centerX,
        double top
    ) {
    }

    // A combined fragment or an interaction use; only a combined fragment has operands.
    private record Frame(
        INodePresentation presentation,
        String description,
        double left,
        double right,
        double top,
        double bottom,
        List<OperandArea> operands
    ) {
    }

    private final List<MessageLine> messageLines = new ArrayList<>();
    private final List<LifelineHead> lifelineHeads = new ArrayList<>();
    private final List<Frame> frames = new ArrayList<>();

    private SequenceDiagramLayout() {
    }

    public static SequenceDiagramLayout of(IDiagram diagram) throws Exception {
        SequenceDiagramLayout layout = new SequenceDiagramLayout();

        for (IPresentation presentation : AstahProToolSupport.getPresentationsOnDiagram(diagram)) {
            if (presentation instanceof ILinkPresentation link && link.getModel() instanceof IMessage message) {
                Point2D[] points = link.getPoints();
                if (points.length == 0) {
                    continue;
                }
                double firstX = points[0].getX();
                double lastX = points[points.length - 1].getX();
                layout.messageLines.add(new MessageLine(message, points[0].getY(), Math.min(firstX, lastX), Math.max(firstX, lastX)));

            } else if (presentation instanceof INodePresentation node) {
                Rectangle2D rectangle = node.getRectangle();

                if (node.getModel() instanceof ILifeline lifeline) {
                    layout.lifelineHeads.add(new LifelineHead(lifeline, rectangle.getCenterX(), rectangle.getY()));

                } else if (node.getModel() instanceof ICombinedFragment combinedFragment) {
                    layout.frames.add(new Frame(node, describe("combined fragment", combinedFragment, node),
                        rectangle.getMinX(), rectangle.getMaxX(), rectangle.getMinY(), rectangle.getMaxY(),
                        operandAreas(node, combinedFragment, rectangle)));

                } else if (node.getModel() instanceof IInteractionUse interactionUse) {
                    layout.frames.add(new Frame(node, describe("interaction use", interactionUse, node),
                        rectangle.getMinX(), rectangle.getMaxX(), rectangle.getMinY(), rectangle.getMaxY(),
                        List.of()));
                }
            }
        }

        return layout;
    }

    // Each operand runs down from where the one above it ends, for the length the presentation keeps for it; the last one runs to the bottom of the combined fragment.
    private static List<OperandArea> operandAreas(INodePresentation presentation, ICombinedFragment combinedFragment, Rectangle2D rectangle) {
        IInteractionOperand[] operands = combinedFragment.getInteractionOperands();
        List<OperandArea> areas = new ArrayList<>();

        double top = rectangle.getMinY();
        for (int i = 0; i < operands.length; i++) {
            double bottom;
            if (i == operands.length - 1) {
                bottom = rectangle.getMaxY();
            } else {
                bottom = top + parseLength(presentation.getProperty(PresentationPropertyUtil.createOperandLengthKey(i + 1)));
            }
            areas.add(new OperandArea(operands[i], rectangle.getMinX(), rectangle.getMaxX(), top, bottom));
            top = bottom;
        }

        return areas;
    }

    private static double parseLength(String length) {
        try {
            return length == null ? 0 : Double.parseDouble(length);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String describe(String kind, INamedElement element, INodePresentation presentation) {
        String name = element.getName();
        if (name == null || name.isEmpty()) {
            return String.format("the unnamed %s (presentation ID: %s)", kind, presentation.getID());
        }
        return String.format("%s '%s' (presentation ID: %s)", kind, name, presentation.getID());
    }

    // IInteractionOperand.getOwner() answers null, so the combined fragment that holds an operand is looked for among all of them.
    public static ICombinedFragment findCombinedFragmentOf(ProjectAccessor projectAccessor, IInteractionOperand operand) throws Exception {
        for (INamedElement element : projectAccessor.findElements(ICombinedFragment.class)) {
            ICombinedFragment combinedFragment = (ICombinedFragment) element;
            for (IInteractionOperand candidate : combinedFragment.getInteractionOperands()) {
                if (candidate.getId().equals(operand.getId())) {
                    return combinedFragment;
                }
            }
        }
        return null;
    }

    public static DrawnContents drawnContentsOf(ProjectAccessor projectAccessor, IInteractionOperand operand) throws Exception {
        ICombinedFragment combinedFragment = findCombinedFragmentOf(projectAccessor, operand);
        if (combinedFragment == null) {
            return DrawnContents.none();
        }

        for (IPresentation presentation : combinedFragment.getPresentations()) {
            if (presentation instanceof INodePresentation node) {
                SequenceDiagramLayout layout = of(node.getDiagram());
                for (OperandArea area : layout.operandAreasOf(node)) {
                    if (area.operand().getId().equals(operand.getId())) {
                        return new DrawnContents(layout.lifelinesIn(area), layout.messagesIn(area), area.height());
                    }
                }
            }
        }
        return DrawnContents.none();
    }

    public List<OperandArea> operandAreasOf(INodePresentation combinedFragmentPresentation) {
        for (Frame frame : frames) {
            if (frame.presentation().getID().equals(combinedFragmentPresentation.getID())) {
                return frame.operands();
            }
        }
        return List.of();
    }

    // Ordered from left to right.
    public List<ILifeline> lifelinesIn(OperandArea area) {
        return lifelineHeads.stream()
            .filter(head -> area.left() <= head.centerX() && head.centerX() <= area.right() && head.top() < area.bottom())
            .sorted(Comparator.comparingDouble(LifelineHead::centerX))
            .map(LifelineHead::lifeline)
            .toList();
    }

    // Ordered from top to bottom.
    public List<IMessage> messagesIn(OperandArea area) {
        List<OperandArea> allAreas = frames.stream().flatMap(frame -> frame.operands().stream()).toList();

        return messageLines.stream()
            .filter(area::holds)
            .filter(line -> allAreas.stream().noneMatch(other -> other != area && other.size() < area.size() && other.holds(line)))
            .sorted(Comparator.comparingDouble(MessageLine::y))
            .map(MessageLine::message)
            .toList();
    }

    // Every drawn element that lies across the given width, at the Y coordinate where it lies: the start of each message,
    // the head of each lifeline, and the top, the bottom and the operand separators of each combined fragment and
    // interaction use. The edge being moved, of the given combined fragment, is left out.
    public List<Mark> marksAcross(double left, double right, INodePresentation movingFragment, double movingEdgeY) {
        List<Mark> marks = new ArrayList<>();

        for (MessageLine line : messageLines) {
            if (line.left() <= right && left <= line.right()) {
                marks.add(new Mark(line.y(), String.format("message '%s' at y=%.1f", line.message().getName(), line.y())));
            }
        }

        for (LifelineHead head : lifelineHeads) {
            if (left <= head.centerX() && head.centerX() <= right) {
                marks.add(new Mark(head.top(), String.format("the head of lifeline '%s' at y=%.1f", head.lifeline().getName(), head.top())));
            }
        }

        for (Frame frame : frames) {
            if (frame.right() < left || right < frame.left()) {
                continue;
            }
            boolean moving = frame.presentation().getID().equals(movingFragment.getID());

            List<Mark> edges = new ArrayList<>();
            edges.add(new Mark(frame.top(), String.format("the top of %s at y=%.1f", frame.description(), frame.top())));
            for (int i = 0; i < frame.operands().size() - 1; i++) {
                double separator = frame.operands().get(i).bottom();
                edges.add(new Mark(separator, String.format("the separator below operand %d of %s at y=%.1f", i + 1, frame.description(), separator)));
            }
            edges.add(new Mark(frame.bottom(), String.format("the bottom of %s at y=%.1f", frame.description(), frame.bottom())));

            for (Mark edge : edges) {
                if (!(moving && edge.y() == movingEdgeY)) {
                    marks.add(edge);
                }
            }
        }

        marks.sort(Comparator.comparingDouble(Mark::y));
        return marks;
    }

    // The combined fragments and interaction uses, other than the given combined fragment, that reach below the given Y coordinate.
    public List<String> framesReachingBelow(double y, INodePresentation except) {
        return frames.stream()
            .filter(frame -> !frame.presentation().getID().equals(except.getID()) && frame.bottom() > y)
            .map(Frame::description)
            .toList();
    }
}
