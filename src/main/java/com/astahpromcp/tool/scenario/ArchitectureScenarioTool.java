package com.astahpromcp.tool.scenario;

import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.ToolProvider;
import com.astahpromcp.tool.ToolSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.astahpromcp.tool.common.outputdto.GuideDTO;
import com.astahpromcp.tool.scenario.inputdto.EvaluationStageDTO;
import com.astahpromcp.tool.scenario.inputdto.WorkTypeDTO;
import com.astahpromcp.tool.scenario.outputdto.SetAsideDirectoryDTO;
import com.astahpromcp.tool.scenario.outputdto.ArchitectureScenariosDTO;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ArchitectureScenarioTool implements ToolProvider {

    private final ArchitectureScenarioFiles files;

    public ArchitectureScenarioTool(Path workspaceDir) {
        this.files = new ArchitectureScenarioFiles(workspaceDir);
    }

    @Override
    public List<ToolDefinition> createToolDefinitions() {
        try {
            return List.of(
                ToolSupport.toolDefinitionReturningDto(
                    "architecture_scenario_development_steps",
                    "Return the steps to develop the scenarios that an architecture you are about to create or change has to satisfy. Call it immediately before you create or change an architecture.",
                    this::getScenarioDevelopmentSteps,
                    WorkTypeDTO.class,
                    GuideDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "read_architecture_scenarios",
                    "Return the working files of the architecture scenario analysis, with what is wrong with them and what to do next.",
                    this::readScenarios,
                    NoInputDTO.class,
                    ArchitectureScenariosDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "architecture_scenario_evaluation_steps",
                    "Return the steps to evaluate and improve the architecture you have just created or changed, or to close that evaluation.",
                    this::getScenarioEvaluationSteps,
                    EvaluationStageDTO.class,
                    GuideDTO.class),

                ToolSupport.toolDefinitionReturningDto(
                    "set_aside_architecture_scenario_dir",
                    "Set the working directory of the architecture scenario analysis aside by renaming it, and return the path it was renamed to. Call it only when the architecture scenario development steps or the architecture scenario evaluation steps tell you to.",
                    this::setDirectoryAside,
                    NoInputDTO.class,
                    SetAsideDirectoryDTO.class)
            );

        } catch (Exception e) {
            log.error("Failed to create architecture scenario analysis tools", e);
            return List.of();
        }
    }

    private GuideDTO getScenarioDevelopmentSteps(WorkTypeDTO param) throws Exception {
        if (param.workType() == null) {
            throw new IllegalArgumentException("workType must be either new or change.");
        }

        ArchitectureScenarioFiles.Snapshot snapshot = files.snapshot();
        log.info("Architecture scenario development state: {} ({}), work type: {}", snapshot.state(), files.directory(), param.workType());

        return switch (snapshot.state()) {
            case NOT_DEVELOPED -> {
                files.createDirectory();
                yield new GuideDTO(ArchitectureScenarioDevelopmentSteps.notDeveloped(files, param.workType()));
            }
            case DEVELOPMENT_IN_PROGRESS, DEVELOPED -> new GuideDTO(ArchitectureScenarioDevelopmentSteps.scenarioFileExists(files, snapshot, param.workType()));
        };
    }

    private ArchitectureScenariosDTO readScenarios(NoInputDTO param) throws Exception {
        String scenarioFilePath = files.scenarioFile().toString();
        String imageFilePath = files.abstractScenarioImageFile().toString();
        String resultsFilePath = files.evaluationResultsFile().toString();
        String improvementsFilePath = files.architectureImprovementsFile().toString();
        List<String> setAsideDirectoryPaths = files.setAsideDirectories().stream().map(Path::toString).toList();

        // A missing scenario file is an answer, not a failure: a subagent and an agent after context compression both ask without knowing whether there is one
        if (!files.scenarioFileExists()) {
            log.info("No scenario file: {}", scenarioFilePath);
            return new ArchitectureScenariosDTO(
                    false,
                    "",
                    List.of("No scenario file exists at " + scenarioFilePath + ". Either no scenarios have been developed, or they were written to another path, or they were set aside, or they were lost because the working directory is emptied, and its path changes, whenever Astah or this MCP server restarts."),
                    List.of(),
                    -1,
                    scenarioFilePath,
                    imageFilePath,
                    files.abstractScenarioImageFileExists(),
                    resultsFilePath,
                    files.evaluationResultsFileExists(),
                    "",
                    improvementsFilePath,
                    "",
                    List.of(),
                    ArchitectureScenarioEvaluationSteps.evaluationGuidance(files),
                    setAsideDirectoryPaths);
        }

        String contents = files.readScenarioFile();
        ScenarioFileCheck.Result check = ScenarioFileCheck.check(contents);
        log.info("Read scenario file: {}", scenarioFilePath);

        // A file that is not valid UTF-8 is reported instead of failing, so that the scenarios are still returned
        List<String> unreadable = new ArrayList<>();
        String results = readEvaluationFile(files::readEvaluationResultsFile, unreadable);
        String improvements = readEvaluationFile(files::readArchitectureImprovementsFile, unreadable);
        List<String> evaluationProblems;
        if (!unreadable.isEmpty()) {
            evaluationProblems = unreadable;
        } else if (results == null && improvements == null) {
            evaluationProblems = List.of();
        } else {
            evaluationProblems = EvaluationFileCheck.check(results, improvements, check.scenarioIds(), files.evaluationHandedOver());
        }

        return new ArchitectureScenariosDTO(
                true,
                contents,
                check.problems(),
                check.decisionProblems(),
                files.minutesSinceScenarioFileLastModified(),
                scenarioFilePath,
                imageFilePath,
                files.abstractScenarioImageFileExists(),
                resultsFilePath,
                files.evaluationResultsFileExists(),
                results == null ? "" : results,
                improvementsFilePath,
                improvements == null ? "" : improvements,
                evaluationProblems,
                ArchitectureScenarioEvaluationSteps.evaluationGuidance(files),
                setAsideDirectoryPaths);
    }

    // A file that is read, or null when it does not exist
    @FunctionalInterface
    private interface FileReader {
        String read() throws Exception;
    }

    // The contents of an evaluation file, or null when it does not exist or cannot be read; why it cannot be read is added to the given list
    private static String readEvaluationFile(FileReader reader, List<String> unreadable) throws Exception {
        try {
            return reader.read();
        } catch (IllegalStateException e) {
            unreadable.add(e.getMessage());
            return null;
        }
    }

    private GuideDTO getScenarioEvaluationSteps(EvaluationStageDTO param) throws Exception {
        if (param.stage() == null) {
            throw new IllegalArgumentException("stage must be either evaluation or closing.");
        }

        ArchitectureScenarioFiles.Snapshot snapshot = files.snapshot();
        log.info("Architecture scenario development state for the {} stage of the evaluation: {} ({})", param.stage(), snapshot.state(), files.directory());

        return new GuideDTO(switch (param.stage()) {
            case EVALUATION -> switch (snapshot.state()) {
                case NOT_DEVELOPED -> ArchitectureScenarioEvaluationSteps.notDeveloped(files);
                case DEVELOPMENT_IN_PROGRESS -> ArchitectureScenarioEvaluationSteps.developmentInProgress(files, snapshot);
                case DEVELOPED -> ArchitectureScenarioEvaluationSteps.developed(files, snapshot);
            };
            // The closing needs fully developed scenarios and the results of its evaluation; without them, the evaluation was left out or its files were lost
            case CLOSING -> switch (snapshot.state()) {
                case NOT_DEVELOPED -> ArchitectureScenarioEvaluationSteps.nothingToClose(files);
                case DEVELOPMENT_IN_PROGRESS -> ArchitectureScenarioEvaluationSteps.developmentInProgress(files, snapshot);
                case DEVELOPED -> files.evaluationResultsFileExists()
                        ? ArchitectureScenarioEvaluationSteps.closing(files, snapshot)
                        : ArchitectureScenarioEvaluationSteps.notEvaluated(files, snapshot);
            };
        });
    }

    private SetAsideDirectoryDTO setDirectoryAside(NoInputDTO param) throws Exception {
        log.info("Set the architecture scenario analysis directory aside: {}", files.directory());
        ArchitectureScenarioFiles.SettingAside settingAside = files.setDirectoryAside();

        return new SetAsideDirectoryDTO(
                files.directory().toString(),
                settingAside.setAside(),
                settingAside.kept() == null ? "" : settingAside.kept().toString());
    }
}
