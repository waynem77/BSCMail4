package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Role;
import lombok.Data;
import lombok.NonNull;

/**
 * Represents information about a {@link Role}.
 */
@Data
public class RoleResponse
{
    private Long id;
    private String name;

    /**
     * Creates a GetRoleResponse from the given Role.
     *
     * @param role the role
     * @return a GetRoleResponse equivalent to role
     */
    public static RoleResponse fromRole(@NonNull Role role)
    {
        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setId(role.getId());
        roleResponse.setName(role.getName());

        return roleResponse;
    }
}
