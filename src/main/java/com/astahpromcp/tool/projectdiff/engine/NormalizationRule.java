package com.astahpromcp.tool.projectdiff.engine;

import com.astahpromcp.tool.projectdiff.xmi.XmiIndex;

// A part that the XMI recreates with a new xmi.id every time a project is loaded, and how to tell which part on the other
// side it is. Only parts confirmed to behave this way are listed, applied in this order.
public enum NormalizationRule {

    // The multiplicity of an association end
    ASSOCIATION_END_MULTIPLICITY("associationEndMultiplicity") {
        @Override
        boolean covers(XmiIndex index, XmiIndex.Element element) {
            return element.tag().equals("UML:Multiplicity")
                    && element.ownerPath().equals("UML:StructuralFeature.multiplicity")
                    && parentTag(index, element).equals("UML:AssociationEnd");
        }
    },

    // The range of that multiplicity
    ASSOCIATION_END_MULTIPLICITY_RANGE("associationEndMultiplicityRange") {
        @Override
        boolean covers(XmiIndex index, XmiIndex.Element element) {
            if (!element.tag().equals("UML:MultiplicityRange") || !element.ownerPath().equals("UML:Multiplicity.range") || element.parent() < 0) {
                return false;
            }
            XmiIndex.Element multiplicity = index.get(element.parent());
            return ASSOCIATION_END_MULTIPLICITY.covers(index, multiplicity);
        }
    },

    // The tagged value 'jude.diagram.updated_version' of a diagram that was saved without it
    DIAGRAM_UPDATED_VERSION("diagramUpdatedVersion") {
        @Override
        boolean covers(XmiIndex index, XmiIndex.Element element) {
            return element.tag().equals("UML:TaggedValue")
                    && element.ownerPath().equals("UML:ModelElement.taggedValue")
                    && "jude.diagram.updated_version".equals(element.taggedValueTag())
                    && element.parent() >= 0
                    && index.get(element.parent()).diagram();
        }

        @Override
        String key(XmiIndex.Element element) {
            return element.taggedValueTag();
        }
    },

    // The name label of a line of a mind map, in a file saved by an older Astah
    MIND_MAP_EDGE_LABEL("mindMapEdgeLabel") {
        @Override
        boolean covers(XmiIndex index, XmiIndex.Element element) {
            return element.tag().equals("JUDE:LabelPresentation")
                    && element.ownerPath().equals("JUDE:PathPresentation.namePresentation")
                    && parentTag(index, element).equals("JUDE:MMEdgePresentation");
        }
    };

    // Raised whenever the rules change, so that a result says which rules produced it
    public static final int VERSION = 1;

    private final String ruleName;

    NormalizationRule(String ruleName) {
        this.ruleName = ruleName;
    }

    public String ruleName() {
        return ruleName;
    }

    // Whether the element is a part this rule matches
    abstract boolean covers(XmiIndex index, XmiIndex.Element element);

    // What, besides the owner and the path from it, tells two parts of the same owner apart
    String key(XmiIndex.Element element) {
        return "";
    }

    private static String parentTag(XmiIndex index, XmiIndex.Element element) {
        return element.parent() < 0 ? "" : index.get(element.parent()).tag();
    }
}
