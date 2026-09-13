/*
 * Exports every diagram in the project as a PNG image file.
 */

// get_dgms_of_element also returns the diagrams owned under the given element, so passing the root package covers the whole project.
var rootPackageId = tools.get_proj({}).element.id;
var diagrams = tools.get_dgms_of_element({ id: rootPackageId }).value;

var lines = [];

for (var i = 0; i < diagrams.length; i++) {
  var diagram = diagrams[i];

  // The output path is decided by the server, and is returned as imageFilePath.
  var image = tools.export_dgm_png_img({ id: diagram.id });

  lines.push(diagram.type + ' ' + diagram.name
    + ' -> ' + image.imageFilePath
    + ' (' + image.imageWidth + 'x' + image.imageHeight + ')');
}

print('Exported ' + lines.length + ' diagram(s)');
print(lines.join('\n'));
