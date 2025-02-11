package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.UpdateAction;
import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.GroupRepository;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdatePersonRequest;
import io.github.waynem77.bscmail4.model.request.UpdateGroupsRequest;
import io.github.waynem77.bscmail4.model.response.GroupResponse;
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

import java.util.*;
import java.util.stream.Stream;

/**
 * Provides database operations on {@link Person} objects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService
{
    @Autowired
    private final GroupService groupService;
    @Autowired
    private final PersonRepository personRepository;
    @Autowired
    private final PermissionRepository permissionRepository;
    @Autowired
    private final GroupRepository groupRepository;

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
        return new PeopleResponse(personRepository.findAll(specification, pageable).map(this::makePersonResponseFromPerson));
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

        return makePersonResponseFromPerson(person);
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

        return makePersonResponseFromPerson(person);
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
     * Adds or removes the given Groups to the given Person and returns a corresponding PersonResponse.
     *
     * @param personId the Person id; may not be null
     * @param request the request containing the Group ids; may not be null
     * @return a PersonResponse representing the updated Person
     * @throws NullPointerException if either argument is null
     * @throws NotFoundException    if personId is invalid
     * @throws BadRequestException  if any id in groupIds is invalid
     */
    public PersonResponse updateGroups(@NonNull Long personId, @NonNull UpdateGroupsRequest request)
    {
        log.info("Adding groups to person. personId={}, request={}", personId, request);

        UpdateAction action = UpdateAction.fromValue(request.getAction());
        if (action == null)
        {
            log.error("Invalid request; action is invalid. action={}", request.getAction());
            throw new BadRequestException("Invalid request.");
        }

        List<Long> groupIds = request.getGroupIds();
        if (groupIds == null)
        {
            log.error("Invalid request; groupIds is null.");
            throw new BadRequestException("Invalid request.");
        }
        for (Long id : groupIds)
        {
            if (!groupRepository.existsById(id))
            {
                log.error("Group does not exist. id={}", id);
                throw new BadRequestException("Group does not exist.");
            }
        }

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> {
                    log.error("Person does not exist. personId={}", personId);
                    return new NotFoundException("Person does not exist.");
                });
        List<Group> oldGroups = person.getGroups() != null ?
                person.getGroups() :
                Collections.emptyList();
        List<Group> requestedGroups = groupRepository.findAllByIdIn(groupIds);

        Set<Group> combinedGroups = new HashSet<>(oldGroups);
        if (action == UpdateAction.ADD)
        {
            combinedGroups.addAll(requestedGroups);
        }
        else if (action == UpdateAction.REMOVE)
        {
            combinedGroups.removeAll(requestedGroups);
        }

        person.setGroups(new ArrayList<>(combinedGroups));
        person = personRepository.save(person);

        return makePersonResponseFromPerson(person);
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
        return makePersonResponseFromPerson(person);
    }

    /**
     * Transforms a Person object into an equivalent PersonResponse object.
     *
     * @param person the Person object
     * @return the PersonResponse object
     */
    public PersonResponse makePersonResponseFromPerson(@NonNull Person person)
    {
        PersonResponse response = new PersonResponse();
        response.setId(person.getId());
        response.setName(person.getName());
        response.setEmailAddress(person.getEmailAddress());
        response.setPhone(person.getPhone());
        response.setPermissions(person.getPermissions().stream().map(PermissionResponse::fromPermission).sorted(Comparator.comparing(PermissionResponse::getName)).toList());
        response.setGroups((person.getGroups() != null ? person.getGroups().stream() : Stream.<Group>empty())
                .map(groupService::makeGroupResponseFromGroup)
                .sorted(Comparator.comparing(GroupResponse::getName))
                .toList());
        response.setActive(person.getActive());

        return response;
    }
}
