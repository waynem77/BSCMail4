package io.github.waynem77.bscmail4.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * A request to create or update a {@link io.github.waynem77.bscmail4.model.entity.Group} object.
 */
@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrUpdateGroupRequest
{
    private String name;
}
