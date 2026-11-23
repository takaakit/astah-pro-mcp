package com.astahpromcp.tool.astah.pro.guide;

final class SequenceDiagramGuide {
    
    private SequenceDiagramGuide() {
    }
    
    static String contents() {
        String contents = """
IMPORTANT POINTS to Keep in Mind:
* An ExecutionSpecification in the UML specification refers to the same thing as an Activation in Astah.
* When creating a lifeline, if a corresponding base class exists, set that class as the type of the lifeline. Do not include the type name in the lifeline name, because the type name is automatically displayed after it.
* When adding a message name, if a corresponding operation exists in the base class associated with the target lifeline of the message, assign that operation to the message. Otherwise, specify an arbitrary message name, arguments, and return value.
* When creating a message, do not blindly specify a lifeline as the sender of the message. If the message should be sent from an existing activation (ExecutionSpecification), be sure to specify the activation (ExecutionSpecification) as the message sender. Specifying the lifeline as the message sender instead will split the sender lifeline's activation (ExecutionSpecification).
* It is not necessary to add '()' at the end of the message name. '()' is automatically displayed at the end of the message.
* Changing the height of an interaction operand other than the last one moves the separator below it. If the separator passes no message, the next operand takes up the difference and nothing else moves. If it passes one, that message and every presentation below it, including those below the combined fragment, move by the same amount, and a moved message can drop out of Astah's record of what an enclosing combined fragment holds. Therefore, when newly placing node/link presentations within a combined fragment, finish adjusting the heights of its operands beforehand.
* NEVER set the height of the last operand of a combined fragment (for an opt or a loop, its only operand): Astah then permanently stops recording which lifelines and messages its operands hold, even those drawn later. Instead, create the combined fragment at the height it needs; the last operand takes the height the other operands leave.
* Lifelines grow to different lengths. Create a combined fragment only where every lifeline it covers already reaches, lengthening them with set_length_of_lifeline if needed: Astah does not record a lifeline that ends above the combined fragment when it is created, nor the messages on that lifeline, even though the diagram shows them inside it.
* A combined fragment cannot be moved or resized. To change its location, width or height, create it again as follows, which keeps both the drawing and Astah's record of what each operand holds.
  1. Read its location, kind, name and height, the guard and drawnHeight of each operand (get_interaction_operand_info), and every message drawn inside it with what is needed to create it again: its sender, receiver, Y-coordinate, name, operation, argument, return value, guard and synchronicity.
  2. Delete those messages from the bottom up, and then the combined fragment.
  3. Create the combined fragment again at the new location, width or height, with the same kind and name. Set the guard of the first operand, add the other operands in order with their guards, and set each operand other than the last, from the first, to its drawnHeight or to the new height you want for it. Nothing is drawn inside it yet, so nothing else moves.
  4. Create the messages again at their Y-coordinates from top to bottom, ending with the return messages, sending each from the same lifeline or activation as before; for an activation that a deleted message started, use the one its re-created message starts.
  5. Confirm that get_interaction_operand_info reports matchesDiagram true for every operand.
* To change the drawing position (Y-coordinate) of a message or a group of messages, delete the target message or group of messages, and then create the message or group of messages at the changed Y-coordinate. Note that this cannot be achieved by changing the coordinates of the message link presentation. To insert messages between existing ones where there is not enough space, delete the messages that must follow the inserted ones together with the return messages after them, and create them again from top to bottom, ending with the return messages. A return message is drawn at the end of the activation it returns from, and that activation lengthens when a message is sent from it further down. A message created below an existing return message is ordered after it in the model, even though it is drawn above it.
* The Interaction owned by a sequence diagram has its own name (empty by default), which does not follow the diagram name. If it has a name, rename it as well when you rename the diagram.


Terminology Definitions (quoted from OMG UML Specification v.2.5.1):
* Interactions are units of behavior of an enclosing Classifier. Interactions focus on the passing of information with Messages between the ConnectableElements of the Classifier.
* An InteractionFragment may either be contained directly in an enclosing Interaction, or may be contained within an InteractionOperand of a CombinedFragment. As a CombinedFragment is itself an InteractionFragment, there may be multiple nesting levels of InteractionFragments within an Interaction.
* The notation for an Interaction in a Sequence Diagram is a solid-outline rectangle. A pentagon in the upper left corner of the rectangle contains ‘sd’ followed by the Interaction name and parameters.
* There is no general notation for an InteractionFragment. The specific subclasses of InteractionFragment define their own notation.
* In an interaction diagram a Lifeline describes the time-line for a process, where time increases down the page. The distance between two events on a time-line does not represent any literal measurement of time, only that non-zero time has passed.
* The semantics of a complete Message is simply the trace <sendEvent, receiveEvent>.
* A lost Message is a Message where the sending event occurrence is known, but there is no receiving event occurrence. We interpret this to be because the destination of the [lost]Message is outside the scope of the description. The semantics is simply the trace <sendEvent>.
* A found Message is a Message where the receiving event occurrence is known, but there is no (known) sending event occurrence. We interpret this to be because the origin of the Message is outside the scope of the description. This may for example be noise or other activity that we do not want to describe in detail. The semantics is simply the trace <receiveEvent>.
* Subclasses of MessageEnd define the specific semantics appropriate to the concept they represent.
* A Gate is a MessageEnd which is used on the boundary of an Interaction, or an InteractionUse, or a CombinedFragment to establish the concrete sender and receiver for every Message.
* A message is shown as a line from the sender MessageEnd to the receiver MessageEnd. The line must be such that every line fragment is either horizontal or downwards when traversed from send event to receive event. The send and receive events may both be on the same lifeline.
* Gates are just points on the frame, the ends of the messages.
* An InteractionOperand is a region within a CombinedFragment.
* Only InteractionOperands with true guards are included in the calculation of the semantics. If no guard is present, this is taken to mean a true guard.
* InteractionConstraints are always used in connection with CombinedFragments.
* The semantics of a CombinedFragment is dependent upon the interactionOperator, as explained below for each kind of interactionOperator.
* The Gates associated with a CombinedFragment represent the syntactic interface between the CombinedFragment and its surroundings, which means the interface towards other InteractionFragments.
* The interactionOperator alt designates that the CombinedFragment represents a choice of behavior.
* The interactionOperator opt designates that the CombinedFragment represents a choice of behavior where either the (sole) operand happens or nothing happens.
* The interactionOperator break designates that the CombinedFragment represents a breaking scenario in the sense that the operand is a scenario that is performed instead of the remainder of the enclosing InteractionFragment.
* The interactionOperator par designates that the CombinedFragment represents a parallel merge between the behaviors of the operands.
* The interactionOperator seq designates that the CombinedFragment represents a weak sequencing between the behaviors of the operands.
* The interactionOperator strict designates that the CombinedFragment represents a strict sequencing between the behaviors of the operands.
* The interactionOperator neg designates that the CombinedFragment represents traces that are defined to be invalid.
* The interactionOperator critical designates that the CombinedFragment represents a critical region.
* The interactionOperator ignore designates that there are some message types that are not shown within this combined fragment.
* The interactionOperator assert designates that the CombinedFragment represents an assertion. The sequences of the operand of the assertion are the only valid continuations.
* The interactionOperator loop designates that the CombinedFragment represents a loop. The loop operand will be repeated a number of times.
* InteractionOperands are separated by a dashed horizontal line. The InteractionOperands together make up the framed CombinedFragment. Within an InteractionOperand of a Sequence Diagram the order of the InteractionFragments are given simply by the topmost vertical position.
* The notation for a CombinedFragment in a Sequence Diagram is a solid-outline rectangle. The operator is shown in a pentagon in the upper left corner of the rectangle.
* The value of the InteractionOperandKind is given as text in a small compartment in the upper left corner of the CombinedFragment frame.
* A "coregion" is a notational shorthand for parallel combined fragments, used for the common situation where the order of event occurrences (or other nested fragments) on one Lifeline is insignificant. This means that in a given “coregion” area of a Lifeline all the directly contained fragments are considered separate operands of a parallel combined fragment.
* The semantics of the InteractionUse is the set of traces of the semantics of the referred Interaction where the gates have been resolved as well as all generic parts having been bound such as the arguments substituting the parameters.
* The InteractionUse is shown as a CombinedFragment symbol where the operator is called ref.
* ExecutionSpecifications are represented as thin rectangles (gray or white) on the lifeline.
        """;
        
        return contents;
    }
}
