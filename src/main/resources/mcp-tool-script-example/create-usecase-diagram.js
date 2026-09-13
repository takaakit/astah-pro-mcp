/*
 * Draws a use case diagram with actors, use cases, a system boundary carrying the system name, and association, generalization, extend and include lines.
 */

var rootPackageId = tools.get_proj({}).element.id;

var diagramId = tools.create_usecase_dgm({
  newUseCaseDiagramName: 'Online Shop',
  parentPackageId: rootPackageId
}).namedElement.element.id;


// ============================================================
// System boundary
// ============================================================

// A system boundary is an ordinary rectangle. Draw it before whatever goes inside it,
// so that the later presentations are drawn on top of it rather than behind it.
tools.insert_rect_on_dgm({
  locationX: 310, locationY: 60,
  width: 480, height: 320,
  targetDiagramId: diagramId
});

// set_label does not work on a rectangle, so the system name is a text presentation of its own.
tools.insert_txt_on_dgm({
  locationX: 507, locationY: 70,
  textContent: 'Online Shop',
  targetDiagramId: diagramId
});


// ============================================================
// Actors
// ============================================================

// create_actor makes the model element only, so it is placed on the diagram afterwards.
var customerId = tools.create_actor({
  newActorName: 'Customer',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var customer = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: customerId,
  locationX: 210, locationY: 132
}).presentation.id;

var premiumCustomerId = tools.create_actor({
  newActorName: 'PremiumCustomer',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var premiumCustomer = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: premiumCustomerId,
  locationX: 80, locationY: 210
}).presentation.id;

var paymentGatewayId = tools.create_actor({
  newActorName: 'PaymentGateway',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var paymentGateway = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: paymentGatewayId,
  locationX: 850, locationY: 272
}).presentation.id;


// ============================================================
// Use cases
// ============================================================

// create_usecase wraps the model element in class, unlike the other create tools.
var placeOrderId = tools.create_usecase({
  newUsecaseName: 'Place Order',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;

var placeOrder = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: placeOrderId,
  locationX: 355, locationY: 140
}).presentation.id;

var authenticateId = tools.create_usecase({
  newUsecaseName: 'Authenticate',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;

var authenticate = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: authenticateId,
  locationX: 613, locationY: 140
}).presentation.id;

var applyDiscountId = tools.create_usecase({
  newUsecaseName: 'Apply Discount',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;

var applyDiscount = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: applyDiscountId,
  locationX: 345, locationY: 250
}).presentation.id;

var payOrderId = tools.create_usecase({
  newUsecaseName: 'Pay Order',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;

var payOrder = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: payOrderId,
  locationX: 620, locationY: 280
}).presentation.id;


// ============================================================
// Relationships
// ============================================================

// An actor is a classifier, so the line to a use case is an ordinary association.
// Leaving both ends unspecified is what keeps the line free of arrowheads.
var customerPlacesOrder = tools.create_asso({
  sourceClassId: customerId,
  targetClassId: placeOrderId,
  sourceAggregationKind: 'none',
  sourceNavigability: 'unspecified',
  targetAggregationKind: 'none',
  targetNavigability: 'unspecified'
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: customerPlacesOrder,
  sourceNodePresentationId: customer,
  targetNodePresentationId: placeOrder
});

var gatewayTakesPayment = tools.create_asso({
  sourceClassId: paymentGatewayId,
  targetClassId: payOrderId,
  sourceAggregationKind: 'none',
  sourceNavigability: 'unspecified',
  targetAggregationKind: 'none',
  targetNavigability: 'unspecified'
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: gatewayTakesPayment,
  sourceNodePresentationId: paymentGateway,
  targetNodePresentationId: payOrder
});

var premiumIsCustomer = tools.create_gen({
  subClassId: premiumCustomerId,
  superClassId: customerId
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: premiumIsCustomer,
  sourceNodePresentationId: premiumCustomer,
  targetNodePresentationId: customer
});

// create_include and create_extend take no diagram: they make the model element only.
var includeAuthenticate = tools.create_include({
  includingUsecaseId: placeOrderId,
  includedUsecaseId: authenticateId,
  newIncludeName: ''
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: includeAuthenticate,
  sourceNodePresentationId: placeOrder,
  targetNodePresentationId: authenticate
});

var includePayOrder = tools.create_include({
  includingUsecaseId: placeOrderId,
  includedUsecaseId: payOrderId,
  newIncludeName: ''
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: includePayOrder,
  sourceNodePresentationId: placeOrder,
  targetNodePresentationId: payOrder
});

// An extend runs from the extending use case to the extended one.
var extendApplyDiscount = tools.create_extend({
  extendingUsecaseId: applyDiscountId,
  extendedUsecaseId: placeOrderId,
  newExtendName: ''
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: extendApplyDiscount,
  sourceNodePresentationId: applyDiscount,
  targetNodePresentationId: placeOrder
});


print('Created the use case diagram Online Shop.');
