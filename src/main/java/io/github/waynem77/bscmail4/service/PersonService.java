package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdatePersonRequest;
import io.github.waynem77.bscmail4.model.response.PeopleResponse;
import io.github.waynem77.bscmail4.model.response.PermissionResponse;
import io.github.waynem77.bscmail4.model.response.PersonResponse;
import io.github.waynem77.bscmail4.model.specification.PersonFilter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;

/**
 * Provides database operations on {@link Person} objects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService
{
    @Autowired
    private final PersonRepository personRepository;
    @Autowired
    private final PermissionRepository permissionRepository;

    /**
     * Returns all Person objects matching the given filter, in ascending order by name, by page.
     *
     * @param filter the filter; see the documentation for {@link PersonFilter} for details; may not be null
     * @param page   the page number to return; this parameter is 0-indexed
     * @param size   the page size
     * @return the requested page
     * @throws NullPointerException if filter is null
     */
    public PeopleResponse getPeopleFiltered(@NonNull PersonFilter filter, int page, int size)
    {
        log.info("Getting people with filters. filter={}, page={}, size={}", filter, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "name");

        Specification<Person> specification = filter.toSpecification();
        return new PeopleResponse(personRepository.findAll(specification, pageable).map(PersonResponse::fromPerson));
    }

    /**
     * Creates a new Person object from the given request and stores it in the database
     *
     * @param request the creation request
     * @return a PersonResponse representing the Person
     * @throws BadRequestException if the request is invalid
     */
    public PersonResponse createPerson(@NonNull CreateOrUpdatePersonRequest request)
    {
        log.info("Creating a new person. request={}", request);
        validateRequestForCreate(request);

        Person person = new Person();
        person.setName(request.getName());
        person.setEmailAddress(request.getEmailAddress());
        person.setPhone(request.getPhone());
        person.setPermissions(permissionRepository.findAllByIdIn(request.getPermissionIds()));
        person.setActive(request.getActive());
        person = personRepository.save(person);

        return createResponseFromPerson(person);
    }

    /**
     * Updates an existing Person object according to the given request. If the Person does not already exist in the
     * database, it is created.
     *
     * @param request the update request
     * @param id      the id of the Person object to update
     * @return a PersonResponse representing the Person
     * @throws BadRequestException if the request is invalid
     */
    public PersonResponse updatePerson(@NonNull CreateOrUpdatePersonRequest request, Long id)
    {
        log.info("Updating person. request={}, id={}", request, id);

        Optional<Person> possibleExistingPerson = personRepository.findById(id);

        if (possibleExistingPerson.isEmpty()) {
            log.info("Person with id={} does not exist.", id);
            return createPerson(request);
        }

        validateRequestForCreate(request);

        Person person = possibleExistingPerson.get();
        person.setName(request.getName());
        person.setEmailAddress(request.getEmailAddress());
        person.setPhone(request.getPhone());
        person.setPermissions(permissionRepository.findAllByIdIn(request.getPermissionIds()));
        person.setActive(request.getActive());
        person = personRepository.save(person);

        return createResponseFromPerson(person);
    }

    /**
     * Deletes the Person with the given id from the database. If no such Person exists, this method does nothing.
     *
     * @param id the id of the Person object to delete
     */
    public void deletePerson(@NonNull Long id)
    {
        log.info("Deleting person. id={}", id);

        personRepository.deleteById(id);
    }

    /**
     * Validates the given request for creation. The request may not be null, and the name, emailAddress, and active
     * fields also may not be null.
     *
     * @param request the creation request
     * @throws BadRequestException if the request is invalid
     */
    private void validateRequestForCreate(CreateOrUpdatePersonRequest request)
    {
        log.info("Validating request for create. request={}", request);
        if (request.getName() == null || request.getEmailAddress() == null || request.getActive() == null) {
            log.error("Invalid request. Request name, email address, and active must all be non-null. request={}",
                    request);
            throw new BadRequestException("Invalid request");
        }
    }

    /**
     * Finds the Person with the given id in the database and returns it.
     *
     * @param id the Person id
     * @return a PersonResponse representing the person
     */
    public PersonResponse getPerson(Long id)
    {
        log.info("Getting person. id={}", id);

        Person person = personRepository.findById(id).orElseThrow(() ->
        {
            log.error("Unable to find person. id={}", id);
            return new NotFoundException("Unable to find person");
        });
        return createResponseFromPerson(person);
    }

    /**
     * Transforms a Person object into an equivalent PersonResponse object.
     *
     * @param person the Person object
     * @return the PersonResponse object
     */
    private PersonResponse createResponseFromPerson(@NonNull Person person)
    {
        PersonResponse response = new PersonResponse();
        response.setId(person.getId());
        response.setName(person.getName());
        response.setEmailAddress(person.getEmailAddress());
        response.setPhone(person.getPhone());
        response.setPermissions(person.getPermissions().stream().map(PermissionResponse::fromPermission).sorted(Comparator.comparing(PermissionResponse::getName)).toList());
        response.setActive(person.getActive());

        return response;
    }
}
