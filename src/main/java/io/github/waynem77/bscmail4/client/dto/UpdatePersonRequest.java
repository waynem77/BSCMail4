package io.github.waynem77.bscmail4.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing Person.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonRequest
{
    /**
     * The person's full name.
     */
    private String name;

    /**
     * The person's email address.
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
}

