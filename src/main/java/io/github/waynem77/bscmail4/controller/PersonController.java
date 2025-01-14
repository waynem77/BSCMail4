package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.request.CreateOrUpdatePersonRequest;
import io.github.waynem77.bscmail4.model.response.PeopleResponse;
import io.github.waynem77.bscmail4.model.response.PersonResponse;
import io.github.waynem77.bscmail4.model.specification.PersonFilter;
import io.github.waynem77.bscmail4.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * APIs regarding {@link io.github.waynem77.bscmail4.model.entity.Person} objects.
 */
@RestController
@RequiredArgsConstructor
public class PersonController
{
    @Autowired
    private PersonService personService;

    @GetMapping("/api/person")
    public PeopleResponse getPeople(
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "like", required = false) String like,
            @RequestParam(name = "roleIds", required = false) String roleIdString,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "25") int size)
    {
        Set<Long> roleIds = roleIdString == null ?
                null :
                Arrays.stream(roleIdString.split(","))
                        .map(Long::valueOf)
                        .collect(Collectors.toSet());
        PersonFilter filter = new PersonFilter()
                .withActive(active)
                .withNameLike(like)
                .withRoleIds(roleIds);
        return personService.getPeopleFiltered(filter, page, size);
    }

    @PostMapping("/api/person")
    public PersonResponse createPerson(
            @RequestBody CreateOrUpdatePersonRequest request)
    {
        return personService.createPerson(request);
    }

    @PutMapping("/api/person/{personId}")
    public PersonResponse updatePerson(
            @PathVariable(name = "personId") Long personId,
            @RequestBody CreateOrUpdatePersonRequest request)
    {
        return personService.updatePerson(request, personId);
    }

    @GetMapping("/api/person/{personId}")
    public PersonResponse getPerson(
            @PathVariable(name = "personId") Long personId)
    {
        return personService.getPerson(personId);
    }

    @DeleteMapping("/api/person/{personId}")
    public void deletePerson(
            @PathVariable(name = "personId") Long personId)
    {
        personService.deletePerson(personId);
    }
}
