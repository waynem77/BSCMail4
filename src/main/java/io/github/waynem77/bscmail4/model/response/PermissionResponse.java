package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Permission;
import lombok.Data;
import lombok.NonNull;

/**
 * Represents information about a {@link Permission}.
 */
@Data
public class PermissionResponse
{
    private Long id;
    private String name;

    /**
     * Creates a PermissionResponse from the given Permission.
     *
     * @param permission the permission
     * @return a PermissionResponse equivalent to permission
     */
    public static PermissionResponse fromPermission(@NonNull Permission permission)
    {
        PermissionResponse permissionResponse = new PermissionResponse();
        permissionResponse.setId(permission.getId());
        permissionResponse.setName(permission.getName());

        return permissionResponse;
    }
}
