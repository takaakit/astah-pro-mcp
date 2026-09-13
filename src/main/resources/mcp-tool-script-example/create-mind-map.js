/*
 * Draws a mind map with topics nested under the root topic, a multi-line topic label, a reordered sibling, a boundary around one branch, an icon inside a topic, a floating topic and a link between two topics.
 */

var rootPackageId = tools.get_proj({}).element.id;

// The diagram name labels the root topic as well, so name the diagram after what the map is about.
var diagram = tools.create_mind_map_dgm({
  newDiagramName: 'Checkout Redesign',
  targetPackageId: rootPackageId
});
var diagramId = diagram.namedElement.element.id;

// A topic is a presentation and nothing else: no model element stands behind it, so every tool
// function below takes a presentation ID, and set_name and set_definition have nothing to work on.
// set_label is what names a topic once it is there.
//
// The root topic comes with the diagram and is the only presentation on it, so it is the first of
// the diagram's. get_mind_map_dgm_info({ id: diagramId }).rootTopic.id answers the same for a mind
// map that is already in the project.
var rootTopic = diagram.namedElement.element.correspondingPresentationIds[0];


// ============================================================
// The branches under the root topic
// ============================================================

// A topic is placed by Astah, which lays the whole map out around the root topic, so create_topic
// takes no coordinates. The line joining a topic to its parent is drawn with it as well.
var scope = tools.create_topic({
  targetDiagramId: diagramId,
  parentTopicId: rootTopic,
  newTopicLabel: 'Scope'
}).presentation.id;

var risks = tools.create_topic({
  targetDiagramId: diagramId,
  parentTopicId: rootTopic,
  newTopicLabel: 'Risks'
}).presentation.id;

var releasePlan = tools.create_topic({
  targetDiagramId: diagramId,
  parentTopicId: rootTopic,
  newTopicLabel: 'Release plan'
}).presentation.id;


// ============================================================
// The topics under the branches
// ============================================================

var paymentMethods = tools.create_topic({
  targetDiagramId: diagramId,
  parentTopicId: scope,
  newTopicLabel: 'Payment methods'
}).presentation.id;

tools.create_topic({
  targetDiagramId: diagramId,
  parentTopicId: scope,
  newTopicLabel: 'Discounts'
});

// A label holds a description under its title as well as the title. Write the line break as '\n':
// a Unicode escape for a line feed is turned into a real line break before the script is parsed,
// which would end the string literal there and stop the run with "Missing close quote".
var providerOutage = tools.create_topic({
  targetDiagramId: diagramId,
  parentTopicId: risks,
  newTopicLabel: 'Card provider outage\nThe shop takes no card payment while the provider is down.'
}).presentation.id;

tools.create_topic({
  targetDiagramId: diagramId,
  parentTopicId: releasePlan,
  newTopicLabel: 'Beta in October'
});

tools.create_topic({
  targetDiagramId: diagramId,
  parentTopicId: releasePlan,
  newTopicLabel: 'General release in November'
});


// ============================================================
// The order of the branches
// ============================================================

// Sibling topics sit in the order they were created in, counting from 0. This moves the release
// plan up to the middle, leaving Scope, Release plan, Risks.
tools.move_topic_within_sibling_order({
  targetDiagramId: diagramId,
  targetTopicId: releasePlan,
  newSiblingIndex: 1
});


// ============================================================
// The look of a branch
// ============================================================

// A boundary encloses the topic and everything under it, so set it once the branch is complete.
tools.set_boundary_of_topic({
  targetDiagramId: diagramId,
  targetTopicId: risks,
  boundaryVisibility: true
});

tools.change_fill_color({
  presentationId: risks,
  color: '#FFF2CC'
});

// An icon goes inside the topic, beside its label. Draw it at 32x32, which is the size a topic has
// room for, and enclose it in svg tags: insert_svg_img_into_topic takes the code rather than a file.
// insert_svg_img_on_dgm, which puts a picture on the diagram itself, is published directly by this
// server instead, so it is called as an MCP tool rather than from here.
tools.insert_svg_img_into_topic({
  targetDiagramId: diagramId,
  targetTopicId: risks,
  imageSvgCode: '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">'
    + '<path d="M16 3 L30 28 L2 28 Z" fill="#F2C037" stroke="#8A6D0B" stroke-width="2" stroke-linejoin="round"/>'
    + '<rect x="14.5" y="12" width="3" height="9" fill="#5A4708"/>'
    + '<rect x="14.5" y="23" width="3" height="3" fill="#5A4708"/>'
    + '</svg>'
});


// ============================================================
// A floating topic
// ============================================================

// A floating topic hangs off nothing, so it is the one topic placed by coordinates. Put it clear of
// the branches, which spread out from the root topic at (200, 200).
tools.create_floating_topic({
  targetDiagramId: diagramId,
  newFloatingTopicLabel: 'Parked: loyalty points',
  locationX: 60,
  locationY: 520
});


// ============================================================
// A link between two topics
// ============================================================

// The parent line of a topic is already drawn, so this is for the other kind of line: one that
// crosses the map to join two topics that are not parent and child.
tools.create_link_between_topics({
  targetDiagramId: diagramId,
  sourceTopicId: providerOutage,
  targetTopicId: paymentMethods
});

// A mind map takes no note presentation -- create_note fails on one -- so what a note would say
// about the map as a whole belongs in the label of the root topic or of a floating topic.


print('Created the mind map Checkout Redesign.');
