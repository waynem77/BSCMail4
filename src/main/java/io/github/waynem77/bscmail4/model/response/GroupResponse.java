package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
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
}
