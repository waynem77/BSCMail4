package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdatePersonRequest;
import io.github.waynem77.bscmail4.model.response.PeopleResponse;
import io.github.waynem77.bscmail4.model.response.PermissionResponse;
import io.github.waynem77.bscmail4.model.response.PersonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Provides integration tests for {@link PersonController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PersonControllerIT extends BaseIT
{
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PersonRepository personRepository;

    private List<Permission> permissions;

    @BeforeEach
    public void setup()
    {
        permissions = List.of(
                createPermissionWithPrefix("a"),
                createPermissionWithPrefix("b"),
                createPermissionWithPrefix("c"));
    }

    @Test
    public void getPeople_gets_all_people_by_default()
    {
        personRepository.deleteAll();

        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission()
        );
        List<Person> people = List.of(
                createPerson(randomString(), Collections.emptySet(), Boolean.TRUE),
                createPerson(randomString(), Set.of(permissions.get(0)), Boolean.FALSE),
                createPerson(randomString(), Set.of(permissions.get(2)), Boolean.TRUE),
                createPerson(randomString(), Set.of(permissions.get(0), permissions.get(2)), Boolean.FALSE),
                createPerson(randomString(), Set.of(permissions.get(0), permissions.get(1), permissions.get(2)), Boolean.TRUE),
                createPerson("Benjamin Franklin", Set.of(permissions.get(0), permissions.get(1), permissions.get(2)), Boolean.FALSE),
                createPerson("Franklin Roosevelt", Set.of(permissions.get(0), permissions.get(1), permissions.get(2)), Boolean.TRUE));

        ResponseEntity<PeopleResponse> responseEntity = restTemplate.getForEntity(
                url("/api/person"),
                PeopleResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse response = responseEntity.getBody();
        assertThat(response, notNullValue());
        assertThat(response.getPageInfo(), notNullValue());
        assertThat(response.getPageInfo().getNumber(), equalTo(0));
        assertThat(response.getPageInfo().getSize(), equalTo(25));
        assertThat(response.getPageInfo().getTotalElements(), equalTo(7L));
        assertThat(response.getPageInfo().isFirst(), equalTo(true));
        assertThat(response.getPageInfo().isLast(), equalTo(true));
        assertThat(response.getContent(), equalToUnordered(List.of(
                getPersonResponseFromPerson(people.get(0)),
                getPersonResponseFromPerson(people.get(1)),
                getPersonResponseFromPerson(people.get(2)),
                getPersonResponseFromPerson(people.get(3)),
                getPersonResponseFromPerson(people.get(4)),
                getPersonResponseFromPerson(people.get(5)),
                getPersonResponseFromPerson(people.get(6)))));
    }

    @Test
    public void getPeople_filters_correctly()
    {
        personRepository.deleteAll();

        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission()
        );
        List<Person> people = List.of(
                createPerson(randomString(), Collections.emptySet(), Boolean.TRUE),
                createPerson(randomString(), Set.of(permissions.get(0)), Boolean.FALSE),
                createPerson(randomString(), Set.of(permissions.get(2)), Boolean.TRUE),
                createPerson(randomString(), Set.of(permissions.get(0), permissions.get(2)), Boolean.FALSE),
                createPerson(randomString(), Set.of(permissions.get(0), permissions.get(1), permissions.get(2)), Boolean.TRUE),
                createPerson("Benjamin Franklin", Set.of(permissions.get(0), permissions.get(1), permissions.get(2)), Boolean.FALSE),
                createPerson("Franklin Roosevelt", Set.of(permissions.get(0), permissions.get(1), permissions.get(2)), Boolean.TRUE));

        ResponseEntity<PeopleResponse> responseEntityFilteringActive = restTemplate.getForEntity(
                url("/api/person?active=true"),
                PeopleResponse.class);

        assertThat(responseEntityFilteringActive, notNullValue());
        assertThat(responseEntityFilteringActive.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse responseFilteringActive = responseEntityFilteringActive.getBody();
        assertThat(responseFilteringActive, notNullValue());
        assertThat(responseFilteringActive.getPageInfo(), notNullValue());
        assertThat(responseFilteringActive.getPageInfo().getNumber(), equalTo(0));
        assertThat(responseFilteringActive.getPageInfo().getSize(), equalTo(25));
        assertThat(responseFilteringActive.getPageInfo().getTotalElements(), equalTo(4L));
        assertThat(responseFilteringActive.getPageInfo().isFirst(), equalTo(true));
        assertThat(responseFilteringActive.getPageInfo().isLast(), equalTo(true));
        assertThat(responseFilteringActive.getContent(), equalToUnordered(List.of(
                getPersonResponseFromPerson(people.get(0)),
                getPersonResponseFromPerson(people.get(2)),
                getPersonResponseFromPerson(people.get(4)),
                getPersonResponseFromPerson(people.get(6)))));

        ResponseEntity<PeopleResponse> responseEntityFilteringName = restTemplate.getForEntity(
                url("/api/person?like=ran"),
                PeopleResponse.class);

        assertThat(responseEntityFilteringName, notNullValue());
        assertThat(responseEntityFilteringName.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse responseFilteringName = responseEntityFilteringName.getBody();
        assertThat(responseFilteringName, notNullValue());
        assertThat(responseFilteringName.getPageInfo(), notNullValue());
        assertThat(responseFilteringName.getPageInfo().getNumber(), equalTo(0));
        assertThat(responseFilteringName.getPageInfo().getSize(), equalTo(25));
        assertThat(responseFilteringName.getPageInfo().getTotalElements(), equalTo(2L));
        assertThat(responseFilteringName.getPageInfo().isFirst(), equalTo(true));
        assertThat(responseFilteringName.getPageInfo().isLast(), equalTo(true));
        assertThat(responseFilteringName.getContent(), equalToUnordered(List.of(
                getPersonResponseFromPerson(people.get(5)),
                getPersonResponseFromPerson(people.get(6)))));

        ResponseEntity<PeopleResponse> responseEntityFilteringPermissions = restTemplate.getForEntity(
                url("/api/person?permissionIds={permissionId0},{permissionId2}"),
                PeopleResponse.class,
                permissions.get(0).getId(),
                permissions.get(2).getId());

        assertThat(responseEntityFilteringPermissions, notNullValue());
        assertThat(responseEntityFilteringPermissions.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse responseFilteringPermissions = responseEntityFilteringPermissions.getBody();
        assertThat(responseFilteringPermissions, notNullValue());
        assertThat(responseFilteringPermissions.getPageInfo(), notNullValue());
        assertThat(responseFilteringPermissions.getPageInfo().getNumber(), equalTo(0));
        assertThat(responseFilteringPermissions.getPageInfo().getSize(), equalTo(25));
        assertThat(responseFilteringPermissions.getPageInfo().getTotalElements(), equalTo(4L));
        assertThat(responseFilteringPermissions.getPageInfo().isFirst(), equalTo(true));
        assertThat(responseFilteringPermissions.getPageInfo().isLast(), equalTo(true));
        assertThat(responseFilteringPermissions.getContent(), equalToUnordered(List.of(
                getPersonResponseFromPerson(people.get(3)),
                getPersonResponseFromPerson(people.get(4)),
                getPersonResponseFromPerson(people.get(5)),
                getPersonResponseFromPerson(people.get(6)))));

        ResponseEntity<PeopleResponse> responseEntityFilteringMultiple = restTemplate.getForEntity(
                url("/api/person?active=true&like=ran&?permissionIds={permissionId0},{permissionId2}"),
                PeopleResponse.class,
                permissions.get(0).getId(),
                permissions.get(2).getId());

        assertThat(responseEntityFilteringMultiple, notNullValue());
        assertThat(responseEntityFilteringMultiple.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse responseFilteringMultiple = responseEntityFilteringMultiple.getBody();
        assertThat(responseFilteringMultiple, notNullValue());
        assertThat(responseFilteringMultiple.getPageInfo(), notNullValue());
        assertThat(responseFilteringMultiple.getPageInfo().getNumber(), equalTo(0));
        assertThat(responseFilteringMultiple.getPageInfo().getSize(), equalTo(25));
        assertThat(responseFilteringMultiple.getPageInfo().getTotalElements(), equalTo(1L));
        assertThat(responseFilteringMultiple.getPageInfo().isFirst(), equalTo(true));
        assertThat(responseFilteringMultiple.getPageInfo().isLast(), equalTo(true));
        assertThat(responseFilteringMultiple.getContent(), equalToUnordered(List.of(
                getPersonResponseFromPerson(people.get(6)))));
    }

    @Test
    public void getPeople_sorts_by_name_ascending()
    {
        personRepository.deleteAll();

        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission()
        );
        List<Person> people = List.of(
                createPerson(randomStringWithPrefix("a"), Collections.emptySet(), Boolean.TRUE),
                createPerson(randomStringWithPrefix("d"), Set.of(permissions.get(0)), Boolean.FALSE),
                createPerson(randomStringWithPrefix("c"), Set.of(permissions.get(1)), Boolean.TRUE),
                createPerson(randomStringWithPrefix("b"), Set.of(permissions.get(0), permissions.get(2)), Boolean.FALSE),
                createPerson(randomStringWithPrefix("e"), Set.of(permissions.get(0), permissions.get(1), permissions.get(2)),
                        Boolean.TRUE));

        ResponseEntity<PeopleResponse> responseEntity = restTemplate.getForEntity(
                url("/api/person"),
                PeopleResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse response = responseEntity.getBody();
        assertThat(response, notNullValue());
        assertThat(response.getPageInfo(), notNullValue());
        assertThat(response.getPageInfo().getNumber(), equalTo(0));
        assertThat(response.getPageInfo().getSize(), equalTo(25));
        assertThat(response.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(response.getPageInfo().isFirst(), equalTo(true));
        assertThat(response.getPageInfo().isLast(), equalTo(true));
        assertThat(response.getContent(), equalTo(List.of(
                getPersonResponseFromPerson(people.get(0)),
                getPersonResponseFromPerson(people.get(3)),
                getPersonResponseFromPerson(people.get(2)),
                getPersonResponseFromPerson(people.get(1)),
                getPersonResponseFromPerson(people.get(4)))));
    }

    @Test
    public void getPeople_pages_correctly()
    {
        personRepository.deleteAll();

        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission()
        );
        List<Person> people = List.of(
                createPerson(randomStringWithPrefix("a"), Collections.emptySet(), Boolean.TRUE),
                createPerson(randomStringWithPrefix("d"), Set.of(permissions.get(0)), Boolean.FALSE),
                createPerson(randomStringWithPrefix("c"), Set.of(permissions.get(1)), Boolean.TRUE),
                createPerson(randomStringWithPrefix("b"), Set.of(permissions.get(0), permissions.get(2)), Boolean.FALSE),
                createPerson(randomStringWithPrefix("e"), Set.of(permissions.get(0), permissions.get(1), permissions.get(2)),
                        Boolean.TRUE));

        ResponseEntity<PeopleResponse> page0ResponseEntity = restTemplate.getForEntity(
                url("/api/person?page=0&size=2"),
                PeopleResponse.class);

        assertThat(page0ResponseEntity, notNullValue());
        assertThat(page0ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse page0Response = page0ResponseEntity.getBody();
        assertThat(page0Response, notNullValue());
        assertThat(page0Response.getPageInfo(), notNullValue());
        assertThat(page0Response.getPageInfo().getNumber(), equalTo(0));
        assertThat(page0Response.getPageInfo().getSize(), equalTo(2));
        assertThat(page0Response.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page0Response.getPageInfo().isFirst(), equalTo(true));
        assertThat(page0Response.getPageInfo().isLast(), equalTo(false));
        assertThat(page0Response.getContent(), equalTo(List.of(
                getPersonResponseFromPerson(people.get(0)),
                getPersonResponseFromPerson(people.get(3)))));

        ResponseEntity<PeopleResponse> page1ResponseEntity = restTemplate.getForEntity(
                url("/api/person?page=1&size=2"),
                PeopleResponse.class);

        assertThat(page1ResponseEntity, notNullValue());
        assertThat(page1ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse page1Response = page1ResponseEntity.getBody();
        assertThat(page1Response, notNullValue());
        assertThat(page1Response.getPageInfo(), notNullValue());
        assertThat(page1Response.getPageInfo().getNumber(), equalTo(1));
        assertThat(page1Response.getPageInfo().getSize(), equalTo(2));
        assertThat(page1Response.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page1Response.getPageInfo().isFirst(), equalTo(false));
        assertThat(page1Response.getPageInfo().isLast(), equalTo(false));
        assertThat(page1Response.getContent(), equalTo(List.of(
                getPersonResponseFromPerson(people.get(2)),
                getPersonResponseFromPerson(people.get(1)))));

        ResponseEntity<PeopleResponse> page2ResponseEntity = restTemplate.getForEntity(
                url("/api/person?page=2&size=2"),
                PeopleResponse.class);

        assertThat(page2ResponseEntity, notNullValue());
        assertThat(page2ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PeopleResponse page2Response = page2ResponseEntity.getBody();
        assertThat(page2Response, notNullValue());
        assertThat(page2Response.getPageInfo(), notNullValue());
        assertThat(page2Response.getPageInfo().getNumber(), equalTo(2));
        assertThat(page2Response.getPageInfo().getSize(), equalTo(2));
        assertThat(page2Response.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page2Response.getPageInfo().isFirst(), equalTo(false));
        assertThat(page2Response.getPageInfo().isLast(), equalTo(true));
        assertThat(page2Response.getContent(), equalTo(List.of(
                getPersonResponseFromPerson(people.get(4)))));
    }

    @Test
    public void createPersonCreatesAPerson()
    {
        CreateOrUpdatePersonRequest request = new CreateOrUpdatePersonRequest();
        request.setName(randomString());
        request.setEmailAddress(randomString());
        request.setPhone(randomString());
        request.setPermissionIds(List.of(
                permissions.get(0).getId(),
                permissions.get(1).getId()));
        request.setActive(randomBoolean());

        ResponseEntity<PersonResponse> responseEntity = restTemplate.postForEntity(
                url("/api/person"),
                request,
                PersonResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PersonResponse personResponse = responseEntity.getBody();
        assertThat(personResponse, notNullValue());
        assertThat(personResponse.getId(), notNullValue());
        addDbCleanup("person", personResponse.getId());
        assertThat(personResponse.getName(), equalTo(request.getName()));
        assertThat(personResponse.getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(
                personResponse.getPermissions(),
                equalTo(Stream.of(permissions.get(0), permissions.get(1)).map(PermissionResponse::fromPermission).toList()));
        assertThat(personResponse.getActive(), equalTo(request.getActive()));

        Optional<Person> person = personRepository.findById(personResponse.getId());
        assertThat(person.isPresent(), equalTo(true));
        assertThat(person.get().getName(), equalTo(request.getName()));
        assertThat(person.get().getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(person.get().getPhone(), equalTo(request.getPhone()));
        assertThat(person.get().getPermissions(), equalTo(Set.of(permissions.get(0), permissions.get(1))));
        assertThat(person.get().getActive(), equalTo(request.getActive()));
    }

    @Test
    public void createPersonWithoutPermissionsCreatesAPerson()
    {
        CreateOrUpdatePersonRequest request = new CreateOrUpdatePersonRequest();
        request.setName(randomString());
        request.setEmailAddress(randomString());
        request.setPhone(randomString());
        request.setPermissionIds(null);
        request.setActive(randomBoolean());

        ResponseEntity<PersonResponse> responseEntity = restTemplate.postForEntity(
                url("/api/person"),
                request,
                PersonResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PersonResponse personResponse = responseEntity.getBody();
        assertThat(personResponse, notNullValue());
        assertThat(personResponse.getId(), notNullValue());
        addDbCleanup("person", personResponse.getId());
        assertThat(personResponse.getName(), equalTo(request.getName()));
        assertThat(personResponse.getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(personResponse.getPhone(), equalTo(request.getPhone()));
        assertThat(personResponse.getPermissions(), equalTo(Collections.emptyList()));
        assertThat(personResponse.getActive(), equalTo(request.getActive()));
    }

    @ParameterizedTest
    @MethodSource("createPersonBadRequests")
    public void createPersonReturnsBadRequestWhenRequestIsInvalid(CreateOrUpdatePersonRequest request)
    {
        ResponseEntity<PersonResponse> responseEntity = restTemplate.postForEntity(
                url("/api/person"),
                request,
                PersonResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updatePersonUpdatesPersonCorrectly()
    {
        Person originalPerson = createPerson();
        Long personId = originalPerson.getId();

        CreateOrUpdatePersonRequest request = new CreateOrUpdatePersonRequest();
        request.setName(randomString());
        request.setEmailAddress(randomString());
        request.setPhone(randomString());
        request.setPermissionIds(List.of(
                permissions.get(0).getId(),
                permissions.get(1).getId()));
        request.setActive(!originalPerson.getActive());

        HttpEntity<CreateOrUpdatePersonRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<PersonResponse> responseEntity = restTemplate.exchange(
                url("/api/person/{personId}"),
                HttpMethod.PUT,
                httpEntity,
                PersonResponse.class,
                personId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PersonResponse personResponse = responseEntity.getBody();
        assertThat(personResponse, notNullValue());
        assertThat(personResponse.getId(), equalTo(personId));
        assertThat(personResponse.getName(), equalTo(request.getName()));
        assertThat(personResponse.getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(
                personResponse.getPermissions(),
                equalTo(Stream.of(permissions.get(0), permissions.get(1)).map(PermissionResponse::fromPermission).toList()));
        assertThat(personResponse.getActive(), equalTo(request.getActive()));

        Optional<Person> person = personRepository.findById(personId);
        assertThat(person.isPresent(), equalTo(true));
        assertThat(person.get().getName(), equalTo(request.getName()));
        assertThat(person.get().getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(person.get().getPhone(), equalTo(request.getPhone()));
        assertThat(person.get().getPermissions(), equalTo(Set.of(permissions.get(0), permissions.get(1))));
        assertThat(person.get().getActive(), equalTo(request.getActive()));
    }

    @Test
    public void updatePersonCreatesPersonWhenOriginalDoesNotExist()
    {
        Long personId = randomLong();

        CreateOrUpdatePersonRequest request = new CreateOrUpdatePersonRequest();
        request.setName(randomString());
        request.setEmailAddress(randomString());
        request.setPhone(randomString());
        request.setPermissionIds(List.of(
                permissions.get(0).getId(),
                permissions.get(1).getId()));
        request.setActive(randomBoolean());

        HttpEntity<CreateOrUpdatePersonRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<PersonResponse> responseEntity = restTemplate.exchange(
                url("/api/person/{personId}"),
                HttpMethod.PUT,
                httpEntity,
                PersonResponse.class,
                personId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PersonResponse personResponse = responseEntity.getBody();
        assertThat(personResponse, notNullValue());
        assertThat(personResponse.getId(), notNullValue());
        addDbCleanup("person", personResponse.getId());
        assertThat(personResponse.getName(), equalTo(request.getName()));
        assertThat(personResponse.getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(
                personResponse.getPermissions(),
                equalTo(Stream.of(permissions.get(0), permissions.get(1)).map(PermissionResponse::fromPermission).toList()));
        assertThat(personResponse.getActive(), equalTo(request.getActive()));

        Optional<Person> person = personRepository.findById(personResponse.getId());
        assertThat(person.isPresent(), equalTo(true));
        assertThat(person.get().getName(), equalTo(request.getName()));
        assertThat(person.get().getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(person.get().getPhone(), equalTo(request.getPhone()));
        assertThat(person.get().getPermissions(), equalTo(Set.of(permissions.get(0), permissions.get(1))));
        assertThat(person.get().getActive(), equalTo(request.getActive()));
    }

    @ParameterizedTest
    @MethodSource("createPersonBadRequests")
    public void updatePersonReturnsBadRequestWhenRequestIsInvalid(CreateOrUpdatePersonRequest request)
    {
        Person originalPerson = createPerson();
        Long personId = originalPerson.getId();

        HttpEntity<CreateOrUpdatePersonRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<PersonResponse> responseEntity = restTemplate.exchange(
                url("/api/person/{personId}"),
                HttpMethod.PUT,
                httpEntity,
                PersonResponse.class,
                personId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @ParameterizedTest
    @MethodSource("createPersonBadRequests")
    public void updatePersonReturnsBadRequestWhenOriginalDoesNotExistAndRequestIsInvalid(CreateOrUpdatePersonRequest request)
    {
        Long personId = randomLong();

        HttpEntity<CreateOrUpdatePersonRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<PersonResponse> responseEntity = restTemplate.exchange(
                url("/api/person/{personId}"),
                HttpMethod.PUT,
                httpEntity,
                PersonResponse.class,
                personId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));

        assertThat(personRepository.existsById(personId), equalTo(false));
    }

    @Test
    public void getPersonReturnsNotFoundWhenPersonDoesNotExist()
    {
        ResponseEntity<PersonResponse> responseEntity = restTemplate.getForEntity(
                url("/api/person/{personId}"),
                PersonResponse.class,
                randomLong());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getPersonReturnsCorrectValue()
    {
        Person person = createPerson();

        ResponseEntity<PersonResponse> responseEntity = restTemplate.getForEntity(
                url("/api/person/{personId}"),
                PersonResponse.class,
                person.getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
        validatePersonResponse(responseEntity.getBody(), person);
    }

    @Test
    public void deletePersonDeletesPerson()
    {
        Person person = createPerson();

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                url("/api/person/{personId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                person.getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(personRepository.existsById(person.getId()), equalTo(false));
    }

    @Test
    public void deletePersonHasNoEffectIfPersonDoesNotExist()
    {
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                url("/api/person/{personId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                randomLong());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
    }

    static Stream<Arguments> createPersonBadRequests()
    {
        return Stream.of(
                        // null name
                        new CreateOrUpdatePersonRequest()
                                .withName(null)
                                .withEmailAddress(randomString())
                                .withPhone(randomString())
                                .withActive(randomBoolean()),

                        // null email address
                        new CreateOrUpdatePersonRequest()
                                .withName(randomString())
                                .withEmailAddress(null)
                                .withPhone(randomString())
                                .withActive(randomBoolean()),

                        // null active
                        new CreateOrUpdatePersonRequest()
                                .withName(randomString())
                                .withEmailAddress(randomString())
                                .withPhone(randomString())
                                .withActive(null))
                .map(Arguments::arguments);
    }

    private Permission createPermission()
    {
        return createPermission(randomString());
    }

    private Permission createPermission(String name)
    {
        Permission permission = new Permission();
        permission.setName(name);
        permission = permissionRepository.save(permission);
        addDbCleanup("permission", permission.getId());

        return permission;
    }

    private Permission createPermissionWithPrefix(String prefix)
    {
        return createPermission(randomStringWithPrefix(prefix));
    }

    private Person createPerson()
    {
        return createPerson(
                randomString(),
                permissions.stream().filter(permission -> randomBoolean()).collect(Collectors.toSet()),
                randomBoolean());
    }

    private Person createPerson(String name, Set<Permission> permissions, Boolean active)
    {
        Person person = new Person();
        person.setName(name);
        person.setEmailAddress(randomString());
        person.setPhone(randomString());
        person.setPermissions(permissions);
        person.setActive(active);
        person = personRepository.save(person);
        addDbCleanup("person", person.getId());

        return person;
    }

    private PermissionResponse getPermissionResponseFromPermission(Permission permission)
    {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());

        return response;
    }

    private PersonResponse getPersonResponseFromPerson(Person person)
    {
        PersonResponse response = new PersonResponse();
        response.setId(person.getId());
        response.setName(person.getName());
        response.setEmailAddress(person.getEmailAddress());
        response.setPhone(person.getPhone());
        response.setPermissions((person.getPermissions() != null ? person.getPermissions() : Collections.<Permission>emptySet())
                .stream()
                .map(this::getPermissionResponseFromPermission)
                .sorted(Comparator.comparing(PermissionResponse::getName))
                .toList());
        response.setActive(person.getActive());

        return response;
    }

    private void validatePersonResponse(PersonResponse response, Person person)
    {
        if (person == null) {
            assertThat(response, nullValue());
            return;
        }

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(person.getId()));
        assertThat(response.getName(), equalTo(person.getName()));
        assertThat(response.getEmailAddress(), equalTo(person.getEmailAddress()));
        assertThat(response.getPhone(), equalTo(person.getPhone()));
        assertThat(response.getActive(), equalTo(person.getActive()));

        Set<PermissionResponse> expectedPermissionResponse = (person.getPermissions() != null ? person.getPermissions().stream() :
                Stream.<Permission>empty())
                .map(permission ->
                {
                    PermissionResponse permissionResponse = new PermissionResponse();
                    permissionResponse.setId(permission.getId());
                    permissionResponse.setName(permission.getName());
                    return permissionResponse;
                })
                .collect(Collectors.toSet());
        assertThat(response.getPermissions(), equalToUnordered(expectedPermissionResponse));
    }
}