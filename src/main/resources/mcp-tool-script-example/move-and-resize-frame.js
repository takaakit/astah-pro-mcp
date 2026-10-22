/*
 * Moves and resizes the frame of a class diagram, shrinking it in the steps Astah accepts.
 */

// find_named_elements_by_name matches partially, and an exact name is not necessarily unique either,
// so the hits are filtered and then counted rather than taking value[0].
var diagrams = tools.find_named_elements_by_name({ name: 'Class Diagram0' }).value
  .filter(function (e) { return e.name === 'Class Diagram0' && e.type === 'ClassDiagram'; });
if (diagrams.length !== 1) {
  throw new Error('Expected 1 ClassDiagram named "Class Diagram0", found ' + diagrams.length + '.');
}
var diagramId = diagrams[0].id;

// The rectangle the frame is to end up with. It must be at least 320 x 240 and leave room around what
// is drawn on the diagram: about 30 pixels on the left, 41 above and 15 on the right and below.
// Otherwise Astah refuses one of the steps below, and the whole run is rolled back.
var target = { x: 20, y: 20, width: 600, height: 400 };


// ============================================================
// The frame
// ============================================================

// The frame is a presentation with no model element behind it. A class diagram has one from the start,
// but not every diagram does: a mind map, for one, has none. The frame of another kind of diagram
// moves and resizes the same way, except on a sequence diagram and a communication diagram: Astah
// refuses to move or resize the frame of either, and the frame of a sequence diagram grows by itself
// instead, to take in what is drawn outside it.
var frames = tools.get_prsts_on_dgm({ id: diagramId }).value
  .filter(function (p) { return p.type === 'Frame'; });
if (frames.length !== 1) {
  throw new Error('Expected 1 frame on the diagram, found ' + frames.length + '.');
}
var frameId = frames[0].id;

var before = tools.get_node_prst_rectangle({ id: frameId });


// ============================================================
// Growing
// ============================================================

// Moving the frame is never refused, and it leaves what is drawn inside it where it is. Resizing is
// refused while the frame does not enclose the drawing, so the frame grows at the target, which does.
var rect = tools.set_node_prst_location({ nodePresentationId: frameId, locationX: target.x, locationY: target.y });

if (target.width > rect.width) {
  rect = tools.set_node_prst_width({ nodePresentationId: frameId, width: target.width });
}
if (target.height > rect.height) {
  rect = tools.set_node_prst_height({ nodePresentationId: frameId, height: target.height });
}


// ============================================================
// Shrinking
// ============================================================

// A smaller width moves the right edge in, but Astah also checks the frame as if its left edge had
// moved in by as much instead, so a frame left where it is hardly shrinks at all. Each step therefore
// keeps half of the extra width and shifts the frame left of the target by that half: the right edge
// then lands on the right edge of the target, and the left edge Astah checks on its left edge, where
// the target leaves room. A height works the same way, with the top edge. The last step keeps
// nothing, so it leaves the frame at the target.
// Halving takes about 11 steps for 1000 pixels, so the cap only stops a run that is going nowhere.
for (var step = 0; rect.width > target.width || rect.height > target.height; step++) {
  if (step === 32) {
    throw new Error('The frame is still ' + rect.width + ' x ' + rect.height + ' after ' + step + ' steps.');
  }

  var keptWidth = Math.floor((rect.width - target.width) / 2);
  var keptHeight = Math.floor((rect.height - target.height) / 2);

  tools.set_node_prst_location({
    nodePresentationId: frameId,
    locationX: target.x - keptWidth,
    locationY: target.y - keptHeight
  });

  if (rect.width > target.width) {
    rect = tools.set_node_prst_width({ nodePresentationId: frameId, width: target.width + keptWidth });
  }
  if (rect.height > target.height) {
    rect = tools.set_node_prst_height({ nodePresentationId: frameId, height: target.height + keptHeight });
  }
}


function rectangleText(r) {
  return '(' + r.x + ', ' + r.y + ') ' + r.width + ' x ' + r.height;
}

print('Moved the frame of Class Diagram0 from ' + rectangleText(before) + ' to ' + rectangleText(rect) + '.');
