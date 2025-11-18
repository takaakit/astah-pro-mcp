package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.change_vision.jude.api.inf.model.IAction;
import com.change_vision.jude.api.inf.model.IInputPin;
import com.change_vision.jude.api.inf.model.IOutputPin;
import lombok.NonNull;
import com.astahpromcp.tool.astah.pro.model.outputdto.ActionDTO;

import java.util.ArrayList;
import java.util.List;

public class ActionDTOAssembler {
    public static ActionDTO toDTO(@NonNull IAction astahAction) throws Exception {

        NameIdTypeDTO callingActivity;
        if (astahAction.getCallingActivity() != null) {
            callingActivity = NameIdTypeDTOAssembler.toDTO(astahAction.getCallingActivity());
        } else {
            callingActivity = NameIdTypeDTO.empty();
        }

        List<NameIdTypeDTO> inputPins = new ArrayList<>();
        for (IInputPin inputPin : astahAction.getInputs()) {
            inputPins.add(NameIdTypeDTOAssembler.toDTO(inputPin));
        }
        
        List<NameIdTypeDTO> outputPins = new ArrayList<>();
        for (IOutputPin outputPin : astahAction.getOutputs()) {
            outputPins.add(NameIdTypeDTOAssembler.toDTO(outputPin));
        }

        return new ActionDTO(
            ActivityNodeDTOAssembler.toDTO(astahAction),
            callingActivity,
            inputPins,
            outputPins,
            astahAction.isAcceptEventAction(),
            astahAction.isAcceptTimeEventAction(),
            astahAction.isCallBehaviorAction(),
            astahAction.isConnector(),
            astahAction.isProcess(),
            astahAction.isSendSignalAction());
    }
}
