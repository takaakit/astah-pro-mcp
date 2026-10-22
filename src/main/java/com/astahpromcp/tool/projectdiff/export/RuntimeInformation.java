package com.astahpromcp.tool.projectdiff.export;

// The Java runtime an XMI was written on
public record RuntimeInformation(
    String javaHome,
    String javaVersion,
    String javaVendor,
    String osName,
    String locale,
    String defaultCharset,
    String commandLineEncoding,
    boolean headless
) {
}
