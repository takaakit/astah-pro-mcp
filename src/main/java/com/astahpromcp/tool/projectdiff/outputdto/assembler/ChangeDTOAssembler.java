package com.astahpromcp.tool.projectdiff.outputdto.assembler;

import com.astahpromcp.tool.projectdiff.engine.Comparison;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.ContextDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ElementRefDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeDTO;
import com.astahpromcp.tool.projectdiff.outputdto.PropertyChangeKind;
import com.astahpromcp.tool.projectdiff.outputdto.ValueEncoding;
import com.astahpromcp.tool.projectdiff.xmi.XmiIndex;
import com.astahpromcp.tool.projectdiff.xmi.XmiProperty;
import com.astahpromcp.tool.projectdiff.xmi.XmiValueCodec;
import lombok.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ChangeDTOAssembler {

    // The path of the value that says from which element to which an element moved in the XMI
    public static final String PARENT_PATH = "(parent)";

    // Where a reference sits, for the references that give a presentation or a model element its context
    private static final String DIAGRAM_REFERENCE = "JUDE:UPresentation.diagram/";
    private static final String MODEL_REFERENCE = "JUDE:UPresentation.semanticModel/";
    private static final String STEREOTYPE_REFERENCE = "UML:ModelElement.stereotype/";
    private static final String PARTICIPANT_REFERENCE = "UML:AssociationEnd.participant/";

    // One side of the comparison: its index, the full values of its changed elements, and the name the project goes by
    public record Side(XmiIndex index, Map<String, List<XmiProperty>> properties, String projectName) {
    }

    public static ChangeDTO toDTO(@NonNull Comparison.Item item, @NonNull Comparison comparison, @NonNull Side base, @NonNull Side target) {
        return switch (item.kind()) {
            case ADDED -> toWholeDTO(target, target.index().get(item.targetIndex()), ChangeKind.ADDED);
            case REMOVED -> toWholeDTO(base, base.index().get(item.baseIndex()), ChangeKind.REMOVED);
            case MODIFIED -> toModifiedDTO(item, comparison, base, target);
        };
    }

    // An element only one side has, with every value it holds
    private static ChangeDTO toWholeDTO(Side side, XmiIndex.Element element, ChangeKind kind) {
        List<PropertyChangeDTO> properties = new ArrayList<>();
        for (XmiProperty property : side.properties().getOrDefault(element.id(), List.of())) {
            XmiValueCodec.Decoded shown = display(element, property);
            String refName = property.reference() ? displayNameOf(side, property.value()) : "";
            if (kind == ChangeKind.ADDED) {
                properties.add(new PropertyChangeDTO(property.path(), PropertyChangeKind.ADDED, "", shown.value(), "", refName, shown.encoding(), false));
            } else {
                properties.add(new PropertyChangeDTO(property.path(), PropertyChangeKind.REMOVED, shown.value(), "", refName, "", shown.encoding(), false));
            }
        }

        return new ChangeDTO(
            kind,
            element.id(),
            "",
            element.tag(),
            stereotypesOf(side, element),
            displayName(side, element),
            contextOf(side, element),
            ContextDTO.empty(),
            ContextDTO.empty(),
            diagramOf(side, element),
            modelOf(side, element),
            0,
            properties.size(),
            properties);
    }

    // An element both sides have, with the values that differ
    private static ChangeDTO toModifiedDTO(Comparison.Item item, Comparison comparison, Side base, Side target) {
        XmiIndex.Element before = base.index().get(item.baseIndex());
        XmiIndex.Element after = target.index().get(item.targetIndex());

        // The lists of references are compared as lists (compareReferenceLists); every other value by its path
        ReferenceLists beforeLists = ReferenceLists.of(base.properties().getOrDefault(before.id(), List.of()));
        ReferenceLists afterLists = ReferenceLists.of(target.properties().getOrDefault(after.id(), List.of()));
        ReferenceLists.keepSingleReferencesApart(beforeLists, afterLists);
        Map<String, XmiProperty> beforeValues = byPath(beforeLists.others());
        Map<String, XmiProperty> afterValues = byPath(afterLists.others());

        List<PropertyChangeDTO> properties = new ArrayList<>();
        if (item.moved()) {
            properties.add(new PropertyChangeDTO(PARENT_PATH, PropertyChangeKind.MODIFIED,
                    before.parent() < 0 ? "" : base.index().get(before.parent()).id(),
                    after.parent() < 0 ? "" : target.index().get(after.parent()).id(),
                    before.parent() < 0 ? "" : displayName(base, base.index().get(before.parent())),
                    after.parent() < 0 ? "" : displayName(target, target.index().get(after.parent())),
                    ValueEncoding.RAW, false));
        }

        for (XmiProperty value : afterValues.values()) {
            XmiProperty old = beforeValues.get(value.path());
            XmiValueCodec.Decoded shownAfter = display(after, value);
            String afterRefName = value.reference() ? displayNameOf(target, value.value()) : "";

            if (old == null) {
                properties.add(new PropertyChangeDTO(value.path(), PropertyChangeKind.ADDED, "", shownAfter.value(), "", afterRefName, shownAfter.encoding(), false));
                continue;
            }

            // A reference is the same when it refers to the element the rules paired with the one the base refers to
            String comparable = value.reference() ? comparison.baseIdOf(value.value()) : value.value();
            if (Objects.equals(old.value(), comparable) && old.reference() == value.reference()) {
                continue;
            }

            XmiValueCodec.Decoded shownBefore = display(before, old);
            String beforeRefName = old.reference() ? displayNameOf(base, old.value()) : "";
            properties.add(new PropertyChangeDTO(value.path(), PropertyChangeKind.MODIFIED, shownBefore.value(), shownAfter.value(),
                    beforeRefName, afterRefName, combined(shownBefore.encoding(), shownAfter.encoding()), false));
        }

        for (XmiProperty old : beforeValues.values()) {
            if (!afterValues.containsKey(old.path())) {
                XmiValueCodec.Decoded shownBefore = display(before, old);
                String beforeRefName = old.reference() ? displayNameOf(base, old.value()) : "";
                properties.add(new PropertyChangeDTO(old.path(), PropertyChangeKind.REMOVED, shownBefore.value(), "", beforeRefName, "", shownBefore.encoding(), false));
            }
        }

        // The lists in the order the target has them, then the ones only the base has
        Set<String> listPaths = new LinkedHashSet<>(afterLists.listPaths());
        listPaths.addAll(beforeLists.listPaths());
        for (String listPath : listPaths) {
            compareReferenceList(listPath, beforeLists.members(listPath), afterLists.members(listPath), comparison, base, target, properties);
        }

        return new ChangeDTO(
            ChangeKind.MODIFIED,
            after.id(),
            before.id().equals(after.id()) ? "" : before.id(),
            after.tag(),
            stereotypesOf(target, after),
            displayName(target, after),
            contextOf(target, after),
            item.moved() ? contextOf(base, before) : ContextDTO.empty(),
            item.moved() ? contextOf(target, after) : ContextDTO.empty(),
            diagramOf(target, after),
            modelOf(target, after),
            0,
            properties.size(),
            properties);
    }

    // A list of references of both sides, compared as a list of the elements it refers to (ReferenceLists): the same
    // references in another order show as one reordered value; otherwise the references the base has and the target
    // lacks show as removed, and the ones the target adds as added, in the order of the lists. Their path is the path of
    // the list, without the index of a member.
    private static void compareReferenceList(String listPath, List<XmiProperty> beforeRefs, List<XmiProperty> afterRefs, Comparison comparison,
                                             Side base, Side target, List<PropertyChangeDTO> properties) {
        List<String> beforeIds = beforeRefs.stream().map(XmiProperty::value).toList();
        List<String> afterIds = afterRefs.stream().map(XmiProperty::value).toList();
        // A reference is the same when it refers to the element the rules paired with the one the base refers to
        List<String> comparableAfter = afterIds.stream().map(comparison::baseIdOf).toList();
        if (beforeIds.equals(comparableAfter)) {
            return;
        }

        if (sameCounts(beforeIds, comparableAfter)) {
            properties.add(new PropertyChangeDTO(listPath, PropertyChangeKind.REORDERED, String.join(", ", beforeIds), String.join(", ", afterIds),
                    namesOf(base, beforeIds), namesOf(target, afterIds), ValueEncoding.RAW, false));
            return;
        }

        for (ReferenceLists.Edit edit : ReferenceLists.align(beforeIds, comparableAfter)) {
            if (edit.removed()) {
                String id = beforeIds.get(edit.index());
                properties.add(new PropertyChangeDTO(listPath, PropertyChangeKind.REMOVED, id, "", displayNameOf(base, id), "", ValueEncoding.RAW, false));
            } else {
                String id = afterIds.get(edit.index());
                properties.add(new PropertyChangeDTO(listPath, PropertyChangeKind.ADDED, "", id, "", displayNameOf(target, id), ValueEncoding.RAW, false));
            }
        }
    }

    // Whether two lists hold the same IDs the same number of times
    private static boolean sameCounts(List<String> a, List<String> b) {
        if (a.size() != b.size()) {
            return false;
        }
        Map<String, Integer> counts = new HashMap<>();
        for (String id : a) {
            counts.merge(id, 1, Integer::sum);
        }
        for (String id : b) {
            if (counts.merge(id, -1, Integer::sum) < 0) {
                return false;
            }
        }
        return true;
    }

    private static String namesOf(Side side, List<String> ids) {
        List<String> names = new ArrayList<>(ids.size());
        for (String id : ids) {
            names.add(displayNameOf(side, id));
        }
        return String.join(", ", names);
    }

    private static XmiValueCodec.Decoded display(XmiIndex.Element owner, XmiProperty property) {
        if (property.reference()) {
            return new XmiValueCodec.Decoded(property.value(), ValueEncoding.RAW);
        }
        return XmiValueCodec.decode(owner.tag(), owner.diagram(), property.path(), property.value());
    }

    // The two values sit at the same position; unknown wins over decoded, and decoded over raw
    private static ValueEncoding combined(ValueEncoding before, ValueEncoding after) {
        if (before == ValueEncoding.UNKNOWN || after == ValueEncoding.UNKNOWN) {
            return ValueEncoding.UNKNOWN;
        }
        if (before == ValueEncoding.DECODED || after == ValueEncoding.DECODED) {
            return ValueEncoding.DECODED;
        }
        return ValueEncoding.RAW;
    }

    private static Map<String, XmiProperty> byPath(List<XmiProperty> properties) {
        Map<String, XmiProperty> propertiesByPath = new LinkedHashMap<>();
        for (XmiProperty property : properties) {
            propertiesByPath.put(property.path(), property);
        }
        return propertiesByPath;
    }

    // The name to show for an element: its name, or a presentation's label. The root model is shown by the project's
    // name, because the XMI's root name is only the name of the file the project was copied to.
    private static String displayName(Side side, XmiIndex.Element element) {
        if (element.root()) {
            return side.projectName();
        }
        if (element.name() != null) {
            return XmiValueCodec.decode(element.tag(), element.diagram(), "@name", element.name()).value();
        }
        if (element.label() != null) {
            return XmiValueCodec.decode(element.tag(), element.diagram(), "@label", element.label()).value();
        }
        return "";
    }

    private static String displayNameOf(Side side, String id) {
        int index = side.index().indexOf(id);
        return index < 0 ? "" : displayName(side, side.index().get(index));
    }

    // The ancestor that gives an element its context: for a model element, the nearest one with a name attribute; for a presentation, its parent
    private static ContextDTO contextOf(Side side, XmiIndex.Element element) {
        XmiIndex index = side.index();
        Deque<String> path = new ArrayDeque<>();
        if (!element.ownerPath().isEmpty()) {
            path.addFirst(element.ownerPath());
        }

        int current = element.parent();
        if (!isPresentation(element)) {
            // Skip the parts that have no name attribute of their own, such as a multiplicity and its range
            while (current >= 0 && !index.get(current).hasName()) {
                XmiIndex.Element unnamed = index.get(current);
                path.addFirst(unnamed.tag());
                if (!unnamed.ownerPath().isEmpty()) {
                    path.addFirst(unnamed.ownerPath());
                }
                current = unnamed.parent();
            }
        }
        if (current < 0) {
            return ContextDTO.empty();
        }

        XmiIndex.Element ancestor = index.get(current);
        String name = displayName(side, ancestor);
        String participant = "";
        if (name.isEmpty() && (ancestor.tag().equals("UML:AssociationEnd") || ancestor.tag().equals("UML:AssociationEndRole"))) {
            participant = referenced(side, ancestor, PARTICIPANT_REFERENCE).name();
        }
        return new ContextDTO(
            ancestor.id(),
            ancestor.tag(),
            name,
            participant,
            String.join("/", path));
    }

    private static boolean isPresentation(XmiIndex.Element element) {
        return element.tag().startsWith("JUDE:") && !element.diagram();
    }

    private static ElementRefDTO diagramOf(Side side, XmiIndex.Element element) {
        if (!isPresentation(element)) {
            return ElementRefDTO.empty();
        }
        ElementRefDTO referenced = referenced(side, element, DIAGRAM_REFERENCE);
        if (!referenced.xmiId().isEmpty()) {
            return referenced;
        }
        // A presentation nested in another (a label, a mind map topic) takes the diagram of its nearest diagram ancestor
        for (int current = element.parent(); current >= 0; current = side.index().get(current).parent()) {
            XmiIndex.Element ancestor = side.index().get(current);
            if (ancestor.diagram()) {
                return new ElementRefDTO(ancestor.id(), displayName(side, ancestor));
            }
        }
        return ElementRefDTO.empty();
    }

    private static ElementRefDTO modelOf(Side side, XmiIndex.Element element) {
        return isPresentation(element) ? referenced(side, element, MODEL_REFERENCE) : ElementRefDTO.empty();
    }

    // The first element the given element refers to under the given path
    private static ElementRefDTO referenced(Side side, XmiIndex.Element element, String pathPrefix) {
        for (int i = 0; i < element.refPaths().length; i++) {
            if (element.refPaths()[i].startsWith(pathPrefix)) {
                String id = element.refIds()[i];
                return new ElementRefDTO(id, displayNameOf(side, id));
            }
        }
        return ElementRefDTO.empty();
    }

    private static List<String> stereotypesOf(Side side, XmiIndex.Element element) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < element.refPaths().length; i++) {
            if (element.refPaths()[i].startsWith(STEREOTYPE_REFERENCE)) {
                String name = displayNameOf(side, element.refIds()[i]);
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return names;
    }
}
