package io.github.waynem77.bscmail4.server.controller;

import io.github.waynem77.bscmail4.server.model.request.CreatePersonRequest;
import io.github.waynem77.bscmail4.server.model.request.UpdatePersonRequest;
import io.github.waynem77.bscmail4.server.model.response.PersonContainer;
import io.github.waynem77.bscmail4.server.model.response.PersonResponse;
import io.github.waynem77.bscmail4.server.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Person entities.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PersonController
{
    private final PersonService personService;

    /**
     * Creates a new Person entity.
     *
     * @param request the request containing person data
     * @return the created person as a PersonResponse
     */
    @PostMapping("/api/person")
    @ResponseStatus(HttpStatus.CREATED)
    public PersonResponse createPerson(@Valid @RequestBody CreatePersonRequest request)
    {
        log.info("Creating person. request={}", request);
        return personService.createPerson(request);
    }

    /**
     * Retrieves a Person entity by its ID.
     *
     * @param personId the ID of the person to retrieve
     * @return the person as a PersonResponse
     */
    @GetMapping("/api/person/{personId}")
    public PersonResponse getPerson(@PathVariable Long personId)
    {
        log.info("Getting person by ID. personId={}", personId);
        return personService.getPersonById(personId);
    }

    /**
     * Updates an existing Person entity.
     *
     * @param personId the ID of the person to update
     * @param request  the request containing updated person data
     * @return the updated person as a PersonResponse
     */
    @PostMapping("/api/person/{personId}")
    public PersonResponse updatePerson(@PathVariable Long personId, @Valid @RequestBody UpdatePersonRequest request)
    {
        log.info("Updating person. personId={}, request={}", personId, request);
        return personService.updatePerson(personId, request);
    }

    /**
     * Retrieves a paginated list of Person entities with optional filtering and sorting.
     *
     * @param page      the page number (0-based, default 0)
     * @param size      the page size (default 20)
     * @param sortBy    the field to sort by (name, emailAddress, or createdAt, default name)
     * @param direction the sort direction (asc or desc, default asc)
     * @param isActive  filter by active status (optional)
     * @param search    filter by search string matching name or emailAddress (optional)
     * @return a PersonContainer containing the paginated results
     */
    @GetMapping("/api/person")
    public PersonContainer getPersons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search)
    {
        log.info("Getting persons. page={}, size={}, sortBy={}, direction={}, isActive={}, search={}",
                page, size, sortBy, direction, isActive, search);
        return personService.getPersons(page, size, sortBy, direction, isActive, search);
    }
}

