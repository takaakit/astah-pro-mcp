/*
 * Lists the presentations on a class diagram together with the model elements behind them, by crossing between the presentation IDs and the model element IDs.
 */

// find_named_elements_by_name matches partially, and an exact name is not necessarily unique either,
// so the hits are filtered and then counted rather than taking value[0].
var diagrams = tools.find_named_elements_by_name({ name: 'Class Diagram0' }).value
  .filter(function (e) { return e.name === 'Class Diagram0' && e.type === 'ClassDiagram'; });
if (diagrams.length !== 1) {
  throw new Error('Expected 1 ClassDiagram named "Class Diagram0", found ' + diagrams.length + '.');
}
var diagramId = diagrams[0].id;


// ============================================================
// Both IDs of every presentation on the diagram
// ============================================================

// A presentation and the model element it draws have separate IDs, and each ID argument wants one
// kind or the other. Read it with get_info_of_tools_callable_from_mcp_tool_script. Passing the wrong kind fails with
// "due to an incorrect type", and the message names the kind that was wanted.

// get_prsts_on_dgm carries both IDs, so it crosses a whole diagram in one call.
var presentations = tools.get_prsts_on_dgm({ id: diagramId }).value;

var lines = [];
for (var i = 0; i < presentations.length; i++) {
  var presentation = presentations[i];
  var model = presentation.correspondingModelElement;

  // The frame, a note and a text draw nothing from the model. correspondingModelElement is still
  // there for them, filled with empty strings, so it is the id that tells them apart.
  lines.push(presentation.type + ' ' + presentation.id + ' -> '
    + (model.id === '' ? '(no model element)' : model.type + ' ' + model.id));
}

print(presentations.length + ' presentation(s) on the diagram:');
print(lines.join('\n'));


// ============================================================
// Crossing when only the presentation ID is at hand
// ============================================================

// A create_* tool that draws a model element already hands the model ID back in
// presentation.correspondingModelElement, so nothing needs asking for right after creating. This
// crossing is for a presentation ID that came from somewhere else: get_prsts_on_dgm, a selection, the
// end of a link.
var nodeId = '';
for (var n = 0; n < presentations.length && nodeId === ''; n++) {
  if (presentations[n].type === 'Class') {
    nodeId = presentations[n].id;
  }
}

if (nodeId === '') {
  throw new Error('No class is drawn on the diagram, so there is no node presentation to cross over from.');
}

var classId = tools.get_node_info({ id: nodeId }).presentation.correspondingModelElement.id;

// get_named_element_info reads the model, so it answers for the crossed-over ID. It would have
// failed for the presentation one, even though that ID is perfectly good on the presentation side.
var element = tools.get_named_element_info({ id: classId });

print('');
print('Node ' + nodeId + ' draws ' + element.type + ' ' + element.nameSpace + '.' + element.name);


// ============================================================
// Crossing back, from the model element
// ============================================================

// The same model element can be drawn on several diagrams, so this direction gives a list rather
// than one ID. Ask each presentation which diagram it sits on instead of pairing this list with
// renderedInDiagrams by index: the two are built separately and need not run in step.
var presentationIds = element.element.correspondingPresentationIds;

print('');
print(element.name + ' is drawn ' + presentationIds.length + ' time(s):');
for (var p = 0; p < presentationIds.length; p++) {
  var drawnIn = tools.get_node_info({ id: presentationIds[p] }).presentation.renderedInDiagram;
  print('  ' + presentationIds[p] + ' on ' + drawnIn.type + ' ' + drawnIn.name);
}
