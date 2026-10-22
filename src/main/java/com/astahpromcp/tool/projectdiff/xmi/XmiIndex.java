package com.astahpromcp.tool.projectdiff.xmi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Every element of an XMI that has an xmi.id, with its values kept only as a digest so that a large XMI fits in memory.
// The values of the elements that turn out to differ are read again afterwards (readProperties).
public final class XmiIndex {

    // One element with an xmi.id. index is its position in document order, and parent the index of its nearest ancestor with an xmi.id, or -1.
    public record Element(int index, String id, String tag, int parent, String ownerPath,
                          String name, String label, String taggedValueTag, boolean root, boolean diagram,
                          long digestHigh, long digestLow, String[] refPaths, String[] refIds) {

        public boolean hasName() {
            return name != null;
        }

        public boolean sameValuesAs(Element other) {
            return digestHigh == other.digestHigh && digestLow == other.digestLow;
        }
    }

    private final List<Element> elements;
    private final Map<String, Integer> indexById;
    private final String exporterVersion;
    private final String currentModelVersion;

    private XmiIndex(List<Element> elements, Map<String, Integer> indexById, String exporterVersion, String currentModelVersion) {
        this.elements = elements;
        this.indexById = indexById;
        this.exporterVersion = exporterVersion;
        this.currentModelVersion = currentModelVersion;
    }

    public List<Element> elements() {
        return elements;
    }

    public Element get(int index) {
        return elements.get(index);
    }

    // The index of the element with the given xmi.id, or -1
    public int indexOf(String id) {
        Integer index = indexById.get(id);
        return index == null ? -1 : index;
    }

    public String exporterVersion() {
        return exporterVersion;
    }

    public String currentModelVersion() {
        return currentModelVersion;
    }

    // Read an XMI. An XMI larger than maxBytes is refused before it is read.
    public static XmiIndex read(Path xmi, long maxBytes) throws IOException {
        long size = Files.size(xmi);
        if (size > maxBytes) {
            throw new IllegalStateException(String.format("The XMI is %,d bytes, which is more than the %,d bytes this server compares.", size, maxBytes));
        }

        MessageDigest digest = sha256();
        Map<String, String> interned = new HashMap<>();
        List<Pending> pendingElements = new ArrayList<>();

        XmiScanner.Header header = XmiScanner.scan(xmi, scanned -> pendingElements.add(summarize(scanned, digest, interned)));

        // Elements arrive as they end, children before parents; put them in document order
        pendingElements.sort(Comparator.comparingInt(Pending::order));
        Map<String, Integer> indexById = new HashMap<>(pendingElements.size() * 2);
        for (int i = 0; i < pendingElements.size(); i++) {
            Integer previous = indexById.put(pendingElements.get(i).id(), i);
            if (previous != null) {
                throw new IllegalStateException("The XMI has two elements with the xmi.id " + pendingElements.get(i).id() + ".");
            }
        }

        List<Element> elements = new ArrayList<>(pendingElements.size());
        for (int i = 0; i < pendingElements.size(); i++) {
            Pending pending = pendingElements.get(i);
            int parent = pending.parentId().isEmpty() ? -1 : indexById.getOrDefault(pending.parentId(), -1);
            elements.add(new Element(i, pending.id(), pending.tag(), parent, pending.ownerPath(), pending.name(), pending.label(),
                    pending.taggedValueTag(), pending.root(), pending.diagram(), pending.digestHigh(), pending.digestLow(),
                    pending.refPaths(), pending.refIds()));
        }

        return new XmiIndex(Collections.unmodifiableList(elements), indexById, header.exporterVersion(), header.currentModelVersion());
    }

    // Read the values of the elements with the given xmi.ids again, in full
    public static Map<String, List<XmiProperty>> readProperties(Path xmi, Set<String> ids) throws IOException {
        Map<String, List<XmiProperty>> properties = new HashMap<>();
        if (ids.isEmpty()) {
            return properties;
        }
        Set<String> wanted = new HashSet<>(ids);
        XmiScanner.scan(xmi, scanned -> {
            if (wanted.contains(scanned.id())) {
                properties.put(scanned.id(), scanned.properties());
            }
        });
        return properties;
    }

    private record Pending(String id, String tag, String parentId, String ownerPath, int order,
                           String name, String label, String taggedValueTag, boolean root, boolean diagram,
                           long digestHigh, long digestLow, String[] refPaths, String[] refIds) {
    }

    private static Pending summarize(XmiScanner.ScannedElement scanned, MessageDigest digest, Map<String, String> interned) {
        List<XmiProperty> values = new ArrayList<>();
        List<XmiProperty> references = new ArrayList<>();
        String name = null;
        String label = null;
        String taggedValueTag = null;

        for (XmiProperty property : scanned.properties()) {
            if (property.reference()) {
                references.add(property);
            } else {
                values.add(property);
            }
            switch (property.path()) {
                case "@name" -> name = property.value();
                case "@label" -> label = property.value();
                case "@tag" -> taggedValueTag = scanned.tag().equals("UML:TaggedValue") ? property.value() : null;
                default -> {
                }
            }
        }

        // The digest does not depend on the order the values were written in; the paths already say where each one sits
        values.sort(Comparator.comparing(XmiProperty::path));
        digest.reset();
        for (XmiProperty value : values) {
            digest.update(value.path().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(value.value().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 1);
        }
        ByteBuffer hash = ByteBuffer.wrap(digest.digest());

        references.sort(Comparator.comparing(XmiProperty::path));
        String[] refPaths = new String[references.size()];
        String[] refIds = new String[references.size()];
        for (int i = 0; i < references.size(); i++) {
            refPaths[i] = intern(interned, references.get(i).path());
            refIds[i] = references.get(i).value();
        }

        // The root model's name is not compared, but it is still what names the project in the XMI
        if (scanned.root()) {
            name = "";
        }

        return new Pending(scanned.id(), intern(interned, scanned.tag()), scanned.parentId(), intern(interned, scanned.ownerPath()),
                scanned.order(), name, label, taggedValueTag, scanned.root(), scanned.diagram(),
                hash.getLong(), hash.getLong(), refPaths, refIds);
    }

    private static String intern(Map<String, String> interned, String value) {
        return interned.computeIfAbsent(value, key -> key);
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");

        } catch (NoSuchAlgorithmException e) {
            // Every Java platform is required to provide SHA-256
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}
