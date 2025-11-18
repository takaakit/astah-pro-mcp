package com.astahpromcp.tool.astah.pro.common.outputdto.assembler;

import lombok.NonNull;
import java.awt.geom.Rectangle2D;
import com.astahpromcp.tool.astah.pro.common.outputdto.RectangleDTO;

public class RectangleDTOAssembler {
    public static RectangleDTO toDTO(@NonNull Rectangle2D astahRectangle) throws Exception {

        return new RectangleDTO(
            astahRectangle.getX(),
            astahRectangle.getY(),
            astahRectangle.getWidth(),
            astahRectangle.getHeight());
    }
}
