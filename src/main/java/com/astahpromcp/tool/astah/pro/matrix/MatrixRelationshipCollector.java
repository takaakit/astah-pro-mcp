package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;
import com.change_vision.jude.api.inf.model.IActivityNode;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IConnector;
import com.change_vision.jude.api.inf.model.IDataFlow;
import com.change_vision.jude.api.inf.model.IDataFlowNode;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.IERRelationship;
import com.change_vision.jude.api.inf.model.IEREntity;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IExtend;
import com.change_vision.jude.api.inf.model.IFlow;
import com.change_vision.jude.api.inf.model.IGeneralization;
import com.change_vision.jude.api.inf.model.IInclude;
import com.change_vision.jude.api.inf.model.IInstanceSpecification;
import com.change_vision.jude.api.inf.model.ILink;
import com.change_vision.jude.api.inf.model.ILinkEnd;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.model.IRealization;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.model.IUsage;
import com.change_vision.jude.api.inf.model.IUseCase;
import com.change_vision.jude.api.inf.model.IVertex;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// Collects the relationships of the chosen kinds whose two ends are both among the given elements, each once
final class MatrixRelationshipCollector {

    private static final String NAVIGABLE = "Navigable";

    private static final Set<MatrixRelationshipKind> ASSOCIATION_KINDS = EnumSet.of(
        MatrixRelationshipKind.ASSOCIATION,
        MatrixRelationshipKind.AGGREGATION,
        MatrixRelationshipKind.COMPOSITION);

    // The requirement relationships, which are dependencies with a stereotype of their own
    private static final List<MatrixRelationshipKind> REQUIREMENT_KINDS = Arrays.stream(MatrixRelationshipKind.values())
            .filter(kind -> kind.requirementStereotype != null)
            .toList();

    private final Map<String, INamedElement> elements = new LinkedHashMap<>();
    private final Set<MatrixRelationshipKind> kinds;
    private final Map<String, MatrixRelationship> found = new LinkedHashMap<>();

    private MatrixRelationshipCollector(Collection<? extends INamedElement> elements, Set<MatrixRelationshipKind> kinds) {
        for (INamedElement element : elements) {
            this.elements.putIfAbsent(element.getId(), element);
        }
        this.kinds = kinds.isEmpty() ? EnumSet.noneOf(MatrixRelationshipKind.class) : EnumSet.copyOf(kinds);
    }

    static List<MatrixRelationship> collect(Collection<? extends INamedElement> elements, Set<MatrixRelationshipKind> kinds) {
        return collect(elements, kinds, false, List.of());
    }

    // impliedSatisfactions adds the satisfy relationships that composition implies, when satisfy is among the kinds.
    // links are every link of the project: the end of a link does not lead back to its link, so the links cannot be reached from the instance specifications they join.
    static List<MatrixRelationship> collect(
            Collection<? extends INamedElement> elements,
            Set<MatrixRelationshipKind> kinds,
            boolean impliedSatisfactions,
            Collection<? extends ILink> links) {
        MatrixRelationshipCollector collector = new MatrixRelationshipCollector(elements, kinds);
        collector.collectAll();
        if (collector.kinds.contains(MatrixRelationshipKind.LINK)) {
            for (ILink link : links) {
                collector.addLink(link);
            }
        }
        if (impliedSatisfactions && collector.kinds.contains(MatrixRelationshipKind.SATISFY)) {
            collector.addImpliedSatisfactions();
        }
        return List.copyOf(collector.found.values());
    }

    private void collectAll() {
        boolean wantsAssociations = kinds.stream().anyMatch(ASSOCIATION_KINDS::contains);
        boolean wantsDependencies = kinds.contains(MatrixRelationshipKind.DEPENDENCY)
                || kinds.stream().anyMatch(REQUIREMENT_KINDS::contains);

        // An association is reached from both of its ends, so the ones already read are skipped
        Set<String> readAssociations = new HashSet<>();

        for (INamedElement element : elements.values()) {
            if (element instanceof IClass astahClass) {
                if (wantsAssociations) {
                    // Every end of an association, navigable or not, is an attribute of the class at the other end
                    for (IAttribute attribute : astahClass.getAttributes()) {
                        IAssociation association = attribute.getAssociation();
                        if (association != null && readAssociations.add(association.getId())) {
                            addAssociation(association);
                        }
                    }
                }

                if (kinds.contains(MatrixRelationshipKind.GENERALIZATION)) {
                    for (IGeneralization generalization : astahClass.getGeneralizations()) {
                        addDirected(generalization.getId(), astahClass, generalization.getSuperType(), List.of(MatrixRelationshipKind.GENERALIZATION));
                    }
                }
            }

            if (kinds.contains(MatrixRelationshipKind.REALIZATION)) {
                for (IRealization realization : element.getClientRealizations()) {
                    addDirected(realization.getId(), element, realization.getSupplier(), List.of(MatrixRelationshipKind.REALIZATION));
                }
            }

            // A usage is no dependency to the Astah API, and is not among the client dependencies
            if (wantsDependencies) {
                for (IDependency dependency : element.getClientDependencies()) {
                    addDependency(element, dependency);
                }
            }

            if (kinds.contains(MatrixRelationshipKind.USAGE)) {
                for (IUsage usage : element.getClientUsages()) {
                    addDirected(usage.getId(), element, usage.getSupplier(), List.of(MatrixRelationshipKind.USAGE));
                }
            }

            if (element instanceof IUseCase useCase) {
                if (kinds.contains(MatrixRelationshipKind.INCLUDE)) {
                    for (IInclude include : useCase.getIncludes()) {
                        addDirected(include.getId(), useCase, include.getAddition(), List.of(MatrixRelationshipKind.INCLUDE));
                    }
                }
                if (kinds.contains(MatrixRelationshipKind.EXTEND)) {
                    for (IExtend extend : useCase.getExtends()) {
                        addDirected(extend.getId(), useCase, extend.getExtendedCase(), List.of(MatrixRelationshipKind.EXTEND));
                    }
                }
            }

            // A containment is no model element: it is told apart by its owner and the element owned.
            // Only the owner the model holds counts, never the diagram an element without an owner is drawn on.
            if (kinds.contains(MatrixRelationshipKind.CONTAINMENT)) {
                IElement owner = element.getOwner();
                if (owner instanceof INamedElement namedOwner && elements.containsKey(namedOwner.getId())) {
                    String key = namedOwner.getId() + ">" + element.getId();
                    found.putIfAbsent(key, MatrixRelationship.directed(key, null, namedOwner.getId(), element.getId(), List.of(MatrixRelationshipKind.CONTAINMENT)));
                }
            }

            // A transition, a flow and a data flow are read from the element they leave
            if (kinds.contains(MatrixRelationshipKind.TRANSITION) && element instanceof IVertex vertex) {
                for (ITransition transition : vertex.getOutgoings()) {
                    addDirected(transition.getId(), transition.getSource(), transition.getTarget(), List.of(MatrixRelationshipKind.TRANSITION));
                }
            }

            if (kinds.contains(MatrixRelationshipKind.FLOW) && element instanceof IActivityNode node) {
                for (IFlow flow : node.getOutgoings()) {
                    addDirected(flow.getId(), flow.getSource(), flow.getTarget(), List.of(MatrixRelationshipKind.FLOW));
                }
            }

            if (kinds.contains(MatrixRelationshipKind.DATA_FLOW) && element instanceof IDataFlowNode node) {
                for (IDataFlow dataFlow : node.getOutgoings()) {
                    addDirected(dataFlow.getId(), dataFlow.getSource(), dataFlow.getTarget(), List.of(MatrixRelationshipKind.DATA_FLOW));
                }
            }

            // An ER relationship goes from the parent entity, whose key the child refers to, to the child
            if (kinds.contains(MatrixRelationshipKind.ER_RELATIONSHIP) && element instanceof IEREntity entity) {
                for (IERRelationship relationship : entity.getChildrenRelationships()) {
                    addDirected(relationship.getId(), relationship.getParent(), relationship.getChild(), List.of(MatrixRelationshipKind.ER_RELATIONSHIP));
                }
            }

            // A port is an attribute too, so this reaches the connectors of the ports as well as those of the parts
            if (kinds.contains(MatrixRelationshipKind.CONNECTOR) && element instanceof IAttribute attribute) {
                for (IConnector connector : attribute.getConnectors()) {
                    addConnector(connector);
                }
            }
        }
    }

    // Each end of a connector is a port, or a part where it has no port. A connector has no direction.
    private void addConnector(IConnector connector) {
        IAttribute[] parts = connector.getParts();
        IPort[] ports = connector.getPorts();
        if (parts == null || ports == null || parts.length != 2 || ports.length != 2) {
            return;
        }
        INamedElement endA = ports[0] != null ? ports[0] : parts[0];
        INamedElement endB = ports[1] != null ? ports[1] : parts[1];
        if (endA == null || endB == null || !contains(endA) || !contains(endB)) {
            return;
        }
        String id = connector.getId();
        found.putIfAbsent(id, MatrixRelationship.undirected(id, endA.getId(), endB.getId(), MatrixRelationshipKind.CONNECTOR));
    }

    // A link joins two instance specifications, and goes the way its navigable ends do, as an association does
    private void addLink(ILink link) {
        ILinkEnd[] ends = link.getMemberEnds();
        if (ends == null || ends.length != 2) {
            return;
        }
        IInstanceSpecification instanceA = ends[0].getType();
        IInstanceSpecification instanceB = ends[1].getType();
        if (instanceA == null || instanceB == null || !contains(instanceA) || !contains(instanceB)) {
            return;
        }
        String id = link.getId();

        boolean navigableA = NAVIGABLE.equals(ends[0].getNavigability());
        boolean navigableB = NAVIGABLE.equals(ends[1].getNavigability());
        if (navigableA && navigableB) {
            found.putIfAbsent(id, MatrixRelationship.bidirectional(id, instanceA.getId(), instanceB.getId(), MatrixRelationshipKind.LINK));
        } else if (navigableB) {
            addDirected(id, instanceA, instanceB, List.of(MatrixRelationshipKind.LINK));
        } else if (navigableA) {
            addDirected(id, instanceB, instanceA, List.of(MatrixRelationshipKind.LINK));
        } else {
            found.putIfAbsent(id, MatrixRelationship.undirected(id, instanceA.getId(), instanceB.getId(), MatrixRelationshipKind.LINK));
        }
    }

    // A class whose part satisfies a requirement is taken to satisfy it too, however deeply composition nests the part.
    // The part need not be a row or a column. A class that satisfies the requirement itself keeps only that relationship.
    private void addImpliedSatisfactions() {
        String satisfy = MatrixRelationshipKind.SATISFY.requirementStereotype;

        for (INamedElement element : elements.values()) {
            if (!(element instanceof IClass whole)) {
                continue;
            }

            Set<String> satisfiedItself = new HashSet<>();
            for (IDependency dependency : whole.getClientDependencies()) {
                if (hasStereotype(dependency, satisfy) && dependency.getSupplier() != null) {
                    satisfiedItself.add(dependency.getSupplier().getId());
                }
            }

            // The parts each requirement is satisfied through, by name
            Map<String, Set<String>> partsByRequirementId = new LinkedHashMap<>();
            for (IClass part : partsOf(whole)) {
                for (IDependency dependency : part.getClientDependencies()) {
                    INamedElement requirement = dependency.getSupplier();
                    if (!hasStereotype(dependency, satisfy) || requirement == null || !contains(requirement)
                            || requirement.getId().equals(whole.getId()) || satisfiedItself.contains(requirement.getId())) {
                        continue;
                    }
                    partsByRequirementId.computeIfAbsent(requirement.getId(), id -> new TreeSet<>()).add(displayName(part));
                }
            }

            for (Map.Entry<String, Set<String>> satisfied : partsByRequirementId.entrySet()) {
                MatrixRelationship implied = MatrixRelationship.implied(
                    whole.getId(),
                    satisfied.getKey(),
                    MatrixRelationshipKind.SATISFY,
                    "via " + String.join(", ", satisfied.getValue()));
                found.putIfAbsent(implied.key(), implied);
            }
        }
    }

    // The classes a class holds by composition, however deeply, each once and never the class itself
    private static List<IClass> partsOf(IClass whole) {
        Map<String, IClass> parts = new LinkedHashMap<>();
        Deque<IClass> pending = new ArrayDeque<>();
        pending.add(whole);
        while (!pending.isEmpty()) {
            IClass current = pending.poll();
            // Every end of an association the class takes part in is an attribute of it or of the class at the other end, so the attributes of the class reach its compositions
            for (IAttribute attribute : current.getAttributes()) {
                IAssociation association = attribute.getAssociation();
                IClass part = association == null ? null : partHeldBy(current, association);
                if (part != null && !part.getId().equals(whole.getId()) && parts.putIfAbsent(part.getId(), part) == null) {
                    pending.add(part);
                }
            }
        }
        return List.copyOf(parts.values());
    }

    // The part of a composition whose whole is the given class, or null when the association is no such composition.
    // Astah marks the end whose type is the whole, as addAssociation reads it.
    private static IClass partHeldBy(IClass whole, IAssociation association) {
        IAttribute[] ends = association.getMemberEnds();
        if (ends == null || ends.length != 2) {
            return null;
        }
        for (int i = 0; i < 2; i++) {
            IAttribute wholeEnd = ends[i];
            IAttribute partEnd = ends[1 - i];
            if (wholeEnd.isComposite() && !partEnd.isComposite()
                    && wholeEnd.getType() != null && wholeEnd.getType().getId().equals(whole.getId())) {
                return partEnd.getType();
            }
        }
        return null;
    }

    private static boolean hasStereotype(IDependency dependency, String stereotype) {
        return Arrays.asList(dependency.getStereotypes()).contains(stereotype);
    }

    private static String displayName(INamedElement element) {
        String name = element.getName();
        return name == null || name.isBlank() ? MatrixElement.UNNAMED : name;
    }

    // Astah keeps the aggregation and the composition marks on the end whose type is the whole, the reverse of UML, and the diamond is drawn at that end.
    // A plain attribute reports isComposite() too, so only the ends of the association are read.
    private void addAssociation(IAssociation association) {
        IAttribute[] ends = association.getMemberEnds();
        if (ends == null || ends.length != 2) {
            return;
        }
        IClass typeA = ends[0].getType();
        IClass typeB = ends[1].getType();
        if (typeA == null || typeB == null || !contains(typeA) || !contains(typeB)) {
            return;
        }
        String id = association.getId();

        boolean wholeA = ends[0].isAggregate() || ends[0].isComposite();
        boolean wholeB = ends[1].isAggregate() || ends[1].isComposite();
        if (wholeA || wholeB) {
            MatrixRelationshipKind kind = ends[0].isComposite() || ends[1].isComposite()
                    ? MatrixRelationshipKind.COMPOSITION
                    : MatrixRelationshipKind.AGGREGATION;
            if (!kinds.contains(kind)) {
                return;
            }

            // An association marked on both ends has no whole, and so no direction
            if (wholeA && wholeB) {
                found.putIfAbsent(id, MatrixRelationship.undirected(id, typeA.getId(), typeB.getId(), kind));
            } else if (wholeA) {
                addDirected(id, typeA, typeB, List.of(kind));
            } else {
                addDirected(id, typeB, typeA, List.of(kind));
            }
            return;
        }

        if (!kinds.contains(MatrixRelationshipKind.ASSOCIATION)) {
            return;
        }

        // An arrowhead is drawn on a navigable end, so the association goes to the type of that end. Unspecified, the default, counts as not navigable.
        boolean navigableA = NAVIGABLE.equals(ends[0].getNavigability());
        boolean navigableB = NAVIGABLE.equals(ends[1].getNavigability());
        if (navigableA && navigableB) {
            found.putIfAbsent(id, MatrixRelationship.bidirectional(id, typeA.getId(), typeB.getId(), MatrixRelationshipKind.ASSOCIATION));
        } else if (navigableB) {
            addDirected(id, typeA, typeB, List.of(MatrixRelationshipKind.ASSOCIATION));
        } else if (navigableA) {
            addDirected(id, typeB, typeA, List.of(MatrixRelationshipKind.ASSOCIATION));
        } else {
            found.putIfAbsent(id, MatrixRelationship.undirected(id, typeA.getId(), typeB.getId(), MatrixRelationshipKind.ASSOCIATION));
        }
    }

    // A dependency with one of the requirement stereotypes is that requirement relationship, never a plain dependency, so that asking for dependencies does not mix them in.
    // One with several of them is one relationship, shown with those of its kinds that were asked for.
    private void addDependency(INamedElement client, IDependency dependency) {
        Set<String> stereotypes = new HashSet<>(Arrays.asList(dependency.getStereotypes()));

        List<MatrixRelationshipKind> requirementKinds = REQUIREMENT_KINDS.stream()
                .filter(kind -> stereotypes.contains(kind.requirementStereotype))
                .toList();

        List<MatrixRelationshipKind> shownKinds;
        if (requirementKinds.isEmpty()) {
            shownKinds = kinds.contains(MatrixRelationshipKind.DEPENDENCY) ? List.of(MatrixRelationshipKind.DEPENDENCY) : List.of();
        } else {
            shownKinds = requirementKinds.stream().filter(kinds::contains).toList();
        }

        if (!shownKinds.isEmpty()) {
            addDirected(dependency.getId(), client, dependency.getSupplier(), shownKinds);
        }
    }

    private void addDirected(String id, INamedElement source, INamedElement target, List<MatrixRelationshipKind> relationshipKinds) {
        if (source == null || target == null || !contains(source) || !contains(target)) {
            return;
        }
        found.putIfAbsent(id, MatrixRelationship.directed(id, id, source.getId(), target.getId(), relationshipKinds));
    }

    private boolean contains(INamedElement element) {
        return elements.containsKey(element.getId());
    }
}
