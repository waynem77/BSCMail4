package io.github.waynem77.bscmail4.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO representing a Person entity for JSON deserialization from server API.
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
}

