package com.astahpromcp;

import com.astahpromcp.tool.ToolProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

// One compiled class of the plugin, as the architecture tests read it. path is relative to com/astahpromcp, with '/' as the separator.
record CompiledClass(
    String path,
    String content
) {

    // A class of the plugin as a class file spells it
    private static final Pattern PLUGIN_CLASS = Pattern.compile("com/astahpromcp/([A-Za-z0-9_$/]+)");

    // Every class compiled from the main sources
    static List<CompiledClass> readAll() throws IOException, URISyntaxException {
        Path root = Path.of(ToolProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertTrue(Files.isDirectory(root), "The main classes must be a directory that can be read, but they are at " + root);
        Path base = root.resolve("com/astahpromcp");

        List<Path> files;
        try (Stream<Path> walk = Files.walk(base)) {
            files = walk.filter(file -> file.getFileName().toString().endsWith(".class")).sorted().toList();
        }

        List<CompiledClass> found = new ArrayList<>();
        for (Path file : files) {
            String path = base.relativize(file).toString().replace(File.separatorChar, '/');
            found.add(new CompiledClass(path, new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1)));
        }
        return List.copyOf(found);
    }

    String packagePath() {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? "" : path.substring(0, slash);
    }

    // Whether this class is in the given package or in one of its subpackages
    boolean isWithin(String packagePath) {
        return path.startsWith(packagePath + "/");
    }

    // Whether this class is the given top-level class of com.astahpromcp, or one of its nested or anonymous classes
    boolean isTopLevelClass(String simpleName) {
        if (!packagePath().isEmpty()) {
            return false;
        }
        String fileName = path.substring(0, path.length() - ".class".length());
        return fileName.equals(simpleName) || fileName.startsWith(simpleName + "$");
    }

    // Whether this class is the given class, or one of its nested or anonymous classes.
    // classPath is relative to com/astahpromcp, without ".class", such as "server/McpServerApp".
    boolean isClass(String classPath) {
        String withoutExtension = path.substring(0, path.length() - ".class".length());
        return withoutExtension.equals(classPath) || withoutExtension.startsWith(classPath + "$");
    }

    String className() {
        return "com.astahpromcp." + path.substring(0, path.length() - ".class".length()).replace('/', '.');
    }

    // The first of the given names this class contains, or null when it contains none of them
    String firstOf(List<String> names) {
        for (String name : names) {
            if (content.contains(name)) {
                return name;
            }
        }
        return null;
    }

    // The packages of the plugin, other than its own, that this class uses, each as a path relative to com/astahpromcp.
    Set<String> referencedPackages() {
        Set<String> packages = new TreeSet<>();
        Matcher matcher = PLUGIN_CLASS.matcher(content);
        while (matcher.find()) {
            String name = matcher.group(1);
            int slash = name.lastIndexOf('/');
            packages.add(slash < 0 ? "" : name.substring(0, slash));
        }
        packages.remove(packagePath());
        return packages;
    }
}
