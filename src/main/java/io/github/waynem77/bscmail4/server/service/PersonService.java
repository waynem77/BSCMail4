package io.github.waynem77.bscmail4.server.service;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import io.github.waynem77.bscmail4.server.database.repository.PersonRepository;
import io.github.waynem77.bscmail4.server.database.specification.PersonSpecifications;
import io.github.waynem77.bscmail4.server.model.PersonSortBy;
import io.github.waynem77.bscmail4.server.model.SortDirection;
import io.github.waynem77.bscmail4.server.model.request.CreatePersonRequest;
import io.github.waynem77.bscmail4.server.model.request.UpdatePersonRequest;
import io.github.waynem77.bscmail4.server.model.response.PersonContainer;
import io.github.waynem77.bscmail4.server.model.response.PersonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Person entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService
{
    private final PersonRepository personRepository;

    /**
     * Creates a new Person entity from the given request and saves it to the database.
     *
     * @param request the request containing person data
     * @return the created and saved Person as a PersonResponse
     */
    @Transactional
    public PersonResponse createPerson(CreatePersonRequest request)
    {
        log.info("Creating person. request={}", request);
        Person person = Person.builder()
                .name(request.getName())
                .emailAddress(request.getEmailAddress())
                .phone(request.getPhone())
                .isActive(true)
                .build();

        Person savedPerson = personRepository.save(person);
        return PersonResponse.fromPerson(savedPerson);
    }

    /**
     * Retrieves a Person entity by its ID.
     *
     * @param personId the ID of the person to retrieve
     * @return the Person as a PersonResponse
     * @throws NotFoundException if no person exists with the given ID
     */
    public PersonResponse getPersonById(Long personId)
    {
        log.info("Getting person by ID. personId={}", personId);
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> {
                    log.error("Person not found. id={}", personId);
                    return new NotFoundException("Person not found.");
                });
        return PersonResponse.fromPerson(person);
    }

    /**
     * Updates an existing Person entity with the given request data.
     *
     * @param personId the ID of the person to update
     * @param request  the request containing updated person data
     * @return the updated Person as a PersonResponse
     * @throws NotFoundException if no person exists with the given ID
     */
    @Transactional
    public PersonResponse updatePerson(Long personId, UpdatePersonRequest request)
    {
        log.info("Updating person. personId={}, request={}", personId, request);
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> {
                    log.error("Person not found. id={}", personId);
                    return new NotFoundException("Person not found.");
                });

        person.setName(request.getName());
        person.setEmailAddress(request.getEmailAddress());
        person.setPhone(request.getPhone());
        person.setIsActive(request.getIsActive());

        Person savedPerson = personRepository.save(person);
        return PersonResponse.fromPerson(savedPerson);
    }

    /**
     * Retrieves a paginated list of Person entities with optional filtering and sorting.
     *
     * @param page      the page number (0-based)
     * @param size      the page size
     * @param sortBy    the field to sort by (must be a valid PersonSortBy value)
     * @param direction the sort direction (must be a valid SortDirection value)
     * @param isActive  filter by active status (null means no filtering, non-null must be a valid Boolean)
     * @param search    filter by search string matching name or emailAddress (null or empty means no filtering)
     * @return a PersonContainer containing the paginated results
     * @throws BadRequestException if sortBy, direction, or isActive contains invalid values
     */
    @Transactional(readOnly = true)
    public PersonContainer getPersons(
            int page,
            int size,
            String sortBy,
            String direction,
            Boolean isActive,
            String search)
    {
        log.info("Getting persons. page={}, size={}, sortBy={}, direction={}, isActive={}, search={}",
                page, size, sortBy, direction, isActive, search);

        // Validate and convert sortBy
        PersonSortBy personSortBy = PersonSortBy.fromString(sortBy).orElseThrow(
                () -> {
                    log.error("Invalid sortBy value. sortBy={}", sortBy);
                    return new BadRequestException("Invalid sort field");
                }
        );

        // Validate and convert direction
        SortDirection sortDirection = SortDirection.fromString(direction).orElseThrow(
                () -> {
                    log.error("Invalid direction value. direction={}", direction);
                    return new BadRequestException("Invalid sort direction");
                }
        );

        // Note: isActive is already type-safe as a Boolean (can be null, true, or false)
        // Spring automatically converts string parameters to Boolean, so no additional validation is needed

        // Build sort
        Sort sort = Sort.by(sortDirection.toSpringSortDirection(), personSortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build specification from filters
        Specification<Person> spec = Specification.where(PersonSpecifications.isActive(isActive))
                .and(PersonSpecifications.matchesSearch(search));

        // Execute query
        Page<Person> personPage = personRepository.findAll(spec, pageable);

        return new PersonContainer(personPage);
    }
}

