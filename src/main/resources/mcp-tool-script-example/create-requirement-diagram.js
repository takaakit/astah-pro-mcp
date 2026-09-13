/*
 * Draws a requirement diagram with requirements and a test case, joined by nesting, deriveReqt, copy, satisfy, verify, refine and trace lines.
 */

var rootPackageId = tools.get_proj({}).element.id;

var diagramId = tools.create_req_dgm({
  newDiagramName: 'Checkout Requirements',
  targetPackageId: rootPackageId
}).namedElement.element.id;


// ============================================================
// Requirements
// ============================================================

// create_req_in_parent_pkg wraps the model element in class, as create_usecase does.
// The identifier and the text are both displayed in the box, so set them before placing the
// requirement: doing it afterwards resizes the box and moves it off the given coordinates.
var checkoutId = tools.create_req_in_parent_pkg({
  newRequirementName: 'Checkout',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;
tools.set_req_id({ id: checkoutId, requirementId: 'REQ-001' });
tools.set_req_text({ id: checkoutId, requirementText: 'The shop shall support checkout.' });

var checkout = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: checkoutId,
  locationX: 444, locationY: 40
}).presentation.id;

// create_req_in_parent_req is the model side of a requirement hierarchy.
// The nesting line drawn on the diagram is a separate presentation, made further down.
var paymentId = tools.create_req_in_parent_req({
  newRequirementName: 'Payment',
  parentRequirementId: checkoutId
}).class.namedElement.element.id;
tools.set_req_id({ id: paymentId, requirementId: 'REQ-002' });
tools.set_req_text({ id: paymentId, requirementText: 'The shop shall accept payment.' });

var payment = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: paymentId,
  locationX: 284, locationY: 200
}).presentation.id;

var discountId = tools.create_req_in_parent_req({
  newRequirementName: 'Discount',
  parentRequirementId: checkoutId
}).class.namedElement.element.id;
tools.set_req_id({ id: discountId, requirementId: 'REQ-003' });
tools.set_req_text({ id: discountId, requirementText: 'The shop shall apply a discount.' });

var discount = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: discountId,
  locationX: 604, locationY: 200
}).presentation.id;

var cardPaymentId = tools.create_req_in_parent_pkg({
  newRequirementName: 'CardPayment',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;
tools.set_req_id({ id: cardPaymentId, requirementId: 'REQ-004' });
tools.set_req_text({ id: cardPaymentId, requirementText: 'The shop shall accept a credit card.' });

var cardPayment = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: cardPaymentId,
  locationX: 284, locationY: 420
}).presentation.id;

var paymentSpecCopyId = tools.create_req_in_parent_pkg({
  newRequirementName: 'PaymentSpecCopy',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;
tools.set_req_id({ id: paymentSpecCopyId, requirementId: 'REQ-005' });
tools.set_req_text({ id: paymentSpecCopyId, requirementText: 'The shop shall accept payment.' });

var paymentSpecCopy = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: paymentSpecCopyId,
  locationX: 66, locationY: 200
}).presentation.id;


// ============================================================
// The elements the requirements are related to
// ============================================================

// A requirement diagram can hold any classifier alongside its requirements and test cases.
var payOrderId = tools.create_usecase({
  newUsecaseName: 'Pay Order',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;

var payOrder = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: payOrderId,
  locationX: 500, locationY: 380
}).presentation.id;

var paymentServiceId = tools.create_class_in_parent_pkg({
  newClassName: 'PaymentService',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var paymentService = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: paymentServiceId,
  locationX: 194, locationY: 590
}).presentation.id;

// create_test_case_in_parent_pkg wraps the model element in class as well.
var cardPaymentTestId = tools.create_test_case_in_parent_pkg({
  newTestCaseName: 'CardPaymentTest',
  parentPackageId: rootPackageId
}).class.namedElement.element.id;

var cardPaymentTest = tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: cardPaymentTestId,
  locationX: 389, locationY: 590
}).presentation.id;


// ============================================================
// Nesting lines
// ============================================================

// A nesting line is a presentation only: it draws the hierarchy that already exists in the model,
// so it needs no relationship element of its own.
tools.create_nesting_link_prst_on_dgm({
  targetDiagramId: diagramId,
  parentNodePresentationId: checkout,
  childNodePresentationId: payment
});

tools.create_nesting_link_prst_on_dgm({
  targetDiagramId: diagramId,
  parentNodePresentationId: checkout,
  childNodePresentationId: discount
});


// ============================================================
// Dependency lines
// ============================================================

// Each of these makes the model element only, so the line is drawn with create_link_prst_on_dgm.
var deriveCardPayment = tools.create_derive_req_dep({
  sourceRequirementId: cardPaymentId,
  targetRequirementId: paymentId
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: deriveCardPayment,
  sourceNodePresentationId: cardPayment,
  targetNodePresentationId: payment
});

var copyPaymentSpec = tools.create_copy_dep({
  sourceRequirementId: paymentSpecCopyId,
  targetRequirementId: paymentId
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: copyPaymentSpec,
  sourceNodePresentationId: paymentSpecCopy,
  targetNodePresentationId: payment
});

// satisfy and refine take any named element as the source, verify takes a test case.
var satisfyCardPayment = tools.create_satisfy_dep({
  sourceNamedElementId: paymentServiceId,
  targetRequirementId: cardPaymentId
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: satisfyCardPayment,
  sourceNodePresentationId: paymentService,
  targetNodePresentationId: cardPayment
});

var verifyCardPayment = tools.create_verify_dep({
  sourceTestCaseId: cardPaymentTestId,
  targetRequirementId: cardPaymentId
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: verifyCardPayment,
  sourceNodePresentationId: cardPaymentTest,
  targetNodePresentationId: cardPayment
});

var refinePayment = tools.create_refine_dep({
  sourceNamedElementId: payOrderId,
  targetRequirementId: paymentId
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: refinePayment,
  sourceNodePresentationId: payOrder,
  targetNodePresentationId: payment
});

// trace is the only one of these whose target need not be a requirement.
var tracePayOrder = tools.create_trace_dep({
  sourceNamedElementId: payOrderId,
  targetNamedElementId: discountId
}).namedElement.element.id;

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: tracePayOrder,
  sourceNodePresentationId: payOrder,
  targetNodePresentationId: discount
});


print('Created the requirement diagram Checkout Requirements.');
