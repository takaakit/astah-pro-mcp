/*
 * Moves the label of a control flow or an object flow, which shows its guard and action together, by an offset.
 */

// A label of a link is not a presentation of its own. Its position is a pair of properties of the link
// presentation, named by PresentationPropertyConstants.Key. A nested class is reached with '$'.
var Key = Java.type('com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants$Key');

// A link presentation ID is unambiguous and takes precedence when supplied.
var linkPresentationId = '';
var diagramName = 'Order Fulfilment';

// The activity nodes the flow leaves and enters. A control node such as a decision or a fork carries the
// name Astah generated for it, and an object node is named without its state, so Order [entered] is Order.
var sourceNodeName = 'Decision Node & Merge Node0';
var targetNodeName = 'ForkNode0';

// How far to move the label, in pixels. Give only the directions it moves in: an omitted one counts as 0.
// The Y axis of a diagram points down, so down adds to Y and up takes from it.
var move = { right: 10, up: 20 };


// ============================================================
// The flow link
// ============================================================

var IDiagram = Java.type('com.change_vision.jude.api.inf.model.IDiagram');
var IFlow = Java.type('com.change_vision.jude.api.inf.model.IFlow');
var ILinkPresentation = Java.type('com.change_vision.jude.api.inf.presentation.ILinkPresentation');

// A control flow and an object flow are both an IFlow, told apart only by the nodes they connect.
function isFlowLink(entity) {
  return entity instanceof ILinkPresentation && entity.getModel() instanceof IFlow;
}

// findElements matches the name exactly, but two diagrams are free to share a name.
function findDiagram(name) {
  var hits = astah.findElements(IDiagram.class, name);
  if (hits.length !== 1) {
    throw new Error('Expected 1 diagram named "' + name + '", found ' + hits.length + '.');
  }
  return hits[0];
}

// Two flows are free to join the same pair of nodes.
function findFlowLink(diagram) {
  var hits = [];
  var presentations = diagram.getPresentations();

  for (var i = 0; i < presentations.length; i++) {
    if (!isFlowLink(presentations[i])) { continue; }

    var model = presentations[i].getModel();
    if (model.getSource().getName() === sourceNodeName && model.getTarget().getName() === targetNodeName) {
      hits.push(presentations[i]);
    }
  }

  if (hits.length !== 1) {
    throw new Error('Expected 1 flow from ' + sourceNodeName + ' to ' + targetNodeName + ' on "'
      + diagram.getName() + '", found ' + hits.length + '. Put the ID of its link presentation in linkPresentationId.');
  }
  return hits[0];
}

// getEntity looks up presentations as well as model elements, and answers null for an ID it does not know.
var link;
if (linkPresentationId !== '') {
  link = astah.getEntity(linkPresentationId);
  if (link === null) {
    throw new Error('No presentation in the current project has ID ' + linkPresentationId + '.');
  }
  if (!isFlowLink(link)) {
    throw new Error(linkPresentationId + ' is not the line of a control flow or an object flow.');
  }
} else {
  link = findFlowLink(findDiagram(diagramName));
}


// ============================================================
// Moving the label
// ============================================================

// A flow has no name of its own: getName answers the label, [guard] / action, made from its guard and its
// action. So the guard and the action are one label with one position, and there is no guard label apart from
// it. A flow with neither shows no label, so there is nothing to move.
var label = link.getLabel();
if (label === '') {
  throw new Error('The flow link ' + link.getID() + ' has neither a guard nor an action, so it shows no label to move.');
}

// A flow keeps no position for its label while its name is hidden, and getProperty answers null. Nor does it in
// the transaction that gave it its guard or action: the label is laid out when that transaction ends.
var x = link.getProperty(Key.NAME_X);
var y = link.getProperty(Key.NAME_Y);
if (x === null || y === null) {
  throw new Error('The flow link ' + link.getID() + ' (label "' + label + '") keeps no label position.'
    + ' Show its name, or end the transaction that set its guard or action before moving the label.');
}

// A position is held as a string, so it is read as a number and written back as a string.
var newX = parseFloat(x) + (move.right || 0) - (move.left || 0);
var newY = parseFloat(y) + (move.down || 0) - (move.up || 0);

var transactionManager = astah.getTransactionManager();
transactionManager.beginTransaction();
try {
  link.setProperty(Key.NAME_X, String(newX));
  link.setProperty(Key.NAME_Y, String(newY));
  transactionManager.endTransaction();
} catch (e) {
  transactionManager.abortTransaction();
  throw e;
}


// A position is kept to many decimal places, so the output rounds it to one.
function rounded(value) {
  return Math.round(value * 10) / 10;
}

print('Moved the label "' + label + '" of the flow link ' + link.getID() + ' from ('
  + rounded(parseFloat(x)) + ', ' + rounded(parseFloat(y)) + ') to (' + rounded(newX) + ', ' + rounded(newY) + ').');
