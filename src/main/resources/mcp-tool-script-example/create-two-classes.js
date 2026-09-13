/*
 * Creates two classes with definitions, attributes, operations and parameters, then places them on a class diagram.
 */

var rootPackageId = tools.get_proj({}).element.id;
// find_named_elements_by_name matches partially and value[0] need not be the exact match.
var diagramId = tools.find_named_elements_by_name({ name: 'Class Diagram0' }).value
  .filter(function (e) { return e.name === 'Class Diagram0' && e.type === 'ClassDiagram'; })[0].id;


// ============================================================
// Money
// ============================================================

// Create the class.
var money = tools.create_class_in_parent_pkg({
  newClassName: 'Money',
  parentPackageId: rootPackageId
});
var moneyId = money.namedElement.element.id;

// Set the definition.
tools.set_definition({
  targetNamedElementId: moneyId,
  definition: 'A value object holding an amount in the minor currency unit.'
});

// Create an attribute. Its type defaults to int, which is what we want here.
tools.create_attr({
  newAttributeName: 'amount',
  parentClassId: moneyId
});

// Create an operation. Its return type defaults to void.
var isGreaterThan = tools.create_ope({
  newOperationName: 'isGreaterThan',
  parentClassId: moneyId
});
var isGreaterThanId = isGreaterThan.namedElement.element.id;

// Change the return type to a primitive type.
tools.set_return_type_expression_of_ope({
  targetOperationId: isGreaterThanId,
  returnTypeExpression: 'boolean'
});

// Create a parameter and set its type. Its type defaults to int.
var other = tools.create_param({
  newParameterName: 'other',
  targetOperationId: isGreaterThanId
});
tools.set_type_of_param({
  targetParameterId: other.namedElement.element.id,
  parameterTypeId: moneyId
});


// ============================================================
// OrderLine
// ============================================================

var orderLine = tools.create_class_in_parent_pkg({
  newClassName: 'OrderLine',
  parentPackageId: rootPackageId
});
var orderLineId = orderLine.namedElement.element.id;

tools.set_definition({
  targetNamedElementId: orderLineId,
  definition: 'One item line of an order. Holds the quantity and the unit price.'
});

tools.create_attr({
  newAttributeName: 'quantity',
  parentClassId: orderLineId
});

var unitPrice = tools.create_attr({
  newAttributeName: 'unitPrice',
  parentClassId: orderLineId
});

// set_type_of_attr takes the model element of the type, so it needs a class rather than a name.
tools.set_type_of_attr({
  targetAttributeId: unitPrice.namedElement.element.id,
  attributeTypeId: moneyId
});

var lineDiscount = tools.create_attr({
  newAttributeName: 'lineDiscount',
  parentClassId: orderLineId
});

tools.set_type_of_attr({
  targetAttributeId: lineDiscount.namedElement.element.id,
  attributeTypeId: moneyId
});

var calculateSubtotal = tools.create_ope({
  newOperationName: 'calculateSubtotalMinorUnits',
  parentClassId: orderLineId
});

tools.set_return_type_expression_of_ope({
  targetOperationId: calculateSubtotal.namedElement.element.id,
  returnTypeExpression: 'long'
});

var exceedsBudget = tools.create_ope({
  newOperationName: 'exceedsBudget',
  parentClassId: orderLineId
});
var exceedsBudgetId = exceedsBudget.namedElement.element.id;

tools.set_return_type_expression_of_ope({
  targetOperationId: exceedsBudgetId,
  returnTypeExpression: 'boolean'
});

var budget = tools.create_param({
  newParameterName: 'budget',
  targetOperationId: exceedsBudgetId
});
tools.set_type_of_param({
  targetParameterId: budget.namedElement.element.id,
  parameterTypeId: moneyId
});


// ============================================================
// Place the classes on the class diagram.
// ============================================================

tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: orderLineId,
  locationX: 40,
  locationY: 40
});

tools.create_node_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: moneyId,
  locationX: 380,
  locationY: 40
});


print('Created Money and OrderLine, and placed them on the class diagram.');
