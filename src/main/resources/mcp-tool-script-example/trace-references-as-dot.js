/*
 * Traces the classifier references around one focus classifier and prints a bounded Graphviz DOT network.
 * The walk covers association, dependency, inheritance and realization in both directions.
 */

// An ID is unambiguous and takes precedence when supplied. Leave it empty to select by exact name.
var focusId = '';
var focusName = 'Class0';

// null means any namespace. An empty string selects the root namespace specifically.
var focusNamespace = null;

var maxHops = 2;
var maxNodesPerDirection = 20;

// This conservative character cap keeps even mostly non-ASCII output within the script's byte limit.
var outputCharLimit = 18000;

var referenceKinds = [
  { key: 'association', label: 'association', style: 'arrowhead=vee', legend: 'solid vee arrow' },
  { key: 'dependency', label: 'dependency', style: 'style=dashed, arrowhead=vee', legend: 'dashed vee arrow' },
  { key: 'inheritance', label: 'inheritance', style: 'arrowhead=empty', legend: 'solid hollow arrow' },
  { key: 'realization', label: 'realization', style: 'style=dashed, arrowhead=empty', legend: 'dashed hollow arrow' }
];

var hopFills = ['#f6c445', '#fde9b5', '#eaf2fb'];


// ============================================================
// Configuration and focus classifier
// ============================================================

function owns(object, key) {
  return Object.prototype.hasOwnProperty.call(object, key);
}

function idKey(id) {
  return 'id:' + id;
}

if (maxHops < 0 || maxHops !== Math.floor(maxHops)) {
  throw 'maxHops must be a non-negative integer.';
}
if (maxNodesPerDirection < 0 || maxNodesPerDirection !== Math.floor(maxNodesPerDirection)) {
  throw 'maxNodesPerDirection must be a non-negative integer.';
}
if (referenceKinds.length === 0) {
  throw 'referenceKinds must contain at least one reference kind.';
}

var kindKeys = {};
for (var configuredKind = 0; configuredKind < referenceKinds.length; configuredKind++) {
  var configuredKey = referenceKinds[configuredKind].key;
  if (owns(kindKeys, configuredKey)) {
    throw 'referenceKinds contains ' + configuredKey + ' more than once.';
  }
  kindKeys[configuredKey] = true;
}

// This call also supplies the namespace of every classifier that the reference tool may return.
var rootId = tools.get_proj({}).element.id;
var classifiers = tools.retrieve_classifiers_within_pkg({ id: rootId }).value;
var classifierById = {};
for (var classifierIndex = 0; classifierIndex < classifiers.length; classifierIndex++) {
  classifierById[idKey(classifiers[classifierIndex].id)] = classifiers[classifierIndex];
}

var focus = null;
if (focusId !== '') {
  if (!owns(classifierById, idKey(focusId))) {
    throw 'No classifier in the current project has ID ' + focusId + '.';
  }
  focus = classifierById[idKey(focusId)];
} else {
  var matches = [];
  for (var candidateIndex = 0; candidateIndex < classifiers.length; candidateIndex++) {
    var candidate = classifiers[candidateIndex];
    if (candidate.name === focusName
        && (focusNamespace === null || candidate.namespace === focusNamespace)) {
      matches.push(candidate);
    }
  }

  if (matches.length === 0) {
    throw 'No classifier is named ' + focusName
      + (focusNamespace === null ? '' : ' in ' + (focusNamespace === '' ? '(root)' : focusNamespace)) + '.';
  }
  if (matches.length > 1) {
    var choices = [];
    for (var matchIndex = 0; matchIndex < matches.length; matchIndex++) {
      choices.push((matches[matchIndex].namespace === '' ? '(root)' : matches[matchIndex].namespace)
        + ' [' + matches[matchIndex].id + ']');
    }
    throw matches.length + ' classifiers are named ' + focusName
      + '. Set focusNamespace to one of these namespaces, or set focusId: ' + choices.join(', ') + '.';
  }

  focus = matches[0];
}

function shownName(name) {
  return name === '' ? '(unnamed)' : name;
}

function qualifiedName(classifier) {
  var namespace = classifier.namespace === '' ? '(root)' : classifier.namespace;
  return namespace + '.' + shownName(classifier.name);
}


// ============================================================
// Reference walk
// ============================================================

var answers = {};
function referencesOf(id) {
  var key = idKey(id);
  if (!owns(answers, key)) {
    answers[key] = tools.retrieve_classifiers_that_ref_or_be_refed_by({ id: id });
  }
  return answers[key];
}

var nodes = {};
var nodeIds = [];
function reach(id, name, type, hop) {
  var key = idKey(id);
  if (owns(nodes, key)) {
    if (nodes[key].hop > hop) {
      nodes[key].hop = hop;
    }
    return;
  }

  // The focus and every classifier the walk accepts are taken from the list, so the list carries this one.
  nodes[key] = {
    name: name,
    type: type,
    namespace: classifierById[key].namespace,
    hop: hop,
    dotId: 'n' + nodeIds.length
  };
  nodeIds.push(id);
}

var edges = {};
var edgeKeys = [];
function addEdge(sourceId, targetId, kindIndex) {
  var key = idKey(sourceId) + '>' + idKey(targetId) + '#' + kindIndex;
  if (!owns(edges, key)) {
    edges[key] = {
      sourceId: sourceId,
      targetId: targetId,
      kindIndex: kindIndex
    };
    edgeKeys.push(key);
  }
}

reach(focus.id, focus.name, focus.type, 0);

// Each direction admits at most maxNodesPerDirection classifiers. At each BFS layer, its candidates
// are sorted and then taken round-robin by reference kind, so one populous kind cannot consume every slot.
function walk(direction) {
  var suffix = (direction === 'upstream' ? 'Source' : 'Target') + 'Classifier';
  var accepted = {};
  accepted[idKey(focus.id)] = true;
  var acceptedCount = 0;
  var omitted = {};
  var frontier = [focus.id];

  for (var hop = 1; hop <= maxHops && frontier.length > 0; hop++) {
    var candidates = {};
    var candidateKeys = [];
    var queues = [];
    var queuedByKind = [];
    for (var queueIndex = 0; queueIndex < referenceKinds.length; queueIndex++) {
      queues.push([]);
      queuedByKind.push({});
    }

    for (var frontierIndex = 0; frontierIndex < frontier.length; frontierIndex++) {
      var fromId = frontier[frontierIndex];
      var answer = referencesOf(fromId);

      for (var kindIndex = 0; kindIndex < referenceKinds.length; kindIndex++) {
        var others = answer[referenceKinds[kindIndex].key + suffix];
        for (var otherIndex = 0; otherIndex < others.length; otherIndex++) {
          var other = others[otherIndex];
          var otherKey = idKey(other.id);

          // The list gathered above is what a classifier of this project means here. An ID missing
          // from it is one that no tool function resolves, so the next hop would fail the whole
          // script on it, and it carries no namespace for its label either.
          if (!owns(classifierById, otherKey)) {
            continue;
          }

          var sourceId = direction === 'upstream' ? other.id : fromId;
          var targetId = direction === 'upstream' ? fromId : other.id;

          if (owns(accepted, otherKey)) {
            addEdge(sourceId, targetId, kindIndex);
            continue;
          }

          if (!owns(candidates, otherKey)) {
            var details = classifierById[otherKey];
            candidates[otherKey] = {
              id: other.id,
              name: other.name,
              type: other.type,
              sortKey: details.namespace + '\n' + other.name + '\n' + other.id,
              edges: []
            };
            candidateKeys.push(otherKey);
          }
          candidates[otherKey].edges.push({
            sourceId: sourceId,
            targetId: targetId,
            kindIndex: kindIndex
          });

          if (!owns(queuedByKind[kindIndex], otherKey)) {
            queuedByKind[kindIndex][otherKey] = true;
            queues[kindIndex].push(otherKey);
          }
        }
      }
    }

    for (var sortKind = 0; sortKind < queues.length; sortKind++) {
      queues[sortKind].sort(function(left, right) {
        var leftKey = candidates[left].sortKey;
        var rightKey = candidates[right].sortKey;
        return leftKey < rightKey ? -1 : (leftKey > rightKey ? 1 : 0);
      });
    }

    var remaining = maxNodesPerDirection - acceptedCount;
    var positions = [];
    for (var positionIndex = 0; positionIndex < queues.length; positionIndex++) {
      positions.push(0);
    }
    var selected = {};
    var selectedKeys = [];
    var madeProgress = true;

    while (remaining > 0 && madeProgress) {
      madeProgress = false;
      for (var selectingKind = 0; selectingKind < queues.length && remaining > 0; selectingKind++) {
        while (positions[selectingKind] < queues[selectingKind].length
            && owns(selected, queues[selectingKind][positions[selectingKind]])) {
          positions[selectingKind]++;
        }
        if (positions[selectingKind] < queues[selectingKind].length) {
          var selectedKey = queues[selectingKind][positions[selectingKind]++];
          selected[selectedKey] = true;
          selectedKeys.push(selectedKey);
          remaining--;
          madeProgress = true;
        }
      }
    }

    var next = [];
    for (var selectedIndex = 0; selectedIndex < selectedKeys.length; selectedIndex++) {
      var acceptedKey = selectedKeys[selectedIndex];
      var acceptedCandidate = candidates[acceptedKey];
      accepted[acceptedKey] = true;
      acceptedCount++;
      reach(acceptedCandidate.id, acceptedCandidate.name, acceptedCandidate.type, hop);
      next.push(acceptedCandidate.id);

      for (var candidateEdgeIndex = 0; candidateEdgeIndex < acceptedCandidate.edges.length; candidateEdgeIndex++) {
        var candidateEdge = acceptedCandidate.edges[candidateEdgeIndex];
        addEdge(candidateEdge.sourceId, candidateEdge.targetId, candidateEdge.kindIndex);
      }
    }

    for (var omittedIndex = 0; omittedIndex < candidateKeys.length; omittedIndex++) {
      if (!owns(selected, candidateKeys[omittedIndex])) {
        omitted[candidateKeys[omittedIndex]] = true;
      }
    }

    frontier = next;
  }

  var omittedCount = 0;
  for (var omittedKey in omitted) {
    if (owns(omitted, omittedKey)) {
      omittedCount++;
    }
  }
  return omittedCount;
}

var omittedUpstream = walk('upstream');
var omittedDownstream = walk('downstream');


// ============================================================
// DOT code
// ============================================================

function escapeDot(text) {
  return String(text)
    .replace(/\\/g, '\\\\')
    .replace(/"/g, '\\"')
    .replace(/\r\n|\r|\n/g, '\\n');
}

var legend = [];
for (var legendIndex = 0; legendIndex < referenceKinds.length; legendIndex++) {
  legend.push(referenceKinds[legendIndex].label + ': ' + referenceKinds[legendIndex].legend);
}

var focusQualifiedName = qualifiedName(focus);
var title = 'References around ' + focusQualifiedName + ', up to ' + maxHops + ' hops each way'
  + '\nEdges - ' + legend.join('; ');
if (omittedUpstream > 0 || omittedDownstream > 0) {
  title += '\nTruncated by the per-direction classifier budget.';
}

var dot = [];
dot.push('digraph references {');
dot.push('  rankdir=LR;');
dot.push('  graph [fontname="sans-serif", labelloc="t", label="' + escapeDot(title) + '"];');
dot.push('  node [fontname="sans-serif", fontsize=11, shape=box, style="rounded,filled", color="#8a8a8a"];');
dot.push('  edge [color="#7a7a7a"];');
dot.push('');

for (var nodeIndex = 0; nodeIndex < nodeIds.length; nodeIndex++) {
  var nodeId = nodeIds[nodeIndex];
  var node = nodes[idKey(nodeId)];
  var nodeNamespace = node.namespace === '' ? '(root)' : node.namespace;
  var nodeLabel = '<<' + node.type + '>>\n' + nodeNamespace + '.' + shownName(node.name);
  var fillIndex = node.hop < hopFills.length ? node.hop : hopFills.length - 1;
  var attributes = 'label="' + escapeDot(nodeLabel) + '"'
    + ', tooltip="' + escapeDot(nodeId) + '"'
    + ', fillcolor="' + hopFills[fillIndex] + '"';
  if (node.hop === 0) {
    attributes += ', color="#c98a00", penwidth=2';
  }
  dot.push('  ' + node.dotId + ' [' + attributes + '];');
}

dot.push('');
for (var edgeIndex = 0; edgeIndex < edgeKeys.length; edgeIndex++) {
  var edge = edges[edgeKeys[edgeIndex]];
  dot.push('  ' + nodes[idKey(edge.sourceId)].dotId + ' -> ' + nodes[idKey(edge.targetId)].dotId
    + ' [' + referenceKinds[edge.kindIndex].style + '];');
}
dot.push('}');


// ============================================================
// Output
// ============================================================

var output = [];
output.push(focus.type + ' ' + focusQualifiedName + ' [' + focus.id + ']: ' + nodeIds.length
  + ' classifier(s) and ' + edgeKeys.length + ' distinct classifier-kind edge(s) shown.');
if (omittedUpstream > 0 || omittedDownstream > 0) {
  var omissions = [];
  if (omittedUpstream > 0) { omissions.push(omittedUpstream + ' upstream'); }
  if (omittedDownstream > 0) { omissions.push(omittedDownstream + ' downstream'); }
  output.push('The budget omitted ' + omissions.join(' and ')
    + ' classifier(s) at the explored boundary; edges to them are not shown.');
}
output.push('');
output.push(dot.join('\n'));
output.push('');
output.push('Render the DOT code above by passing it to generate_graph_img_from_dot as a direct MCP call.');

var renderedOutput = output.join('\n');
if (renderedOutput.length > outputCharLimit) {
  throw 'The ' + renderedOutput.length + '-character result exceeds the conservative '
    + outputCharLimit + '-character output limit. Lower maxNodesPerDirection and run the script again.';
}
print(renderedOutput);
