/*
 * Moves the labels of an association link -- its name, the role names, the multiplicities, the constraints and the stereotypes -- each by an offset of its own.
 */

// A label of a link is not a presentation of its own. Its position is a pair of properties of the link
// presentation, named by PresentationPropertyConstants.Key. A nested class is reached with '$'.
var Key = Java.type('com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants$Key');

// On a class diagram only the link of an association, or of an association class, keeps these positions.
var labels = {
  name: { x: Key.NAME_X, y: Key.NAME_Y },
  endAName: { x: Key.END_A_NAME_X, y: Key.END_A_NAME_Y },
  endBName: { x: Key.END_B_NAME_X, y: Key.END_B_NAME_Y },
  endAMultiplicity: { x: Key.END_A_MULTIPLICITY_X, y: Key.END_A_MULTIPLICITY_Y },
  endBMultiplicity: { x: Key.END_B_MULTIPLICITY_X, y: Key.END_B_MULTIPLICITY_Y },
  constraint: { x: Key.CONSTRAINT_X, y: Key.CONSTRAINT_Y },
  endAConstraint: { x: Key.END_A_CONSTRAINT_X, y: Key.END_A_CONSTRAINT_Y },
  endBConstraint: { x: Key.END_B_CONSTRAINT_X, y: Key.END_B_CONSTRAINT_Y }
};

// Each stereotype has a pair of its own, counting from 0, so its keys are made from a format.
function stereotypeLabel(index) {
  return {
    x: Key.STEREOTYPE_X_FORMAT.replace('{0}', index),
    y: Key.STEREOTYPE_Y_FORMAT.replace('{0}', index)
  };
}

// A link presentation ID is unambiguous and takes precedence when supplied.
var linkPresentationId = '';
var diagramName = 'Class Diagram0';

// End A and end B are fixed when the association is made, not by the way the line is drawn, and the
// labels of an end stay with that end, so the class names are matched in this order.
var endAClassName = 'Class0';
var endBClassName = 'Class1';

// How far to move each label, in pixels. Give only the directions a label moves in: an omitted one counts
// as 0. The Y axis of a diagram points down, so down adds to Y and up takes from it.
var moves = [
  { label: labels.endAName, right: 10, down: 20 },
  { label: labels.endAMultiplicity, right: 5, up: 15 }
];


// ============================================================
// The association link
// ============================================================

var IDiagram = Java.type('com.change_vision.jude.api.inf.model.IDiagram');
var IAssociation = Java.type('com.change_vision.jude.api.inf.model.IAssociation');
var ILinkPresentation = Java.type('com.change_vision.jude.api.inf.presentation.ILinkPresentation');

// An association class is drawn as a node and as a link at once, and both answer to IAssociation, so only
// the line is taken.
function isAssociationLink(entity) {
  return entity instanceof ILinkPresentation && entity.getModel() instanceof IAssociation;
}

// findElements matches the name exactly, but two diagrams are free to share a name.
function findDiagram(name) {
  var hits = astah.findElements(IDiagram.class, name);
  if (hits.length !== 1) {
    throw new Error('Expected 1 diagram named "' + name + '", found ' + hits.length + '.');
  }
  return hits[0];
}

// getMemberEnds answers end A first and end B second, and the type of an end is the class at that end.
function findAssociationLink(diagram) {
  var hits = [];
  var presentations = diagram.getPresentations();

  for (var i = 0; i < presentations.length; i++) {
    if (!isAssociationLink(presentations[i])) { continue; }

    var ends = presentations[i].getModel().getMemberEnds();
    if (ends[0].getType().getName() === endAClassName && ends[1].getType().getName() === endBClassName) {
      hits.push(presentations[i]);
    }
  }

  if (hits.length !== 1) {
    throw new Error('Expected 1 association from ' + endAClassName + ' to ' + endBClassName + ' on "'
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

  if (!isAssociationLink(link)) {
    throw new Error(linkPresentationId + ' is not the line of an association.');
  }
} else {
  link = findAssociationLink(findDiagram(diagramName));
}


// ============================================================
// Moving the labels
// ============================================================

// A position is kept to many decimal places, so the output rounds it to one.
function rounded(value) {
  return Math.round(value * 10) / 10;
}

var moved = [];
var skipped = [];

var transactionManager = astah.getTransactionManager();
transactionManager.beginTransaction();
try {
  for (var m = 0; m < moves.length; m++) {
    var move = moves[m];
    if (!move.label) {
      throw new Error('moves[' + m + '] names no label. Use one of labels, or stereotypeLabel(index).');
    }

    // The label name is the key without its trailing '.x', such as end_a.name.point.
    var labelName = move.label.x.replace(/\.x$/, '');

    // A label that is not shown -- a name left empty, an end with no multiplicity -- keeps no position,
    // and getProperty answers null for it. There is nothing on the diagram to move, so it is skipped.
    var x = link.getProperty(move.label.x);
    var y = link.getProperty(move.label.y);
    if (x === null || y === null) {
      skipped.push(labelName);
      continue;
    }

    // A position is held as a string, so it is read as a number and written back as a string.
    var newX = parseFloat(x) + (move.right || 0) - (move.left || 0);
    var newY = parseFloat(y) + (move.down || 0) - (move.up || 0);
    link.setProperty(move.label.x, String(newX));
    link.setProperty(move.label.y, String(newY));

    moved.push('  ' + labelName + ': (' + rounded(parseFloat(x)) + ', ' + rounded(parseFloat(y)) + ') -> ('
      + rounded(newX) + ', ' + rounded(newY) + ')');
  }

  // An association that shows none of these labels keeps no position for them, so a run that moved
  // nothing is reported as the wrong choice of labels rather than as done.
  if (moved.length === 0) {
    throw new Error('The ' + link.getType() + ' link ' + link.getID() + ' shows none of the labels to move.'
      + ' Only the link of an association keeps label positions, and only for the labels it shows.');
  }

  transactionManager.endTransaction();
} catch (e) {
  transactionManager.abortTransaction();
  throw e;
}


print('Moved ' + moved.length + ' label(s) of the ' + link.getType() + ' link ' + link.getID() + ':');
print(moved.join('\n'));
if (skipped.length > 0) {
  print('Skipped ' + skipped.length + ' label(s) the link does not show: ' + skipped.join(', '));
}
