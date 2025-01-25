package io.github.waynem77.bscmail4.model.request;

import io.github.waynem77.bscmail4.model.entity.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

/**
 * A request to create or update a {@link Person}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class CreateOrUpdatePersonRequest
{
    private String name;
    private String emailAddress;
    private String phone;
    private List<Long> permissionIds;
    private Boolean active;
}
