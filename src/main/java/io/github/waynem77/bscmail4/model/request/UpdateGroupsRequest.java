package io.github.waynem77.bscmail4.model.request;

import lombok.Data;

import java.util.List;

/**
 * A request to update the groups of a Person.
 * <p>
 * Valid values for the action field are "add" and "remove". See {@link io.github.waynem77.bscmail4.model.UpdateAction}.
 * <p>
 * The groupIds field is a list of ids of Group objects to either add or remove from the Person. The field
 * may be empty, but it may not be null.
 */
@Data
public class UpdateGroupsRequest
{
    private String action;
    private List<Long> groupIds;
}
