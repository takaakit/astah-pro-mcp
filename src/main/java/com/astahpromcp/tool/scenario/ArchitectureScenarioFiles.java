package com.astahpromcp.tool.scenario;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

// The working directory of the architecture scenario analysis and the files in it.
@Slf4j
final class ArchitectureScenarioFiles {

    private static final String DIRECTORY_NAME = "architecture-scenario-analysis";
    private static final String SCENARIO_FILE_NAME = "scenarios.md";
    private static final String ABSTRACT_SCENARIO_IMAGE_FILE_NAME = "abstract-scenario.svg";
    private static final String EVALUATION_RESULTS_FILE_NAME = "evaluation-results.md";
    private static final String ARCHITECTURE_IMPROVEMENTS_FILE_NAME = "architecture-improvements.md";

    // What the working directory is renamed to when it is set aside, which it stays as until the workspace directory is deleted with this MCP server
    private static final String SET_ASIDE_DIRECTORY_NAME_PREFIX = DIRECTORY_NAME + "-set-aside-";
    private static final DateTimeFormatter SET_ASIDE_DIRECTORY_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    // The heading that step 6 of the evaluation adds to the evaluation results file when it hands over to the steps for after editing
    private static final Pattern CHANGES_AFTER_THE_EVALUATION = Pattern.compile("^#[ \\t]+Changes After the Evaluation[ \\t]*#*[ \\t]*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // How far the architecture scenario development has got, judged from the contents of the scenario file
    enum State {
        // No scenario file
        NOT_DEVELOPED,
        // A scenario file that does not hold everything the architecture scenario development steps write
        DEVELOPMENT_IN_PROGRESS,
        // A scenario file that holds everything the architecture scenario development steps write
        DEVELOPED
    }

    // The scenario file as it is at one moment. The check is null, and the minutes are -1, when there is no scenario file.
    record Snapshot(
        State state,
        ScenarioFileCheck.Result check,
        long minutesSinceLastModified
    ) {
    }

    // What setting the working directory aside came to. The kept directory is where its files stay, under another name; null when it did not exist.
    record SettingAside(
        boolean setAside,
        Path kept
    ) {
    }

    private final Path workspaceDir;
    private final Path directory;

    ArchitectureScenarioFiles(Path workspaceDir) {
        this.workspaceDir = workspaceDir.toAbsolutePath();
        this.directory = this.workspaceDir.resolve(DIRECTORY_NAME);
    }

    Path directory() {
        return directory;
    }

    Path scenarioFile() {
        return directory.resolve(SCENARIO_FILE_NAME);
    }

    Path abstractScenarioImageFile() {
        return directory.resolve(ABSTRACT_SCENARIO_IMAGE_FILE_NAME);
    }

    Path evaluationResultsFile() {
        return directory.resolve(EVALUATION_RESULTS_FILE_NAME);
    }

    Path architectureImprovementsFile() {
        return directory.resolve(ARCHITECTURE_IMPROVEMENTS_FILE_NAME);
    }

    boolean scenarioFileExists() {
        return Files.isRegularFile(scenarioFile());
    }

    boolean abstractScenarioImageFileExists() {
        return Files.isRegularFile(abstractScenarioImageFile());
    }

    // Whether an evaluation has left any of its files
    boolean evaluationFileExists() {
        return Files.isRegularFile(evaluationResultsFile()) || Files.isRegularFile(architectureImprovementsFile());
    }

    // Whether the evaluation has written its results, which is what there is to close
    boolean evaluationResultsFileExists() {
        return Files.isRegularFile(evaluationResultsFile());
    }

    // Whether the evaluation has been handed over to the steps for after editing, which its step 6 marks with a heading in the evaluation results file.
    // Only that heading, which is ASCII, is looked for, so a file that is not valid UTF-8 is read all the same. A file that cannot be read counts as not handed over: the evaluation steps then tell the AI agent to look for the heading itself.
    boolean evaluationHandedOver() {
        if (!evaluationResultsFileExists()) {
            return false;
        }

        try {
            String contents = new String(Files.readAllBytes(evaluationResultsFile()), StandardCharsets.UTF_8);
            return CHANGES_AFTER_THE_EVALUATION.matcher(contents).find();
        } catch (IOException e) {
            log.warn("Failed to read the evaluation results file: {}", evaluationResultsFile(), e);
            return false;
        }
    }

    // Judge how far the architecture scenario development has got. A scenario file that cannot be read counts as one whose scenarios are not fully developed.
    Snapshot snapshot() throws IOException {
        if (!scenarioFileExists()) {
            return new Snapshot(State.NOT_DEVELOPED, null, -1);
        }

        ScenarioFileCheck.Result check;
        try {
            check = ScenarioFileCheck.check(readScenarioFile());
        } catch (IllegalStateException e) {
            check = ScenarioFileCheck.unreadable(e.getMessage());
        }

        State state = check.complete() ? State.DEVELOPED : State.DEVELOPMENT_IN_PROGRESS;
        return new Snapshot(state, check, minutesSinceScenarioFileLastModified());
    }

    // Minutes since the scenario file was last modified, which helps the AI agent tell whether the file is its own
    long minutesSinceScenarioFileLastModified() throws IOException {
        Instant lastModified = Files.getLastModifiedTime(scenarioFile()).toInstant();
        return Math.max(0, Duration.between(lastModified, Instant.now()).toMinutes());
    }

    // Create the working directory in advance, so that the AI agent only has to write the files
    void createDirectory() throws IOException {
        Files.createDirectories(directory);
    }

    // Read the scenario file strictly as UTF-8, without a byte order mark
    String readScenarioFile() throws IOException {
        return readUtf8(scenarioFile(), "scenario file");
    }

    // Read the evaluation results file strictly as UTF-8, without a byte order mark; null when it does not exist
    String readEvaluationResultsFile() throws IOException {
        return Files.isRegularFile(evaluationResultsFile()) ? readUtf8(evaluationResultsFile(), "evaluation results file") : null;
    }

    // Read the improvement file strictly as UTF-8, without a byte order mark; null when it does not exist
    String readArchitectureImprovementsFile() throws IOException {
        return Files.isRegularFile(architectureImprovementsFile()) ? readUtf8(architectureImprovementsFile(), "improvement file") : null;
    }

    private static String readUtf8(Path file, String name) throws IOException {
        try {
            String contents = Files.readString(file, StandardCharsets.UTF_8);
            // A byte order mark is no part of what the AI agent wrote
            return contents.startsWith("﻿") ? contents.substring(1) : contents;
        } catch (CharacterCodingException e) {
            throw new IllegalStateException("The " + name + " is not valid UTF-8: " + file + ". Write it again in UTF-8 with your file-writing tool, not with shell redirection.", e);
        }
    }

    // Set the working directory aside, and only that. Either the whole directory goes or nothing does: it is renamed, which fails as a whole while a file in it is open elsewhere.
    SettingAside setDirectoryAside() {
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            return new SettingAside(false, null);
        }

        Path renamed = renamedDirectory();
        try {
            Files.move(directory, renamed, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("Nothing was changed. The working directory of the architecture scenario analysis cannot be set aside, most likely because a file in it is open in another program: " + directory + ". Cause: " + e, e);
        }
        return new SettingAside(true, renamed);
    }

    // The working directories set aside earlier, newest first
    List<Path> setAsideDirectories() {
        if (!Files.isDirectory(workspaceDir)) {
            return List.of();
        }

        List<Path> setAside = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(workspaceDir, SET_ASIDE_DIRECTORY_NAME_PREFIX + "*")) {
            for (Path entry : entries) {
                if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                    setAside.add(entry);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to list the working directories of the architecture scenario analysis set aside in {}", workspaceDir, e);
        }

        // The names end in the time they were set aside, so the later name is the newer one
        setAside.sort(Comparator.comparing((Path entry) -> entry.getFileName().toString()).reversed());
        return setAside;
    }

    // A path next to the working directory that nothing has yet
    private Path renamedDirectory() {
        String name = SET_ASIDE_DIRECTORY_NAME_PREFIX + LocalDateTime.now().format(SET_ASIDE_DIRECTORY_TIMESTAMP);
        Path renamed = workspaceDir.resolve(name);
        for (int number = 2; Files.exists(renamed, LinkOption.NOFOLLOW_LINKS); number++) {
            renamed = workspaceDir.resolve(name + "-" + number);
        }
        return renamed;
    }
}
