package com.astahpromcp.tool.astah.pro;

import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class AstahProToolSupport {
    
    private static final Pattern RRGGBB = Pattern.compile("#[0-9A-Fa-f]{6}");

    private static final Pattern MULTIPLICITY_BOUND = Pattern.compile("(\\d+|\\*|[A-Za-z_]\\w*)?");

    private final ProjectAccessor projectAccessor;
    
    public AstahProToolSupport(ProjectAccessor projectAccessor) {
        this.projectAccessor = projectAccessor;
    }

    private <T> T getEntity(String id, Class<T> clazz, String typeName) {

        IEntity astahEntity = projectAccessor.getEntity(id);

        if (astahEntity == null) {
            // A primitive type is never returned by getEntity(), so an ID that reaches here may still name one. Say so, because "incorrect ID" would be misleading.
            IClass astahPrimitiveType = findPrimitiveType(id);
            if (astahPrimitiveType != null) {
                throw new IllegalArgumentException(String.format(
                    "Failed to get %s (ID: %s) because '%s' is a primitive type predefined in the project. A primitive type can only be used as a type or a base classifier (for example, the type of an attribute or the base class of an object node); it can never be renamed, edited or deleted.",
                    typeName, id, astahPrimitiveType.getName()));
            }

            throw new IllegalArgumentException(String.format("Failed to get %s (ID: %s) due to an incorrect ID.", typeName, id));
        }

        if (!clazz.isInstance(astahEntity)) {
            String actualType = "";
            if (astahEntity instanceof IPresentation astahPresentation) {
                actualType = String.format(" (actual type: %s presentation)", astahPresentation.getType());
            } else {
                NamedElementDTO.Type type = NamedElementDTO.Type.getCorrespondingType(astahEntity);
                if (type != NamedElementDTO.Type.UNKNOWN && type != NamedElementDTO.Type.ENTITY) {
                    actualType = String.format(" (actual type: %s)", type.typeName);
                }
            }

            throw new IllegalArgumentException(String.format("Failed to get %s (ID: %s) due to an incorrect type%s.", typeName, id, actualType));
        }

        return clazz.cast(astahEntity);
    }

    public List<IClass> getPrimitiveTypes() {

        Set<INamedElement> astahPrimitiveTypes;
        try {
            astahPrimitiveTypes = projectAccessor.getPrimitiveTypes();
        } catch (Exception e) {
            // No project is open, for example.
            log.debug("Failed to get the primitive types of the project.", e);
            return List.of();
        }

        List<IClass> primitiveTypes = new ArrayList<>();
        for (INamedElement astahPrimitiveType : astahPrimitiveTypes) {
            // The set holds a null entry in place of a primitive type that has been broken by an edit, so it is not enough to take every entry.
            if (astahPrimitiveType instanceof IClass) {
                primitiveTypes.add((IClass) astahPrimitiveType);
            }
        }
        primitiveTypes.sort(Comparator.comparing(INamedElement::getName));

        return primitiveTypes;
    }

    public IClass findPrimitiveType(String id) {

        for (IClass primitiveType : getPrimitiveTypes()) {
            if (primitiveType.getId().equals(id)) {
                return primitiveType;
            }
        }

        return null;
    }

    public IClass getClassOrPrimitiveType(String id) {

        IClass astahPrimitiveType = findPrimitiveType(id);
        if (astahPrimitiveType != null) {
            return astahPrimitiveType;
        }

        return getEntity(id, IClass.class, "classifier");
    }

    public void verifyDeleted(String id) {

        if (projectAccessor.getEntity(id) == null) {
            return;
        }

        throw new IllegalStateException(String.format("Failed to delete entity (ID: %s) because it still exists in the project. Astah may have raised a confirmation dialog and a user may have answered No to it, cancelling the delete.", id));
    }

    public IDiagram getDiagram(String id) {
        return getEntity(id, IDiagram.class, "diagram");
    }

    public static String requireRrggbbColor(String color) {

        if (color == null || !RRGGBB.matcher(color).matches()) {
            throw new IllegalArgumentException(String.format(
                "Invalid color '%s'. Pass # followed by exactly 6 hexadecimal digits (#RRGGBB), such as #FFFFFF. Shorthand (#FFF), transparency (#RRGGBBAA) and color names (red) are not accepted.",
                color));
        }

        return color;
    }

    public static String requireMultiplicityBound(String bound) {

        if (bound == null || !MULTIPLICITY_BOUND.matcher(bound).matches()) {
            throw new IllegalArgumentException(String.format(
                "Invalid multiplicity bound '%s'. Pass a single bound to each of lowerMultiplicity and upperMultiplicity: a non-negative integer, '*', or a name such as MAX. For example, pass '0' and '1' for 0..1.",
                bound));
        }

        return bound;
    }

    public static List<IPresentation> getPresentationsOnDiagram(IDiagram astahDiagram) throws InvalidUsingException {

        List<IPresentation> astahPresentations = new ArrayList<>();
        for (IPresentation astahPresentation : astahDiagram.getPresentations()) {
            // getPresentations() also returns the header cells that show this diagram on a CRUD, which are drawn on the CRUD rather than on this diagram, so skip such elements
            if (astahDiagram.equals(astahPresentation.getDiagram())) {
                astahPresentations.add(astahPresentation);
            }
        }

        return astahPresentations;
    }

    public static List<IPresentation> getCorrespondingPresentations(IElement astahElement) throws InvalidUsingException {

        if (!(astahElement instanceof IDiagram astahDiagram)) {
            return Arrays.asList(astahElement.getPresentations());
        }

        List<IPresentation> astahPresentations = new ArrayList<>();
        for (IPresentation astahPresentation : astahDiagram.getPresentations()) {
            // For a diagram, getPresentations() returns the presentations drawn on it as well as the ones that show the diagram itself, such as the header cells of a CRUD, so only the latter are taken
            if (!astahDiagram.equals(astahPresentation.getDiagram())) {
                astahPresentations.add(astahPresentation);
            }
        }

        return astahPresentations;
    }

    public static List<IDiagram> getRenderedInDiagrams(IElement astahElement) throws InvalidUsingException {

        // Keyed by ID, since the API can hand back a different object for the same element (the model of a data store's presentation, for one), so equals() is not relied on
        Map<String, IDiagram> astahDiagrams = new LinkedHashMap<>();
        for (IPresentation astahPresentation : getCorrespondingPresentations(astahElement)) {
            IDiagram astahDiagram = astahPresentation.getDiagram();
            if (astahDiagram != null) {
                astahDiagrams.putIfAbsent(astahDiagram.getId(), astahDiagram);
            }
        }

        return new ArrayList<>(astahDiagrams.values());
    }

    public IClassDiagram getClassDiagram(String id) {
        return getEntity(id, IClassDiagram.class, "class diagram");
    }

    public ICompositeStructureDiagram getCompositeStructureDiagram(String id) {
        return getEntity(id, ICompositeStructureDiagram.class, "composite structure diagram");
    }
    
    public IElement getElement(String id) {
        return getEntity(id, IElement.class, "element");
    }

    public INamedElement getNamedElement(String id) {
        return getEntity(id, INamedElement.class, "named element");
    }

    public IPackage getPackage(String id) {
        return getEntity(id, IPackage.class, "package");
    }

    public IClass getClass(String id) {
        return getEntity(id, IClass.class, "class");
    }

    public IClass getInterface(String id) {
        IClass astahClass = getEntity(id, IClass.class, "interface");

        if (!astahClass.hasStereotype("interface")) {
            throw new IllegalArgumentException(String.format(
                "Failed to get interface (ID: %s) because '%s' is a class without the 'interface' stereotype.",
                id, astahClass.getName()));
        }

        return astahClass;
    }

    public IAttribute getAttribute(String id) {
        return getEntity(id, IAttribute.class, "attribute");
    }

    public IOperation getOperation(String id) {
        return getEntity(id, IOperation.class, "operation");
    }

    public IParameter getParameter(String id) {
        return getEntity(id, IParameter.class, "parameter");
    }

    public IEnumeration getEnumeration(String id) {
        return getEntity(id, IEnumeration.class, "enumeration");
    }

    public IEnumerationLiteral getEnumerationLiteral(String id) {
        return getEntity(id, IEnumerationLiteral.class, "enumeration literal");
    }

    public IAssociation getAssociation(String id) {
        return getEntity(id, IAssociation.class, "association");
    }

    public IAttribute getAssociationEnd(String id, int endIndex) {
        IAttribute astahAttribute = getEntity(id, IAttribute.class, "association end");

        // An ordinary attribute and a port are attributes too, but belong to no association
        IAssociation astahAssociation = astahAttribute.getAssociation();
        if (astahAssociation == null) {
            throw new IllegalArgumentException(String.format(
                "Failed to get association end (ID: %s) because it is not an end of an association, such as an ordinary attribute or a port. Pass an ID listed as associationEndA or associationEndB by get_asso_info.", id));
        }

        // End A is the first member end and end B the second
        IAttribute astahExpectedEnd = astahAssociation.getMemberEnds()[endIndex];
        if (!astahExpectedEnd.getId().equals(id)) {
            char wantedEnd = (char) ('A' + endIndex);
            char actualEnd = (char) ('B' - endIndex);
            throw new IllegalArgumentException(String.format(
                "Failed to get association end %c (ID: %s) because it is association end %c. Use the tool function for association end %c, or pass association end %c (ID: %s).",
                wantedEnd, id, actualEnd, actualEnd, wantedEnd, astahExpectedEnd.getId()));
        }

        return astahAttribute;
    }

    public IAssociationClass getAssociationClass(String id) {
        return getEntity(id, IAssociationClass.class, "association class");
    }

    public IDependency getDependency(String id) {
        return getEntity(id, IDependency.class, "dependency");
    }

    public IGeneralization getGeneralization(String id) {
        return getEntity(id, IGeneralization.class, "generalization");
    }

    public IRealization getRealization(String id) {
        return getEntity(id, IRealization.class, "realization");
    }

    public IUsage getUsage(String id) {
        return getEntity(id, IUsage.class, "usage");
    }

    public ILinkEnd getLinkEnd(String id) {
        return getEntity(id, ILinkEnd.class, "link end");
    }

    public IMultiplicityRange getMultiplicityRange(String id) {
        return getEntity(id, IMultiplicityRange.class, "multiplicity range");
    }

    public ITaggedValue getTaggedValue(String id) {
        return getEntity(id, ITaggedValue.class, "tagged value");
    }

    public IPresentation getPresentation(String id) {
        return getEntity(id, IPresentation.class, "presentation");
    }

    public INodePresentation getNodePresentation(String id) {
        return getEntity(id, INodePresentation.class, "node presentation");
    }

    public ILinkPresentation getLinkPresentation(String id) {
        return getEntity(id, ILinkPresentation.class, "link presentation");
    }

    public ICombinedFragment getCombinedFragment(String id) {
        return getEntity(id, ICombinedFragment.class, "combined fragment");
    }

    public IInteractionOperand getInteractionOperand(String id) {
        return getEntity(id, IInteractionOperand.class, "interaction operand");
    }

    public IComment getComment(String id) {
        return getEntity(id, IComment.class, "comment");
    }

    public IGate getGate(String id) {
        return getEntity(id, IGate.class, "gate");
    }

    public IInstanceSpecification getInstanceSpecification(String id) {
        return getEntity(id, IInstanceSpecification.class, "instance specification");
    }

    public IInteraction getInteraction(String id) {
        return getEntity(id, IInteraction.class, "interaction");
    }

    public IInteractionUse getInteractionUse(String id) {
        return getEntity(id, IInteractionUse.class, "interaction use");
    }

    public ISequenceDiagram getSequenceDiagram(String id) {
        return getEntity(id, ISequenceDiagram.class, "sequence diagram");
    }

    public ILifeline getLifeline(String id) {
        return getEntity(id, ILifeline.class, "lifeline");
    }

    public ILifelineLink getLifelineLink(String id) {
        return getEntity(id, ILifelineLink.class, "lifeline link");
    }

    public ILink getLink(String id) {
        return getEntity(id, ILink.class, "link");
    }

    public IMessage getMessage(String id) {
        return getEntity(id, IMessage.class, "message");
    }

    public IAction getAction(String id) {
        return getEntity(id, IAction.class, "action");
    }

    public IActivity getActivity(String id) {
        return getEntity(id, IActivity.class, "activity");
    }

    public IActivityDiagram getActivityDiagram(String id) {
        return getEntity(id, IActivityDiagram.class, "activity diagram");
    }

    public IActivityNode getActivityNode(String id) {
        return getEntity(id, IActivityNode.class, "activity node");
    }

    public IControlNode getControlNode(String id) {
        return getEntity(id, IControlNode.class, "control node");
    }

    public IFlow getFlow(String id) {
        return getEntity(id, IFlow.class, "flow");
    }

    public IObjectNode getObjectNode(String id) {
        return getEntity(id, IObjectNode.class, "object node");
    }

    public IPartition getPartition(String id) {
        return getEntity(id, IPartition.class, "partition");
    }

    public ITransition getTransition(String id) {
        return getEntity(id, ITransition.class, "transition");
    }

    public IVertex getVertex(String id) {
        return getEntity(id, IVertex.class, "vertex");
    }

    public IRequirement getRequirement(String id) {
        return getEntity(id, IRequirement.class, "requirement");
    }

    public ISlot getSlot(String id) {
        return getEntity(id, ISlot.class, "slot");
    }

    public ITestCase getTestCase(String id) {
        return getEntity(id, ITestCase.class, "test case");
    }

    public IPseudostate getPseudostate(String id) {
        return getEntity(id, IPseudostate.class, "pseudostate");
    }

    public IState getState(String id) {
        return getEntity(id, IState.class, "state");
    }

    public IStateMachine getStateMachine(String id) {
        return getEntity(id, IStateMachine.class, "state machine");
    }

    public IStateMachineDiagram getStateMachineDiagram(String id) {
        return getEntity(id, IStateMachineDiagram.class, "state machine diagram");
    }

    public IUseCase getUseCase(String id) {
        return getEntity(id, IUseCase.class, "usecase");
    }

    public IExtend getExtend(String id) {
        return getEntity(id, IExtend.class, "extend");
    }

    public IInclude getInclude(String id) {
        return getEntity(id, IInclude.class, "include");
    }

    public IUseCaseDiagram getUseCaseDiagram(String id) {
        return getEntity(id, IUseCaseDiagram.class, "usecase diagram");
    }

    public ICommunicationDiagram getCommunicationDiagram(String id) {
        return getEntity(id, ICommunicationDiagram.class, "communication diagram");
    }

    public IPort getPort(String id) {
        return getEntity(id, IPort.class, "port");
    }

    public IConnector getConnector(String id) {
        return getEntity(id, IConnector.class, "connector");
    }

    public IConstraint getConstraint(String id) {
        return getEntity(id, IConstraint.class, "constraint");
    }

    public IERDiagram getERDiagram(String id) {
        return getEntity(id, IERDiagram.class, "ER diagram");
    }

    public IERDatatype getERDatatype(String id) {
        return getEntity(id, IERDatatype.class, "ER datatype");
    }

    public IERDomain getERDomain(String id) {
        return getEntity(id, IERDomain.class, "ER domain");
    }

    public IERAttribute getERAttribute(String id) {
        return getEntity(id, IERAttribute.class, "ER attribute");
    }

    public IEREntity getEREntity(String id) {
        return getEntity(id, IEREntity.class, "ER entity");
    }

    public IERIndex getERIndex(String id) {
        return getEntity(id, IERIndex.class, "ER index");
    }

    public IERModel getERModel(String id) {
        return getEntity(id, IERModel.class, "ER model");
    }

    public IERPackage getERPackage(String id) {
        return getEntity(id, IERPackage.class, "ER package");
    }

    public IERRelationship getERRelationship(String id) {
        return getEntity(id, IERRelationship.class, "ER relationship");
    }

    public IERSchema getERSchema(String id) {
        return getEntity(id, IERSchema.class, "ER schema");
    }

    public IERSubtypeRelationship getERSubtypeRelationship(String id) {
        return getEntity(id, IERSubtypeRelationship.class, "ER subtype relationship");
    }

    public IMindMapDiagram getMindMapDiagram(String id) {
        return getEntity(id, IMindMapDiagram.class, "mind map diagram");
    }
}
