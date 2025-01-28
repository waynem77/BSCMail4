package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Person;
import lombok.Data;
import lombok.NonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents information about a {@link io.github.waynem77.bscmail4.model.entity.Group}.
 */
@Data
public class GroupResponse
{
    private Long id;
    private String name;
    private Long memberCount;
    private Long activeMemberCount;
    private List<PermissionResponse> permissions;
    private List<String> messages;

    /**
     * Creates a GroupResponse from a Group.
     *
     * @param group the group
     * @return an equivalent GroupResponse
     */
    public static GroupResponse fromGroup(@NonNull Group group)
    {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setMemberCount(group.getPeople() != null ?
                group.getPeople().size() :
                0L);
        response.setActiveMemberCount(group.getPeople() != null ?
                group.getPeople().stream().filter(Person::getActive).count() :
                0L);
        response.setPermissions(group.getPermissions() != null ?
                group.getPermissions().stream()
                        .map(PermissionResponse::fromPermission)
                        .sorted(Comparator.comparing(PermissionResponse::getName))
                        .toList() :
                Collections.emptyList());
        response.setMessages(Collections.emptyList());
        return response;
    }
}
