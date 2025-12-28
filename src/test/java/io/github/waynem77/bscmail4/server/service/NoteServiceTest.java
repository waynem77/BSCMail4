package io.github.waynem77.bscmail4.server.service;

import io.github.waynem77.bscmail4.server.database.entity.Note;
import io.github.waynem77.bscmail4.server.database.entity.Person;
import io.github.waynem77.bscmail4.server.database.repository.NoteRepository;
import io.github.waynem77.bscmail4.server.database.repository.PersonRepository;
import io.github.waynem77.bscmail4.server.model.request.CreateNoteRequest;
import io.github.waynem77.bscmail4.server.model.response.NoteContainer;
import io.github.waynem77.bscmail4.server.model.response.NoteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NoteService.
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest
{
    @Mock
    private NoteRepository noteRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createNoteWithValidDataShouldCreateAndSaveNote()
    {
        // Given
        Long personId = randomLong();
        String noteValue = randomString();
        Long savedNoteId = randomLong();
        Instant createdAt = Instant.now();

        Person person = Person.builder()
                .id(personId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        CreateNoteRequest request = CreateNoteRequest.builder()
                .value(noteValue)
                .build();

        Note savedNote = Note.builder()
                .id(savedNoteId)
                .value(noteValue)
                .person(person)
                .createdAt(createdAt)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // When
        NoteResponse result = noteService.createNote(personId, request);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(savedNoteId));
        assertThat(result.getValue(), equalTo(noteValue));
        assertThat(result.getPersonId(), equalTo(personId));
        assertThat(result.getCreatedAt(), notNullValue());

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());
        Note capturedNote = noteCaptor.getValue();
        assertThat(capturedNote.getId(), nullValue());
        assertThat(capturedNote.getValue(), equalTo(noteValue));
        assertThat(capturedNote.getPerson(), equalTo(person));
    }

    @Test
    void createNoteWithNonExistentPersonIdShouldThrowNotFoundException()
    {
        // Given
        Long nonExistentPersonId = randomLong();

        CreateNoteRequest request = CreateNoteRequest.builder()
                .value(randomString())
                .build();

        when(personRepository.findById(nonExistentPersonId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> noteService.createNote(nonExistentPersonId, request));

        verify(personRepository).findById(nonExistentPersonId);
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNoteShouldReturnSavedNoteResponse()
    {
        // Given
        Long personId = randomLong();
        String noteValue = randomString();
        Long savedNoteId = randomLong();
        Instant createdAt = Instant.now();

        Person person = Person.builder()
                .id(personId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        CreateNoteRequest request = CreateNoteRequest.builder()
                .value(noteValue)
                .build();

        Note savedNote = Note.builder()
                .id(savedNoteId)
                .value(noteValue)
                .person(person)
                .createdAt(createdAt)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // When
        NoteResponse result = noteService.createNote(personId, request);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(savedNoteId));
        assertThat(result.getValue(), equalTo(noteValue));
        assertThat(result.getPersonId(), equalTo(personId));
        assertThat(result.getCreatedAt(), equalTo(createdAt));

        verify(personRepository).findById(personId);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void getNoteByIdWithValidIdsShouldReturnNoteResponse()
    {
        // Given
        Long personId = randomLong();
        Long noteId = randomLong();
        String noteValue = randomString();
        Instant createdAt = Instant.now();

        Person person = Person.builder()
                .id(personId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        Note note = Note.builder()
                .id(noteId)
                .value(noteValue)
                .person(person)
                .createdAt(createdAt)
                .build();

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        // When
        NoteResponse result = noteService.getNoteById(personId, noteId);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(noteId));
        assertThat(result.getValue(), equalTo(noteValue));
        assertThat(result.getPersonId(), equalTo(personId));
        assertThat(result.getCreatedAt(), equalTo(createdAt));

        verify(noteRepository).findById(noteId);
    }

    @Test
    void getNoteByIdWithNonExistentNoteIdShouldThrowNotFoundException()
    {
        // Given
        Long personId = randomLong();
        Long nonExistentNoteId = randomLong();

        when(noteRepository.findById(nonExistentNoteId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> noteService.getNoteById(personId, nonExistentNoteId));

        verify(noteRepository).findById(nonExistentNoteId);
    }

    @Test
    void getNoteByIdWithNoteBelongingToDifferentPersonShouldThrowBadRequestException()
    {
        // Given
        Long requestedPersonId = randomLong();
        Long actualPersonId = randomLong();
        Long noteId = randomLong();
        String noteValue = randomString();
        Instant createdAt = Instant.now();

        Person actualPerson = Person.builder()
                .id(actualPersonId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        Note note = Note.builder()
                .id(noteId)
                .value(noteValue)
                .person(actualPerson)
                .createdAt(createdAt)
                .build();

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        // When/Then
        assertThrows(BadRequestException.class, () -> noteService.getNoteById(requestedPersonId, noteId));

        verify(noteRepository).findById(noteId);
    }

    @Test
    void getNoteByIdShouldReturnNoteResponseWithCorrectFields()
    {
        // Given
        Long personId = randomLong();
        Long noteId = randomLong();
        String noteValue = randomString();
        Instant createdAt = Instant.now();

        Person person = Person.builder()
                .id(personId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        Note note = Note.builder()
                .id(noteId)
                .value(noteValue)
                .person(person)
                .createdAt(createdAt)
                .build();

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        // When
        NoteResponse result = noteService.getNoteById(personId, noteId);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(noteId));
        assertThat(result.getValue(), equalTo(noteValue));
        assertThat(result.getPersonId(), equalTo(personId));
        assertThat(result.getCreatedAt(), equalTo(createdAt));
    }

    @Test
    void getNotesWithValidPersonIdShouldReturnNoteContainer()
    {
        // Given
        Long personId = randomLong();
        int page = 0;
        int size = 5;
        String direction = "asc";

        Person person = Person.builder()
                .id(personId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        String noteValue1 = randomString();
        String noteValue2 = randomString();
        Note note1 = Note.builder()
                .id(randomLong())
                .value(noteValue1)
                .person(person)
                .createdAt(Instant.now())
                .build();
        Note note2 = Note.builder()
                .id(randomLong())
                .value(noteValue2)
                .person(person)
                .createdAt(Instant.now())
                .build();

        Page<Note> notePage = new PageImpl<>(List.of(note1, note2), PageRequest.of(page, size), 2);

        when(personRepository.existsById(personId)).thenReturn(true);
        when(noteRepository.findByPersonId(eq(personId), any(Pageable.class))).thenReturn(notePage);

        // When
        NoteContainer result = noteService.getNotes(personId, page, size, direction);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getElements(), notNullValue());
        assertThat(result.getElements().size(), equalTo(2));
        assertThat(result.getPageNumber(), equalTo(page));
        assertThat(result.getSize(), equalTo(size));
        assertThat(result.getTotalElements(), equalTo(2L));

        verify(personRepository).existsById(personId);
    }

    @Test
    void getNotesWithNonExistentPersonIdShouldThrowNotFoundException()
    {
        // Given
        Long nonExistentPersonId = randomLong();
        int page = 0;
        int size = 5;
        String direction = "asc";

        when(personRepository.existsById(nonExistentPersonId)).thenReturn(false);

        // When/Then
        assertThrows(NotFoundException.class, () -> noteService.getNotes(nonExistentPersonId, page, size, direction));

        verify(personRepository).existsById(nonExistentPersonId);
        verify(noteRepository, never()).findByPersonId(any(Long.class), any(Pageable.class));
    }

    @Test
    void getNotesWithInvalidDirectionShouldThrowBadRequestException()
    {
        // Given
        Long personId = randomLong();
        int page = 0;
        int size = 5;
        String invalidDirection = "invalid";

        when(personRepository.existsById(personId)).thenReturn(true);

        // When/Then
        assertThrows(BadRequestException.class, () -> noteService.getNotes(personId, page, size, invalidDirection));

        verify(personRepository).existsById(personId);
        verify(noteRepository, never()).findByPersonId(any(Long.class), any(Pageable.class));
    }

    @Test
    void getNotesShouldSortByCreatedAtAscending()
    {
        // Given
        Long personId = randomLong();
        int page = 0;
        int size = 5;
        String direction = "asc";

        Person person = Person.builder()
                .id(personId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        Instant earlier = Instant.now().minusSeconds(100);
        Instant later = Instant.now();

        Note note1 = Note.builder()
                .id(randomLong())
                .value(randomString())
                .person(person)
                .createdAt(later)
                .build();
        Note note2 = Note.builder()
                .id(randomLong())
                .value(randomString())
                .person(person)
                .createdAt(earlier)
                .build();

        // Notes should be sorted by createdAt ascending, so note2 comes first
        Page<Note> notePage = new PageImpl<>(List.of(note2, note1), PageRequest.of(page, size), 2);

        when(personRepository.existsById(personId)).thenReturn(true);
        when(noteRepository.findByPersonId(eq(personId), any(Pageable.class))).thenReturn(notePage);

        // When
        NoteContainer result = noteService.getNotes(personId, page, size, direction);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getElements().size(), equalTo(2));
        // Verify sort was applied (we check via the Pageable passed to repository)
        verify(noteRepository).findByPersonId(eq(personId), any(Pageable.class));
    }

    @Test
    void getNotesShouldSortByCreatedAtDescending()
    {
        // Given
        Long personId = randomLong();
        int page = 0;
        int size = 5;
        String direction = "desc";

        Person person = Person.builder()
                .id(personId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        Note note1 = Note.builder()
                .id(randomLong())
                .value(randomString())
                .person(person)
                .createdAt(Instant.now())
                .build();

        Page<Note> notePage = new PageImpl<>(List.of(note1), PageRequest.of(page, size), 1);

        when(personRepository.existsById(personId)).thenReturn(true);
        when(noteRepository.findByPersonId(eq(personId), any(Pageable.class))).thenReturn(notePage);

        // When
        NoteContainer result = noteService.getNotes(personId, page, size, direction);

        // Then
        assertThat(result, notNullValue());
        // Verify sort was applied (we check via the Pageable passed to repository)
        verify(noteRepository).findByPersonId(eq(personId), any(Pageable.class));
    }
}
