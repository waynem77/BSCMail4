package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.ShiftTemplate;
import lombok.Data;
import lombok.NonNull;

/**
 * Represents information about a {@link ShiftTemplate}.
 */
@Data
public class ShiftTemplateResponse
{
    private Long id;
    private String name;
    private Long requiredPermissionId;

    /**
     * Creates a ShiftTemplateResponse from the given ShiftTemplate.
     *
     * @param shiftTemplate the shift template
     * @return a ShiftTemplateResponse equivalent to shiftTemplate
     */
    public static ShiftTemplateResponse fromShiftTemplate(@NonNull ShiftTemplate shiftTemplate)
    {
        ShiftTemplateResponse response = new ShiftTemplateResponse();
        response.setId(shiftTemplate.getId());
        response.setName(shiftTemplate.getName());
        response.setRequiredPermissionId(shiftTemplate.getRequiredPermissionId());

        return response;
    }
}
