package io.github.waynem77.bscmail4.model.request;

import io.github.waynem77.bscmail4.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * A request to create a new {@link Role}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class CreateRoleRequest
{
    private String name;

    /**
     * Creates a new Role from the CreateRoleRequest.
     *
     * @return the new role
     */
    public Role toRole()
    {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}
