/*
 * Draws an activity diagram with partitions that each represent a class, an initial and a final node, actions, an object node, decision, merge, fork and join nodes, input and output pins, and the control and object flows between them.
 */

var rootPackageId = tools.get_proj({}).element.id;


// ============================================================
// Classes
// ============================================================

// create_obj_node and create_in_or_out_pin both take a base class, and so does a lane,
// so the classes are created first.
var orderClassId = tools.create_class_in_parent_pkg({
  newClassName: 'Order',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var customerClassId = tools.create_class_in_parent_pkg({
  newClassName: 'Customer',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var salesClassId = tools.create_class_in_parent_pkg({
  newClassName: 'Sales',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var warehouseClassId = tools.create_class_in_parent_pkg({
  newClassName: 'Warehouse',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var diagramId = tools.create_activity_dgm({
  newActivityDiagramName: 'Order Fulfilment',
  parentPackageId: rootPackageId
}).diagram.namedElement.element.id;


// ============================================================
// Partitions
// ============================================================

// create_partition takes no coordinates: each new partition is appended after the previous one.
var customerLane = tools.create_partition({
  isHorizontal: false,
  newPartitionName: 'Customer',
  previousPartitionId: '',
  superPartitionId: '',
  targetActivityDiagramId: diagramId
});
var customer = customerLane.presentation.id;

// set_represents_of_partition takes the model element of the partition rather than its
// presentation, and the lane header then shows the name of the class instead of the lane name.
tools.set_represents_of_partition({
  targetPartitionId: customerLane.presentation.correspondingModelElement.id,
  representsId: customerClassId
});

var salesLane = tools.create_partition({
  isHorizontal: false,
  newPartitionName: 'Sales',
  previousPartitionId: customer,
  superPartitionId: '',
  targetActivityDiagramId: diagramId
});
var sales = salesLane.presentation.id;

tools.set_represents_of_partition({
  targetPartitionId: salesLane.presentation.correspondingModelElement.id,
  representsId: salesClassId
});

var warehouseLane = tools.create_partition({
  isHorizontal: false,
  newPartitionName: 'Warehouse',
  previousPartitionId: sales,
  superPartitionId: '',
  targetActivityDiagramId: diagramId
});
var warehouse = warehouseLane.presentation.id;

tools.set_represents_of_partition({
  targetPartitionId: warehouseLane.presentation.correspondingModelElement.id,
  representsId: warehouseClassId
});

// Give each partition its final position and size before putting anything inside it, because
// resizing a partition moves whatever it already contains. Resizing one does not push the others
// aside either, so every lane is positioned explicitly. A partition starts at y = -35, above which
// its name is drawn.
tools.set_node_prst_location({ nodePresentationId: customer, locationX: 0, locationY: -35 });
tools.set_node_prst_width({ nodePresentationId: customer, width: 200 });
tools.set_node_prst_height({ nodePresentationId: customer, height: 660 });

tools.set_node_prst_location({ nodePresentationId: sales, locationX: 200, locationY: -35 });
tools.set_node_prst_width({ nodePresentationId: sales, width: 300 });
tools.set_node_prst_height({ nodePresentationId: sales, height: 660 });

tools.set_node_prst_location({ nodePresentationId: warehouse, locationX: 500, locationY: -35 });
tools.set_node_prst_width({ nodePresentationId: warehouse, width: 360 });
tools.set_node_prst_height({ nodePresentationId: warehouse, height: 660 });


// ============================================================
// Nodes in the Customer lane
// ============================================================

var initialNode = tools.create_init_node({
  newInitialNodeName: 'start',
  locationX: 90, locationY: 30,
  targetActivityDiagramId: diagramId
}).presentation.id;

var placeOrder = tools.create_act({
  newActionName: 'Place Order',
  locationX: 48, locationY: 100,
  targetActivityDiagramId: diagramId
});
var placeOrderNode = placeOrder.presentation.id;
var placeOrderRect = placeOrder.drawnRectangle;

// A pin snaps to the border of its parent action nearest the given point.
// Passing the right edge of the action puts this output pin there.
var placedOrderPin = tools.create_in_or_out_pin({
  baseClassId: orderClassId,
  isInput: false,
  newPinName: 'placedOrder',
  locationX: Math.round(placeOrderRect.x + placeOrderRect.width),
  locationY: Math.round(placeOrderRect.y + placeOrderRect.height / 2 - 7),
  parentActionId: placeOrderNode,
  targetActivityDiagramId: diagramId
}).presentation.id;


// ============================================================
// Nodes in the Sales lane
// ============================================================

var orderObjectNode = tools.create_obj_node({
  baseClassId: orderClassId,
  newObjectNodeName: 'order',
  locationX: 306, locationY: 180,
  targetActivityDiagramId: diagramId
}).presentation.id;

// An object node is placed near, but not exactly at, the given point, so set its location again.
tools.set_node_prst_location({
  nodePresentationId: orderObjectNode,
  locationX: 306, locationY: 180
});

var validateOrder = tools.create_act({
  newActionName: 'Validate Order',
  locationX: 291, locationY: 260,
  targetActivityDiagramId: diagramId
});
var validateOrderNode = validateOrder.presentation.id;
var validateOrderRect = validateOrder.drawnRectangle;

// Passing the top edge of the action puts this input pin there.
var targetOrderPin = tools.create_in_or_out_pin({
  baseClassId: orderClassId,
  isInput: true,
  newPinName: 'targetOrder',
  locationX: Math.round(validateOrderRect.x + validateOrderRect.width / 2 - 7),
  locationY: Math.round(validateOrderRect.y),
  parentActionId: validateOrderNode,
  targetActivityDiagramId: diagramId
}).presentation.id;

// create_decision_merge_node makes both: a decision node and a merge node are the same element.
var decisionNode = tools.create_decision_merge_node({
  locationX: 335, locationY: 330,
  targetActivityDiagramId: diagramId
}).presentation.id;

var mergeNode = tools.create_decision_merge_node({
  locationX: 335, locationY: 530,
  targetActivityDiagramId: diagramId
}).presentation.id;

var finalNode = tools.create_final_node({
  newFinalNodeName: 'end',
  locationX: 340, locationY: 580,
  targetActivityDiagramId: diagramId
}).presentation.id;


// ============================================================
// Nodes in the Warehouse lane
// ============================================================

// A fork or join bar is created 50 wide, whatever it has to span, so widen it explicitly.
var forkNode = tools.create_fork_node({
  locationX: 610, locationY: 390,
  targetActivityDiagramId: diagramId
}).presentation.id;
tools.set_node_prst_width({ nodePresentationId: forkNode, width: 140 });

var pickItems = tools.create_act({
  newActionName: 'Pick Items',
  locationX: 562, locationY: 430,
  targetActivityDiagramId: diagramId
}).presentation.id;

var printInvoice = tools.create_act({
  newActionName: 'Print Invoice',
  locationX: 697, locationY: 430,
  targetActivityDiagramId: diagramId
}).presentation.id;

var joinNode = tools.create_join_node({
  locationX: 610, locationY: 490,
  targetActivityDiagramId: diagramId
}).presentation.id;
tools.set_node_prst_width({ nodePresentationId: joinNode, width: 140 });


// ============================================================
// Flows
// ============================================================

// create_flow makes a control flow or an object flow according to what it connects:
// a flow that starts or ends at a pin or an object node carries objects, the rest carry control.
// Each flow is then given a right-angle line, which is how an activity diagram draws them.

var startFlow = tools.create_flow({
  sourceNodePresentationId: initialNode,
  targetNodePresentationId: placeOrderNode,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: startFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var placedOrderFlow = tools.create_flow({
  sourceNodePresentationId: placedOrderPin,
  targetNodePresentationId: orderObjectNode,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: placedOrderFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

// Astah routes a flow from the centre of one node to the centre of the other. Where that path is
// not wanted, give the flow one of its own. The first and the last point must lie inside the node
// presentations the flow connects, not on their borders.
tools.set_points_of_link_prst({
  targetLinkPresentationId: placedOrderFlow.presentation.id,
  drawPoints: [{ x: 160, y: 112 }, { x: 350, y: 112 }, { x: 350, y: 192 }]
});

var targetOrderFlow = tools.create_flow({
  sourceNodePresentationId: orderObjectNode,
  targetNodePresentationId: targetOrderPin,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: targetOrderFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var validatedFlow = tools.create_flow({
  sourceNodePresentationId: validateOrderNode,
  targetNodePresentationId: decisionNode,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: validatedFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var validFlow = tools.create_flow({
  sourceNodePresentationId: decisionNode,
  targetNodePresentationId: forkNode,
  targetActivityDiagramId: diagramId
});
tools.set_guard_of_flow({
  targetFlowId: validFlow.presentation.correspondingModelElement.id,
  guard: 'valid'
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: validFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

// Two flows leaving the same node share their first segment unless one is given its own path.
tools.set_points_of_link_prst({
  targetLinkPresentationId: validFlow.presentation.id,
  drawPoints: [{ x: 360, y: 340 }, { x: 680, y: 340 }, { x: 680, y: 392 }]
});

var pickItemsFlow = tools.create_flow({
  sourceNodePresentationId: forkNode,
  targetNodePresentationId: pickItems,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: pickItemsFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var printInvoiceFlow = tools.create_flow({
  sourceNodePresentationId: forkNode,
  targetNodePresentationId: printInvoice,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: printInvoiceFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var pickedFlow = tools.create_flow({
  sourceNodePresentationId: pickItems,
  targetNodePresentationId: joinNode,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: pickedFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var printedFlow = tools.create_flow({
  sourceNodePresentationId: printInvoice,
  targetNodePresentationId: joinNode,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: printedFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var joinedFlow = tools.create_flow({
  sourceNodePresentationId: joinNode,
  targetNodePresentationId: mergeNode,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: joinedFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var invalidFlow = tools.create_flow({
  sourceNodePresentationId: decisionNode,
  targetNodePresentationId: mergeNode,
  targetActivityDiagramId: diagramId
});
tools.set_guard_of_flow({
  targetFlowId: invalidFlow.presentation.correspondingModelElement.id,
  guard: 'invalid'
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: invalidFlow.presentation.id,
  lineStyle: 'line_right_angle'
});

var endFlow = tools.create_flow({
  sourceNodePresentationId: mergeNode,
  targetNodePresentationId: finalNode,
  targetActivityDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: endFlow.presentation.id,
  lineStyle: 'line_right_angle'
});


print('Created the activity diagram Order Fulfilment.');
