/*
 * Creates an association between two existing classes, sets the association name, the role names and the multiplicities, then draws it on a class diagram.
 */

// find_named_elements_by_name matches the name partially, and hits are in the project's internal order,
// so value[0] can be another element: 'State' hands back a StateInvariant, not the State class.
function findIdByExactName(name, type) {
  var hits = tools.find_named_elements_by_name({ name: name }).value.filter(function (e) {
    return e.name === name && e.type === type;
  });
  if (hits.length !== 1) {
    throw new Error('Expected 1 ' + type + ' named "' + name + '", found ' + hits.length + '.');
  }
  return hits[0].id;
}

var diagramId = findIdByExactName('Class Diagram0', 'ClassDiagram');
var sourceClassId = findIdByExactName('Class0', 'Class');
var targetClassId = findIdByExactName('Class1', 'Class');


// ============================================================
// Create the association
// ============================================================

// Association end A is on the source class side, and end B is on the target class side.
var association = tools.create_asso({
  sourceClassId: sourceClassId,
  targetClassId: targetClassId,
  sourceAggregationKind: 'none',
  sourceNavigability: 'unspecified',
  targetAggregationKind: 'none',
  targetNavigability: 'navigable'
});
var associationId = association.namedElement.element.id;
var endAId = association.associationEndA.id;
var endBId = association.associationEndB.id;

tools.set_name({
  targetNamedElementId: associationId,
  name: 'owns'
});

// The role name of an association end is the name of the end itself, so set_name serves both.
tools.set_name({
  targetNamedElementId: endAId,
  name: 'owner'
});
tools.set_name({
  targetNamedElementId: endBId,
  name: 'item'
});

// A multiplicity is given as a lower and an upper value. To write a single value such as 1,
// set the lower one and leave the upper one empty.
tools.set_multiplicity_of_asso_end_a({
  targetAttributeId: endAId,
  lowerMultiplicity: '1',
  upperMultiplicity: ''
});

tools.set_multiplicity_of_asso_end_b({
  targetAttributeId: endBId,
  lowerMultiplicity: '0',
  upperMultiplicity: '*'
});


// ============================================================
// Draw the association on the class diagram
// ============================================================

// A link presentation connects the node presentations of the two classes, so look them up first.
var sourceNodeId = tools.get_class_info({ id: sourceClassId }).namedElement.element.correspondingPresentationIds[0];
var targetNodeId = tools.get_class_info({ id: targetClassId }).namedElement.element.correspondingPresentationIds[0];

tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: associationId,
  sourceNodePresentationId: sourceNodeId,
  targetNodePresentationId: targetNodeId
});


print('Created the association and drew it on the class diagram.');
