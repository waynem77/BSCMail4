package io.github.waynem77.bscmail4.server.model.response;

import io.github.waynem77.bscmail4.server.database.entity.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO representing a Note entity for JSON serialization.
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

    /**
     * Creates a NoteResponse from a Note entity.
     *
     * @param note the Note entity to convert
     * @return a NoteResponse corresponding to the given Note, or null if note is null
     */
    public static NoteResponse fromNote(Note note)
    {
        if (note == null)
        {
            return null;
        }
        return NoteResponse.builder()
                .id(note.getId())
                .value(note.getValue())
                .createdAt(note.getCreatedAt())
                .personId(note.getPerson() != null ? note.getPerson().getId() : null)
                .build();
    }
}
