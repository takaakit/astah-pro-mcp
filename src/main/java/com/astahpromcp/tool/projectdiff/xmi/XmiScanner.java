package com.astahpromcp.tool.projectdiff.xmi;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

// Reads an XMI element by element without building a tree, and hands over each element with an xmi.id with the values it holds.
// Both passes of the comparison read the XMI through this class, so that they see exactly the same values.
final class XmiScanner {

    // An element with an xmi.id. parentId is that of the nearest ancestor with one, and ownerPath the tags between the two.
    record ScannedElement(String id, String tag, String parentId, String ownerPath, int order,
                          List<XmiProperty> properties, boolean root, boolean diagram) {
    }

    // What the XMI header says about the writer
    record Header(String exporterVersion, String currentModelVersion) {
    }

    private static final String ID = "xmi.id";
    private static final String IDREF = "xmi.idref";
    private static final String CONTENT = "XMI.content";
    private static final String STYLE_PROPERTY_SUFFIX = ".styleProperty";

    private XmiScanner() {
    }

    // The element an open start tag belongs to
    private static class Frame {
        final String tag;
        // The nearest element with an xmi.id that encloses this one, or this one itself; null outside every such element
        final IdFrame owner;
        // Where this element sits below its owner, with sibling indexes; empty for the owner itself
        final String path;
        // The same without sibling indexes, which is what an element with an xmi.id below it records as its ownerPath
        final String tagPath;
        final Map<String, Integer> childCounts = new HashMap<>();
        StringBuilder text;
        boolean hasChildElements;

        Frame(String tag, IdFrame owner, String path, String tagPath) {
            this.tag = tag;
            this.owner = owner;
            this.path = path;
            this.tagPath = tagPath;
        }
    }

    private static final class IdFrame extends Frame {
        final String id;
        final String parentId;
        final String ownerPath;
        final int order;
        final boolean root;
        final boolean diagram;
        final List<XmiProperty> properties = new ArrayList<>();

        IdFrame(String tag, String id, String parentId, String ownerPath, int order, boolean root, boolean diagram) {
            super(tag, null, "", "");
            this.id = id;
            this.parentId = parentId;
            this.ownerPath = ownerPath;
            this.order = order;
            this.root = root;
            this.diagram = diagram;
        }
    }

    // Hand each element with an xmi.id to the consumer once it ends, children before parents
    static Header scan(Path xmi, Consumer<ScannedElement> consumer) throws IOException {
        try (InputStream in = Files.newInputStream(xmi)) {
            XMLStreamReader reader = newFactory().createXMLStreamReader(in);
            try {
                return scan(reader, consumer);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new IllegalStateException("The XMI is not well-formed: " + e.getMessage(), e);
        }
    }

    // A parser that resolves no DTD and no external entity.
    // The JDK's own parser, always: the plugin embeds other XML libraries, and a factory looked up through the
    // service loader could be any of them, with its own reading of these properties.
    private static XMLInputFactory newFactory() {
        XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        return factory;
    }

    private static Header scan(XMLStreamReader reader, Consumer<ScannedElement> consumer) throws XMLStreamException {
        Deque<Frame> stack = new ArrayDeque<>();
        int order = 0;
        boolean rootSeen = false;
        String exporterVersion = "";
        String currentModelVersion = "";

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                String tag = qualifiedName(reader.getPrefix(), reader.getLocalName());
                Frame parent = stack.peek();
                if (parent != null) {
                    parent.hasChildElements = true;
                }

                IdFrame enclosing = ownerOf(parent);
                String id = reader.getAttributeValue(null, ID);
                if (id != null) {
                    String parentId = enclosing == null ? "" : enclosing.id;
                    String ownerPath = enclosing == null ? "" : parent.tagPath;
                    boolean root = !rootSeen && parent != null && parent.tag.equals(CONTENT) && tag.equals("UML:Model");
                    rootSeen |= root;
                    boolean diagram = tag.startsWith("JUDE:") && reader.getAttributeValue(null, "typeInfo") != null;

                    IdFrame frame = new IdFrame(tag, id, parentId, ownerPath, order++, root, diagram);
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        String name = qualifiedName(reader.getAttributePrefix(i), reader.getAttributeLocalName(i));
                        if (!name.equals(ID)) {
                            addProperty(frame, "@" + name, reader.getAttributeValue(i), name.equals(IDREF));
                        }
                    }
                    stack.push(frame);
                    continue;
                }

                if (enclosing == null) {
                    // Outside every element with an xmi.id: the header, XMI.content itself, and extensions directly below it
                    stack.push(new Frame(tag, null, "", ""));
                    continue;
                }

                IdFrame owner = enclosing;
                String key = tag.endsWith(STYLE_PROPERTY_SUFFIX) ? reader.getAttributeValue(null, "key") : null;
                String segment = segment(parent, tag, key);
                String path = parent.path.isEmpty() ? segment : parent.path + "/" + segment;
                String tagPath = parent.tagPath.isEmpty() ? tag : parent.tagPath + "/" + tag;

                Frame frame = new Frame(tag, owner, path, tagPath);
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    String name = qualifiedName(reader.getAttributePrefix(i), reader.getAttributeLocalName(i));
                    if (key != null && name.equals("key")) {
                        continue;
                    }
                    addProperty(owner, path + "/@" + name, reader.getAttributeValue(i), name.equals(IDREF));
                }
                stack.push(frame);

            } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                Frame frame = stack.peek();
                if (frame != null && !(frame instanceof IdFrame)) {
                    if (frame.text == null) {
                        frame.text = new StringBuilder();
                    }
                    frame.text.append(reader.getText());
                }

            } else if (event == XMLStreamConstants.END_ELEMENT) {
                Frame frame = stack.pop();

                if (frame instanceof IdFrame idFrame) {
                    consumer.accept(new ScannedElement(idFrame.id, idFrame.tag, idFrame.parentId, idFrame.ownerPath,
                            idFrame.order, List.copyOf(idFrame.properties), idFrame.root, idFrame.diagram));
                    continue;
                }

                String text = frame.text == null ? "" : frame.text.toString();
                if (frame.owner != null) {
                    if (!frame.hasChildElements && !text.isBlank()) {
                        addProperty(frame.owner, frame.path, text, false);
                    }
                } else if (frame.tag.equals("XMI.exporterVersion")) {
                    exporterVersion = text.strip();
                } else if (frame.tag.equals("XMI.currentModelVersion")) {
                    currentModelVersion = text.strip();
                }
            }
        }

        return new Header(exporterVersion, currentModelVersion);
    }

    // The path segment of a child: its tag, with the index among the siblings of the same tag after the first one.
    // A style property is told apart by its key instead, so that adding one style does not shift the others.
    // XMI.field is always indexed, because its position is what it means (x, then y).
    private static String segment(Frame parent, String tag, String key) {
        if (key != null) {
            return tag + "[key=" + key + "]";
        }
        int index = parent.childCounts.merge(tag, 1, Integer::sum) - 1;
        if (index == 0 && !tag.equals("XMI.field")) {
            return tag;
        }
        return tag + "[" + index + "]";
    }

    // The element with an xmi.id that the given frame belongs to, or null outside every such element
    private static IdFrame ownerOf(Frame frame) {
        if (frame == null) {
            return null;
        }
        return frame instanceof IdFrame idFrame ? idFrame : frame.owner;
    }

    private static void addProperty(IdFrame owner, String path, String value, boolean reference) {
        if (!isExcluded(owner, path)) {
            owner.properties.add(new XmiProperty(path, value, reference));
        }
    }

    // Values that are never compared: the name of the root model, which Astah takes from the file name, the z-order of
    // the messages and activations of a sequence diagram, which each process computes on its own, and the data of an image.
    private static boolean isExcluded(IdFrame owner, String path) {
        if (owner.root && path.equals("@name")) {
            return true;
        }
        if ((owner.tag.equals("JUDE:MessagePresentation") || owner.tag.equals("JUDE:ActivationPresentation")) && path.equals("@depth")) {
            return true;
        }
        return owner.tag.equals("JUDE:ImagePresentation") && path.equals("JUDE:ImagePresentation.imageData");
    }

    private static String qualifiedName(String prefix, String localName) {
        return prefix == null || prefix.isEmpty() ? localName : prefix + ":" + localName;
    }
}
