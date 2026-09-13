/*
 * Lists the packages and the model elements they own as a tree, by walking down from the root package.
 */

var lines = [];

// ownedElementNameAndIds holds the direct children of a package, each as { name, id, type }.
function listPackage(packageId, indent) {
  var children = tools.get_pkg_info({ id: packageId }).ownedElementNameAndIds;
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    lines.push(indent + child.type + ' ' + child.name + ' (' + child.id + ')');
    if (child.type === 'Package') {
      listPackage(child.id, indent + '  ');
    }
  }
}

// The root package is the project itself.
var root = tools.get_proj({});
lines.push(root.type + ' ' + root.name + ' (' + root.element.id + ')');
listPackage(root.element.id, '  ');

print(lines.join('\n'));
