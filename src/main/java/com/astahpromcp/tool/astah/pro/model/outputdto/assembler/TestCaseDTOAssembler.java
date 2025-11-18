package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.change_vision.jude.api.inf.model.ITestCase;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.TestCaseDTO;

public class TestCaseDTOAssembler {
    public static TestCaseDTO toDTO(@NonNull ITestCase testCase) throws Exception {
        return new TestCaseDTO(
            ClassDTOAssembler.toDTO(testCase),
            testCase.getTestCaseID()
        );
    }
}
