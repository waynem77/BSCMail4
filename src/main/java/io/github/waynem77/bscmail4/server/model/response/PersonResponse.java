package io.github.waynem77.bscmail4.server.model.response;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO representing a Person entity for JSON serialization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponse
{
    /**
     * Unique identifier for the person.
     */
    private Long id;

    /**
     * The person's full name.
     */
    private String name;

    /**
     * The person's email address (optional).
     */
    private String emailAddress;

    /**
     * The person's phone number (optional).
     */
    private String phone;

    /**
     * Whether the person is currently active in the system.
     */
    private Boolean isActive;

    /**
     * The timestamp when the person was created.
     */
    private Instant createdAt;

    /**
     * Creates a PersonResponse from a Person entity.
     *
     * @param person the Person entity to convert
     * @return a PersonResponse corresponding to the given Person, or null if person is null
     */
    public static PersonResponse fromPerson(Person person)
    {
        if (person == null)
        {
            return null;
        }
        return PersonResponse.builder()
                .id(person.getId())
                .name(person.getName())
                .emailAddress(person.getEmailAddress())
                .phone(person.getPhone())
                .isActive(person.getIsActive())
                .createdAt(person.getCreatedAt())
                .build();
    }
}

