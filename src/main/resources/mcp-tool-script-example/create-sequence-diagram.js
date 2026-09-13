/*
 * Draws a sequence diagram with lifelines typed by a base class, synchronous messages, a create message and a combined fragment.
 */

var rootPackageId = tools.get_proj({}).element.id;


// ============================================================
// Classes used as the base class of the lifelines
// ============================================================

// A message is bound to an operation of the class behind the receiving lifeline,
// so the classes and their operations are created first.
var checkoutControllerClassId = tools.create_class_in_parent_pkg({
  newClassName: 'CheckoutController',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var orderClassId = tools.create_class_in_parent_pkg({
  newClassName: 'Order',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var confirmOperationId = tools.create_ope({
  newOperationName: 'confirm',
  parentClassId: orderClassId
}).namedElement.element.id;

var receiptClassId = tools.create_class_in_parent_pkg({
  newClassName: 'Receipt',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var markPaidOperationId = tools.create_ope({
  newOperationName: 'markPaid',
  parentClassId: receiptClassId
}).namedElement.element.id;

var markFailedOperationId = tools.create_ope({
  newOperationName: 'markFailed',
  parentClassId: receiptClassId
}).namedElement.element.id;

var diagramId = tools.create_seq_dgm({
  newSequenceDiagramName: 'Checkout',
  parentPackageId: rootPackageId
}).diagram.namedElement.element.id;


// ============================================================
// Lifelines
// ============================================================

// Only the X coordinate is given. The Y coordinate is decided by Astah.
// Do not put the class name in the lifeline name: setting the base class appends it as
// "name : Class", and that longer label widens the box around its centre.
var controllerLifeline = tools.create_lifeline({
  newLifelineName: 'controller',
  locationX: 60,
  targetSequenceDiagramId: diagramId
});
var controller = controllerLifeline.presentation.id;

// set_base_class_of_lifeline takes the model element of the lifeline, not its presentation.
tools.set_base_class_of_lifeline({
  targetLifelineId: controllerLifeline.presentation.correspondingModelElement.id,
  baseClassId: checkoutControllerClassId
});

var orderLifeline = tools.create_lifeline({
  newLifelineName: 'order',
  locationX: 280,
  targetSequenceDiagramId: diagramId
});
var order = orderLifeline.presentation.id;

tools.set_base_class_of_lifeline({
  targetLifelineId: orderLifeline.presentation.correspondingModelElement.id,
  baseClassId: orderClassId
});

var receiptLifeline = tools.create_lifeline({
  newLifelineName: 'receipt',
  locationX: 480,
  targetSequenceDiagramId: diagramId
});
var receipt = receiptLifeline.presentation.id;

tools.set_base_class_of_lifeline({
  targetLifelineId: receiptLifeline.presentation.correspondingModelElement.id,
  baseClassId: receiptClassId
});


// ============================================================
// Messages
// ============================================================

// A synchronous message. The sender is a lifeline here, because it has no activation yet.
var confirm = tools.create_msg({
  newMessageName: 'confirm',
  senderNodePresentationId: controller,
  receiverNodePresentationId: order,
  locationY: 100,
  targetSequenceDiagramId: diagramId
});

// set_ope_of_msg takes the model element of the message, and needs the base class
// of the receiving lifeline to be set already.
tools.set_ope_of_msg({
  targetMessageId: confirm.presentation.correspondingModelElement.id,
  operationId: confirmOperationId
});

// targetNodeEnd is the activation the message created on the receiver side.
// Send the following messages from it, otherwise the activation of order gets split.
var orderActivation = confirm.targetNodeEnd.id;

// A create message. The receiver must be a lifeline, and its head moves down to this Y coordinate.
tools.create_create_msg({
  newCreateMessageName: 'create',
  senderNodePresentationId: orderActivation,
  receiverNodePresentationId: receipt,
  locationY: 160,
  targetSequenceDiagramId: diagramId
});


// ============================================================
// Combined fragment
// ============================================================

// A combined fragment is placed by rectangle, so it has to be given coordinates that span exactly
// the lifelines taking part in it.
var fragment = tools.create_combined_fragment({
  combinedFragmentKind: 'alt',
  newCombinedFragmentName: '',
  locationX: 260,
  locationY: 200,
  width: 350,
  height: 170,
  targetSequenceDiagramId: diagramId
});
var fragmentNode = fragment.presentation.id;
var fragmentId = fragment.presentation.correspondingModelElement.id;

// A new combined fragment already has one operand, so set its guard and add the second one.
var firstOperandId = tools.get_combined_fragment_info({ id: fragmentId }).interactionOperands[0].id;
tools.set_guard_of_interaction_operand({
  targetInteractionOperandId: firstOperandId,
  guard: 'payment accepted'
});
tools.add_interaction_operand({
  targetCombinedFragmentId: fragmentId,
  newInteractionOperandName: '',
  guard: 'else'
});

// Fix the operand heights before putting any message inside, because changing a height moves whatever is already there.
// An operand needs to hold the message plus the 25 high activation it starts, so 85 leaves a margin of 10 below it.
tools.set_height_of_interaction_operand({
  targetCombinedFragmentNodePresentationId: fragmentNode,
  targetInteractionOperandIndex: 1,
  height: 85
});
tools.set_height_of_interaction_operand({
  targetCombinedFragmentNodePresentationId: fragmentNode,
  targetInteractionOperandIndex: 2,
  height: 85
});


// ============================================================
// Messages inside the combined fragment
// ============================================================

// A message goes into an operand by its Y coordinate: there is no operand to name.
var markPaid = tools.create_msg({
  newMessageName: 'markPaid',
  senderNodePresentationId: orderActivation,
  receiverNodePresentationId: receipt,
  locationY: 250,
  targetSequenceDiagramId: diagramId
});
tools.set_ope_of_msg({
  targetMessageId: markPaid.presentation.correspondingModelElement.id,
  operationId: markPaidOperationId
});

var markFailed = tools.create_msg({
  newMessageName: 'markFailed',
  senderNodePresentationId: orderActivation,
  receiverNodePresentationId: receipt,
  locationY: 335,
  targetSequenceDiagramId: diagramId
});
tools.set_ope_of_msg({
  targetMessageId: markFailed.presentation.correspondingModelElement.id,
  operationId: markFailedOperationId
});


print('Created the sequence diagram Checkout.');
