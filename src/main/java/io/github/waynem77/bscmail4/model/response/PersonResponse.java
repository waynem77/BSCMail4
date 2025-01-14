package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.entity.Role;
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
    private List<RoleResponse> roles;
    private Boolean active;


    /**
     * Creates a PersonResponse from the given Person.
     *
     * @param person the person
     * @return a PersonResponse equivalent to person
     */
    public static PersonResponse fromPerson(@NonNull Person person)
    {
        PersonResponse response = new PersonResponse();
        response.setId(person.getId());
        response.setName(person.getName());
        response.setEmailAddress(person.getEmailAddress());
        response.setPhone(person.getPhone());
        response.setRoles(
                (person.getRoles() != null ? person.getRoles().stream() : Stream.<Role>empty())
                        .map(RoleResponse::fromRole)
                        .sorted(Comparator.comparing(RoleResponse::getName))
                        .toList());
        response.setActive(person.getActive());

        return response;
    }
}
