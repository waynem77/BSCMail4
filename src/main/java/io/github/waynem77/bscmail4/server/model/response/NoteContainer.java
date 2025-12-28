package io.github.waynem77.bscmail4.server.model.response;

import io.github.waynem77.bscmail4.server.database.entity.Note;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class NoteContainer extends Container<Note, NoteResponse>
{
    public NoteContainer(Page<Note> page)
    {
        super(page);
    }

    @Override
    protected NoteResponse transformEntityToResponse(Note entity)
    {
        return NoteResponse.fromNote(entity);
    }
}
