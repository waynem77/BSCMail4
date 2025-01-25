package io.github.waynem77.bscmail4.model.request;

import io.github.waynem77.bscmail4.model.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * A request to create a new {@link Permission}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class CreatePermissionRequest
{
    private String name;

    /**
     * Creates a new Permission from the CreatePermissionRequest.
     *
     * @return the new Permission
     */
    public Permission toPermission()
    {
        Permission permission = new Permission();
        permission.setName(name);
        return permission;
    }
}
