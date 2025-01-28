package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.GroupRepository;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdateGroupRequest;
import io.github.waynem77.bscmail4.model.request.UpdatePermissionsRequest;
import io.github.waynem77.bscmail4.model.response.GroupResponse;
import io.github.waynem77.bscmail4.model.response.GroupsResponse;
import io.github.waynem77.bscmail4.model.response.PermissionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for {@link GroupController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupControllerIT extends BaseIT
{
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PersonRepository personRepository;

    @Test
    public void createGroupAddsAGroup()
    {
        CreateOrUpdateGroupRequest request = new CreateOrUpdateGroupRequest()
                .withName(randomString());

        ResponseEntity<GroupResponse> responseEntity = restTemplate.postForEntity(
                url("/api/group"),
                request,
                GroupResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        GroupResponse groupResponse = responseEntity.getBody();
        assertThat(groupResponse, notNullValue());
        assertThat(groupResponse.getId(), notNullValue());
        addDbCleanup("groupp", groupResponse.getId());
        assertThat(groupResponse.getName(), equalTo(request.getName()));

        Optional<Group> group = groupRepository.findById(groupResponse.getId());
        assertThat(group.isPresent(), equalTo(true));
        assertThat(group.get().getName(), equalTo(request.getName()));
    }

    @Test
    public void createGroupReturnsBadRequestWhenRequestIsInvalid()
    {
        CreateOrUpdateGroupRequest request = new CreateOrUpdateGroupRequest();

        ResponseEntity<GroupResponse> responseEntity = restTemplate.postForEntity(
                url("/api/group"),
                request,
                GroupResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void createGroupReturnsBadRequestWhenGroupNameIsNotUnique()
    {
        CreateOrUpdateGroupRequest request = new CreateOrUpdateGroupRequest()
                .withName(randomString());

        ResponseEntity<GroupResponse> originalResponseEntity = restTemplate.postForEntity(
                url("/api/group"),
                request,
                GroupResponse.class,
                request);

        assertThat(originalResponseEntity, notNullValue());
        assertThat(originalResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
        addDbCleanup("groupp", originalResponseEntity.getBody().getId());

        ResponseEntity<GroupResponse> duplicateResponseEntity = restTemplate.postForEntity(
                url("/api/group"),
                request,
                GroupResponse.class,
                request);

        assertThat(duplicateResponseEntity, notNullValue());
        assertThat(duplicateResponseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void getGroupByIdReturnsNotFoundWhenGroupDoesNotExist()
    {
        Long groupId = randomLong();

        ResponseEntity<GroupResponse> responseEntity = restTemplate.getForEntity(
                url("/api/group/{groupId}"),
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getGroupByIdReturnsCorrectValue()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission());
        List<Person> people = List.of(
                createPerson(true),
                createPerson(false),
                createPerson(true),
                createPerson(false),
                createPerson(true));
        Group group = createGroup(
                Set.of(permissions.get(0), permissions.get(2)),
                List.of(people.get(0), people.get(1), people.get(2)));
        Long groupId = group.getId();

        ResponseEntity<GroupResponse> responseEntity = restTemplate.getForEntity(
                url("/api/group/{groupId}"),
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
        GroupResponse response = responseEntity.getBody();
        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(groupId));
        assertThat(response.getName(), equalTo(group.getName()));
        assertThat(response.getMemberCount(), equalTo(3L));
        assertThat(response.getActiveMemberCount(), equalTo(2L));
        assertThat(
                response.getPermissions(),
                equalToUnordered(List.of(
                        getResponseFromPermission(permissions.get(0)),
                        getResponseFromPermission(permissions.get(2)))));
        assertThat(response.getMessages(), equalTo(Collections.emptyList()));
    }

    @Test
    public void deleteGroupDeletesPermission()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission());
        List<Person> people = List.of(
                createPerson(),
                createPerson(),
                createPerson());
        List<Group> groups = List.of(
                createGroup(Set.of(permissions.get(0), permissions.get(1)), List.of(people.get(0), people.get(1))),
                createGroup(Set.of(permissions.get(0)), List.of(people.get(0))));

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                groups.get(0).getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(groupRepository.existsById(groups.get(0).getId()), equalTo(false));
        assertThat(groupRepository.existsById(groups.get(1).getId()), equalTo(true));

        assertThat(permissionRepository.existsById(permissions.get(0).getId()), equalTo(true));
        assertThat(permissionRepository.existsById(permissions.get(1).getId()), equalTo(true));
        assertThat(permissionRepository.existsById(permissions.get(2).getId()), equalTo(true));

        assertThat(personRepository.existsById(people.get(0).getId()), equalTo(true));
        assertThat(personRepository.existsById(people.get(1).getId()), equalTo(true));
        assertThat(personRepository.existsById(people.get(2).getId()), equalTo(true));
    }

    @Test
    public void deleteGroupDoesNothingWhenGroupDoesNotExist()
    {
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                randomLong());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
    }

    @Test
    public void deleteGroupIsIdempotent()
    {
        List<Group> groups = List.of(
                createGroup(Collections.emptySet(), Collections.emptyList()),
                createGroup(Collections.emptySet(), Collections.emptyList()));

        ResponseEntity<Void> responseEntityForExistingGroup = restTemplate.exchange(
                url("/api/group/{groupId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                groups.get(0).getId());

        assertThat(responseEntityForExistingGroup, notNullValue());
        assertThat(responseEntityForExistingGroup.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(groupRepository.existsById(groups.get(0).getId()), equalTo(false));
        assertThat(groupRepository.existsById(groups.get(1).getId()), equalTo(true));

        ResponseEntity<Void> responseEntityForDeletedGroup = restTemplate.exchange(
                url("/api/group/{groupId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                groups.get(0).getId());

        assertThat(responseEntityForDeletedGroup, notNullValue());
        assertThat(responseEntityForDeletedGroup.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(groupRepository.existsById(groups.get(0).getId()), equalTo(false));
        assertThat(groupRepository.existsById(groups.get(1).getId()), equalTo(true));
    }

    @Nested
    public class GetAllGroups
    {
        private String nameFilter;
        private String permissionFilter;
        private String personFilter;

        private List<Group> groups;

        @BeforeEach
        public void setup()
        {
            nameFilter = "xyz";

            List<Permission> permissions = List.of(
                    createPermission(),
                    createPermission(),
                    createPermission());
            permissionFilter = permissions.get(0).getId() + "," + permissions.get(0).getId();

            List<Person> people = List.of(
                    createPerson(),
                    createPerson(),
                    createPerson());
            personFilter = people.get(0).getId() + "," + people.get(0).getId();

            groups = List.of(
                    // Item 0: matches name, permission, person filters
                    createGroup(
                            randomStringContaining(nameFilter),
                            Set.of(permissions.get(0), permissions.get(1), permissions.get(2)),
                            List.of(people.get(0), people.get(1), people.get(2))),

                    // Item 1: matches name, permission filters
                    createGroup(
                            randomStringContaining(nameFilter),
                            Set.of(permissions.get(0), permissions.get(1), permissions.get(2)),
                            List.of(people.get(1), people.get(2))),

                    // Item 2: matches name, person filters
                    createGroup(
                            randomStringWithPrefix(nameFilter),
                            Set.of(permissions.get(1), permissions.get(2)),
                            List.of(people.get(0), people.get(1), people.get(2))),

                    // Item 3: matches name filter
                    createGroup(
                            randomStringWithSuffix(nameFilter),
                            Set.of(permissions.get(1), permissions.get(2)),
                            List.of(people.get(1), people.get(2))),

                    // Item 4: matches permission, person filters
                    createGroup(
                            Set.of(permissions.get(0), permissions.get(1), permissions.get(2)),
                            List.of(people.get(0), people.get(1), people.get(2))),

                    // Item 5: matches permission filter
                    createGroup(
                            Set.of(permissions.get(0), permissions.get(1), permissions.get(2)),
                            List.of(people.get(1), people.get(2))),

                    // Item 6: matches person filter
                    createGroup(
                            Set.of(permissions.get(1), permissions.get(2)),
                            List.of(people.get(0), people.get(1), people.get(2))),

                    // Item 7: matches no filters
                    createGroup(
                            Set.of(permissions.get(1), permissions.get(2)),
                            List.of(people.get(1), people.get(2))));
        }

        @Test
        public void getAllGroupsGetsAllGroups()
        {
            ResponseEntity<GroupsResponse> responseEntity = restTemplate.getForEntity(
                    url("/api/group"),
                    GroupsResponse.class);
            assertThat(responseEntity, notNullValue());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponses = groups.stream()
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName))
                    .toList();

            GroupsResponse groupsResponse = responseEntity.getBody();
            assertThat(groupsResponse, notNullValue());
            assertThat(groupsResponse.getPageInfo(), notNullValue());
            assertThat(groupsResponse.getPageInfo().getNumber(), equalTo(0));
            assertThat(groupsResponse.getPageInfo().getSize(), equalTo(25));
            assertThat(groupsResponse.getPageInfo().getTotalElements(), equalTo(8L));
            assertThat(groupsResponse.getPageInfo().isFirst(), equalTo(true));
            assertThat(groupsResponse.getPageInfo().isLast(), equalTo(true));
            assertThat(groupsResponse.getContent(), equalTo(expectedGroupResponses));
        }

        @Test
        public void getAllGroupsFiltersByName()
        {
            ResponseEntity<GroupsResponse> responseEntityForName = restTemplate.getForEntity(
                    url("/api/group?name={name}"),
                    GroupsResponse.class,
                    nameFilter);
            assertThat(responseEntityForName, notNullValue());
            assertThat(responseEntityForName.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForName = IntStream.of(0, 1, 2, 3)
                    .mapToObj(groups::get)
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName))
                    .toList();

            GroupsResponse groupsResponseForName = responseEntityForName.getBody();
            assertThat(groupsResponseForName, notNullValue());
            assertThat(groupsResponseForName.getPageInfo(), notNullValue());
            assertThat(groupsResponseForName.getPageInfo().getNumber(), equalTo(0));
            assertThat(groupsResponseForName.getPageInfo().getSize(), equalTo(25));
            assertThat(groupsResponseForName.getPageInfo().getTotalElements(), equalTo(4L));
            assertThat(groupsResponseForName.getPageInfo().isFirst(), equalTo(true));
            assertThat(groupsResponseForName.getPageInfo().isLast(), equalTo(true));
            assertThat(groupsResponseForName.getContent(), equalTo(expectedGroupResponsesForName));
        }

        @Test
        public void getAllGroupsFiltersByPermissions()
        {
            ResponseEntity<GroupsResponse> responseEntityForName = restTemplate.getForEntity(
                    url("/api/group?permissions={permissions}"),
                    GroupsResponse.class,
                    permissionFilter);
            assertThat(responseEntityForName, notNullValue());
            assertThat(responseEntityForName.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForName = IntStream.of(0, 1, 4, 5)
                    .mapToObj(groups::get)
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName))
                    .toList();

            GroupsResponse groupsResponseForName = responseEntityForName.getBody();
            assertThat(groupsResponseForName, notNullValue());
            assertThat(groupsResponseForName.getPageInfo(), notNullValue());
            assertThat(groupsResponseForName.getPageInfo().getNumber(), equalTo(0));
            assertThat(groupsResponseForName.getPageInfo().getSize(), equalTo(25));
            assertThat(groupsResponseForName.getPageInfo().getTotalElements(), equalTo(4L));
            assertThat(groupsResponseForName.getPageInfo().isFirst(), equalTo(true));
            assertThat(groupsResponseForName.getPageInfo().isLast(), equalTo(true));
            assertThat(groupsResponseForName.getContent(), equalTo(expectedGroupResponsesForName));
        }

        @Test
        public void getAllGroupsFiltersByPerson()
        {
            ResponseEntity<GroupsResponse> responseEntityForName = restTemplate.getForEntity(
                    url("/api/group?people={people}"),
                    GroupsResponse.class,
                    personFilter);
            assertThat(responseEntityForName, notNullValue());
            assertThat(responseEntityForName.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForName = IntStream.of(0, 2, 4, 6)
                    .mapToObj(groups::get)
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName))
                    .toList();

            GroupsResponse groupsResponseForName = responseEntityForName.getBody();
            assertThat(groupsResponseForName, notNullValue());
            assertThat(groupsResponseForName.getPageInfo(), notNullValue());
            assertThat(groupsResponseForName.getPageInfo().getNumber(), equalTo(0));
            assertThat(groupsResponseForName.getPageInfo().getSize(), equalTo(25));
            assertThat(groupsResponseForName.getPageInfo().getTotalElements(), equalTo(4L));
            assertThat(groupsResponseForName.getPageInfo().isFirst(), equalTo(true));
            assertThat(groupsResponseForName.getPageInfo().isLast(), equalTo(true));
            assertThat(groupsResponseForName.getContent(), equalTo(expectedGroupResponsesForName));
        }

        @Test
        public void getAllGroupsFiltersMultipleFilters()
        {
            ResponseEntity<GroupsResponse> responseEntityForName = restTemplate.getForEntity(
                    url("/api/group?name={name}&permissions={permissions}&people={people}"),
                    GroupsResponse.class,
                    nameFilter,
                    permissionFilter,
                    personFilter);
            assertThat(responseEntityForName, notNullValue());
            assertThat(responseEntityForName.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForName = IntStream.of(0)
                    .mapToObj(groups::get)
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .toList();

            GroupsResponse groupsResponseForName = responseEntityForName.getBody();
            assertThat(groupsResponseForName, notNullValue());
            assertThat(groupsResponseForName.getPageInfo(), notNullValue());
            assertThat(groupsResponseForName.getPageInfo().getNumber(), equalTo(0));
            assertThat(groupsResponseForName.getPageInfo().getSize(), equalTo(25));
            assertThat(groupsResponseForName.getPageInfo().getTotalElements(), equalTo(1L));
            assertThat(groupsResponseForName.getPageInfo().isFirst(), equalTo(true));
            assertThat(groupsResponseForName.getPageInfo().isLast(), equalTo(true));
            assertThat(groupsResponseForName.getContent(), equalTo(expectedGroupResponsesForName));
        }

        @Test
        public void getAllGroupsSortsByName()
        {
            ResponseEntity<GroupsResponse> responseEntityForAscending = restTemplate.getForEntity(
                    url("/api/group?direction=ascending"),
                    GroupsResponse.class);
            assertThat(responseEntityForAscending, notNullValue());
            assertThat(responseEntityForAscending.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForAscending = groups.stream()
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName))
                    .toList();

            GroupsResponse groupsResponseForAscending = responseEntityForAscending.getBody();
            assertThat(groupsResponseForAscending, notNullValue());
            assertThat(groupsResponseForAscending.getPageInfo(), notNullValue());
            assertThat(groupsResponseForAscending.getPageInfo().getNumber(), equalTo(0));
            assertThat(groupsResponseForAscending.getPageInfo().getSize(), equalTo(25));
            assertThat(groupsResponseForAscending.getPageInfo().getTotalElements(), equalTo(8L));
            assertThat(groupsResponseForAscending.getPageInfo().isFirst(), equalTo(true));
            assertThat(groupsResponseForAscending.getPageInfo().isLast(), equalTo(true));
            assertThat(groupsResponseForAscending.getContent(), equalTo(expectedGroupResponsesForAscending));

            ResponseEntity<GroupsResponse> responseEntityForDescending = restTemplate.getForEntity(
                    url("/api/group?direction=descending"),
                    GroupsResponse.class);
            assertThat(responseEntityForDescending, notNullValue());
            assertThat(responseEntityForDescending.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForDescending = groups.stream()
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName).reversed())
                    .toList();

            GroupsResponse groupsResponseForDescending = responseEntityForDescending.getBody();
            assertThat(groupsResponseForDescending, notNullValue());
            assertThat(groupsResponseForDescending.getPageInfo(), notNullValue());
            assertThat(groupsResponseForDescending.getPageInfo().getNumber(), equalTo(0));
            assertThat(groupsResponseForDescending.getPageInfo().getSize(), equalTo(25));
            assertThat(groupsResponseForDescending.getPageInfo().getTotalElements(), equalTo(8L));
            assertThat(groupsResponseForDescending.getPageInfo().isFirst(), equalTo(true));
            assertThat(groupsResponseForDescending.getPageInfo().isLast(), equalTo(true));
            assertThat(groupsResponseForDescending.getContent(), equalTo(expectedGroupResponsesForDescending));
        }

        @Test
        public void getAllGroupsPages()
        {
            ResponseEntity<GroupsResponse> responseEntityForPage0 = restTemplate.getForEntity(
                    url("/api/group?page=0&size=3"),
                    GroupsResponse.class);
            assertThat(responseEntityForPage0, notNullValue());
            assertThat(responseEntityForPage0.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForPage0 = groups.stream()
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName))
                    .limit(3)
                    .toList();

            GroupsResponse groupsResponseForPage0 = responseEntityForPage0.getBody();
            assertThat(groupsResponseForPage0, notNullValue());
            assertThat(groupsResponseForPage0.getPageInfo(), notNullValue());
            assertThat(groupsResponseForPage0.getPageInfo().getNumber(), equalTo(0));
            assertThat(groupsResponseForPage0.getPageInfo().getSize(), equalTo(3));
            assertThat(groupsResponseForPage0.getPageInfo().getTotalElements(), equalTo(8L));
            assertThat(groupsResponseForPage0.getPageInfo().isFirst(), equalTo(true));
            assertThat(groupsResponseForPage0.getPageInfo().isLast(), equalTo(false));
            assertThat(groupsResponseForPage0.getContent(), equalTo(expectedGroupResponsesForPage0));

            ResponseEntity<GroupsResponse> responseEntityForPage1 = restTemplate.getForEntity(
                    url("/api/group?page=1&size=3"),
                    GroupsResponse.class);
            assertThat(responseEntityForPage1, notNullValue());
            assertThat(responseEntityForPage1.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForPage1 = groups.stream()
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName))
                    .skip(3)
                    .limit(3)
                    .toList();

            GroupsResponse groupsResponseForPage1 = responseEntityForPage1.getBody();
            assertThat(groupsResponseForPage1, notNullValue());
            assertThat(groupsResponseForPage1.getPageInfo(), notNullValue());
            assertThat(groupsResponseForPage1.getPageInfo().getNumber(), equalTo(1));
            assertThat(groupsResponseForPage1.getPageInfo().getSize(), equalTo(3));
            assertThat(groupsResponseForPage1.getPageInfo().getTotalElements(), equalTo(8L));
            assertThat(groupsResponseForPage1.getPageInfo().isFirst(), equalTo(false));
            assertThat(groupsResponseForPage1.getPageInfo().isLast(), equalTo(false));
            assertThat(groupsResponseForPage1.getContent(), equalTo(expectedGroupResponsesForPage1));

            ResponseEntity<GroupsResponse> responseEntityForPage2 = restTemplate.getForEntity(
                    url("/api/group?page=2&size=3"),
                    GroupsResponse.class);
            assertThat(responseEntityForPage2, notNullValue());
            assertThat(responseEntityForPage2.getStatusCode().is2xxSuccessful(), equalTo(true));

            List<GroupResponse> expectedGroupResponsesForPage2 = groups.stream()
                    .map(GroupControllerIT.this::getGroupResponseFromGroup)
                    .sorted(Comparator.comparing(GroupResponse::getName))
                    .skip(6)
                    .toList();

            GroupsResponse groupsResponseForPage2 = responseEntityForPage2.getBody();
            assertThat(groupsResponseForPage2, notNullValue());
            assertThat(groupsResponseForPage2.getPageInfo(), notNullValue());
            assertThat(groupsResponseForPage2.getPageInfo().getNumber(), equalTo(2));
            assertThat(groupsResponseForPage2.getPageInfo().getSize(), equalTo(3));
            assertThat(groupsResponseForPage2.getPageInfo().getTotalElements(), equalTo(8L));
            assertThat(groupsResponseForPage2.getPageInfo().isFirst(), equalTo(false));
            assertThat(groupsResponseForPage2.getPageInfo().isLast(), equalTo(true));
            assertThat(groupsResponseForPage2.getContent(), equalTo(expectedGroupResponsesForPage2));
        }
    }

    @Test
    public void updatePermissionsReturnsNotFoundWhenGroupDoesNotExist()
    {
        Permission permission = createPermission();
        Long groupId = randomLong();

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction("add");
        request.setPermissionIds(List.of(permission.getId()));
        HttpEntity<UpdatePermissionsRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntity,
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void updatePermissionsReturnsBadRequestWhenPermissionIdsIsNull()
    {
        Group group = createGroup(Collections.emptySet(), Collections.emptyList());
        Long groupId = group.getId();

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction("add");
        HttpEntity<UpdatePermissionsRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntity,
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updatePermissionsReturnsBadRequestWhenPermissionIdDoesNotExist()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission());
        Group group = createGroup(Collections.emptySet(), Collections.emptyList());
        Long groupId = group.getId();

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setPermissionIds(List.of(permissions.get(0).getId(), randomLong(), permissions.get(1).getId()));
        HttpEntity<UpdatePermissionsRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntity,
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updatePermissionsReturnsBadRequestWhenActionIsNull()
    {
        Permission permission = createPermission();

        Group group = createGroup(Collections.emptySet(), Collections.emptyList());
        Long groupId = group.getId();

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setPermissionIds(List.of(permission.getId()));
        HttpEntity<UpdatePermissionsRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntity,
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updatePermissionsReturnsBadRequestWhenActionIsInvalid()
    {
        Permission permission = createPermission();

        Group group = createGroup(Collections.emptySet(), Collections.emptyList());
        Long groupId = group.getId();

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction(randomString());
        request.setPermissionIds(List.of(permission.getId()));
        HttpEntity<UpdatePermissionsRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntity,
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updatePermissionsAddsPermissions()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission(),
                createPermission());
        Group group = createGroup(Collections.emptySet(), Collections.emptyList());
        Long groupId = group.getId();

        UpdatePermissionsRequest requestForGroupWithNoPermissions = new UpdatePermissionsRequest();
        requestForGroupWithNoPermissions.setAction("add");
        requestForGroupWithNoPermissions.setPermissionIds(
                List.of(permissions.get(0).getId(), permissions.get(1).getId()));
        HttpEntity<UpdatePermissionsRequest> httpEntityForGroupWithNoPermissions =
                new HttpEntity<>(requestForGroupWithNoPermissions);
        ResponseEntity<GroupResponse> responseEntityForGroupWithNoPermissions = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntityForGroupWithNoPermissions,
                GroupResponse.class,
                groupId);
        assertThat(responseEntityForGroupWithNoPermissions, notNullValue());
        assertThat(responseEntityForGroupWithNoPermissions.getStatusCode().is2xxSuccessful(), equalTo(true));
        GroupResponse expectedResponseForGroupWithNoPermissions = getGroupResponseFromGroupAndPermissions(group,
                List.of(permissions.get(0), permissions.get(1)));
        assertThat(
                responseEntityForGroupWithNoPermissions.getBody(),
                equalTo(expectedResponseForGroupWithNoPermissions));

        Group groupInDbAfterFirstRequest = groupRepository.findById(groupId).get();
        assertThat(groupInDbAfterFirstRequest.getPermissions(), equalTo(Set.of(permissions.get(0),
                permissions.get(1))));

        UpdatePermissionsRequest requestForGroupWithPermissions = new UpdatePermissionsRequest();
        requestForGroupWithPermissions.setAction("add");
        requestForGroupWithPermissions.setPermissionIds(List.of(permissions.get(2).getId(),
                permissions.get(3).getId()));
        HttpEntity<UpdatePermissionsRequest> httpEntityForGroupWithPermissions =
                new HttpEntity<>(requestForGroupWithPermissions);
        ResponseEntity<GroupResponse> responseEntityForGroupWithPermissions = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntityForGroupWithPermissions,
                GroupResponse.class,
                groupId);
        assertThat(responseEntityForGroupWithPermissions, notNullValue());
        assertThat(responseEntityForGroupWithPermissions.getStatusCode().is2xxSuccessful(), equalTo(true));
        GroupResponse expectedResponseForGroupWithPermissions = getGroupResponseFromGroup(group);
        expectedResponseForGroupWithPermissions.setPermissions(permissions.stream().map(PermissionResponse::fromPermission).sorted(Comparator.comparing(PermissionResponse::getName)).toList());
        assertThat(responseEntityForGroupWithPermissions.getBody(), equalTo(expectedResponseForGroupWithPermissions));

        Group groupInDbAfterSecondRequest = groupRepository.findById(groupId).get();
        assertThat(
                groupInDbAfterSecondRequest.getPermissions(),
                equalTo(Set.of(
                        permissions.get(0),
                        permissions.get(1),
                        permissions.get(2),
                        permissions.get(3))));
    }

    @Test
    public void updatePermissionsIgnoresDuplicatesDuringAdd()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission());
        Group group = createGroup(
                Set.of(permissions.get(0)),
                Collections.emptyList());
        Long groupId = group.getId();

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction("add");
        request.setPermissionIds(List.of(
                permissions.get(0).getId(),      // duplicates existing permission
                permissions.get(1).getId(),
                permissions.get(1).getId()));    // duplicates another permission in request
        HttpEntity<UpdatePermissionsRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntity,
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
        GroupResponse expectedResponse = getGroupResponseFromGroupAndPermissions(
                group,
                List.of(permissions.get(0), permissions.get(1)));
        assertThat(responseEntity.getBody(), equalTo(expectedResponse));

        Group groupInDbAfterFirstRequest = groupRepository.findById(groupId).get();
        assertThat(
                groupInDbAfterFirstRequest.getPermissions(),
                equalTo(Set.of(permissions.get(0), permissions.get(1))));
    }

    @Test
    public void updatePermissionsRemovesPermissions()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission(),
                createPermission());
        Group group = createGroup(permissions.stream().collect(Collectors.toSet()), Collections.emptyList());
        Long groupId = group.getId();

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction("remove");
        request.setPermissionIds(List.of(permissions.get(0).getId(), permissions.get(1).getId()));
        HttpEntity<UpdatePermissionsRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntity,
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
        GroupResponse expectedResponse = getGroupResponseFromGroupAndPermissions(
                group,
                List.of(permissions.get(2), permissions.get(3)));
        assertThat(responseEntity.getBody(), equalTo(expectedResponse));
    }

    @Test
    public void updatePermissionsIgnoresDuplicatesInRequestAndPermissionsNotInGroupDuringRemove()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission());
        Group group = createGroup(
                Set.of(permissions.get(0), permissions.get(1)),
                Collections.emptyList());
        Long groupId = group.getId();

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction("remove");
        request.setPermissionIds(List.of(
                permissions.get(0).getId(),
                permissions.get(0).getId(),      // duplicates another permission in request
                permissions.get(2).getId()));    // permission not in group
        HttpEntity<UpdatePermissionsRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(
                url("/api/group/{groupId}/permission"),
                HttpMethod.PATCH,
                httpEntity,
                GroupResponse.class,
                groupId);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
        GroupResponse expectedResponse = getGroupResponseFromGroupAndPermissions(group, List.of(permissions.get(1)));
        assertThat(responseEntity.getBody(), equalTo(expectedResponse));

        Group groupInDb = groupRepository.findById(groupId).get();
        assertThat(groupInDb.getPermissions(), equalTo(Set.of(permissions.get(1))));
    }

    private GroupResponse getGroupResponseFromGroup(Group group)
    {
        return getGroupResponseFromGroupAndPermissions(group, group.getPermissions());
    }

    private GroupResponse getGroupResponseFromGroupAndPermissions(Group group, Collection<Permission> permissions)
    {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setMemberCount((long) group.getPeople().size());
        response.setActiveMemberCount(group.getPeople().stream().filter(Person::getActive).count());
        response.setPermissions(
                permissions.stream()
                        .map(this::getPermissionResponseFromPermission)
                        .sorted(Comparator.comparing(PermissionResponse::getName))
                        .toList());
        response.setMessages(Collections.emptyList());

        return response;
    }

    private PermissionResponse getPermissionResponseFromPermission(Permission permission)
    {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());

        return response;
    }

    private Permission createPermission()
    {
        Permission permission = new Permission();
        permission.setName(randomString());
        permission = permissionRepository.save(permission);
        addDbCleanup("permission", permission.getId());

        return permission;
    }

    private Person createPerson()
    {
        return createPerson(true);
    }

    private Person createPerson(boolean isActive)
    {
        Person person = new Person();
        person.setName(randomString());
        person.setEmailAddress(randomString());
        person.setPhone(randomString());
        person.setActive(isActive);
        person = personRepository.save(person);
        addDbCleanup("person", person.getId());

        return person;
    }

    private Group createGroup(Set<Permission> permissions, List<Person> people)
    {
        return createGroup(randomString(), permissions, people);
    }

    private Group createGroup(String name, Set<Permission> permissions, List<Person> people)
    {
        Group group = new Group();
        group.setName(name);
        group.setPermissions(permissions);
        group.setPeople(people);
        group = groupRepository.save(group);
        addDbCleanup("groupp", group.getId());

        return group;
    }

    private PermissionResponse getResponseFromPermission(Permission permission)
    {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());

        return response;
    }
}