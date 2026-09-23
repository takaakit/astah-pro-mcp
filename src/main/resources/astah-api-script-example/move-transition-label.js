/*
 * Moves the label of a transition link, which shows its trigger, guard and action together, by an offset.
 */

// A label of a link is not a presentation of its own. Its position is a pair of properties of the link
// presentation, named by PresentationPropertyConstants.Key. A nested class is reached with '$'.
var Key = Java.type('com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants$Key');

// A link presentation ID is unambiguous and takes precedence when supplied.
var linkPresentationId = '';
var diagramName = 'Media Player';

var sourceStateName = 'Idle';
var targetStateName = 'Playing';

// How far to move the label, in pixels. Give only the directions it moves in: an omitted one counts as 0.
// The Y axis of a diagram points down, so down adds to Y and up takes from it.
var move = { right: 10, up: 20 };


// ============================================================
// The transition link
// ============================================================

var IDiagram = Java.type('com.change_vision.jude.api.inf.model.IDiagram');
var ITransition = Java.type('com.change_vision.jude.api.inf.model.ITransition');
var ILinkPresentation = Java.type('com.change_vision.jude.api.inf.presentation.ILinkPresentation');

// The link of a flow, a message or an association keeps its label position under the same keys, so only
// the line of a transition is taken.
function isTransitionLink(entity) {
  return entity instanceof ILinkPresentation && entity.getModel() instanceof ITransition;
}

// findElements matches the name exactly, but two diagrams are free to share a name.
function findDiagram(name) {
  var hits = astah.findElements(IDiagram.class, name);
  if (hits.length !== 1) {
    throw new Error('Expected 1 diagram named "' + name + '", found ' + hits.length + '.');
  }
  return hits[0];
}

// Two transitions are free to join the same pair of states, one for each trigger.
function findTransitionLink(diagram) {
  var hits = [];
  var presentations = diagram.getPresentations();

  for (var i = 0; i < presentations.length; i++) {
    if (!isTransitionLink(presentations[i])) { continue; }

    var model = presentations[i].getModel();
    if (model.getSource().getName() === sourceStateName && model.getTarget().getName() === targetStateName) {
      hits.push(presentations[i]);
    }
  }

  if (hits.length !== 1) {
    throw new Error('Expected 1 transition from ' + sourceStateName + ' to ' + targetStateName + ' on "'
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

  if (!isTransitionLink(link)) {
    throw new Error(linkPresentationId + ' is not the line of a transition.');
  }
} else {
  link = findTransitionLink(findDiagram(diagramName));
}


// ============================================================
// Moving the label
// ============================================================

// A transition with an empty label keeps no position for it, and getProperty answers null.
var x = link.getProperty(Key.NAME_X);
var y = link.getProperty(Key.NAME_Y);
if (x === null || y === null) {
  throw new Error('The ' + link.getType() + ' link ' + link.getID() + ' (label "' + link.getLabel() + '")'
    + ' keeps no label position. A transition keeps one only while its label has something in it.');
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

print('Moved the label "' + link.getLabel() + '" of the ' + link.getType() + ' link ' + link.getID() + ' from ('
  + rounded(parseFloat(x)) + ', ' + rounded(parseFloat(y)) + ') to (' + rounded(newX) + ', ' + rounded(newY) + ').');
