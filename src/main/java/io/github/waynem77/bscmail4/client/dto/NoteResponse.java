package io.github.waynem77.bscmail4.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO representing a Note entity for JSON deserialization from server API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse
{
    /**
     * Unique identifier for the note.
     */
    private Long id;

    /**
     * The content/text of the note.
     */
    private String value;

    /**
     * The timestamp when the note was created.
     */
    private Instant createdAt;

    /**
     * The ID of the person this note belongs to.
     */
    private Long personId;
}
