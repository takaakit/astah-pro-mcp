/*
 * Draws a composite structure diagram with a structured class, parts typed by classes, ports on a part and on the structured class boundary, provided and required interfaces, and connectors between the parts.
 */

var rootPackageId = tools.get_proj({}).element.id;


// ============================================================
// Classifiers
// ============================================================

var atmId = tools.create_class_in_parent_pkg({
  newClassName: 'ATM',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: atmId,
  definition: 'The teller machine as a whole. Encapsulates the devices it is built from and exposes its services through its ports.'
});

var cardReaderId = tools.create_class_in_parent_pkg({
  newClassName: 'CardReader',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: cardReaderId,
  definition: 'Reads the bank card presented by the customer and reports the card data to the controller.'
});

var cashCartridgeId = tools.create_class_in_parent_pkg({
  newClassName: 'CashCartridge',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: cashCartridgeId,
  definition: 'Holds the banknotes of one denomination and releases them on the command of the controller.'
});

var atmControllerId = tools.create_class_in_parent_pkg({
  newClassName: 'AtmController',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: atmControllerId,
  definition: 'Drives the devices of the machine and settles each withdrawal with the bank.'
});

var cashWithdrawalId = tools.create_interface_in_parent_pkg({
  newInterfaceName: 'CashWithdrawal',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: cashWithdrawalId,
  definition: 'The service the machine offers to the customer at its customer port.'
});

var bankServiceId = tools.create_interface_in_parent_pkg({
  newInterfaceName: 'BankService',
  parentPackageId: rootPackageId
}).namedElement.element.id;

tools.set_definition({
  targetNamedElementId: bankServiceId,
  definition: 'The service the machine expects from the bank at its network port.'
});


// ============================================================
// Parts
// ============================================================

// A part must be an association end: create_connector_between_parts_and_ports rejects a plain
// attribute. End B is the end the source class owns, so it is the part of the ATM.
// Astah draws the part box solid only when the composite marker sits on the source side.
var readerAssociation = tools.create_asso({
  sourceClassId: atmId,
  targetClassId: cardReaderId,
  sourceAggregationKind: 'composite',
  sourceNavigability: 'unspecified',
  targetAggregationKind: 'none',
  targetNavigability: 'navigable'
});
var cardReaderPartId = readerAssociation.associationEndB.id;

var cartridgeAssociation = tools.create_asso({
  sourceClassId: atmId,
  targetClassId: cashCartridgeId,
  sourceAggregationKind: 'composite',
  sourceNavigability: 'unspecified',
  targetAggregationKind: 'none',
  targetNavigability: 'navigable'
});
var cashCartridgePartId = cartridgeAssociation.associationEndB.id;

var controllerAssociation = tools.create_asso({
  sourceClassId: atmId,
  targetClassId: atmControllerId,
  sourceAggregationKind: 'composite',
  sourceNavigability: 'unspecified',
  targetAggregationKind: 'none',
  targetNavigability: 'navigable'
});
var controllerPartId = controllerAssociation.associationEndB.id;

// The role name of a part is the name of the association end itself.
tools.set_name({ targetNamedElementId: cardReaderPartId, name: 'cardReader' });
tools.set_name({ targetNamedElementId: cashCartridgePartId, name: 'cashCartridge' });
tools.set_name({ targetNamedElementId: controllerPartId, name: 'controller' });

tools.set_multiplicity_of_asso_end_b({
  targetAttributeId: cashCartridgePartId,
  lowerMultiplicity: '1',
  upperMultiplicity: '4'
});


// ============================================================
// Ports and their interfaces
// ============================================================

// A port belongs to the class that owns it, so a port on a part belongs to the type of the part.
var customerPortId = tools.create_port({
  parentClassId: atmId,
  newPortName: 'customer'
}).attribute.namedElement.element.id;

var networkPortId = tools.create_port({
  parentClassId: atmId,
  newPortName: 'network'
}).attribute.namedElement.element.id;

var sessionPortId = tools.create_port({
  parentClassId: atmControllerId,
  newPortName: 'session'
}).attribute.namedElement.element.id;

var bankPortId = tools.create_port({
  parentClassId: atmControllerId,
  newPortName: 'bank'
}).attribute.namedElement.element.id;

tools.create_provided_interface_of_port({
  targetPortId: customerPortId,
  targetInterfaceId: cashWithdrawalId
});

tools.create_required_interface_of_port({
  targetPortId: networkPortId,
  targetInterfaceId: bankServiceId
});


// ============================================================
// Diagram
// ============================================================

var diagramId = tools.create_composite_structure_dgm({
  targetPackageId: rootPackageId,
  newDiagramName: 'ATM Internal Structure'
}).namedElement.element.id;

// A structured class presentation cannot be shrunk once it holds parts, so size it first.
var atm = tools.create_structured_class_prst({
  targetCompositeStructureDiagramId: diagramId,
  targetClassId: atmId,
  locationX: 50, locationY: 60
}).presentation.id;

tools.set_node_prst_width({ nodePresentationId: atm, width: 700 });
tools.set_node_prst_height({ nodePresentationId: atm, height: 280 });

// Sizing every part explicitly is what lets the ports and the lines below be placed exactly;
// left alone, a part takes the height of its label.
var cardReader = tools.create_part_prst({
  targetCompositeStructureDiagramId: diagramId,
  targetAttributeId: cardReaderPartId,
  parentNodePresentationId: atm,
  locationX: 100, locationY: 130
}).presentation.id;

tools.set_node_prst_width({ nodePresentationId: cardReader, width: 275 });
tools.set_node_prst_height({ nodePresentationId: cardReader, height: 60 });

var cashCartridge = tools.create_part_prst({
  targetCompositeStructureDiagramId: diagramId,
  targetAttributeId: cashCartridgePartId,
  parentNodePresentationId: atm,
  locationX: 100, locationY: 210
}).presentation.id;

tools.set_node_prst_width({ nodePresentationId: cashCartridge, width: 275 });
tools.set_node_prst_height({ nodePresentationId: cashCartridge, height: 60 });

var controller = tools.create_part_prst({
  targetCompositeStructureDiagramId: diagramId,
  targetAttributeId: controllerPartId,
  parentNodePresentationId: atm,
  locationX: 460, locationY: 120
}).presentation.id;

tools.set_node_prst_width({ nodePresentationId: controller, width: 200 });
tools.set_node_prst_height({ nodePresentationId: controller, height: 150 });

// A port presentation is a 14 x 14 square placed by its top left corner, so a port sits on the
// middle of an edge when it is given the midpoint of that edge less 7.
var bank = tools.create_port_prst({
  targetCompositeStructureDiagramId: diagramId,
  targetNodePresentationId: controller,
  targetPortId: bankPortId,
  locationX: 653, locationY: 188
}).presentation.id;

var session = tools.create_port_prst({
  targetCompositeStructureDiagramId: diagramId,
  targetNodePresentationId: controller,
  targetPortId: sessionPortId,
  locationX: 553, locationY: 263
}).presentation.id;

var network = tools.create_port_prst({
  targetCompositeStructureDiagramId: diagramId,
  targetNodePresentationId: atm,
  targetPortId: networkPortId,
  locationX: 743, locationY: 188
}).presentation.id;

var customer = tools.create_port_prst({
  targetCompositeStructureDiagramId: diagramId,
  targetNodePresentationId: atm,
  targetPortId: customerPortId,
  locationX: 553, locationY: 333
}).presentation.id;

// The interfaces a port already has are drawn with their lines by showing them.
// The symbol is 20 x 20 and is also placed by its top left corner, so it is given 3 less than
// the port to share the port's centre line and keep the line to it straight.
tools.show_interface_prsts_of_port({
  targetCompositeStructureDiagramId: diagramId,
  targetNodePresentationId: network,
  locationX: 820, locationY: 185
});

tools.show_interface_prsts_of_port({
  targetCompositeStructureDiagramId: diagramId,
  targetNodePresentationId: customer,
  locationX: 550, locationY: 380
});


// ============================================================
// Connectors
// ============================================================

// Connectors come last. Created before the parts are drawn, Astah draws some of them by itself
// and the link presentation below then doubles the line.
var readerConnectorId = tools.create_connector_between_parts_and_ports({
  sourcePartId: controllerPartId,
  sourcePortId: '',
  targetPartId: cardReaderPartId,
  targetPortId: ''
}).namedElement.element.id;

var readerLink = tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: readerConnectorId,
  sourceNodePresentationId: cardReader,
  targetNodePresentationId: controller
}).presentation.id;

var cartridgeConnectorId = tools.create_connector_between_parts_and_ports({
  sourcePartId: controllerPartId,
  sourcePortId: '',
  targetPartId: cashCartridgePartId,
  targetPortId: ''
}).namedElement.element.id;

var cartridgeLink = tools.create_link_prst_on_dgm({
  targetDiagramId: diagramId,
  targetElementId: cartridgeConnectorId,
  sourceNodePresentationId: cashCartridge,
  targetNodePresentationId: controller
}).presentation.id;

// Astah routes a line from one node centre to the other, which leaves it slanted whenever the
// centres are not level. The points below keep each line straight, and they have to fall inside
// the node rectangles rather than on their borders.
tools.set_points_of_link_prst({
  targetLinkPresentationId: readerLink,
  drawPoints: [{ x: 365, y: 160 }, { x: 470, y: 160 }]
});

tools.set_points_of_link_prst({
  targetLinkPresentationId: cartridgeLink,
  drawPoints: [{ x: 365, y: 240 }, { x: 470, y: 240 }]
});


// ============================================================
// Delegation connectors: an astah api script, not this script
// ============================================================

// The two boundary ports of the ATM are left unconnected here, so the diagram this script draws
// has no delegation connector. One cannot be made from an mcp tool script:
// create_connector_between_parts_and_ports needs an association end at each end, and the
// boundary port of the ATM is not one. The Astah API method that does it,
// CompositeStructureDiagramEditor.createConnectorPresentation, takes two node presentations,
// which may be part or port presentations, and is reachable only through run_astah_api_script.
//
//   var editor = astah.getDiagramEditorFactory().getCompositeStructureDiagramEditor();
//   editor.setDiagram(diagram);
//   editor.createConnectorPresentation(bankPortPresentation, networkPortPresentation, '');
//   editor.createConnectorPresentation(sessionPortPresentation, customerPortPresentation, '');
//
// Run that after this script to join bank to network and session to customer, and straighten
// each line afterwards with set_points_of_link_prst as above.


print('Created the composite structure diagram ATM Internal Structure.');
