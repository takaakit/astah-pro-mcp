package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.change_vision.jude.api.inf.model.ITestCase;
import lombok.NonNull;

public class TestCaseDTOAssembler {
    public static TestCaseDTO toDTO(@NonNull ITestCase testCase) throws Exception {
        return new TestCaseDTO(
            ClassDTOAssembler.toDTO(testCase),
            testCase.getTestCaseID()
        );
    }
}
