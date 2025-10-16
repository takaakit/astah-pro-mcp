package com.astahpromcp.tool.astah.pro;

import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AstahProToolSupport {
    
    private final ProjectAccessor projectAccessor;
    
    public AstahProToolSupport(ProjectAccessor projectAccessor) {
        this.projectAccessor = projectAccessor;
    }

    private <T> T getEntity(String id, Class<T> clazz, String typeName) {
        T entity;
        try {
            entity = clazz.cast(projectAccessor.getEntity(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get " + typeName + " due to an incorrect ID.");
        }

        if (entity == null) {
            throw new NullPointerException("Failed to get " + typeName + " due to an incorrect ID.");
        }
        
        return entity;
    }

    public IDiagram getDiagram(String id) {
        return getEntity(id, IDiagram.class, "diagram");
    }
    
    public IClassDiagram getClassDiagram(String id) {
        return getEntity(id, IClassDiagram.class, "class diagram");
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

    public IAttribute getAssociationEnd(String id) {
        return getEntity(id, IAttribute.class, "association end");
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
}
