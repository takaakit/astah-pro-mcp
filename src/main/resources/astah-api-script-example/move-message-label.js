/*
 * Moves the label of a message on a sequence diagram sideways by an offset.
 */

// A label of a link is not a presentation of its own. Its position is a pair of properties of the link
// presentation, named by PresentationPropertyConstants.Key. A nested class is reached with '$'.
var Key = Java.type('com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants$Key');

// A link presentation ID is unambiguous and takes precedence when supplied.
var linkPresentationId = '';
var diagramName = 'Checkout';

// The sequence number shown in front of the message name, such as 3.1.2 or 3.1.2.1-A, which getIndex answers.
// Two messages are free to share a name and both lifelines, as a call made twice does, so the number is what
// tells them apart. A return message has no number, so give the ID of its link presentation instead.
var messageIndex = '1.2';

// How far to move the label, in pixels: give right or left. An omitted one counts as 0.
var move = { right: 40 };


// ============================================================
// The message link
// ============================================================

var IDiagram = Java.type('com.change_vision.jude.api.inf.model.IDiagram');
var IMessage = Java.type('com.change_vision.jude.api.inf.model.IMessage');
var ILinkPresentation = Java.type('com.change_vision.jude.api.inf.presentation.ILinkPresentation');

// The destroy mark at the foot of a lifeline is a node presentation of the destroy message itself, and a message
// on a communication diagram is a node presentation too, so only the line of a message is taken.
function isMessageLink(entity) {
  return entity instanceof ILinkPresentation && entity.getModel() instanceof IMessage;
}

// findElements matches the name exactly, but two diagrams are free to share a name.
function findDiagram(name) {
  var hits = astah.findElements(IDiagram.class, name);
  if (hits.length !== 1) {
    throw new Error('Expected 1 diagram named "' + name + '", found ' + hits.length + '.');
  }
  return hits[0];
}

function findMessageLink(diagram) {
  var hits = [];
  var presentations = diagram.getPresentations();

  for (var i = 0; i < presentations.length; i++) {
    if (!isMessageLink(presentations[i])) { continue; }

    if (presentations[i].getModel().getIndex() === messageIndex) {
      hits.push(presentations[i]);
    }
  }

  if (hits.length !== 1) {
    throw new Error('Expected 1 message numbered ' + messageIndex + ' on "' + diagram.getName() + '", found '
      + hits.length + '. Put the ID of its link presentation in linkPresentationId.');
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

  // The link of a transition or an association keeps its label position under the same keys, so an ID of
  // another kind is refused rather than having its label moved in place of a message's.
  if (!isMessageLink(link)) {
    throw new Error(linkPresentationId + ' is not the line of a message on a sequence diagram.');
  }
} else {
  link = findMessageLink(findDiagram(diagramName));
}

var index = link.getModel().getIndex();
var description = (index === null ? 'unnumbered message' : 'message ' + index) + ' link ' + link.getID();


// ============================================================
// Moving the label
// ============================================================

// A message keeps no label position while its label shows no name -- a return message left without one, or a
// message whose name is hidden -- and getProperty answers null. A stereotype such as create is drawn as part of
// the label, so it keeps no position of its own either: it moves along with the name.
var x = link.getProperty(Key.NAME_X);
if (x === null) {
  throw new Error('The ' + description + ' (label "' + link.getLabel() + '") keeps no label position.'
    + ' A message keeps one only while its label shows its name.');
}

// A position is held as a string, so it is read as a number and written back as a string. The label moves
// sideways only, so NAME_Y is left as it is.
var newX = parseFloat(x) + (move.right || 0) - (move.left || 0);

var transactionManager = astah.getTransactionManager();
transactionManager.beginTransaction();
try {
  link.setProperty(Key.NAME_X, String(newX));
  transactionManager.endTransaction();
} catch (e) {
  transactionManager.abortTransaction();
  throw e;
}


// A position is kept to many decimal places, so the output rounds it to one.
function rounded(value) {
  return Math.round(value * 10) / 10;
}

print('Moved the label "' + link.getLabel() + '" of the ' + description + ' from x = ' + rounded(parseFloat(x))
  + ' to x = ' + rounded(newX) + '.');
