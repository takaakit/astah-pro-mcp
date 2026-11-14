package com.astahpromcp.tool.projectdiff.outputdto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiffResultDTOTest {
    
    private static final ProjectSideDTO SIDE = new ProjectSideDTO(ProjectKind.FILE, "", "", "", "", "", "", -1, List.of());
    
    private static DiffResultDTO header() {
        return DiffResultDTO.withoutPage("d1-abc", DiffStatus.DONE, "", DiffFailureDTO.empty(), SIDE, SIDE,
                new NormalizationDTO(2, false, List.of()), List.of(), List.of(), SummaryDTO.empty(), "result.json");
    }
    
    // An answer without a page starts at the beginning of the result, holds no changes, and says nothing comes after it
    @Test
    void withoutPage_ok_holdsNoPage() {
        DiffResultDTO answer = header();
        
        assertEquals(0, answer.startIndex());
        assertEquals(0, answer.startPropertyIndex());
        assertEquals(-1, answer.nextIndex());
        assertEquals(0, answer.nextPropertyIndex());
        assertEquals(List.of(), answer.changes());
        assertFalse(answer.truncated());
        assertFalse(answer.shortened());
    }
    
    // 'truncated' follows from 'nextIndex', which is how the guide tells an agent whether to ask for the next page
    @Test
    void withPage_ok_isTruncatedExactlyWhenANextPageStarts() {
        DiffResultDTO frame = header().startingAt("2026-10-31T00:00+09:00", 3, 1);
        
        DiffResultDTO last = frame.withPage(SummaryDTO.empty(), -1, 0, List.of());
        DiffResultDTO more = frame.withPage(SummaryDTO.empty(), 5, 2, List.of());
        
        assertFalse(last.truncated(), "A page after which no page starts must not be truncated");
        assertTrue(more.truncated(), "A page after which another page starts must be truncated");
        assertEquals(3, more.startIndex());
        assertEquals(1, more.startPropertyIndex());
        assertEquals(5, more.nextIndex());
        assertEquals(2, more.nextPropertyIndex());
        assertEquals("2026-10-31T00:00+09:00", more.expiresAt());
    }
}
