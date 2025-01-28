package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.UpdateAction;
import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.GroupRepository;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdateGroupRequest;
import io.github.waynem77.bscmail4.model.request.UpdatePermissionsRequest;
import io.github.waynem77.bscmail4.model.response.GroupResponse;
import io.github.waynem77.bscmail4.model.response.GroupsResponse;
import io.github.waynem77.bscmail4.model.specification.GroupFilter;
import io.github.waynem77.bscmail4.model.specification.SortDirection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.JpaSystemException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.waynem77.bscmail4.TestUtils.randomLong;
import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GroupService}.
 */
class GroupServiceTest
{
    private GroupRepository groupRepository;
    private PermissionRepository permissionRepository;

    @BeforeEach
    public void setup()
    {
        groupRepository = mock(GroupRepository.class);
        permissionRepository = mock(PermissionRepository.class);
    }

    @Test
    public void createGroupThrowsIfRequestIsNull()
    {
        CreateOrUpdateGroupRequest request = null;

        assertThrows(NullPointerException.class, () -> createTestable().createGroup(request));

        verify(groupRepository, never()).save(any());
    }

    @Test
    public void createGroupThrowsIfRequestIsInvalid()
    {
        CreateOrUpdateGroupRequest request = mock(CreateOrUpdateGroupRequest.class);
        given(request.getName()).willReturn(null);

        assertThrows(BadRequestException.class, () -> createTestable().createGroup(request));

        verify(groupRepository, never()).save(any());
    }

    @Test
    public void createGroupThrowsIfRepositoryThrows()
    {
        given(groupRepository.save(any())).willThrow(JpaSystemException.class);

        CreateOrUpdateGroupRequest request = mock(CreateOrUpdateGroupRequest.class);
        given(request.getName()).willReturn(randomString());

        assertThrows(BadRequestException.class, () -> createTestable().createGroup(request));

        verify(groupRepository, times(1)).save(any());
    }

    @Test
    public void createGroupCreatesGroup()
    {
        String groupName = randomString();
        Long groupId = randomLong();

        Group groupToReturn = mock(Group.class);
        given(groupToReturn.getId()).willReturn(groupId);
        given(groupToReturn.getName()).willReturn(groupName);

        given(groupRepository.save(any(Group.class))).willReturn(groupToReturn);

        CreateOrUpdateGroupRequest request = mock(CreateOrUpdateGroupRequest.class);
        given(request.getName()).willReturn(groupName);

        GroupService groupService = createTestable();
        GroupResponse response = groupService.createGroup(request);

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(groupId));
        assertThat(response.getName(), equalTo(groupName));
        assertThat(response.getMemberCount(), equalTo(0L));
        assertThat(response.getActiveMemberCount(), equalTo(0L));
        assertThat(response.getPermissions(), equalTo(Collections.emptyList()));

        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(groupCaptor.capture());
        assertThat(groupCaptor.getValue(), notNullValue());
        assertThat(groupCaptor.getValue().getName(), equalTo(groupName));
        assertThat(groupCaptor.getValue().getPeople(), nullValue());
        assertThat(groupCaptor.getValue().getPermissions(), nullValue());
    }

    @Test
    public void getGroupByIdReturnsNotFoundWhenGroupDoesNotExist()
    {
        given(groupRepository.findById(any())).willReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> createTestable().getGroupById(randomLong()));
    }

    @Test
    public void getGroupByIdReturnsCorrectValue()
    {
        Group group = createGroup();
        given(groupRepository.findById(group.getId())).willReturn(Optional.of(group));

        Permission permission = group.getPermissions().iterator().next();

        GroupService groupService = createTestable();

        GroupResponse response = groupService.getGroupById(group.getId());
        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(group.getId()));
        assertThat(response.getName(), equalTo(group.getName()));
        assertThat(response.getMemberCount(), equalTo(3L));
        assertThat(response.getActiveMemberCount(), equalTo(2L));
        assertThat(response.getPermissions(), notNullValue());
        assertThat(response.getPermissions().size(), equalTo(1));
        assertThat(response.getPermissions().get(0), notNullValue());
        assertThat(response.getPermissions().get(0).getId(), equalTo(permission.getId()));
        assertThat(response.getPermissions().get(0).getName(), equalTo(permission.getName()));
    }

    @Test
    public void deletePermissionByIdDeletesPermission()
    {
        Long id = randomLong();

        GroupService groupService = createTestable();
        groupService.deleteGroupById(id);

        verify(groupRepository, times(1)).deleteById(eq(id));
    }

    @Test
    public void getGroupsThrowsWhenFilterIsNull()
    {
        GroupFilter filter = null;
        SortDirection direction = SortDirection.ASC;
        int page = 0;
        int size = 1;

        GroupService groupService = createTestable();
        assertThrows(NullPointerException.class, () -> groupService.getGroups(filter, direction, page, size));
    }

    @Test
    public void getGroupsThrowsWhenSortDirectionIsNull()
    {
        GroupFilter filter = new GroupFilter();
        SortDirection direction = null;
        int page = 0;
        int size = 1;

        GroupService groupService = createTestable();
        assertThrows(NullPointerException.class, () -> groupService.getGroups(filter, direction, page, size));
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -1})
    public void getGroupsThrowsWhenPageIsInvalid(int page)
    {
        GroupFilter filter = new GroupFilter();
        SortDirection direction = SortDirection.ASC;
        int size = 1;

        GroupService groupService = createTestable();
        assertThrows(IllegalArgumentException.class, () -> groupService.getGroups(filter, direction, page, size));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void getGroupsThrowsWhenSizeIsInvalid(int size)
    {
        GroupFilter filter = new GroupFilter();
        SortDirection direction = SortDirection.ASC;
        int page = 0;

        GroupService groupService = createTestable();
        assertThrows(IllegalArgumentException.class, () -> groupService.getGroups(filter, direction, page, size));
    }

    @Test
    public void getGroupsFiltersCorrectly()
    {
        Specification<Group> specification =
                (Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> null;

        GroupFilter filter = mock(GroupFilter.class);
        given(filter.toSpecification()).willReturn(specification);

        SortDirection direction = SortDirection.ASC;
        int page = 0;
        int size = 25;

        Page<Group> personPage = new PageImpl<>(List.of(mock(Group.class)));
        given(groupRepository.findAll(eq(specification), any(Pageable.class))).willReturn(personPage);

        GroupService groupService = createTestable();

        GroupsResponse response = groupService.getGroups(filter, direction, page, size);
        assertThat(response, notNullValue());

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(groupRepository).findAll(eq(specification), pageableArgumentCaptor.capture());
        Pageable pageable = pageableArgumentCaptor.getValue();
        assertThat(pageable.getPageNumber(), CoreMatchers.equalTo(page));
        assertThat(pageable.getPageSize(), CoreMatchers.equalTo(size));
        assertThat(pageable.getSort(), CoreMatchers.equalTo(Sort.by(Sort.Direction.ASC, "name")));
    }

    @Test
    public void updatePermissionsThrowsIfEitherParameterIsNull()
    {
        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setPermissionIds(List.of(randomLong()));

        GroupService groupService = createTestable();

        assertThrows(NullPointerException.class, () -> groupService.updatePermissions(null, request));
        assertThrows(NullPointerException.class, () -> groupService.updatePermissions(randomLong(), null));
    }

    @Test
    public void updatePermissionsThrowsIfRequestHasInvalidAction()
    {
        Long groupId = randomLong();

        UpdatePermissionsRequest request1 = new UpdatePermissionsRequest();
        request1.setPermissionIds(List.of(randomLong()));

        GroupService groupService = createTestable();
        assertThrows(BadRequestException.class, () -> groupService.updatePermissions(groupId, request1));

        UpdatePermissionsRequest request2 = new UpdatePermissionsRequest();
        request2.setAction(randomString());
        request2.setPermissionIds(List.of(randomLong()));

        assertThrows(BadRequestException.class, () -> groupService.updatePermissions(groupId, request2));
    }

    @Test
    public void updatePermissionsThrowsWhenGroupDoesNotExist()
    {
        given(permissionRepository.existsById(any())).willReturn(true);

        Long groupId = randomLong();
        given(groupRepository.findById(groupId)).willReturn(Optional.empty());

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction(UpdateAction.ADD.getValue());
        request.setPermissionIds(List.of(randomLong()));

        GroupService groupService = createTestable();

        assertThrows(NotFoundException.class, () -> groupService.updatePermissions(groupId, request));
    }

    @Test
    public void updatePermissionsThrowsWhenPermissionDoesNotExist()
    {
        Long groupId = randomLong();
        given(groupRepository.findById(groupId)).willReturn(Optional.of(mock(Group.class)));

        List<Long> permissionIds = List.of(randomLong(), randomLong());
        given(permissionRepository.existsById(permissionIds.get(0))).willReturn(true);
        given(permissionRepository.existsById(permissionIds.get(1))).willReturn(false);
        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction(UpdateAction.ADD.getValue());
        request.setPermissionIds(permissionIds);

        GroupService groupService = createTestable();

        assertThrows(BadRequestException.class, () -> groupService.updatePermissions(groupId, request));
    }

    @Test
    public void updatePermissionsDoesNotThrowWhenPermissionIdsIsEmpty()
    {
        Long groupId = randomLong();
        given(groupRepository.findById(groupId)).willReturn(Optional.of(mock(Group.class)));
        given(groupRepository.save(any())).willReturn(mock(Group.class));

        List<Long> permissionIds = Collections.emptyList();
        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction(UpdateAction.ADD.getValue());
        request.setPermissionIds(permissionIds);

        GroupService groupService = createTestable();

        assertDoesNotThrow(() -> groupService.updatePermissions(groupId, request));
    }

    @Test
    public void updatePermissionsAddsPermissions()
    {
        Group group = createGroup();
        Long groupId = group.getId();
        Permission existingPermission = group.getPermissions().stream().findAny().get();
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupRepository.save(any())).willReturn(group);

        Permission newPermission = createPermission();
        List<Long> permissionIds = List.of(existingPermission.getId(), newPermission.getId(), newPermission.getId());
        given(permissionRepository.existsById(existingPermission.getId())).willReturn(true);
        given(permissionRepository.existsById(newPermission.getId())).willReturn(true);
        given(permissionRepository.findAllByIdIn(permissionIds)).willReturn(Set.of(existingPermission, newPermission));

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction(UpdateAction.ADD.getValue());
        request.setPermissionIds(permissionIds);

        GroupService groupService = createTestable();
        groupService.updatePermissions(groupId, request);

        verify(group).setPermissions(Set.of(existingPermission, newPermission));
        verify(groupRepository).save(group);
    }

    @Test
    public void updatePermissionsRemovesPermissions()
    {
        List<Permission> existingPermissions = List.of(
                createPermission(),
                createPermission());

        Group group = createGroup();
        given(group.getPermissions()).willReturn(Set.of(existingPermissions.get(0), existingPermissions.get(1)));
        Long groupId = group.getId();
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupRepository.save(any())).willReturn(group);

        Permission newPermission = createPermission();
        List<Long> permissionIds = List.of(existingPermissions.get(0).getId(), existingPermissions.get(0).getId(),
                newPermission.getId());
        given(permissionRepository.existsById(existingPermissions.get(0).getId())).willReturn(true);
        given(permissionRepository.existsById(newPermission.getId())).willReturn(true);
        given(permissionRepository.findAllByIdIn(permissionIds)).willReturn(Set.of(existingPermissions.get(0),
                newPermission));

        UpdatePermissionsRequest request = new UpdatePermissionsRequest();
        request.setAction(UpdateAction.REMOVE.getValue());
        request.setPermissionIds(permissionIds);

        GroupService groupService = createTestable();
        groupService.updatePermissions(groupId, request);

        verify(group).setPermissions(Set.of(existingPermissions.get(1)));
        verify(groupRepository).save(group);
    }

    private GroupService createTestable()
    {
        return new GroupService(
                groupRepository,
                permissionRepository);
    }

    private Permission createPermission()
    {
        Permission permission = mock(Permission.class);
        given(permission.getId()).willReturn(randomLong());
        given(permission.getName()).willReturn(randomString());

        return permission;
    }

    private Person createPerson(boolean isActive)
    {
        Person person = mock(Person.class);
        given(person.getId()).willReturn(randomLong());
        given(person.getName()).willReturn(randomString());
        given(person.getEmailAddress()).willReturn(randomString());
        given(person.getPhone()).willReturn(randomString());
        given(person.getActive()).willReturn(isActive);

        return person;
    }

    private Group createGroup()
    {
        Set<Permission> permissions = Set.of(createPermission());

        List<Person> people = List.of(createPerson(true), createPerson(false), createPerson(true));

        Group group = mock(Group.class);
        given(group.getId()).willReturn(randomLong());
        given(group.getName()).willReturn(randomString());
        given(group.getPermissions()).willReturn(permissions);
        given(group.getPeople()).willReturn(people);

        return group;
    }
}