package com.astahpromcp.tool.projectdiff;

import com.astahpromcp.tool.projectdiff.export.ExportInformation;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectKind;
import com.astahpromcp.tool.projectdiff.outputdto.ProjectSideDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ReferenceProjectDTO;
import com.astahpromcp.tool.projectdiff.outputdto.ReferenceProjectStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectDiffRunnerTest {
    
    private static ProjectSideDTO side(String name) {
        return new ProjectSideDTO(ProjectKind.FILE, "C:/" + name + ".asta", name, "", "", "", "12.0.0", -1, List.of());
    }
    
    private static ExportInformation saved(int projectModelVersion, String... referenceProjects) {
        List<ReferenceProjectDTO> references = Arrays.stream(referenceProjects)
                .map(path -> new ReferenceProjectDTO(path, ReferenceProjectStatus.LATEST))
                .toList();
        return new ExportInformation(projectModelVersion, 44, "12.0.0", references, null);
    }
    
    @Test
    void formatWarnings_ok_saysNothingForTwoCurrentProjects() {
        assertTrue(ProjectDiffRunner.formatWarnings(side("A"), saved(44), side("B"), saved(44)).isEmpty());
    }
    
    @Test
    void formatWarnings_ok_givesANoteBothSidesCallForOnceForBoth() {
        List<String> warnings = ProjectDiffRunner.formatWarnings(side("SampleModel"), saved(31), side("SampleModel"), saved(31));
        
        assertEquals(1, warnings.size(), warnings::toString);
        String warning = warnings.get(0);
        assertTrue(warning.startsWith("Both the base, 'SampleModel' (model version 31), and the target, 'SampleModel' (model version 31), were saved by an older Astah"), warning);
        assertTrue(warning.contains("even when a file is compared with itself"), warning);
        assertTrue(warning.contains("from one comparison to the next"), warning);
    }
    
    @Test
    void formatWarnings_ok_saysWhichSideANoteIsAbout() {
        List<String> warnings = ProjectDiffRunner.formatWarnings(side("Old"), saved(44), side("New"), saved(31, "C:/Ref.asta"));
        
        assertEquals(2, warnings.size(), warnings::toString);
        assertTrue(warnings.get(0).startsWith("The target, 'New', was saved by an older Astah (model version 31; this Astah writes 44)"), warnings.get(0));
        assertTrue(warnings.get(1).startsWith("The target, 'New', references other projects (see 'referenceProjects' of the target)"), warnings.get(1));
    }
    
    private static ProjectSideDTO open(String path, String name) {
        return new ProjectSideDTO(ProjectKind.OPEN_PROJECT, path, name, "root", "2026-10-20T13:00:00+09:00", "2026-10-20T13:00:01+09:00", "12.0.0", -1, List.of());
    }
    
    @Test
    void formatWarnings_ok_saysNothingOfAnOpenProjectNeverSaved() {
        // Astah reports model version 0 for a project created and not saved yet, which was not loaded from any file
        assertTrue(ProjectDiffRunner.formatWarnings(side("Sample"), saved(44), open("", "no_title"), saved(0)).isEmpty());
    }
    
    @Test
    void formatWarnings_ok_saysAnOpenProjectWasLoadedFromAnOlderFile() {
        List<String> warnings = ProjectDiffRunner.formatWarnings(side("Old"), saved(43), open("C:/Old.asta", "Old"), saved(43));
        
        assertEquals(1, warnings.size(), warnings::toString);
        assertTrue(warnings.get(0).startsWith("Both the base, 'Old' (model version 43), and the target, 'Old' (model version 43), were saved by an older Astah"), warnings.get(0));
    }
    
    @Test
    void formatWarnings_ok_givesTheNoteOnReferencedProjectsOnceForBoth() {
        List<String> warnings = ProjectDiffRunner.formatWarnings(side("A"), saved(44, "C:/Ref.asta"), side("B"), saved(44, "C:/Ref.asta"));
        
        assertEquals(List.of("Both the base, 'A', and the target, 'B', reference other projects (see their 'referenceProjects')."
                + " The elements they take from them are part of their XMIs, so they appear in the result too, including changes that come only from updating the references."), warnings);
    }
    
    @Test
    void stopCheck_ok_looksOnceEveryInterval() {
        int[] looks = {0};
        ProjectDiffRunner.StopCheck check = new ProjectDiffRunner.StopCheck(() -> {
            looks[0]++;
            return false;
        }, 60, 3);
        
        for (int i = 0; i < 9; i++) {
            check.run();
        }
        
        assertEquals(3, looks[0]);
    }
    
    @Test
    void stopCheck_ok_letsAComparisonWithinItsTimeGoOn() {
        ProjectDiffRunner.StopCheck check = new ProjectDiffRunner.StopCheck(() -> false, 60, 1);
        
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 1_000; i++) {
                check.run();
            }
        });
    }
    
    @Test
    void stopCheck_ng_stopsWhenStopIsRequested() {
        ProjectDiffRunner.StopCheck check = new ProjectDiffRunner.StopCheck(() -> true, 60, 2);
        
        // The first run is not yet the one that looks
        check.run();
        IllegalStateException e = assertThrows(IllegalStateException.class, check::run);
        assertEquals("The comparison was stopped.", e.getMessage());
    }
    
    @Test
    void stopCheck_ng_stopsWhenTheThreadIsInterrupted() {
        ProjectDiffRunner.StopCheck check = new ProjectDiffRunner.StopCheck(() -> false, 60, 1);
        Thread.currentThread().interrupt();
        try {
            IllegalStateException e = assertThrows(IllegalStateException.class, check::run);
            assertEquals("The comparison was stopped.", e.getMessage());
            assertTrue(Thread.currentThread().isInterrupted(), "The interrupt is kept for whatever waits next");
        } finally {
            Thread.interrupted();
        }
    }
    
    @Test
    void stopCheck_ng_failsWhenTheTimeRunsOut() {
        ProjectDiffRunner.StopCheck check = new ProjectDiffRunner.StopCheck(() -> false, 0, 1);
        
        IllegalStateException e = assertThrows(IllegalStateException.class, check::run);
        assertEquals("Reading and comparing the XMIs took longer than 0 seconds.", e.getMessage());
    }
    
    @Test
    void stopCheck_ng_reportsAStopRatherThanTheTimeRunningOut() {
        ProjectDiffRunner.StopCheck check = new ProjectDiffRunner.StopCheck(() -> true, 0, 1);
        
        IllegalStateException e = assertThrows(IllegalStateException.class, check::run);
        assertEquals("The comparison was stopped.", e.getMessage());
    }
}
