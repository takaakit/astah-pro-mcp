package com.astahpromcp.tool.astah.pro.review;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.astah.pro.AstahProToolSupport;
import com.astahpromcp.tool.astah.pro.common.outputdto.StepsDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.astahpromcp.tool.astah.pro.TransactionSupport;

@Slf4j
public class DiagramConsistencyTool implements ToolProvider {

    private final ProjectAccessor projectAccessor;
    private final TransactionSupport txnAstah;
    private final AstahProToolSupport astahProToolSupport;
    private final boolean includeEditTools;

    public DiagramConsistencyTool(ProjectAccessor projectAccessor, TransactionSupport transactionSupport, AstahProToolSupport astahProToolSupport, boolean includeEditTools) {
        this.projectAccessor = projectAccessor;
        this.txnAstah = transactionSupport;
        this.astahProToolSupport = astahProToolSupport;
        this.includeEditTools = includeEditTools;
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            List<ToolDefinition> tools = new ArrayList<>(createQueryTools());
            if (includeEditTools) {
                tools.addAll(createEditTools());
            }

            return List.copyOf(tools);

        } catch (Exception e) {
            log.error("Failed to create diagram consistency maintenance tools", e);
            return List.of();
        }
    }

    private List<ToolDefinition> createQueryTools() {
        return List.of();
    }

    private List<ToolDefinition> createEditTools() {
        return List.of(
            ToolSupport.toolDefinitionReturningDto(
                "diagram_consistency_maintenance_steps",
                "Return the steps that the MCP client (you) MUST follow to maintain the semantic consistency of diagrams. When you need to eliminate semantic inconsistencies and ensure semantic consistency within and across diagrams, call this tool and perform the work in strict accordance with the returned steps.",
                this::getSteps,
                NoInputDTO.class,
                StepsDTO.class));
    }

    private StepsDTO getSteps(NoInputDTO param) throws Exception {
        log.debug("Get diagram consistency maintenance steps: {}", param);

        String contents = """
Follow these steps to maintain the semantic consistency of diagrams. Do not skip, reorder, or merge steps. Every rule has an ID (e.g., CLS-05); every rule of every applicable group MUST appear as one row in the verdict table of step 5 — silently omitting a rule is forbidden.

1. Inventory the diagrams and decide rule applicability.
   1-1. Using the query tools provided by this MCP server, enumerate every diagram in the project together with its type (class diagram, use case diagram, sequence diagram, communication diagram, state machine diagram, ...).
   1-2. Decide which rule groups apply:
        - Group CLS (CLS-01..CLS-17): at least one class diagram exists. Note that, in Astah, object diagrams are represented as class diagrams.
        - Group SEQ (SEQ-01): at least one sequence diagram exists.
        - Group STM (STM-01..STM-02): at least one state machine diagram exists.
        - Group UCxCLS (UCxCLS-01): a use case diagram AND a class diagram exist.
        - Group CLSxSEQ (CLSxSEQ-01..06): a class diagram AND a sequence diagram exist.
        - Group CLSxCOM (CLSxCOM-01..02): a class diagram AND a communication diagram exist.
        - Group SEQxSTM (SEQxSTM-01): a sequence diagram AND a state machine diagram exist.
        - Group STMxCOM (STMxCOM-01): a state machine diagram AND a communication diagram exist.
   1-3. Before proceeding, output the applicability decision: each group with APPLICABLE or NOT-APPLICABLE and the reason.

2. Gather the model data required by the applicable groups using the query tools provided by this MCP server. Do NOT guess model contents from diagram names, element names, or exported images; always read the model through the tools. Retrieve at least:
   - For group CLS: the classes (abstract flag, attributes, operations with their full signatures and abstract/static/isLeaf/visibility properties), associations and their ends (aggregation kind, multiplicity, navigability), generalizations, realizations, dependencies, and all constraints, invariants, pre-/post-conditions, and guard conditions. For CLS-17 additionally: instance specifications, links, and slots.
   - For groups SEQ, CLSxSEQ, SEQxSTM: for each sequence diagram, its lifelines (and the classifiers typing them), its messages (and the operations they refer to), and its interactions.
   - For group STM: the state machines, states, transitions (with their triggering events and guards), and pseudostates.
   - For group UCxCLS: all use cases and their names.
   - For groups CLSxCOM, STMxCOM: for each communication diagram, its lifelines/objects, links, and messages.

3. Evaluate EVERY rule of every applicable group against the gathered data.

   The rule statements below are quoted verbatim from:
   - Torre, Damiano, et al. "A systematic identification of consistency rules for UML diagrams." Journal of Systems and Software 144 (2018): 121-142.
   - Torre, Damiano, et al. "How consistency is handled in model-driven software engineering and UML: an expert opinion survey." Software Quality Journal 31.1 (2023): 1-54.

   Consistency within Class Diagrams:
   - CLS-01: A class diagram is consistent if it can be instantiated without violating any of the constraints in the diagram (e.g., association end multiplicities).
   - CLS-02: If a navigation expression is used in an operation contract, then the expression must be a legal one (according to the syntax of the language and the class diagram).
   - CLS-03: This Liskov’s substitution principle holds.
   - CLS-04: A class that realizes an interface must declare all the operations in the interface with the same signatures (including parameter direction, default values, concurrency, polymorphic property, query characteristic).
   - CLS-05: An abstract operation can only belong to an abstract class.
   - CLS-06: No (public) method of a class violates, as indicated by its pre and post-conditions, the class invariant of that class.
   - CLS-07: No precondition should violate the class invariant.
   - CLS-08: No post-condition should violate the class invariant.
   - CLS-09: There must be no cycle in the directed path of aggregation associations:  A class cannot be a part of an aggregation in which it is the whole; A class cannot be a part of an aggregation in which its superclass (or ancestor) is the whole.
   - CLS-10: A class cannot be a part of more than one composition - no composite part may be shared by two composite classes.
   - CLS-11: Each concrete class, i.e., it is not abstract, must implement all the abstract operations of its superclass(es).
   - CLS-12: An operation may not be overridden by a descendant class only if its isLeaf attribute (from metaclass RedefinableElement) is defined accordingly.
   - CLS-13: A static operation cannot access an instance attribute (as indicated by its pre and post conditions, for instance).
   - CLS-14: A static operation cannot invoke an instance operation (as indicated by its pre and post conditions, for instance).
   - CLS-15: The multiplicity range for an attribute must be adhered to by all elements (operation contracts, guard conditions) that access it.
   - CLS-16: For class A's operations to use another class B, as indicated by contracts in A, there must be a means (e.g., in the form of a path involving associations, generalization and/or dependencies) in the class diagram for A to get a hold on B.
   - CLS-17: The number of occurrences of a link in an object diagram, an instance of an association in a class diagram, must satisfy the multiplicity constraints specified for the association.

   Consistency within Sequence Diagrams:
   - SEQ-01: In a sequence diagram, if an attribute is assigned the return value of a message, then the types have to be compatible.

   Consistency within State Machine Diagrams:
   - STM-01: A state machine should be deadlock-free.
   - STM-02: A state machine must be deterministic, that is, in every state, only one transition (accounting for the different levels of nested states) should fire on a reception of an event.

   Consistency between Use Case and Class Diagrams:
   - UCxCLS-01: The noun of the use case's name should equal the name of one class in the class diagram.

   Consistency between Class and Sequence Diagrams:
   - CLSxSEQ-01: The type of a lifeline (type of the connectable element of the lifeline) in a sequence diagram must not be an interface nor an abstract class.
   - CLSxSEQ-02: In case a message in a sequence diagram is referring to an operation, that operation must not be abstract.
   - CLSxSEQ-03: If a message in a sequence diagram refers to an operation, through the signature of the message, then that operation must belong, as per the class diagram, to the class that types the target lifeline of the message.
   - CLSxSEQ-04: Interactions between objects in a sequence diagram, specifically the numbers of types of interacting objects, must comply with the multiplicity restrictions specified by the class diagram (e.g., association end multiplicities).
   - CLSxSEQ-05: In order for objects to exchange messages in a sequence diagram, the sending object must have a handle to the receiving object as specified in the class diagram. Another way of saying this is that the sender must have visibility to the receiver. A specific case of this situation is when the sending object’s class has an association (possibly inherited) to the receiving object’s class.
   - CLSxSEQ-06: No operation can be used in a message of a sequence diagram if this breaks the visibility rules of the class diagram (public, protected, private).

   Consistency between Class and Communication Diagrams:
   - CLSxCOM-01: Objects involved in a communication diagram should be instances of classes of the class diagram.
   - CLSxCOM-02: In order for objects to exchange messages in a communication diagram, the sending object must have a handle to the receiving object as specified in the class diagram. Another way of saying this is that the sender must have visibility to the receiver. A specific case of this situation is when the sending object’s class has an association (possibly inherited) to the receiving object’s class.

   Consistency between Sequence and State Machine Diagrams:
   - SEQxSTM-01: When one specifies an active class, i.e., one that has a state-based behaviour described in a state machine diagram, and an instance of this active class is used in a sequence diagram, the messages sent to this objects and emitted by this object as specified in the sequence diagram must comply (e.g., sequence and types of signals, receivers and emitters of signals) to the protocol specified in the state machine diagram.

   Consistency between State Machine and Communication Diagrams:
   - STMxCOM-01: When one specifies an active class, i.e., one that has a state-based behaviour described in a state machine diagram, and an instance of this active class is used in a communication diagram, the messages sent to this objects and emitted by this object as specified in the communication diagram must comply to the protocol specified in the state machine diagram.

4. Assign exactly one verdict to each rule of each applicable group:
   - PASS: you verified the rule against the gathered data and found no violation.
   - VIOLATION: you found at least one concrete violation; record the violating elements (names and IDs).
   - NOT-EVALUABLE: the model does not contain the information the rule needs (e.g., no operation contracts, pre-/post-conditions, invariants, or guard conditions are recorded). NOT-EVALUABLE is allowed ONLY for missing information — never because the check is laborious. State what information is missing.

5. Report the results as a markdown table with one row per rule of every applicable group: | Rule ID | Verdict | Evidence (element names/IDs) or missing information |. After the table, list every VIOLATION with the violating elements and the diagrams involved.

6. If any VIOLATION is found, resolve it.
   6-1. For each violation, enumerate the possible repairs. Most rules admit more than one (e.g., CLS-05 can be repaired by making the class abstract OR by making the operation concrete); choosing between them is a design decision.
   6-2. If exactly one repair is reasonable given the model's intent, apply it. Otherwise present the options to the user and get confirmation before changing anything.
   6-3. Apply the confirmed corrections with the edit tools of this MCP server, re-run the check for each corrected rule to confirm the violation is resolved, and report the correction details.
        """;

        return new StepsDTO(contents);
    }
}
