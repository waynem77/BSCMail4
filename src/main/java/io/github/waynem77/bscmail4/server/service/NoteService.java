package io.github.waynem77.bscmail4.server.service;

import io.github.waynem77.bscmail4.server.database.entity.Note;
import io.github.waynem77.bscmail4.server.database.entity.Person;
import io.github.waynem77.bscmail4.server.database.repository.NoteRepository;
import io.github.waynem77.bscmail4.server.database.repository.PersonRepository;
import io.github.waynem77.bscmail4.server.model.SortDirection;
import io.github.waynem77.bscmail4.server.model.request.CreateNoteRequest;
import io.github.waynem77.bscmail4.server.model.response.NoteContainer;
import io.github.waynem77.bscmail4.server.model.response.NoteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Note entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService
{
    private final NoteRepository noteRepository;
    private final PersonRepository personRepository;

    /**
     * Creates a new Note entity for the given person.
     *
     * @param personId the ID of the person to create the note for
     * @param request  the request containing note data
     * @return the created and saved Note as a NoteResponse
     * @throws NotFoundException if no person exists with the given ID
     */
    @Transactional
    public NoteResponse createNote(Long personId, CreateNoteRequest request)
    {
        log.info("Creating note for person. personId={}, request={}", personId, request);
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> {
                    log.error("Person not found. id={}", personId);
                    return new NotFoundException("Person not found.");
                });

        Note note = Note.builder()
                .value(request.getValue())
                .person(person)
                .build();

        Note savedNote = noteRepository.save(note);
        // Build response directly with personId from loaded person to avoid lazy loading issues
        return NoteResponse.builder()
                .id(savedNote.getId())
                .value(savedNote.getValue())
                .createdAt(savedNote.getCreatedAt())
                .personId(person.getId())
                .build();
    }

    /**
     * Retrieves a Note entity by its ID, ensuring it belongs to the specified person.
     *
     * @param personId the ID of the person who should own the note
     * @param noteId   the ID of the note to retrieve
     * @return the Note as a NoteResponse
     * @throws NotFoundException   if no note exists with the given ID
     * @throws BadRequestException if the note exists but does not belong to the specified person
     */
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long personId, Long noteId)
    {
        log.info("Getting note by ID. personId={}, noteId={}", personId, noteId);
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> {
                    log.error("Note not found. id={}", noteId);
                    return new NotFoundException("Note not found.");
                });

        if (!note.getPerson().getId().equals(personId))
        {
            log.error("Note does not belong to person. noteId={}, notePersonId={}, requestedPersonId={}",
                    noteId, note.getPerson().getId(), personId);
            throw new BadRequestException("Note does not belong to the specified person.");
        }

        return NoteResponse.builder()
                .id(note.getId())
                .value(note.getValue())
                .createdAt(note.getCreatedAt())
                .personId(note.getPerson().getId())
                .build();
    }

    /**
     * Retrieves a paginated list of Note entities for the specified person.
     *
     * @param personId  the ID of the person to retrieve notes for
     * @param page      the page number (0-based)
     * @param size      the page size
     * @param direction the sort direction (must be a valid SortDirection value)
     * @return a NoteContainer containing the paginated results
     * @throws NotFoundException   if no person exists with the given ID
     * @throws BadRequestException if direction contains an invalid value
     */
    @Transactional(readOnly = true)
    public NoteContainer getNotes(Long personId, int page, int size, String direction)
    {
        log.info("Getting notes for person. personId={}, page={}, size={}, direction={}", personId, page, size,
                direction);

        // Validate person exists
        if (!personRepository.existsById(personId))
        {
            log.error("Person not found. id={}", personId);
            throw new NotFoundException("Person not found.");
        }

        // Validate and convert direction
        SortDirection sortDirection = SortDirection.fromString(direction).orElseThrow(
                () -> {
                    log.error("Invalid direction value. direction={}", direction);
                    return new BadRequestException("Invalid sort direction");
                }
        );

        // Build sort (always by createdAt)
        Sort sort = Sort.by(sortDirection.toSpringSortDirection(), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        // Execute query
        Page<Note> notePage = noteRepository.findByPersonId(personId, pageable);

        return new NoteContainer(notePage);
    }
}
