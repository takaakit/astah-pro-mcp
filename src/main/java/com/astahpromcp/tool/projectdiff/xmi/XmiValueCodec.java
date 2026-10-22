package com.astahpromcp.tool.projectdiff.xmi;

import com.astahpromcp.tool.projectdiff.outputdto.ValueEncoding;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

// Turns a value written in an XMI into the value to show. Astah URL-encodes the values at some positions only, so a value
// is decoded only at a position where the encoding has been confirmed. Values are always compared as written.
public final class XmiValueCodec {

    public record Decoded(String value, ValueEncoding encoding) {
    }

    private XmiValueCodec() {
    }

    // The value to show for a property, given the tag of the element with the xmi.id that holds it and the property's path below it
    public static Decoded decode(String ownerTag, boolean ownerIsDiagram, String path, String raw) {
        if (raw == null || raw.isEmpty()) {
            return new Decoded(raw == null ? "" : raw, ValueEncoding.RAW);
        }

        Position position = Position.of(ownerTag, path);

        if (ownerIsDiagram && position.isOwnAttribute() && (position.name().equals("name") || position.name().equals("typeInfo"))) {
            return new Decoded(raw, ValueEncoding.RAW);
        }

        if (isEncodedPosition(position)) {
            try {
                return new Decoded(URLDecoder.decode(raw, StandardCharsets.UTF_8), ValueEncoding.DECODED);
            } catch (IllegalArgumentException e) {
                // A malformed escape: the value was not encoded after all, so it is shown as written
                return new Decoded(raw, ValueEncoding.UNKNOWN);
            }
        }

        if (isNotEncodedPosition(position) || !couldBeUrlEncoded(raw)) {
            return new Decoded(raw, ValueEncoding.RAW);
        }
        return new Decoded(raw, ValueEncoding.UNKNOWN);
    }

    // Positions where values are URL-encoded, as Astah Professional 12.0.0 writes them
    private static boolean isEncodedPosition(Position position) {
        String tag = position.tag();
        String name = position.name();

        if (position.isText()) {
            return false;
        }
        if (tag.startsWith("UML:") && name.equals("name")) {
            return true;
        }
        if (tag.equals("UML:Comment") && name.equals("body")) {
            return true;
        }
        if (tag.equals("UML:TaggedValue") && name.equals("value")) {
            return true;
        }
        if (tag.equals("UML:ModelElement.definition") && name.equals("xmi.value")) {
            return true;
        }
        if (tag.startsWith("JUDE:") && tag.endsWith("Presentation") && (name.equals("label") || name.equals("text"))) {
            return true;
        }
        return tag.equals("JUDE:Hyperlink") && (name.equals("name") || name.equals("path"));
    }

    // Positions where values are written as they are
    private static boolean isNotEncodedPosition(Position position) {
        if (position.isText()) {
            return position.tag().equals("UML:Expression.body") || position.tag().equals("XMI.field");
        }
        return false;
    }

    // Whether the value could be the output of a URL encoder that changed something: such output holds only letters,
    // digits, '*', '-', '.', '_', '+' and escapes of a '%' and two hex digits, and has at least one '+' or escape.
    // Package-private so that a test can check it on values that decode() cannot reach.
    static boolean couldBeUrlEncoded(String value) {
        boolean changed = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '+') {
                changed = true;
            } else if (c == '%') {
                if (i + 2 >= value.length() || !isHexDigit(value.charAt(i + 1)) || !isHexDigit(value.charAt(i + 2))) {
                    return false;
                }
                changed = true;
                i += 2;
            } else if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '*' || c == '-' || c == '.' || c == '_')) {
                return false;
            }
        }
        return changed;
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    // Where a property sits: the tag of the element that carries it, and the attribute, or the text when name is empty
    private record Position(String tag, String name, boolean isOwnAttribute) {

        boolean isText() {
            return name.isEmpty();
        }

        static Position of(String ownerTag, String path) {
            if (path.startsWith("@")) {
                return new Position(ownerTag, path.substring(1), true);
            }

            String elementPath = path;
            String name = "";
            int at = path.lastIndexOf("/@");
            if (at >= 0) {
                elementPath = path.substring(0, at);
                name = path.substring(at + 2);
            }

            int slash = elementPath.lastIndexOf('/');
            String segment = slash >= 0 ? elementPath.substring(slash + 1) : elementPath;
            int bracket = segment.indexOf('[');
            String tag = bracket >= 0 ? segment.substring(0, bracket) : segment;
            return new Position(tag, name, false);
        }
    }
}
