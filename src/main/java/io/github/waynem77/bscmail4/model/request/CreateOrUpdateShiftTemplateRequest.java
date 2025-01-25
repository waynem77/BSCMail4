package io.github.waynem77.bscmail4.model.request;

import lombok.Data;

/**
 * A request to create or update a {@link io.github.waynem77.bscmail4.model.entity.ShiftTemplate}.
 */
@Data
public class CreateOrUpdateShiftTemplateRequest
{
    private String name;
    private Long requiredPermissionId;
}
