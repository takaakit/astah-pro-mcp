/*
 * Finds the classifiers that reference one another in a circle, along association, inheritance, realization and dependency lines, and prints the distinct shortest circles through them.
 */

// Every classifier under this package is examined, the whole project by default.
// list-elements-by-package.js prints the ID of every package, to put a smaller one here.
var packageId = tools.get_proj({}).element.id;

// The kinds of reference a circle may run along. retrieve_classifiers_that_ref_or_be_refed_by answers
// with one array per kind and direction, so these names are what the arrays below are built from.
var kinds = ['association', 'inheritance', 'realization', 'dependency'];

var maxClassifiers = 198;

// The server keeps 65536 UTF-8 bytes of output and drops the rest. One character costs at most three
// of those bytes, so this cap holds even when every name in the model is multibyte.
var outputCharLimit = 18000;

var classifiers = tools.retrieve_classifiers_within_pkg({ id: packageId }).value;

if (classifiers.length > maxClassifiers) {
  throw 'The package holds ' + classifiers.length + ' classifiers, more than the ' + maxClassifiers
    + ' one run can ask about. Run list-elements-by-package.js to pick a smaller package, put its ID'
    + ' in packageId, and run this again.';
}


// ============================================================
// The reference graph
// ============================================================

// Two classifiers are free to share a name, so a circle printed by name alone can be unreadable.
// Only the names that are actually shared carry their namespace and ID, to keep the rest short.
var ids = [];
var baseNames = {};
var nameCounts = {};
var labels = {};

for (var i = 0; i < classifiers.length; i++) {
  var classifier = classifiers[i];
  var baseName = classifier.name === '' ? '(unnamed)' : classifier.name;
  var nameKey = 'name:' + baseName;

  ids.push(classifier.id);
  baseNames[classifier.id] = baseName;
  nameCounts[nameKey] = nameCounts.hasOwnProperty(nameKey) ? nameCounts[nameKey] + 1 : 1;
}

for (var l = 0; l < classifiers.length; l++) {
  var item = classifiers[l];
  var itemName = baseNames[item.id];
  labels[item.id] = nameCounts['name:' + itemName] === 1
    ? itemName
    : itemName + ' (' + item.namespace + ', ' + item.id + ')';
}

// edges[from][to] names the kinds of reference running that way, and only within the package: what
// a classifier outside it references in turn was never asked, so a circle that leaves the package
// and comes back stays invisible. An association with both ends unspecified is answered as a
// reference each way, making a circle of the pair it joins; a navigability on one end says which way.
var edges = {};

for (var n = 0; n < ids.length; n++) {
  // The Target arrays are what this classifier references. Following those alone finds every
  // circle, because the Source arrays hold the same references seen from the other end.
  var answer = tools.retrieve_classifiers_that_ref_or_be_refed_by({ id: ids[n] });
  var outgoing = {};

  for (var k = 0; k < kinds.length; k++) {
    var referenced = answer[kinds[k] + 'TargetClassifier'];

    for (var r = 0; r < referenced.length; r++) {
      var to = referenced[r].id;
      if (!labels.hasOwnProperty(to)) { continue; }

      // Self-references are excluded.
      if (to === ids[n]) { continue; }

      outgoing[to] = outgoing.hasOwnProperty(to)
        ? outgoing[to] + ', ' + kinds[k]
        : kinds[k];
    }
  }

  edges[ids[n]] = outgoing;
}


// ============================================================
// The shortest circle through one classifier
// ============================================================

// A breadth-first walk, so the first way back to the start is the shortest circle through it.
// cameFrom holds the way to each classifier reached, and says which ones have been reached already.
// The queue is walked by index rather than shifted, because shift() re-indexes the whole array.
function shortestCircleThrough(startId) {
  var cameFrom = {};
  var queue = [startId];

  for (var q = 0; q < queue.length; q++) {
    var from = queue[q];

    for (var to in edges[from]) {
      if (!edges[from].hasOwnProperty(to)) { continue; }

      // Back at the start, so walk cameFrom to lay the circle out, in reverse.
      if (to === startId) {
        var circle = [];
        for (var at = from; at !== startId; at = cameFrom[at]) { circle.push(at); }
        circle.push(startId);
        circle.reverse();

        return circle;
      }

      if (!cameFrom.hasOwnProperty(to)) {
        cameFrom[to] = from;
        queue.push(to);
      }
    }
  }

  return null;
}


// ============================================================
// The circles
// ============================================================

// Every classifier gets a walk of its own. Skipping the ones an already found circle runs through
// would be cheaper, but it hides a short circle behind a longer one the same classifiers sit on, and
// leaves the answer depending on the order retrieve_classifiers_within_pkg happens to reply in.
//
// Walking from all of them finds the same circle once per classifier on it, so each one is rotated to
// begin at its smallest ID and dropped when that rotation has been seen. Rotating leaves the order of
// the hops alone, so two circles over the same classifiers the opposite way round stay apart.
//
// This is the shortest circle through each classifier rather than every circle in the package: a
// classifier sitting on several only ever contributes its shortest.
var circles = [];
var seen = {};
var cyclicClassifierCount = 0;

for (var s = 0; s < ids.length; s++) {
  var found = shortestCircleThrough(ids[s]);
  if (found === null) { continue; }

  cyclicClassifierCount++;

  var smallestAt = 0;
  for (var t = 1; t < found.length; t++) {
    if (found[t] < found[smallestAt]) { smallestAt = t; }
  }

  var rotated = found.slice(smallestAt).concat(found.slice(0, smallestAt));
  var key = rotated.join('>');
  if (seen.hasOwnProperty(key)) { continue; }

  seen[key] = true;
  circles.push(rotated);
}


// ============================================================
// The output
// ============================================================

var lines = [];

for (var p = 0; p < circles.length; p++) {
  var circle = circles[p];
  var text = '  ';

  for (var m = 0; m < circle.length; m++) {
    var start = circle[m];
    var end = circle[(m + 1) % circle.length];
    text += labels[start] + ' -' + edges[start][end] + '-> ';
  }

  lines.push({ hops: circle.length, text: text + labels[circle[0]] });
}

// The tightest circles are the ones worth breaking first, so the shortest are printed first. A tie is settled by the text.
lines.sort(function (a, b) {
  if (a.hops !== b.hops) { return a.hops - b.hops; }
  return a.text < b.text ? -1 : a.text > b.text ? 1 : 0;
});

if (lines.length === 0) {
  print('No circular reference among the ' + ids.length + ' classifier(s) of the package.');
} else {
  var header = cyclicClassifierCount + ' of the ' + ids.length + ' classifier(s) of the package sit'
    + ' on a circular reference, on ' + lines.length + ' distinct shortest circle(s):';
  print(header);

  var outputChars = header.length + 1;
  var printed = 0;

  for (printed = 0; printed < lines.length; printed++) {
    if (outputChars + lines[printed].text.length + 1 > outputCharLimit) { break; }

    outputChars += lines[printed].text.length + 1;
    print(lines[printed].text);
  }

  if (printed < lines.length) {
    print('The output stopped short of the limit the server puts on it, leaving '
      + (lines.length - printed) + ' circle(s) unprinted. Put the ID of a smaller package in'
      + ' packageId to see them.');
  }
}
