package com.astahpromcp.tool.astah.pro.model.outputdto.assembler;

import com.astahpromcp.tool.astah.pro.common.outputdto.NameIdTypeDTO;
import com.astahpromcp.tool.astah.pro.common.outputdto.assembler.NameIdTypeDTOAssembler;
import com.astahpromcp.tool.astah.pro.model.outputdto.AttributeDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ConnectorDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.PortDTO;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IConnector;
import com.change_vision.jude.api.inf.model.IPort;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ConnectorDTOAssembler {
    public static ConnectorDTO toDTO(@NonNull IConnector astahConnector) throws Exception {
        List<AttributeDTO> parts = new ArrayList<>();
        for (IAttribute part : astahConnector.getParts()) {
            // Note: Some elements may be null (probably an API bug), so add a null check.
            if (part != null) {
                parts.add(AttributeDTOAssembler.toDTO(part));
            }
        }

        List<PortDTO> ports = new ArrayList<>();
        for (IPort port : astahConnector.getPorts()) {
            ports.add(PortDTOAssembler.toDTO(port));
        }

        NameIdTypeDTO type;
        try {
            type = NameIdTypeDTOAssembler.toDTO(astahConnector.getType());
        } catch (Exception e) {
            type = NameIdTypeDTO.empty();
        }

        return new ConnectorDTO(
            NamedElementDTOAssembler.toDTO(astahConnector),
            parts,
            ports,
            type);
    }
}
