package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.astah.pro.AstahElementType;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.matrix.inputdto.MatrixAxisCriteria;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IMatrixDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IRequirement;
import com.change_vision.jude.api.inf.model.IRequirementTable;
import com.change_vision.jude.api.inf.model.ITestCase;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// Checks the criteria of one axis of a relationship matrix, and collects the elements they choose
final class MatrixElementCollector {

    // The types an empty elementTypes takes.
    // An interface, an actor and a block are classes with a stereotype, and AstahElementType tells them as Class.
    static final List<AstahElementType> DEFAULT_TYPES = List.of(
        AstahElementType.CLASS,
        AstahElementType.REQUIREMENT,
        AstahElementType.TEST_CASE,
        AstahElementType.USE_CASE,
        AstahElementType.COMPONENT,
        AstahElementType.NODE,
        AstahElementType.ARTIFACT,
        AstahElementType.ENUMERATION);

    // The types that can be rows and columns, in the order the messages name them.
    // Beyond the default types, they are the packages and the ends of the transitions, flows, connectors, links, data flows and ER relationships, taken only when elementTypes names them.
    static final List<AstahElementType> ELEMENT_TYPES = List.of(
        AstahElementType.CLASS,
        AstahElementType.REQUIREMENT,
        AstahElementType.TEST_CASE,
        AstahElementType.USE_CASE,
        AstahElementType.COMPONENT,
        AstahElementType.NODE,
        AstahElementType.ARTIFACT,
        AstahElementType.ENUMERATION,
        AstahElementType.PACKAGE,
        AstahElementType.SUBSYSTEM,
        AstahElementType.STATE,
        AstahElementType.FINAL_STATE,
        AstahElementType.PSEUDOSTATE,
        AstahElementType.ACTION,
        AstahElementType.CONTROL_NODE,
        AstahElementType.OBJECT_NODE,
        AstahElementType.ACTIVITY_PARAMETER_NODE,
        AstahElementType.INPUT_PIN,
        AstahElementType.OUTPUT_PIN,
        AstahElementType.ATTRIBUTE,
        AstahElementType.PORT,
        AstahElementType.INSTANCE_SPECIFICATION,
        AstahElementType.PROCESS_BOX,
        AstahElementType.DATA_STORE,
        AstahElementType.EXTERNAL_ENTITY,
        AstahElementType.ER_ENTITY);

    // The types of the packages, which bound an axis as packages rather than as elements
    private static final Set<AstahElementType> PACKAGE_TYPES = Set.of(AstahElementType.PACKAGE, AstahElementType.SUBSYSTEM);

    private static final String ELEMENT_TYPE_NAMES = orList(ELEMENT_TYPES.stream().map(type -> type.typeName).toList());

    private static final String NON_PACKAGE_TYPE_NAMES = orList(ELEMENT_TYPES.stream()
            .filter(type -> !PACKAGE_TYPES.contains(type))
            .map(type -> type.typeName)
            .toList());

    private static final String DEFAULT_TYPE_NAMES = String.join(", ", DEFAULT_TYPES.stream().map(type -> type.typeName).toList());

    private static final String STEREOTYPED_CLASSES = "An interface, an actor and a block are classes with a stereotype: specify Class in elementTypes and put interface, actor or block in stereotypes.";

    private final ProjectAccessor projectAccessor;
    private final AstahProToolSupport astahProToolSupport;

    MatrixElementCollector(ProjectAccessor projectAccessor, AstahProToolSupport astahProToolSupport) {
        this.projectAccessor = projectAccessor;
        this.astahProToolSupport = astahProToolSupport;
    }

    // One package, diagram or element that bounds an axis
    record Scope(Kind kind, INamedElement element) {

        enum Kind {
            // Contributes the elements it owns, and with includeNestedElements those nested more deeply
            PACKAGE,
            // Contributes the elements drawn on it
            DIAGRAM,
            // Contributes itself, and with includeNestedElements the elements nested under it
            ELEMENT
        }

        // Such as Package "Requirements"
        String describe() {
            return AstahElementType.getCorrespondingType(element).typeName + " \"" + oneLine(element.getFullName(".")) + "\"";
        }
    }

    // The criteria of one axis, checked
    record Axis(
        // rows or columns, as the arguments name them
        String name,
        // The type names an element must have one of
        Set<String> typeNames,
        // Whether elementTypes named the types, rather than leaving them to the default
        boolean typesNamed,
        List<String> stereotypes,
        List<Scope> scopes,
        boolean includeNestedElements
    ) {

        // Such as: Class with stereotype block in Package "Design" (nested elements included)
        String describe() {
            StringBuilder text = new StringBuilder(typesNamed
                    ? String.join(", ", typeNames)
                    : "every default type (" + DEFAULT_TYPE_NAMES + ")");

            if (!stereotypes.isEmpty()) {
                text.append(" with stereotype ").append(String.join(" or ", stereotypes));
            }

            if (scopes.isEmpty()) {
                text.append(" in the whole project");
                return text.toString();
            }

            text.append(" in ").append(scopes.stream().map(Scope::describe).collect(Collectors.joining(", ")));

            // includeNestedElements does nothing for a diagram
            if (scopes.stream().anyMatch(scope -> scope.kind() != Scope.Kind.DIAGRAM)) {
                text.append(includeNestedElements ? " (nested elements included)" : " (nested elements excluded)");
            }
            return text.toString();
        }
    }

    // Checks the criteria of an axis, so that a name or an ID given wrongly is reported rather than leaving the axis silently empty
    Axis resolve(String axisName, MatrixAxisCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException(String.format("%s is missing. Specify the criteria for the %s.", axisName, axisName));
        }

        Set<String> typeNames = new LinkedHashSet<>();
        for (String typeName : orEmpty(criteria.elementTypes())) {
            typeNames.add(checkedTypeName(axisName, typeName));
        }
        boolean typesNamed = !typeNames.isEmpty();
        if (!typesNamed) {
            for (AstahElementType type : DEFAULT_TYPES) {
                typeNames.add(type.typeName);
            }
        }

        List<Scope> scopes = new ArrayList<>();
        for (String scopeId : orEmpty(criteria.scopeIds())) {
            if (scopeId == null || scopeId.isBlank()) {
                throw new IllegalArgumentException(String.format("%s.scopeIds holds an empty ID. Specify the IDs of packages, diagrams or elements, or an empty list to search the whole project.", axisName));
            }

            // Reports an ID that names nothing in the project as the other tool functions do
            INamedElement element = astahProToolSupport.getNamedElement(scopeId);
            scopes.add(scopeOf(axisName, element));
        }

        // The order of the type names is kept for the Criteria section, which names them as they were given
        return new Axis(
            axisName,
            Collections.unmodifiableSet(typeNames),
            typesNamed,
            orEmpty(criteria.stereotypes()).stream().filter(Objects::nonNull).toList(),
            List.copyOf(scopes),
            criteria.includeNestedElements());
    }

    private static String checkedTypeName(String axisName, String typeName) {
        AstahElementType type = AstahElementType.getCorrespondingType(typeName);

        if (type == AstahElementType.UNKNOWN) {
            throw new IllegalArgumentException(String.format(
                "Unknown element type '%s' in %s.elementTypes. Specify %s, spelled exactly so. %s",
                typeName, axisName, ELEMENT_TYPE_NAMES, STEREOTYPED_CLASSES));
        }

        if (!ELEMENT_TYPES.contains(type)) {
            throw new IllegalArgumentException(String.format(
                "The element type '%s' in %s.elementTypes cannot be a row or a column. Specify %s.",
                typeName, axisName, ELEMENT_TYPE_NAMES));
        }

        return type.typeName;
    }

    private static Scope scopeOf(String axisName, INamedElement element) {
        // A table lists its rows rather than drawing them, so it has to be checked before the other diagrams
        if (element instanceof IRequirementTable) {
            throw new IllegalArgumentException(String.format(
                "The requirement table %s in %s.scopeIds cannot bound the %s: it is a table, not a diagram that elements are drawn on. Specify the package that holds the requirements it shows instead, and set includeNestedElements to true to take the requirements in its subpackages and those nested under other requirements as well.",
                identify(element), axisName, axisName));
        }

        if (element instanceof IMatrixDiagram) {
            throw new IllegalArgumentException(String.format(
                "The CRUD %s in %s.scopeIds cannot bound the %s: it is a table, not a diagram that elements are drawn on. Specify packages, other diagrams or elements instead.",
                identify(element), axisName, axisName));
        }

        if (element instanceof IDiagram) {
            return new Scope(Scope.Kind.DIAGRAM, element);
        }

        // A subsystem is a package as well as a class, and bounds an axis as a package does
        if (element instanceof IPackage) {
            return new Scope(Scope.Kind.PACKAGE, element);
        }

        AstahElementType type = AstahElementType.getCorrespondingType(element);
        if (ELEMENT_TYPES.contains(type)) {
            return new Scope(Scope.Kind.ELEMENT, element);
        }

        throw new IllegalArgumentException(String.format(
            "The %s %s in %s.scopeIds cannot bound the %s. Specify packages, diagrams, or elements of the types %s.",
            type.typeName, identify(element), axisName, axisName, NON_PACKAGE_TYPE_NAMES));
    }

    // Every element of the types the axes take, each once.
    // findElements returns nested elements too, such as a requirement nested under another, and a subsystem both as a class and as a package.
    List<INamedElement> candidates(Axis... axes) throws ProjectNotFoundException {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        for (Axis axis : axes) {
            for (String typeName : axis.typeNames()) {
                interfaces.add(AstahElementType.getCorrespondingType(typeName).typeClass);
            }
        }

        Map<String, INamedElement> candidates = new LinkedHashMap<>();
        for (Class<?> type : interfaces) {
            for (INamedElement element : projectAccessor.findElements(type)) {
                candidates.putIfAbsent(element.getId(), element);
            }
        }
        return List.copyOf(candidates.values());
    }

    // Where an element belongs: its owner, or, for an element that has none, such as a state, an action or a process of a data flow diagram, the owner of the first diagram it is drawn on
    static IElement placeOf(INamedElement element) throws InvalidUsingException {
        IElement owner = element.getOwner();
        if (owner != null) {
            return owner;
        }
        for (IPresentation presentation : element.getPresentations()) {
            IDiagram diagram = presentation.getDiagram();
            if (diagram != null && diagram.getOwner() != null) {
                return diagram.getOwner();
            }
        }
        return null;
    }

    // The nearest package that holds an element: a package, a subsystem, or the project itself for an element that belongs to no other
    static IPackage packageOf(INamedElement element, IPackage project) throws InvalidUsingException {
        for (IElement holder = placeOf(element); holder != null; holder = holder.getOwner()) {
            if (holder instanceof IPackage holdingPackage) {
                return holdingPackage;
            }
        }
        return project;
    }

    // The candidates that the type, the stereotypes and the scopes of the axis all take
    List<INamedElement> select(Axis axis, List<INamedElement> candidates) throws Exception {
        ScopeMembership membership = new ScopeMembership(axis);

        List<INamedElement> selected = new ArrayList<>();
        for (INamedElement element : candidates) {
            // The deepest type, so that a requirement or a use case is never taken as a Class, nor the project itself (a Model) as a Package
            if (!axis.typeNames().contains(AstahElementType.getCorrespondingType(element).typeName)) {
                continue;
            }
            if (!hasAnyStereotype(element, axis.stereotypes())) {
                continue;
            }
            if (!axis.scopes().isEmpty() && !membership.contains(element)) {
                continue;
            }
            selected.add(element);
        }
        return selected;
    }

    // Which elements the scopes of an axis take
    private static final class ScopeMembership {

        // The elements of the element scopes themselves
        private final Set<String> elementIds = new HashSet<>();
        // The elements drawn on the diagram scopes
        private final Set<String> drawnIds = new HashSet<>();
        // The packages whose own elements are taken
        private final Set<String> ownerIds = new HashSet<>();
        // The packages and elements under which every element is taken, however deeply nested
        private final Set<String> ancestorIds = new HashSet<>();

        ScopeMembership(Axis axis) throws Exception {
            for (Scope scope : axis.scopes()) {
                String id = scope.element().getId();
                switch (scope.kind()) {
                    case PACKAGE -> (axis.includeNestedElements() ? ancestorIds : ownerIds).add(id);
                    case ELEMENT -> {
                        elementIds.add(id);
                        if (axis.includeNestedElements()) {
                            ancestorIds.add(id);
                        }
                    }
                    case DIAGRAM -> addDrawnElements((IDiagram) scope.element());
                }
            }
        }

        // The header cells that show a diagram on a CRUD are left out, being drawn on the CRUD rather than on the diagram
        private void addDrawnElements(IDiagram diagram) throws Exception {
            for (IPresentation presentation : AstahProToolSupport.getPresentationsOnDiagram(diagram)) {
                if (presentation.getModel() instanceof INamedElement model) {
                    drawnIds.add(model.getId());
                }
            }
        }

        boolean contains(INamedElement element) throws InvalidUsingException {
            if (elementIds.contains(element.getId()) || drawnIds.contains(element.getId())) {
                return true;
            }

            // An element without an owner, such as a state, is taken to belong where the diagram it is drawn on belongs
            IElement owner = placeOf(element);
            if (owner != null && ownerIds.contains(owner.getId())) {
                return true;
            }

            if (!ancestorIds.isEmpty()) {
                for (IElement ancestor = owner; ancestor != null; ancestor = ancestor.getOwner()) {
                    if (ancestorIds.contains(ancestor.getId())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    // The values of an element that a row or a column shows
    static MatrixElement toMatrixElement(INamedElement element) {
        String requirementOrTestCaseId = "";
        if (element instanceof IRequirement requirement) {
            requirementOrTestCaseId = requirement.getRequirementID();
        } else if (element instanceof ITestCase testCase) {
            requirementOrTestCaseId = testCase.getTestCaseID();
        }

        return new MatrixElement(
            element.getId(),
            element.getName(),
            element.getFullNamespace("."),
            element.getFullName("."),
            AstahElementType.getCorrespondingType(element).typeName,
            requirementOrTestCaseId == null ? "" : requirementOrTestCaseId);
    }

    private static boolean hasAnyStereotype(INamedElement element, List<String> stereotypes) {
        if (stereotypes.isEmpty()) {
            return true;
        }
        for (String stereotype : element.getStereotypes()) {
            if (stereotypes.contains(stereotype)) {
                return true;
            }
        }
        return false;
    }

    // Such as "Stopping distance" (ID: 1x-...), or the ID alone for an element without a name
    private static String identify(INamedElement element) {
        String name = element.getName();
        String quotedName = name == null || name.isBlank() ? "" : "\"" + oneLine(name) + "\" ";
        return quotedName + "(ID: " + element.getId() + ")";
    }

    private static String oneLine(String text) {
        return text == null ? "" : text.replace("\r\n", " ").replace('\r', ' ').replace('\n', ' ');
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    // Such as "A, B or C"
    private static String orList(List<String> items) {
        if (items.size() <= 1) {
            return String.join("", items);
        }
        return String.join(", ", items.subList(0, items.size() - 1)) + " or " + items.get(items.size() - 1);
    }
}
