/*
 * Lists the presentations on a class diagram together with the model elements behind them, by crossing between the presentation IDs and the model element IDs.
 */

// find_named_elements_by_name matches partially and value[0] need not be the exact match.
var diagramId = tools.find_named_elements_by_name({ name: 'Class Diagram0' }).value
  .filter(function (e) { return e.name === 'Class Diagram0' && e.type === 'ClassDiagram'; })[0].id;


// ============================================================
// Both IDs of every presentation on the diagram
// ============================================================

// A presentation and the model element it draws have separate IDs, and a tool function takes one of
// the two only. Whatever draws, moves, sizes or colours something takes a presentation ID, as
// create_link_prst_on_dgm, set_node_prst_location, set_length_of_lifeline and change_fill_color do.
// Whatever reads or edits the model takes a model element ID, as every get_*_info tool, set_name and
// delete_elem do.
// Passing the wrong one fails with "due to an incorrect type".

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

// A create_* tool that draws something hands back a presentation on its own, so the model element
// has to be asked for. get_node_info crosses over for a node, and get_link_prst_info for a link.
var nodeId = '';
for (var n = 0; n < presentations.length && nodeId === ''; n++) {
  if (presentations[n].type === 'Class') {
    nodeId = presentations[n].id;
  }
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
