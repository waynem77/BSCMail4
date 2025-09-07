package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdatePersonRequest;
import io.github.waynem77.bscmail4.model.response.PersonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for working with Person entities.
 * Provides business logic and operations for Person management.
 */
@Service
@RequiredArgsConstructor
public class PersonService
{
    private final PersonRepository personRepository;

    /**
     * Retrieves a page of persons from the database using a custom specification with pagination and sorting.
     *
     * @param specification The specification to use for filtering
     * @param pageable      The pagination and sorting information
     * @return Page of persons as PersonResponse objects
     */
    public Page<PersonResponse> findBySpecification(Specification<Person> specification, Pageable pageable)
    {
        return personRepository.findAll(specification, pageable)
                .map(PersonResponse::fromPerson);
    }

    /**
     * Retrieves a person by their ID.
     *
     * @param id The ID of the person to retrieve
     * @return Optional containing the person as PersonResponse if found, empty otherwise
     */
    public Optional<PersonResponse> findById(Long id)
    {
        return personRepository.findById(id)
                .map(PersonResponse::fromPerson);
    }

    /**
     * Creates a new person from a CreateOrUpdatePersonRequest.
     *
     * @param request The request containing person data
     * @return The created person as PersonResponse
     */
    public PersonResponse createPerson(CreateOrUpdatePersonRequest request)
    {
        Person person = request.createPerson();
        Person savedPerson = personRepository.save(person);
        return PersonResponse.fromPerson(savedPerson);
    }

    /**
     * Updates an existing person in the database.
     *
     * @param id      The ID of the person to update
     * @param request The request containing updated person data
     * @return The updated person as PersonResponse
     * @throws RuntimeException if the person with the given ID is not found
     */
    public PersonResponse updatePerson(Long id, CreateOrUpdatePersonRequest request)
    {
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));

        existingPerson.setName(request.getName());
        existingPerson.setEmailAddress(request.getEmailAddress());
        existingPerson.setPhone(request.getPhone());
        existingPerson.setActive(request.getActive());

        Person savedPerson = personRepository.save(existingPerson);
        return PersonResponse.fromPerson(savedPerson);
    }

    /**
     * Deletes a person by their ID.
     *
     * @param id The ID of the person to delete
     */
    public void deleteById(Long id)
    {
        personRepository.deleteById(id);
    }

    /**
     * Checks if a person exists by their ID.
     *
     * @param id The ID to check
     * @return true if the person exists, false otherwise
     */
    public boolean existsById(Long id)
    {
        return personRepository.existsById(id);
    }

    /**
     * Toggles the active status of a person.
     *
     * @param id The ID of the person to toggle
     * @return The updated person as PersonResponse
     * @throws RuntimeException if the person with the given ID is not found
     */
    public PersonResponse toggleActiveStatus(Long id)
    {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));

        person.setActive(!person.getActive());
        Person savedPerson = personRepository.save(person);
        return PersonResponse.fromPerson(savedPerson);
    }
}