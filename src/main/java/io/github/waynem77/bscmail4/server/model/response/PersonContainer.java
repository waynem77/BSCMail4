package io.github.waynem77.bscmail4.server.model.response;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import io.github.waynem77.bscmail4.server.database.repository.NoteRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonContainer extends Container<Person, PersonResponse>
{
    private Map<Long, Long> noteCounts = new HashMap<>();

    public PersonContainer(Page<Person> page)
    {
        super(page);
        // Initialize empty noteCounts for cases where repository is not provided
        this.noteCounts = new HashMap<>();
    }

    public PersonContainer(Page<Person> page, NoteRepository noteRepository)
    {
        super(page);
        
        // Extract person IDs from the page
        List<Long> personIds = page.getContent().stream()
                .map(Person::getId)
                .collect(Collectors.toList());

        // Batch fetch note counts for all persons
        this.noteCounts = personIds.stream()
                .collect(Collectors.toMap(
                        personId -> personId,
                        noteRepository::countByPersonId
                ));

        // Re-transform elements with note counts using the setter
        setElements(page.getContent().stream()
                .map(this::transformEntityToResponse)
                .collect(Collectors.toList()));
    }

    @Override
    protected PersonResponse transformEntityToResponse(Person entity)
    {
        long numberOfNotes = (noteCounts != null) ? noteCounts.getOrDefault(entity.getId(), 0L) : 0L;
        return PersonResponse.fromPerson(entity, numberOfNotes);
    }
}
