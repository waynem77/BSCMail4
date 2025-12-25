package io.github.waynem77.bscmail4.server.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new Person.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePersonRequest
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
}

