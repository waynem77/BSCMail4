package io.github.waynem77.bscmail4.model.request;

import lombok.Data;

import java.util.List;

/**
 * A request to update the permissions of a Group.
 * <p>
 * Valid values for the action field are "add" and "remove". See {@link io.github.waynem77.bscmail4.model.UpdateAction}.
 * <p>
 * The permissionIds field is a list of ids of Permission objects to either add or remove from the Group. The field
 * may be empty, but it may not be null.
 */
@Data
public class UpdatePermissionsRequest
{
    private String action;
    private List<Long> permissionIds;
}
