package io.github.waynem77.bscmail4.server.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * The person's email address.
     */
    @NotBlank(message = "Email address is required")
    private String emailAddress;

    /**
     * The person's phone number (optional).
     */
    private String phone;

    /**
     * Whether the person is currently active in the system.
     */
    @NotNull(message = "isActive is required")
    private Boolean isActive;
}

