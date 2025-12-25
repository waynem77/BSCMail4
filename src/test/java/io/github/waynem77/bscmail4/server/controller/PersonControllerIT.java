package io.github.waynem77.bscmail4.server.controller;

import io.github.waynem77.bscmail4.BaseIT;
import io.github.waynem77.bscmail4.server.database.entity.Person;
import io.github.waynem77.bscmail4.server.database.repository.PersonRepository;
import io.github.waynem77.bscmail4.server.model.request.CreatePersonRequest;
import io.github.waynem77.bscmail4.server.model.request.UpdatePersonRequest;
import io.github.waynem77.bscmail4.server.model.response.PersonContainer;
import io.github.waynem77.bscmail4.server.model.response.PersonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.List;
import java.util.stream.Stream;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for PersonController.
 */
class PersonControllerIT extends BaseIT
{
    @Autowired
    private PersonRepository personRepository;

    // Tests for GET /api/person/{personId}
    @Nested
    class GetPerson
    {
        @Test
        void getPersonWithExistingIdShouldReturnOkStatusAndPersonData()
        {
            // Given
            String name = randomString();
            String emailAddress = randomStringWithSuffix("@example.com");
            String phone = randomString();

            Person savedPerson = createPerson(name, emailAddress, phone, true);

            // When
            ResponseEntity<PersonResponse> response = restTemplate.getForEntity(
                    url("/api/person/" + savedPerson.getId()), PersonResponse.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonResponse responseBody = response.getBody();
            assertThat(responseBody.getId(), equalTo(savedPerson.getId()));
            assertThat(responseBody.getName(), equalTo(name));
            assertThat(responseBody.getEmailAddress(), equalTo(emailAddress));
            assertThat(responseBody.getPhone(), equalTo(phone));
            assertThat(responseBody.getIsActive(), equalTo(true));
        }

        @Test
        void getPersonWithNullPhoneShouldReturnPersonWithNullPhone()
        {
            // Given
            String name = randomString();
            String emailAddress = randomStringWithSuffix("@example.com");

            Person savedPerson = createPerson(name, emailAddress, null, true);

            // When
            ResponseEntity<PersonResponse> response = restTemplate.getForEntity(
                    url("/api/person/" + savedPerson.getId()), PersonResponse.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonResponse responseBody = response.getBody();
            assertThat(responseBody.getId(), equalTo(savedPerson.getId()));
            assertThat(responseBody.getName(), equalTo(name));
            assertThat(responseBody.getEmailAddress(), equalTo(emailAddress));
            assertThat(responseBody.getPhone(), nullValue());
            assertThat(responseBody.getIsActive(), equalTo(true));
        }

        @Test
        void getPersonWithNonExistentIdShouldReturnNotFoundStatus()
        {
            // Given
            Long nonExistentId = randomLong();

            // When
            ResponseEntity<String> response = restTemplate.getForEntity(
                    url("/api/person/" + nonExistentId), String.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        }
    }

    // Tests for POST /api/person
    @Nested
    class CreatePerson
    {
        @Test
        void createPersonWithValidDataShouldReturnCreatedStatusAndSavePersonToDatabase()
        {
            // Given
            String name = randomString();
            String emailAddress = randomStringWithSuffix("@example.com");
            String phone = randomString();

            CreatePersonRequest request = CreatePersonRequest.builder()
                    .name(name)
                    .emailAddress(emailAddress)
                    .phone(phone)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreatePersonRequest> httpEntity = new HttpEntity<>(request, headers);

            // When
            ResponseEntity<PersonResponse> response = restTemplate.postForEntity(
                    url("/api/person"), httpEntity, PersonResponse.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
            assertThat(response.getBody(), notNullValue());

            PersonResponse responseBody = response.getBody();
            assertThat(responseBody.getId(), notNullValue());
            addDbCleanup("person", responseBody.getId());
            assertThat(responseBody.getName(), equalTo(name));
            assertThat(responseBody.getEmailAddress(), equalTo(emailAddress));
            assertThat(responseBody.getPhone(), equalTo(phone));
            assertThat(responseBody.getIsActive(), equalTo(true));

            Person savedPerson = personRepository.findById(responseBody.getId()).orElse(null);
            assertThat(savedPerson, notNullValue());
            assertThat(savedPerson.getName(), equalTo(name));
            assertThat(savedPerson.getEmailAddress(), equalTo(emailAddress));
            assertThat(savedPerson.getPhone(), equalTo(phone));
            assertThat(savedPerson.getIsActive(), equalTo(true));
        }

        @Test
        void createPersonWithNullPhoneShouldSavePersonWithNullPhone()
        {
            // Given
            String name = randomString();
            String emailAddress = randomStringWithSuffix("@example.com");

            CreatePersonRequest request = CreatePersonRequest.builder()
                    .name(name)
                    .emailAddress(emailAddress)
                    .phone(null)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreatePersonRequest> httpEntity = new HttpEntity<>(request, headers);

            // When
            ResponseEntity<PersonResponse> response = restTemplate.postForEntity(
                    url("/api/person"), httpEntity, PersonResponse.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
            PersonResponse responseBody = response.getBody();
            assertThat(responseBody, notNullValue());
            assertThat(responseBody.getPhone(), nullValue());

            Person savedPerson = personRepository.findById(responseBody.getId()).orElse(null);
            assertThat(savedPerson, notNullValue());
            assertThat(savedPerson.getPhone(), nullValue());

            addDbCleanup("person", savedPerson.getId());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidCreatePersonRequests")
        void createPersonWithInvalidDataShouldReturnBadRequest(String displayName, CreatePersonRequest request)
        {
            // Given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreatePersonRequest> httpEntity = new HttpEntity<>(request, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url("/api/person"), httpEntity, String.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        }

        private static Stream<Arguments> invalidCreatePersonRequests()
        {
            return Stream.of(
                    Arguments.of("Missing name", CreatePersonRequest.builder()
                            .name(null)
                            .emailAddress(randomStringWithSuffix("@example.com"))
                            .phone(randomString())
                            .build()),
                    Arguments.of("Blank name", CreatePersonRequest.builder()
                            .name("")
                            .emailAddress(randomStringWithSuffix("@example.com"))
                            .phone(randomString())
                            .build()),
                    Arguments.of("Missing email address", CreatePersonRequest.builder()
                            .name(randomString())
                            .emailAddress(null)
                            .phone(randomString())
                            .build()),
                    Arguments.of("Blank email address", CreatePersonRequest.builder()
                            .name(randomString())
                            .emailAddress("")
                            .phone(randomString())
                            .build())
            );
        }
    }

    // Tests for POST /api/person/{personId}
    @Nested
    class UpdatePerson
    {
        @Test
        void updatePersonWithValidDataShouldReturnOkStatusAndUpdatePersonInDatabase()
        {
            // Given
            String originalName = randomString();
            String originalEmailAddress = randomStringWithSuffix("@example.com");
            String originalPhone = randomString();
            boolean originalIsActive = true;

            Person savedPerson = createPerson(originalName, originalEmailAddress, originalPhone, originalIsActive);

            String updatedName = randomString();
            String updatedEmailAddress = randomStringWithSuffix("@example.com");
            String updatedPhone = randomString();
            boolean updatedIsActive = false;

            UpdatePersonRequest request = UpdatePersonRequest.builder()
                    .name(updatedName)
                    .emailAddress(updatedEmailAddress)
                    .phone(updatedPhone)
                    .isActive(updatedIsActive)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpdatePersonRequest> httpEntity = new HttpEntity<>(request, headers);

            // When
            ResponseEntity<PersonResponse> response = restTemplate.postForEntity(
                    url("/api/person/" + savedPerson.getId()), httpEntity, PersonResponse.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonResponse responseBody = response.getBody();
            assertThat(responseBody.getId(), equalTo(savedPerson.getId()));
            assertThat(responseBody.getName(), equalTo(updatedName));
            assertThat(responseBody.getEmailAddress(), equalTo(updatedEmailAddress));
            assertThat(responseBody.getPhone(), equalTo(updatedPhone));
            assertThat(responseBody.getIsActive(), equalTo(updatedIsActive));

            Person updatedPerson = personRepository.findById(savedPerson.getId()).orElse(null);
            assertThat(updatedPerson, notNullValue());
            assertThat(updatedPerson.getName(), equalTo(updatedName));
            assertThat(updatedPerson.getEmailAddress(), equalTo(updatedEmailAddress));
            assertThat(updatedPerson.getPhone(), equalTo(updatedPhone));
            assertThat(updatedPerson.getIsActive(), equalTo(updatedIsActive));
        }

        @Test
        void updatePersonWithNullPhoneShouldUpdatePersonWithNullPhone()
        {
            // Given
            String originalName = randomString();
            String originalEmailAddress = randomStringWithSuffix("@example.com");
            String originalPhone = randomString();
            boolean originalIsActive = true;

            Person savedPerson = createPerson(originalName, originalEmailAddress, originalPhone, originalIsActive);

            String updatedName = randomString();
            String updatedEmailAddress = randomStringWithSuffix("@example.com");
            boolean updatedIsActive = false;

            UpdatePersonRequest request = UpdatePersonRequest.builder()
                    .name(updatedName)
                    .emailAddress(updatedEmailAddress)
                    .phone(null)
                    .isActive(updatedIsActive)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpdatePersonRequest> httpEntity = new HttpEntity<>(request, headers);

            // When
            ResponseEntity<PersonResponse> response = restTemplate.postForEntity(
                    url("/api/person/" + savedPerson.getId()), httpEntity, PersonResponse.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            PersonResponse responseBody = response.getBody();
            assertThat(responseBody, notNullValue());
            assertThat(responseBody.getPhone(), nullValue());

            Person updatedPerson = personRepository.findById(savedPerson.getId()).orElse(null);
            assertThat(updatedPerson, notNullValue());
            assertThat(updatedPerson.getPhone(), nullValue());
        }

        @Test
        void updatePersonWithNonExistentIdShouldReturnNotFoundStatus()
        {
            // Given
            Long nonExistentId = randomLong();

            UpdatePersonRequest request = UpdatePersonRequest.builder()
                    .name(randomString())
                    .emailAddress(randomStringWithSuffix("@example.com"))
                    .phone(randomString())
                    .isActive(true)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpdatePersonRequest> httpEntity = new HttpEntity<>(request, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url("/api/person/" + nonExistentId), httpEntity, String.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidUpdatePersonRequests")
        void updatePersonWithInvalidDataShouldReturnBadRequest(String displayName, UpdatePersonRequest request)
        {
            // Given
            Person savedPerson = createPerson(randomString(), randomStringWithSuffix("@example.com"), randomString(),
                    true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpdatePersonRequest> httpEntity = new HttpEntity<>(request, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url("/api/person/" + savedPerson.getId()), httpEntity, String.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        }

        private static Stream<Arguments> invalidUpdatePersonRequests()
        {
            return Stream.of(
                    Arguments.of("Missing name", UpdatePersonRequest.builder()
                            .name(null)
                            .emailAddress(randomStringWithSuffix("@example.com"))
                            .phone(randomString())
                            .isActive(true)
                            .build()),
                    Arguments.of("Blank name", UpdatePersonRequest.builder()
                            .name("")
                            .emailAddress(randomStringWithSuffix("@example.com"))
                            .phone(randomString())
                            .isActive(true)
                            .build()),
                    Arguments.of("Missing email address", UpdatePersonRequest.builder()
                            .name(randomString())
                            .emailAddress(null)
                            .phone(randomString())
                            .isActive(true)
                            .build()),
                    Arguments.of("Blank email address", UpdatePersonRequest.builder()
                            .name(randomString())
                            .emailAddress("")
                            .phone(randomString())
                            .isActive(true)
                            .build()),
                    Arguments.of("Missing isActive", UpdatePersonRequest.builder()
                            .name(randomString())
                            .emailAddress(randomStringWithSuffix("@example.com"))
                            .phone(randomString())
                            .isActive(null)
                            .build())
            );
        }
    }

    // Tests for GET /api/person
    @Nested
    class ListPersons
    {
        private List<Person> people;

        @BeforeEach
        void setup()
        {
            people = List.of(
                    createPerson(randomStringWithPrefix("a"), randomStringWithPrefix("c"), randomString(), true),
                    createPerson("c" + randomStringContaining("XXX"), randomStringWithPrefix("e"), randomString(),
                            true),
                    createPerson(randomStringWithPrefix("b"), randomStringWithPrefix("d"), randomString(), false),
                    createPerson(randomStringWithPrefix("d"), "a" + randomStringContaining("XXX"), randomString(),
                            false),
                    createPerson(randomStringWithPrefix("XXX"), randomStringWithPrefix("b"), null, true),
                    createPerson(randomStringWithPrefix("f"), randomStringWithPrefix("f"), randomString(), false),
                    createPerson(randomStringWithPrefix("e"), randomStringWithPrefix("XXX"), null, true)
            );

            // Expected indexes:
            //   Sorted by name ascending (default): 0, 2, 1, 3, 6, 5, 4
            //   Sorted by name descending: 4, 5, 6, 3, 1, 2, 0
            //   Sorted by email address ascending: 3, 4, 0, 2, 1, 5, 6
            //   Sorted by email address descending: 6, 5, 1, 2, 0, 4, 3
            //   Only active: 0, 1, 6, 4
            //   Only inactive: 2, 3, 5
            //   Only matching "xxx": 1, 3, 6, 4
        }

        @Test
        void getPersonsSortsByNameAscendingByDefault()
        {
            // Given
            List<Long> expectedIds = Stream.of(0, 2, 1, 3, 6, 5, 4)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(7));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(7L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanSortByNameAscending()
        {
            // Given
            List<Long> expectedIds = Stream.of(0, 2, 1, 3, 6, 5, 4)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?sortBy=name&direction=asc"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(7));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(7L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanSortByNameDescending()
        {
            // Given
            List<Long> expectedIds = Stream.of(4, 5, 6, 3, 1, 2, 0)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?sortBy=name&direction=desc"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(7));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(7L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanSortByEmailAddressAscending()
        {
            // Given
            List<Long> expectedIds = Stream.of(3, 4, 0, 2, 1, 5, 6)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?sortBy=emailAddress&direction=asc"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(7));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(7L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanSortByEmailAddressDescending()
        {
            // Given
            List<Long> expectedIds = Stream.of(6, 5, 1, 2, 0, 4, 3)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?sortBy=emailAddress&direction=desc"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(7));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(7L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanSortByCreatedAtAscending()
        {
            // Given
            // Persons are created in order, so createdAt ascending should match creation order: 0, 1, 2, 3, 4, 5, 6
            List<Long> expectedIds = Stream.of(0, 1, 2, 3, 4, 5, 6)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?sortBy=createdAt&direction=asc"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(7));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(7L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanSortByCreatedAtDescending()
        {
            // Given
            // Persons are created in order, so createdAt descending should be reverse order: 6, 5, 4, 3, 2, 1, 0
            List<Long> expectedIds = Stream.of(6, 5, 4, 3, 2, 1, 0)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?sortBy=createdAt&direction=desc"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(7));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(7L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanFilterOnlyActivePersons()
        {
            // Given
            List<Long> expectedIds = Stream.of(0, 1, 6, 4)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?isActive=true"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(4));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(4L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanFilterOnlyInactivePersons()
        {
            // Given
            List<Long> expectedIds = Stream.of(2, 3, 5)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?isActive=false"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(3));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(3L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanFilterBySearchTerm()
        {
            // Given
            List<Long> expectedIds = Stream.of(1, 3, 6, 4)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // When
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(
                    url("/api/person?search=xxx"), PersonContainer.class);

            // Then
            assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response.getBody(), notNullValue());

            PersonContainer container = response.getBody();
            assertThat(container.getElements(), notNullValue());
            assertThat(
                    container.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds));
            assertThat(container.getPageNumber(), equalTo(0));
            assertThat(container.getSize(), equalTo(20));
            assertThat(container.getNumberOfElements(), equalTo(4));
            assertThat(container.getTotalPages(), equalTo(1));
            assertThat(container.getTotalElements(), equalTo(4L));
            assertThat(container.isFirst(), equalTo(true));
            assertThat(container.isLast(), equalTo(true));
            assertThat(container.hasNext(), equalTo(false));
            assertThat(container.hasPrevious(), equalTo(false));
        }

        @Test
        void getPersonsCanPage()
        {
            // Given
            List<Long> expectedIds0 = Stream.of(0, 2, 1)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();
            List<Long> expectedIds1 = Stream.of(3, 6, 5)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();
            List<Long> expectedIds2 = Stream.of(4)
                    .map(people::get)
                    .map(Person::getId)
                    .toList();

            // Page 0
            ResponseEntity<PersonContainer> response0 = restTemplate.getForEntity(
                    url("/api/person?page=0&size=3"), PersonContainer.class);

            assertThat(response0.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response0.getBody(), notNullValue());

            PersonContainer container0 = response0.getBody();
            assertThat(container0.getElements(), notNullValue());
            assertThat(
                    container0.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds0));
            assertThat(container0.getPageNumber(), equalTo(0));
            assertThat(container0.getSize(), equalTo(3));
            assertThat(container0.getNumberOfElements(), equalTo(3));
            assertThat(container0.getTotalPages(), equalTo(3));
            assertThat(container0.getTotalElements(), equalTo(7L));
            assertThat(container0.isFirst(), equalTo(true));
            assertThat(container0.isLast(), equalTo(false));
            assertThat(container0.hasNext(), equalTo(true));
            assertThat(container0.hasPrevious(), equalTo(false));

            // Page 1
            ResponseEntity<PersonContainer> response1 = restTemplate.getForEntity(
                    url("/api/person?page=1&size=3"), PersonContainer.class);

            assertThat(response1.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response1.getBody(), notNullValue());

            PersonContainer container1 = response1.getBody();
            assertThat(container1.getElements(), notNullValue());
            assertThat(
                    container1.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds1));
            assertThat(container1.getPageNumber(), equalTo(1));
            assertThat(container1.getSize(), equalTo(3));
            assertThat(container1.getNumberOfElements(), equalTo(3));
            assertThat(container1.getTotalPages(), equalTo(3));
            assertThat(container1.getTotalElements(), equalTo(7L));
            assertThat(container1.isFirst(), equalTo(false));
            assertThat(container1.isLast(), equalTo(false));
            assertThat(container1.hasNext(), equalTo(true));
            assertThat(container1.hasPrevious(), equalTo(true));

            // Page 2
            ResponseEntity<PersonContainer> response2 = restTemplate.getForEntity(
                    url("/api/person?page=2&size=3"), PersonContainer.class);

            assertThat(response2.getStatusCode(), equalTo(HttpStatus.OK));
            assertThat(response2.getBody(), notNullValue());

            PersonContainer container2 = response2.getBody();
            assertThat(container2.getElements(), notNullValue());
            assertThat(
                    container2.getElements().stream().map(PersonResponse::getId).toList(),
                    equalTo(expectedIds2));
            assertThat(container2.getPageNumber(), equalTo(2));
            assertThat(container2.getSize(), equalTo(3));
            assertThat(container2.getNumberOfElements(), equalTo(1));
            assertThat(container2.getTotalPages(), equalTo(3));
            assertThat(container2.getTotalElements(), equalTo(7L));
            assertThat(container2.isFirst(), equalTo(false));
            assertThat(container2.isLast(), equalTo(true));
            assertThat(container2.hasNext(), equalTo(false));
            assertThat(container2.hasPrevious(), equalTo(true));
        }
    }

    private Person createPerson(String name, String emailAddress, String phone, boolean isActive)
    {
        Person person = Person.builder()
                .name(name)
                .emailAddress(emailAddress)
                .phone(phone)
                .isActive(isActive)
                .build();
        person = personRepository.save(person);
        addDbCleanup("person", person.getId());

        return person;
    }
}

