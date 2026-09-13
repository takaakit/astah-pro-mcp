/*
 * Draws a state machine diagram with an initial pseudostate, a final state, states, a composite state holding two regions, the substates inside those regions, and the transitions between them.
 */

var rootPackageId = tools.get_proj({}).element.id;

// create_state_machine_dgm takes any named element as the parent, so the diagram can go under a class.
var mediaPlayerClassId = tools.create_class_in_parent_pkg({
  newClassName: 'MediaPlayer',
  parentPackageId: rootPackageId
}).namedElement.element.id;

var diagramId = tools.create_state_machine_dgm({
  newStateMachineDiagramName: 'Media Player',
  parentNamedElementId: mediaPlayerClassId
}).namedElement.element.id;


// ============================================================
// Top level
// ============================================================

// An empty parent node presentation means the top level of the diagram.
var start = tools.create_init_pseudostate({
  locationX: 320, locationY: 40,
  parentNodePresentationId: '',
  targetDiagramId: diagramId
}).presentation.id;

// A state is placed with its centre 25 to the right of and 25 below the given point,
// so set the location again to put its top-left corner where it belongs.
var idle = tools.create_state({
  newStateName: 'Idle',
  locationX: 310, locationY: 110,
  parentNodePresentationId: '',
  targetDiagramId: diagramId
}).presentation.id;
tools.set_node_prst_location({ nodePresentationId: idle, locationX: 310, locationY: 110 });

var stop = tools.create_final_state({
  locationX: 320, locationY: 560,
  parentNodePresentationId: '',
  targetDiagramId: diagramId
}).presentation.id;


// ============================================================
// Composite state with two regions
// ============================================================

var playing = tools.create_state({
  newStateName: 'Playing',
  locationX: 150, locationY: 220,
  parentNodePresentationId: '',
  targetDiagramId: diagramId
}).presentation.id;

// add_region halves the height the state already has and gives the first region 20 more than that
// half, while the second region takes whatever is left. A state 240 tall therefore gives a first
// region of 140, and a final height of 280 leaves the second region 140 as well.
tools.set_node_prst_width({ nodePresentationId: playing, width: 360 });
tools.set_node_prst_height({ nodePresentationId: playing, height: 240 });

tools.add_region({
  isHorizontal: true,
  parentNodePresentationId: playing,
  targetDiagramId: diagramId
});

// Finish sizing the state before putting anything inside it, because resizing it moves whatever it
// already holds. The rectangle of a composite state covers its regions and nothing else: the name
// is drawn above that rectangle.
tools.set_node_prst_location({ nodePresentationId: playing, locationX: 150, locationY: 220 });
tools.set_node_prst_width({ nodePresentationId: playing, width: 360 });
tools.set_node_prst_height({ nodePresentationId: playing, height: 280 });


// ============================================================
// Substates in the upper region
// ============================================================

// A substate names the composite state as its parent. Which of the two regions it lands in is
// decided by its own coordinates: the upper region runs from y 220 to y 360, the lower one from
// y 360 to y 500.
var bufferingStart = tools.create_init_pseudostate({
  locationX: 180, locationY: 280,
  parentNodePresentationId: playing,
  targetDiagramId: diagramId
}).presentation.id;

var buffering = tools.create_state({
  newStateName: 'Buffering',
  locationX: 250, locationY: 272,
  parentNodePresentationId: playing,
  targetDiagramId: diagramId
}).presentation.id;
tools.set_node_prst_location({ nodePresentationId: buffering, locationX: 250, locationY: 272 });

var streaming = tools.create_state({
  newStateName: 'Streaming',
  locationX: 390, locationY: 272,
  parentNodePresentationId: playing,
  targetDiagramId: diagramId
}).presentation.id;
tools.set_node_prst_location({ nodePresentationId: streaming, locationX: 390, locationY: 272 });


// ============================================================
// Substates in the lower region
// ============================================================

var soundStart = tools.create_init_pseudostate({
  locationX: 180, locationY: 420,
  parentNodePresentationId: playing,
  targetDiagramId: diagramId
}).presentation.id;

var soundOn = tools.create_state({
  newStateName: 'SoundOn',
  locationX: 250, locationY: 412,
  parentNodePresentationId: playing,
  targetDiagramId: diagramId
}).presentation.id;
tools.set_node_prst_location({ nodePresentationId: soundOn, locationX: 250, locationY: 412 });

var muted = tools.create_state({
  newStateName: 'Muted',
  locationX: 410, locationY: 412,
  parentNodePresentationId: playing,
  targetDiagramId: diagramId
}).presentation.id;
tools.set_node_prst_location({ nodePresentationId: muted, locationX: 410, locationY: 412 });


// ============================================================
// Transitions
// ============================================================

// Astah runs a transition straight from one centre to the other, so two transitions joining the
// same pair of nodes end up on the same line. Give each of those a path of its own: a point inside
// the source, a point bent away from the straight run, and a point inside the target. Bending at a
// single point makes a gentle arc rather than a right-angled corner. Set the line style before the
// points, because setting the style discards a path that is already there.

var startTransition = tools.create_transition({
  sourceNodePresentationId: start,
  targetNodePresentationId: idle,
  targetDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: startTransition.presentation.id,
  lineStyle: 'curve'
});

// set_event_of_transition takes the model element of the transition, not its presentation.
var playTransition = tools.create_transition({
  sourceNodePresentationId: idle,
  targetNodePresentationId: playing,
  targetDiagramId: diagramId
});
tools.set_event_of_transition({
  targetTransitionId: playTransition.presentation.correspondingModelElement.id,
  event: 'play'
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: playTransition.presentation.id,
  lineStyle: 'curve'
});
tools.set_points_of_link_prst({
  targetLinkPresentationId: playTransition.presentation.id,
  drawPoints: [{ x: 345, y: 135 }, { x: 405, y: 175 }, { x: 420, y: 250 }]
});

var stopTransition = tools.create_transition({
  sourceNodePresentationId: playing,
  targetNodePresentationId: idle,
  targetDiagramId: diagramId
});
tools.set_event_of_transition({
  targetTransitionId: stopTransition.presentation.correspondingModelElement.id,
  event: 'stop'
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: stopTransition.presentation.id,
  lineStyle: 'curve'
});
tools.set_points_of_link_prst({
  targetLinkPresentationId: stopTransition.presentation.id,
  drawPoints: [{ x: 240, y: 250 }, { x: 255, y: 175 }, { x: 315, y: 135 }]
});

var powerOffTransition = tools.create_transition({
  sourceNodePresentationId: playing,
  targetNodePresentationId: stop,
  targetDiagramId: diagramId
});
tools.set_event_of_transition({
  targetTransitionId: powerOffTransition.presentation.correspondingModelElement.id,
  event: 'powerOff'
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: powerOffTransition.presentation.id,
  lineStyle: 'curve'
});

var bufferingStartTransition = tools.create_transition({
  sourceNodePresentationId: bufferingStart,
  targetNodePresentationId: buffering,
  targetDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: bufferingStartTransition.presentation.id,
  lineStyle: 'curve'
});

var bufferedTransition = tools.create_transition({
  sourceNodePresentationId: buffering,
  targetNodePresentationId: streaming,
  targetDiagramId: diagramId
});
tools.set_event_of_transition({
  targetTransitionId: bufferedTransition.presentation.correspondingModelElement.id,
  event: 'buffered'
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: bufferedTransition.presentation.id,
  lineStyle: 'curve'
});

var soundStartTransition = tools.create_transition({
  sourceNodePresentationId: soundStart,
  targetNodePresentationId: soundOn,
  targetDiagramId: diagramId
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: soundStartTransition.presentation.id,
  lineStyle: 'curve'
});

var muteTransition = tools.create_transition({
  sourceNodePresentationId: soundOn,
  targetNodePresentationId: muted,
  targetDiagramId: diagramId
});
tools.set_event_of_transition({
  targetTransitionId: muteTransition.presentation.correspondingModelElement.id,
  event: 'mute'
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: muteTransition.presentation.id,
  lineStyle: 'curve'
});
tools.set_points_of_link_prst({
  targetLinkPresentationId: muteTransition.presentation.id,
  drawPoints: [{ x: 300, y: 425 }, { x: 365, y: 395 }, { x: 430, y: 425 }]
});

var unmuteTransition = tools.create_transition({
  sourceNodePresentationId: muted,
  targetNodePresentationId: soundOn,
  targetDiagramId: diagramId
});
tools.set_event_of_transition({
  targetTransitionId: unmuteTransition.presentation.correspondingModelElement.id,
  event: 'unmute'
});
tools.set_line_style_of_link_prst({
  targetLinkPresentationId: unmuteTransition.presentation.id,
  lineStyle: 'curve'
});
tools.set_points_of_link_prst({
  targetLinkPresentationId: unmuteTransition.presentation.id,
  drawPoints: [{ x: 430, y: 438 }, { x: 365, y: 468 }, { x: 300, y: 438 }]
});


print('Created the state machine diagram Media Player.');
