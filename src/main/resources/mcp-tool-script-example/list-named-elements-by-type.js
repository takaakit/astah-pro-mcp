/*
 * Walks every chunk of the named elements in the project, printing a census of them by type and then the name of each element of one chosen type.
 */

// A project holds more named elements than fit in one response, so they are handed over in chunks.
// get_info_of_all_named_elements is what builds those chunks: it returns how many there are along with the
// first of them, and get_chunk_of_all_named_elements throws until it has been called. Those chunks are
// then held for a limited time only, which is why this walk belongs in a script: every call of it runs
// back to back, whereas the same walk done as one MCP call per chunk has a conversation happening in
// between and can outlive the cache.
var info = tools.get_info_of_all_named_elements({});

// find_named_elements_by_name and search_within_named_elements both match on text, so neither can answer
// "every element of this type". Walking the chunks is what does, and get_all_named_element_types names the
// types worth asking for.
var wantedType = 'Class';

var counts = {};
var wantedNames = [];
var total = 0;

for (var i = 0; i < info.totalChunks; i++) {
  // Chunk 0 is the one the info call already returned, so take it from there rather than fetching it twice.
  var chunk = i === 0
    ? info.firstChunk
    : tools.get_chunk_of_all_named_elements({ chunkIndex: i }).value;

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
