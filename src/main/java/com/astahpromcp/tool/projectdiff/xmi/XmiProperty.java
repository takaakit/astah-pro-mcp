package com.astahpromcp.tool.projectdiff.xmi;

// One value an element with an xmi.id holds: an attribute of its own, or an attribute or the text of a descendant that has no xmi.id
public record XmiProperty(String path, String value, boolean reference) {
}
