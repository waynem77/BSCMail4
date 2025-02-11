package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.entity.Person;
import lombok.Data;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents information about a {@link Person}.
 */
@Data
public class PersonResponse
{
    private Long id;
    private String name;
    private String emailAddress;
    private String phone;
    private List<PermissionResponse> permissions;
    private List<GroupResponse> groups;
    private Boolean active;
}
