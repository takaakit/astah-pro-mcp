/*
 * Draws a class diagram with classes, an interface and an enumeration, each with attributes, operations, parameters and return types, joined by association, generalization, realization and dependency lines.
 */

var rootPackageId = tools.get_proj({}).element.id;


// ============================================================
// PaymentStatus (enumeration)
// ============================================================

// create_enum_in_parent_pkg wraps the model element in classDTO, unlike the other create tools.
var paymentStatusId = tools.create_enum_in_parent_pkg({
  newEnumerationName: 'PaymentStatus',
  parentPackageId: rootPackageId
}).classDTO.namedElement.element.id;

tools.set_definition({
  targetNamedElementId: paymentStatusId,
  definition: 'The outcome of a payment attempt. Gives each outcome a name of its own, so that a caller never has to interpret a raw result code.'
});

tools.create_enum_literal({
  newEnumerationLiteralName: 'PENDING',
  parentEnumerationId: paymentStatusId
});
tools.create_enum_literal({
  newEnumerationLiteralName: 'PAID',
  parentEnumerationId: paymentStatusId
});
tools.create_enum_literal({
  newEnumerationLiteralName: 'FAILED',
  parentEnumerationId: paymentStatusId
});


// ============================================================
// PaymentMethod (interface)
// ============================================================

var paymentMethodId = tools.create_interface_in_parent_pkg({
  newInterfaceName: 'PaymentMethod',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: paymentMethodId,
  definition: 'The service through which an order takes money. Declares how a charge is requested and what its outcome is, and leaves how the money actually moves to the implementing class.'
});

// An interface takes operations through create_ope, the same as a class.
var chargeId = tools.create_ope({
  newOperationName: 'charge',
  parentClassId: paymentMethodId
}).namedElement.element.id;

// PaymentStatus is not a primitive type, so the return type is given by ID.
tools.set_return_type_of_ope({
  targetOperationId: chargeId,
  returnTypeId: paymentStatusId
});

var amountId = tools.create_param({
  newParameterName: 'amount',
  targetOperationId: chargeId
}).namedElement.element.id;

// long is a primitive type, so the parameter type is given as an expression.
tools.set_type_expression_of_param({
  targetParameterId: amountId,
  typeExpression: 'long'
});


// ============================================================
// Order (class)
// ============================================================

var orderId = tools.create_class_in_parent_pkg({
  newClassName: 'Order',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: orderId,
  definition: 'One order placed by a customer. Holds the order number and the lines that make the order up, and is responsible for paying the order through a payment method.'
});

// The attribute type defaults to int, which is what orderNo needs.
tools.create_attr({
  newAttributeName: 'orderNo',
  parentClassId: orderId
});

var payId = tools.create_ope({
  newOperationName: 'pay',
  parentClassId: orderId
}).namedElement.element.id;

tools.set_return_type_expression_of_ope({
  targetOperationId: payId,
  returnTypeExpression: 'boolean'
});

var methodId = tools.create_param({
  newParameterName: 'method',
  targetOperationId: payId
}).namedElement.element.id;

// The parameter is typed by the interface, so it is given by ID.
tools.set_type_of_param({
  targetParameterId: methodId,
  parameterTypeId: paymentMethodId
});


// ============================================================
// SubscriptionOrder, OrderLine, CreditCard (classes)
// ============================================================

var subscriptionOrderId = tools.create_class_in_parent_pkg({
  newClassName: 'SubscriptionOrder',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: subscriptionOrderId,
  definition: 'An order that repeats on a fixed cycle. Adds the renewal period to what an order holds, and is responsible for deciding when the next order falls due.'
});

tools.create_attr({
  newAttributeName: 'renewalCycleDays',
  parentClassId: subscriptionOrderId
});

var orderLineId = tools.create_class_in_parent_pkg({
  newClassName: 'OrderLine',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: orderLineId,
  definition: 'One item line within an order. Holds the quantity ordered and is responsible for working out the subtotal of that line.'
});

tools.create_attr({
  newAttributeName: 'quantity',
  parentClassId: orderLineId
});

var subtotalId = tools.create_ope({
  newOperationName: 'subtotalMinorUnits',
  parentClassId: orderLineId
}).namedElement.element.id;

tools.set_return_type_expression_of_ope({
  targetOperationId: subtotalId,
  returnTypeExpression: 'long'
});

var creditCardId = tools.create_class_in_parent_pkg({
  newClassName: 'CreditCard',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: creditCardId,
  definition: 'A payment method backed by a credit card. Responsible for charging the card and for reporting the payment status that comes back.'
});

tools.create_attr({
  newAttributeName: 'brandCode',
  parentClassId: creditCardId
});


// ============================================================
// Relationships
// ============================================================

var holds = tools.create_asso({
  sourceClassId: orderId,
  targetClassId: orderLineId,
  sourceAggregationKind: 'none',
  sourceNavigability: 'unspecified',
  targetAggregationKind: 'none',
  targetNavigability: 'navigable'
});
var holdsId = holds.namedElement.element.id;

tools.set_name({ targetNamedElementId: holdsId, name: 'holds' });
tools.set_name({ targetNamedElementId: holds.associationEndA.id, name: 'order' });
tools.set_name({ targetNamedElementId: holds.associationEndB.id, name: 'line' });

tools.set_multiplicity_of_asso_end_a({
  targetAttributeId: holds.associationEndA.id,
  lowerMultiplicity: '1',
  upperMultiplicity: ''
});
tools.set_multiplicity_of_asso_end_b({
  targetAttributeId: holds.associationEndB.id,
  lowerMultiplicity: '0',
  upperMultiplicity: '*'
});

var generalizationId = tools.create_gen({
  subClassId: subscriptionOrderId,
  superClassId: orderId
}).namedElement.element.id;

var realizationId = tools.create_real({
  clientClassId: creditCardId,
  supplierClassId: paymentMethodId
}).namedElement.element.id;

var orderToMethodId = tools.create_dep({
  sourceNamedElementId: orderId,
  targetNamedElementId: paymentMethodId
}).namedElement.element.id;

var methodToStatusId = tools.create_dep({
  sourceNamedElementId: paymentMethodId,
  targetNamedElementId: paymentStatusId
}).namedElement.element.id;


// ============================================================
// Diagram
// ============================================================

var diagramId = tools.create_class_dgm({
  newDiagramName: 'Ordering',
  targetPackageId: rootPackageId
}).namedElement.element.id;

// Place the node presentations before drawing any line between them.
var orderNode = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: orderId,
  locationX: 40, locationY: 60
}).presentation.id;

var orderLineNode = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: orderLineId,
  locationX: 408, locationY: 60
}).presentation.id;

var subscriptionOrderNode = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: subscriptionOrderId,
  locationX: 89, locationY: 210
}).presentation.id;

var paymentMethodNode = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: paymentMethodId,
  locationX: 340, locationY: 210
}).presentation.id;

// An interface node is placed by its centre rather than by its top-left corner,
// so set the location again to line it up with the classes beside it.
tools.set_node_prst_location({
  nodePresentationId: paymentMethodNode,
  locationX: 340, locationY: 210
});

// A link presentation runs between the centres of the two nodes, so two boxes of different heights
// have to be offset vertically for the line between them to come out horizontal.
var paymentStatusNode = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: paymentStatusId,
  locationX: 650, locationY: 199
}).presentation.id;

var creditCardNode = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: creditCardId,
  locationX: 405, locationY: 370
}).presentation.id;

// A link presentation connects two node presentations and names the relationship it draws.
tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: holdsId,
  sourceNodePresentationId: orderNode,
  targetNodePresentationId: orderLineNode
});

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: generalizationId,
  sourceNodePresentationId: subscriptionOrderNode,
  targetNodePresentationId: orderNode
});

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: realizationId,
  sourceNodePresentationId: creditCardNode,
  targetNodePresentationId: paymentMethodNode
});

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: orderToMethodId,
  sourceNodePresentationId: orderNode,
  targetNodePresentationId: paymentMethodNode
});

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId, targetElementId: methodToStatusId,
  sourceNodePresentationId: paymentMethodNode,
  targetNodePresentationId: paymentStatusNode
});


print('Created the class diagram Ordering (' + diagramId + ').');
