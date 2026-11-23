package com.astahpromcp.tool.astah.pro.matrix;

import com.astahpromcp.tool.astah.pro.common.MatrixRelationshipKind;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDataFlow;
import com.change_vision.jude.api.inf.model.IDataFlowNode;
import com.change_vision.jude.api.inf.model.IDataStore;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.IExternalEntity;
import com.change_vision.jude.api.inf.model.IProcessBox;
import com.change_vision.jude.api.inf.model.IRequirement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatrixRelationshipCollectorTest {

    private static IDataFlow dataFlow(String id, IDataFlowNode source, IDataFlowNode target) {
        IDataFlow flow = mock(IDataFlow.class);
        when(flow.getId()).thenReturn(id);
        when(flow.getSource()).thenReturn(source);
        when(flow.getTarget()).thenReturn(target);
        return flow;
    }

    // The API has no editor for data flow diagrams, so mocks stand in for their processes, stores and entities
    @Test
    void collect_ok_readsADataFlowFromTheNodeItLeaves() {
        IExternalEntity customer = mock(IExternalEntity.class);
        IProcessBox withdraw = mock(IProcessBox.class);
        IDataStore account = mock(IDataStore.class);
        when(customer.getId()).thenReturn("customer");
        when(withdraw.getId()).thenReturn("withdraw");
        when(account.getId()).thenReturn("account");

        IDataFlow request = dataFlow("request", customer, withdraw);
        IDataFlow debit = dataFlow("debit", withdraw, account);
        when(customer.getOutgoings()).thenReturn(new IDataFlow[] { request });
        when(withdraw.getOutgoings()).thenReturn(new IDataFlow[] { debit });
        when(account.getOutgoings()).thenReturn(new IDataFlow[0]);

        List<MatrixRelationship> relationships = MatrixRelationshipCollector.collect(List.of(customer, withdraw, account), Set.of(MatrixRelationshipKind.DATA_FLOW));

        assertEquals(List.of(
            MatrixRelationship.directed("request", "request", "customer", "withdraw", List.of(MatrixRelationshipKind.DATA_FLOW)),
            MatrixRelationship.directed("debit", "debit", "withdraw", "account", List.of(MatrixRelationshipKind.DATA_FLOW))),
            relationships);
    }

    @Test
    void collect_ok_leavesOutADataFlowToANodeThatIsNeitherARowNorAColumn() {
        IProcessBox withdraw = mock(IProcessBox.class);
        IDataStore account = mock(IDataStore.class);
        when(withdraw.getId()).thenReturn("withdraw");
        when(account.getId()).thenReturn("account");
        IDataFlow debit = dataFlow("debit", withdraw, account);
        when(withdraw.getOutgoings()).thenReturn(new IDataFlow[] { debit });

        assertEquals(List.of(), MatrixRelationshipCollector.collect(List.of(withdraw), Set.of(MatrixRelationshipKind.DATA_FLOW)));
    }

    private static IClass type(String id, String name) {
        IClass astahClass = mock(IClass.class);
        when(astahClass.getId()).thenReturn(id);
        when(astahClass.getName()).thenReturn(name);
        when(astahClass.getAttributes()).thenReturn(new IAttribute[0]);
        when(astahClass.getClientDependencies()).thenReturn(new IDependency[0]);
        return astahClass;
    }

    private static IAttribute end(IClass type, boolean composite) {
        IAttribute end = mock(IAttribute.class);
        when(end.getType()).thenReturn(type);
        when(end.isComposite()).thenReturn(composite);
        return end;
    }

    // An association between the whole and the part, marked composite on the end typed by the whole when composite is true, and reached from the attributes of both
    private static void associate(String id, IClass whole, IClass part, boolean composite) {
        IAttribute wholeEnd = end(whole, composite);
        IAttribute partEnd = end(part, false);
        IAssociation association = mock(IAssociation.class);
        when(association.getId()).thenReturn(id);
        when(association.getMemberEnds()).thenReturn(new IAttribute[] { wholeEnd, partEnd });
        when(wholeEnd.getAssociation()).thenReturn(association);
        when(partEnd.getAssociation()).thenReturn(association);
        // Each end is an attribute of the class at the other end. The arrays are made before stubbing, as calling a mock while stubbing it confuses Mockito.
        IAttribute[] wholeAttributes = append(whole.getAttributes(), partEnd);
        IAttribute[] partAttributes = append(part.getAttributes(), wholeEnd);
        when(whole.getAttributes()).thenReturn(wholeAttributes);
        when(part.getAttributes()).thenReturn(partAttributes);
    }

    private static IAttribute[] append(IAttribute[] attributes, IAttribute attribute) {
        IAttribute[] appended = Arrays.copyOf(attributes, attributes.length + 1);
        appended[attributes.length] = attribute;
        return appended;
    }

    private static void satisfies(String id, IClass client, IRequirement requirement) {
        IDependency dependency = mock(IDependency.class);
        when(dependency.getId()).thenReturn(id);
        when(dependency.getStereotypes()).thenReturn(new String[] { "satisfy" });
        when(dependency.getSupplier()).thenReturn(requirement);
        when(client.getClientDependencies()).thenReturn(new IDependency[] { dependency });
    }

    private static IRequirement requirement(String id) {
        IRequirement requirement = mock(IRequirement.class);
        when(requirement.getId()).thenReturn(id);
        when(requirement.getAttributes()).thenReturn(new IAttribute[0]);
        when(requirement.getClientDependencies()).thenReturn(new IDependency[0]);
        return requirement;
    }

    @Test
    void collect_ok_impliesNothingThroughAnAggregation() {
        IClass whole = type("whole", "Whole");
        IClass part = type("part", "Part");
        IRequirement requirement = requirement("req");
        associate("a", whole, part, false);
        satisfies("s", part, requirement);

        List<MatrixRelationship> relationships = MatrixRelationshipCollector.collect(
            List.of(whole, requirement), Set.of(MatrixRelationshipKind.SATISFY), true, List.of());

        assertEquals(List.of(), relationships);
    }

    @Test
    void collect_ok_followsCompositionsThatComeBackToTheWholeOnlyOnce() {
        // A holds B, and B holds A again; B satisfies the requirement
        IClass a = type("a", "A");
        IClass b = type("b", "B");
        IRequirement requirement = requirement("req");
        associate("ab", a, b, true);
        associate("ba", b, a, true);
        satisfies("s", b, requirement);

        List<MatrixRelationship> relationships = MatrixRelationshipCollector.collect(
            List.of(a, requirement), Set.of(MatrixRelationshipKind.SATISFY), true, List.of());

        assertEquals(List.of(MatrixRelationship.implied("a", "req", MatrixRelationshipKind.SATISFY, "via B")), relationships);
        assertTrue(relationships.get(0).implied());
        assertNull(relationships.get(0).id());
        assertEquals("satisfy (implied)", relationships.get(0).label());
    }

    @Test
    void collect_ok_impliesNothingUnlessAsked() {
        IClass whole = type("whole", "Whole");
        IClass part = type("part", "Part");
        IRequirement requirement = requirement("req");
        associate("c", whole, part, true);
        satisfies("s", part, requirement);

        assertEquals(List.of(), MatrixRelationshipCollector.collect(List.of(whole, requirement), Set.of(MatrixRelationshipKind.SATISFY), false, List.of()));
        assertEquals(List.of(), MatrixRelationshipCollector.collect(List.of(whole, requirement), Set.of(MatrixRelationshipKind.TRACE), true, List.of()));
    }
}
