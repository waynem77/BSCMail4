package io.github.waynem77.bscmail4.server.service;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import io.github.waynem77.bscmail4.server.database.repository.NoteRepository;
import io.github.waynem77.bscmail4.server.database.repository.PersonRepository;
import io.github.waynem77.bscmail4.server.model.request.CreatePersonRequest;
import io.github.waynem77.bscmail4.server.model.request.UpdatePersonRequest;
import io.github.waynem77.bscmail4.server.model.response.PersonResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PersonService.
 */
@ExtendWith(MockitoExtension.class)
class PersonServiceTest
{
    @Mock
    private PersonRepository personRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private PersonService personService;

    @Test
    void createPersonWithAllFieldsShouldCreateAndSavePersonWithAllFields()
    {
        // Given
        String name = randomString();
        String emailAddress = randomStringWithSuffix("@example.com");
        String phone = randomString();
        Long savedId = randomLong();

        CreatePersonRequest request = CreatePersonRequest.builder()
                .name(name)
                .emailAddress(emailAddress)
                .phone(phone)
                .build();

        Person savedPerson = Person.builder()
                .id(savedId)
                .name(name)
                .emailAddress(emailAddress)
                .phone(phone)
                .isActive(true)
                .build();

        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);
        when(noteRepository.countByPersonId(savedId)).thenReturn(0L);

        // When
        PersonResponse result = personService.createPerson(request);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(savedId));
        assertThat(result.getName(), equalTo(name));
        assertThat(result.getEmailAddress(), equalTo(emailAddress));
        assertThat(result.getPhone(), equalTo(phone));
        assertThat(result.getIsActive(), equalTo(true));

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person capturedPerson = personCaptor.getValue();
        assertThat(capturedPerson.getId(), nullValue());
        assertThat(capturedPerson.getName(), equalTo(name));
        assertThat(capturedPerson.getEmailAddress(), equalTo(emailAddress));
        assertThat(capturedPerson.getPhone(), equalTo(phone));
        assertThat(capturedPerson.getIsActive(), equalTo(true));
    }

    @Test
    void createPersonWithNullPhoneShouldCreatePersonWithNullPhone()
    {
        // Given
        String name = randomString();
        String emailAddress = randomStringWithSuffix("@example.com");
        Long savedId = randomLong();

        CreatePersonRequest request = CreatePersonRequest.builder()
                .name(name)
                .emailAddress(emailAddress)
                .phone(null)
                .build();

        Person savedPerson = Person.builder()
                .id(savedId)
                .name(name)
                .emailAddress(emailAddress)
                .phone(null)
                .isActive(true)
                .build();

        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);
        when(noteRepository.countByPersonId(savedId)).thenReturn(0L);

        // When
        PersonResponse result = personService.createPerson(request);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(savedId));
        assertThat(result.getName(), equalTo(name));
        assertThat(result.getEmailAddress(), equalTo(emailAddress));
        assertThat(result.getPhone(), nullValue());
        assertThat(result.getIsActive(), equalTo(true));

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person capturedPerson = personCaptor.getValue();
        assertThat(capturedPerson.getName(), equalTo(name));
        assertThat(capturedPerson.getEmailAddress(), equalTo(emailAddress));
        assertThat(capturedPerson.getPhone(), nullValue());
        assertThat(capturedPerson.getIsActive(), equalTo(true));
    }

    @Test
    void createPersonShouldAlwaysSetIsActiveToTrue()
    {
        // Given
        String name = randomString();
        String emailAddress = randomStringWithSuffix("@example.com");
        Long savedId = randomLong();

        CreatePersonRequest request = CreatePersonRequest.builder()
                .name(name)
                .emailAddress(emailAddress)
                .phone(randomString())
                .build();

        Person savedPerson = Person.builder()
                .id(savedId)
                .name(name)
                .emailAddress(emailAddress)
                .phone(request.getPhone())
                .isActive(true)
                .build();

        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);
        when(noteRepository.countByPersonId(savedId)).thenReturn(0L);

        // When
        PersonResponse result = personService.createPerson(request);

        // Then
        assertThat(result.getIsActive(), equalTo(true));

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person capturedPerson = personCaptor.getValue();
        assertThat(capturedPerson.getIsActive(), equalTo(true));
    }

    @Test
    void createPersonShouldReturnSavedPerson()
    {
        // Given
        String name = randomString();
        String emailAddress = randomStringWithSuffix("@example.com");
        Long savedId = randomLong();

        CreatePersonRequest request = CreatePersonRequest.builder()
                .name(name)
                .emailAddress(emailAddress)
                .phone(randomString())
                .build();

        Person savedPerson = Person.builder()
                .id(savedId)
                .name(name)
                .emailAddress(emailAddress)
                .phone(request.getPhone())
                .isActive(true)
                .build();

        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);
        when(noteRepository.countByPersonId(savedId)).thenReturn(0L);

        // When
        PersonResponse result = personService.createPerson(request);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(savedId));
        assertThat(result.getName(), equalTo(name));
        assertThat(result.getEmailAddress(), equalTo(emailAddress));
        assertThat(result.getPhone(), equalTo(request.getPhone()));
        assertThat(result.getIsActive(), equalTo(true));
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void getPersonByIdWithExistingIdShouldReturnPerson()
    {
        // Given
        Long personId = randomLong();
        String name = randomString();
        String emailAddress = randomStringWithSuffix("@example.com");
        String phone = randomString();

        Person person = Person.builder()
                .id(personId)
                .name(name)
                .emailAddress(emailAddress)
                .phone(phone)
                .isActive(true)
                .build();

        when(personRepository.findById(personId)).thenReturn(java.util.Optional.of(person));
        when(noteRepository.countByPersonId(personId)).thenReturn(0L);

        // When
        PersonResponse result = personService.getPersonById(personId);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(personId));
        assertThat(result.getName(), equalTo(name));
        assertThat(result.getEmailAddress(), equalTo(emailAddress));
        assertThat(result.getPhone(), equalTo(phone));
        assertThat(result.getIsActive(), equalTo(true));

        verify(personRepository).findById(personId);
    }

    @Test
    void getPersonByIdWithNonExistentIdShouldThrowNotFoundException()
    {
        // Given
        Long nonExistentId = randomLong();

        when(personRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> personService.getPersonById(nonExistentId));

        verify(personRepository).findById(nonExistentId);
    }

    @Test
    void updatePersonWithAllFieldsShouldUpdateAndSavePersonWithAllFields()
    {
        // Given
        Long personId = randomLong();
        String originalName = randomString();
        String originalEmailAddress = randomStringWithSuffix("@example.com");
        String originalPhone = randomString();
        Boolean originalIsActive = true;

        String updatedName = randomString();
        String updatedEmailAddress = randomStringWithSuffix("@example.com");
        String updatedPhone = randomString();
        Boolean updatedIsActive = false;

        Person existingPerson = Person.builder()
                .id(personId)
                .name(originalName)
                .emailAddress(originalEmailAddress)
                .phone(originalPhone)
                .isActive(originalIsActive)
                .build();

        UpdatePersonRequest request = UpdatePersonRequest.builder()
                .name(updatedName)
                .emailAddress(updatedEmailAddress)
                .phone(updatedPhone)
                .isActive(updatedIsActive)
                .build();

        Person updatedPerson = Person.builder()
                .id(personId)
                .name(updatedName)
                .emailAddress(updatedEmailAddress)
                .phone(updatedPhone)
                .isActive(updatedIsActive)
                .build();

        when(personRepository.findById(personId)).thenReturn(java.util.Optional.of(existingPerson));
        when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);
        when(noteRepository.countByPersonId(personId)).thenReturn(0L);

        // When
        PersonResponse result = personService.updatePerson(personId, request);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(personId));
        assertThat(result.getName(), equalTo(updatedName));
        assertThat(result.getEmailAddress(), equalTo(updatedEmailAddress));
        assertThat(result.getPhone(), equalTo(updatedPhone));
        assertThat(result.getIsActive(), equalTo(updatedIsActive));

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).findById(personId);
        verify(personRepository).save(personCaptor.capture());
        Person capturedPerson = personCaptor.getValue();
        assertThat(capturedPerson.getId(), equalTo(personId));
        assertThat(capturedPerson.getName(), equalTo(updatedName));
        assertThat(capturedPerson.getEmailAddress(), equalTo(updatedEmailAddress));
        assertThat(capturedPerson.getPhone(), equalTo(updatedPhone));
        assertThat(capturedPerson.getIsActive(), equalTo(updatedIsActive));
    }

    @Test
    void updatePersonWithNullPhoneShouldUpdatePersonWithNullPhone()
    {
        // Given
        Long personId = randomLong();
        String originalName = randomString();
        String originalEmailAddress = randomStringWithSuffix("@example.com");
        String originalPhone = randomString();
        Boolean originalIsActive = true;

        String updatedName = randomString();
        String updatedEmailAddress = randomStringWithSuffix("@example.com");
        Boolean updatedIsActive = false;

        Person existingPerson = Person.builder()
                .id(personId)
                .name(originalName)
                .emailAddress(originalEmailAddress)
                .phone(originalPhone)
                .isActive(originalIsActive)
                .build();

        UpdatePersonRequest request = UpdatePersonRequest.builder()
                .name(updatedName)
                .emailAddress(updatedEmailAddress)
                .phone(null)
                .isActive(updatedIsActive)
                .build();

        Person updatedPerson = Person.builder()
                .id(personId)
                .name(updatedName)
                .emailAddress(updatedEmailAddress)
                .phone(null)
                .isActive(updatedIsActive)
                .build();

        when(personRepository.findById(personId)).thenReturn(java.util.Optional.of(existingPerson));
        when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);
        when(noteRepository.countByPersonId(personId)).thenReturn(0L);

        // When
        PersonResponse result = personService.updatePerson(personId, request);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(personId));
        assertThat(result.getName(), equalTo(updatedName));
        assertThat(result.getEmailAddress(), equalTo(updatedEmailAddress));
        assertThat(result.getPhone(), nullValue());
        assertThat(result.getIsActive(), equalTo(updatedIsActive));

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).findById(personId);
        verify(personRepository).save(personCaptor.capture());
        Person capturedPerson = personCaptor.getValue();
        assertThat(capturedPerson.getName(), equalTo(updatedName));
        assertThat(capturedPerson.getEmailAddress(), equalTo(updatedEmailAddress));
        assertThat(capturedPerson.getPhone(), nullValue());
        assertThat(capturedPerson.getIsActive(), equalTo(updatedIsActive));
    }

    @Test
    void updatePersonWithNonExistentIdShouldThrowNotFoundException()
    {
        // Given
        Long nonExistentId = randomLong();

        UpdatePersonRequest request = UpdatePersonRequest.builder()
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        when(personRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> personService.updatePerson(nonExistentId, request));

        verify(personRepository).findById(nonExistentId);
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void updatePersonShouldReturnUpdatedPerson()
    {
        // Given
        Long personId = randomLong();
        String updatedName = randomString();
        String updatedEmailAddress = randomStringWithSuffix("@example.com");
        String updatedPhone = randomString();
        Boolean updatedIsActive = false;

        Person existingPerson = Person.builder()
                .id(personId)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .phone(randomString())
                .isActive(true)
                .build();

        UpdatePersonRequest request = UpdatePersonRequest.builder()
                .name(updatedName)
                .emailAddress(updatedEmailAddress)
                .phone(updatedPhone)
                .isActive(updatedIsActive)
                .build();

        Person updatedPerson = Person.builder()
                .id(personId)
                .name(updatedName)
                .emailAddress(updatedEmailAddress)
                .phone(updatedPhone)
                .isActive(updatedIsActive)
                .build();

        when(personRepository.findById(personId)).thenReturn(java.util.Optional.of(existingPerson));
        when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);
        when(noteRepository.countByPersonId(personId)).thenReturn(0L);

        // When
        PersonResponse result = personService.updatePerson(personId, request);

        // Then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(personId));
        assertThat(result.getName(), equalTo(updatedName));
        assertThat(result.getEmailAddress(), equalTo(updatedEmailAddress));
        assertThat(result.getPhone(), equalTo(updatedPhone));
        assertThat(result.getIsActive(), equalTo(updatedIsActive));
        verify(personRepository).findById(personId);
        verify(personRepository).save(any(Person.class));
    }
}

