/*
 * Walks every chunk of the named elements in the project, printing a census of them by type and then the name of each element of one chosen type.
 */

// A project holds more named elements than fit in one response, so they are handed over in chunks.
// get_info_of_all_named_elements returns how many there are along with the first of them and a snapshot ID,
// and get_chunk_of_all_named_elements takes that snapshot ID with the index of each further chunk. If the
// list changes in between, because the model was edited or another project was opened, it throws rather
// than hand over a chunk of the changed list, so the chunks put together are always one and the same list.
var info = tools.get_info_of_all_named_elements({});

// find_named_elements_by_name matches on text, so it cannot answer "every element of this type".
// search_within_named_elements with an empty string does return every named element, but all in one
// result, which a large project outgrows. Walking the chunks works at any size, and
// get_all_named_element_types names the types worth asking for.
var wantedType = 'Class';

var counts = {};
var wantedNames = [];
var total = 0;

for (var i = 0; i < info.totalChunks; i++) {
  // Chunk 0 is the one the info call already returned, so take it from there rather than fetching it twice.
  var chunk = i === 0
    ? info.firstChunk
    : tools.get_chunk_of_all_named_elements({ snapshotId: info.snapshotId, chunkIndex: i }).value;

  // Each entry is { name, id, type }.
  for (var e = 0; e < chunk.length; e++) {
    var element = chunk[e];

    counts[element.type] = (counts[element.type] || 0) + 1;
    total++;

    if (element.type === wantedType) {
      // A named element is allowed to have no name at all, so give the nameless ones something to show.
      wantedNames.push(element.name === '' ? '(unnamed)' : element.name);
    }
  }
}


// ============================================================
// The census
// ============================================================

// Counting here rather than in the conversation is the point of doing this in a script: the whole project
// comes back in one round trip, and only the tally below reaches you instead of every name and ID in it.
var types = [];
for (var type in counts) {
  if (counts.hasOwnProperty(type)) {
    types.push(type);
  }
}
types.sort();

print(total + ' named elements in ' + info.totalChunks + ' chunk(s), by type:');
for (var t = 0; t < types.length; t++) {
  print('  ' + types[t] + ': ' + counts[types[t]]);
}


// ============================================================
// Every element of the wanted type
// ============================================================

// The output of a script is truncated once it grows past a limit, so cap the list rather than dumping it.
// The same name can appear more than once, because two elements in different packages are free to share one.
var nameLimit = 50;
wantedNames.sort();

print('');
print(wantedType + ' (' + wantedNames.length + '):');

for (var w = 0; w < wantedNames.length && w < nameLimit; w++) {
  print('  ' + wantedNames[w]);
}
if (wantedNames.length > nameLimit) {
  print('  ... and ' + (wantedNames.length - nameLimit) + ' more');
}
