package com.astahpromcp.tool.projectdiff.engine;

import com.astahpromcp.tool.projectdiff.xmi.XmiIndex;

import java.util.Set;

// A value that Astah recomputes on its own, without any edit and without marking the project modified, and by how much
// it may differ before the difference counts as a change. Every other value is compared exactly.
public enum ToleratedDifference {

    // The label of a message on a communication diagram: showing the diagram makes Astah measure the label's text with
    // the fonts of the screen and resize the label around its center, by up to 1.6 pixels in width.
    COMMUNICATION_MESSAGE_LABEL_SIZE("communicationMessageLabelSize", "JUDE:LabelPresentation", "JUDE:MessageCLPresentation.namePresentation",
            Set.of("@width", "JUDE:JomtPresentation.location/XMI.field[0]", "JUDE:JomtPresentation.location/XMI.field[1]"), 2.0);

    private final String ruleName;
    private final String tag;
    private final String ownerPath;
    private final Set<String> paths;
    private final double tolerance;

    ToleratedDifference(String ruleName, String tag, String ownerPath, Set<String> paths, double tolerance) {
        this.ruleName = ruleName;
        this.tag = tag;
        this.ownerPath = ownerPath;
        this.paths = paths;
        this.tolerance = tolerance;
    }

    public String ruleName() {
        return ruleName;
    }

    // Whether the element is one whose values this covers
    boolean covers(XmiIndex.Element element) {
        return element.tag().equals(tag) && element.ownerPath().equals(ownerPath);
    }

    // Whether two values at the given path differ by no more than this allows
    boolean tolerates(String path, String before, String after) {
        if (!paths.contains(path)) {
            return false;
        }
        try {
            return Math.abs(Double.parseDouble(before) - Double.parseDouble(after)) < tolerance;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static ToleratedDifference covering(XmiIndex.Element element) {
        for (ToleratedDifference difference : values()) {
            if (difference.covers(element)) {
                return difference;
            }
        }
        return null;
    }
}
