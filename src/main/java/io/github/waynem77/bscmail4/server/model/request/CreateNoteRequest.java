package io.github.waynem77.bscmail4.server.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new Note.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteRequest
{
    /**
     * The content/text of the note.
     */
    @NotBlank(message = "Value is required")
    private String value;
}
