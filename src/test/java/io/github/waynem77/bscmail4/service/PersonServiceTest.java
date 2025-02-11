package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.UpdateAction;
import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.GroupRepository;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdatePersonRequest;
import io.github.waynem77.bscmail4.model.request.UpdateGroupsRequest;
import io.github.waynem77.bscmail4.model.response.PeopleResponse;
import io.github.waynem77.bscmail4.model.response.PersonResponse;
import io.github.waynem77.bscmail4.model.specification.PersonFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PersonService}.
 */
class PersonServiceTest
{
    private GroupService groupService;
    private PersonRepository personRepository;
    private PermissionRepository permissionRepository;
    private GroupRepository groupRepository;

    private Person person;
    private CreateOrUpdatePersonRequest request;

    @BeforeEach
    public void setup()
    {
        groupService = mock(groupService);
        personRepository = mock(PersonRepository.class);
        permissionRepository = mock(PermissionRepository.class);
        groupRepository = mock(GroupRepository.class);

        resetPersonAndRequest();

        given(personRepository.findById(any())).willReturn(Optional.of(person));
        given(personRepository.save(any(Person.class))).willReturn(person);
    }

    @Test
    public void getPeopleFilteredThrowsWhenFilterIsNull()
    {
        PersonService personService = createTestable();

        PersonFilter filter = null;
        int page = 0;
        int size = 25;
        assertThrows(NullPointerException.class, () -> personService.getPeopleFiltered(filter, page, size));
    }

    @Test
    public void getPeopleFilteredReturnsCorrectValue()
    {
        Specification<Person> specification =
                (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> null;

        PersonFilter filter = mock(PersonFilter.class);
        given(filter.toSpecification()).willReturn(specification);

        int page = 0;
        int size = 25;

        Page<Person> personPage = new PageImpl<>(List.of(mock(Person.class)));
        given(personRepository.findAll(eq(specification), any(Pageable.class))).willReturn(personPage);

        PersonService personService = createTestable();

        PeopleResponse response = personService.getPeopleFiltered(filter, page, size);
        assertThat(response, notNullValue());

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(personRepository).findAll(eq(specification), pageableArgumentCaptor.capture());
        Pageable pageable = pageableArgumentCaptor.getValue();
        assertThat(pageable.getPageNumber(), equalTo(page));
        assertThat(pageable.getPageSize(), equalTo(size));
        assertThat(pageable.getSort(), equalTo(Sort.by(Sort.Direction.ASC, "name")));
    }

    @Test
    public void createPersonThrowsWhenRequestIsNull()
    {
        PersonService personService = createTestable();

        request = null;
        assertThrows(NullPointerException.class, () -> personService.createPerson(request));

        verify(personRepository, never()).save(any());
    }

    @Test
    public void createPersonThrowsWhenRequestIsInvalid()
    {
        PersonService personService = createTestable();

        given(request.getName()).willReturn(null);
        assertThrows(BadRequestException.class, () -> personService.createPerson(request));

        resetRequest();
        given(request.getEmailAddress()).willReturn(null);
        assertThrows(BadRequestException.class, () -> personService.createPerson(request));

        resetRequest();
        given(request.getActive()).willReturn(null);
        assertThrows(BadRequestException.class, () -> personService.createPerson(request));

        verify(personRepository, never()).save(any());
    }

    @Test
    public void createPersonCreatesPerson()
    {
        PersonService personService = createTestable();

        PersonResponse response = personService.createPerson(request);
        validateResponseFromRequest(response, request);

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository, times(1)).save(personCaptor.capture());
        validatePersonFromRequest(personCaptor.getValue(), request);
    }

    @Test
    public void updatePersonUpdatesPerson()
    {
        Person person = createPerson();
        given(personRepository.findById(person.getId())).willReturn(Optional.of(person));

        PersonService personService = createTestable();

        PersonResponse response = personService.updatePerson(request, person.getId());
        validateResponseFromRequest(response, request);

        verify(person).setName(request.getName());
        verify(person).setEmailAddress(request.getEmailAddress());
        verify(person).setPhone(request.getPhone());
        verify(person).setPermissions(any());
        verify(person).setActive(request.getActive());

        verify(personRepository).save(person);
    }

    @Test
    public void updatePersonCreatesPersonWhenOriginalDoesNotExist()
    {
        Long personId = randomLong();
        given(personRepository.findById(personId)).willReturn(Optional.empty());

        PersonService personService = createTestable();

        PersonResponse response = personService.updatePerson(request, personId);
        validateResponseFromRequest(response, request);

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository, times(1)).save(personCaptor.capture());
        validatePersonFromRequest(personCaptor.getValue(), request);
    }

    @Test
    public void getPersonThrowsWhenPersonDoesNotExist()
    {
        given(personRepository.findById(any())).willReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> createTestable().getPerson(randomLong()));
    }

    @Test
    public void getPersonReturnsCorrectValue()
    {
        Person person = createPerson();
        Permission permission = person.getPermissions().stream().findFirst().get();
        given(personRepository.findById(person.getId())).willReturn(Optional.of(person));

        PersonService personService = createTestable();

        PersonResponse response = personService.getPerson(person.getId());
        validateResponseFromPerson(response, person);
        assertThat(response.getPermissions(), notNullValue());
        assertThat(response.getPermissions().size(), equalTo(1));
        assertThat(response.getPermissions().get(0), notNullValue());
        assertThat(response.getPermissions().get(0).getId(), equalTo(permission.getId()));
        assertThat(response.getPermissions().get(0).getName(), equalTo(permission.getName()));
    }

    @Test
    public void deletePersonThrowsWhenIdIsNull()
    {
        assertThrows(NullPointerException.class, () -> createTestable().deletePerson(null));
    }

    @Test
    public void deletePersonDeletesPerson()
    {
        PersonService personService = createTestable();
        personService.deletePerson(person.getId());

        verify(personRepository).deleteById(person.getId());
    }

    @Test
    public void updatepGroupersonsThrowsIfEitherParameterIsNull()
    {
        UpdateGroupsRequest request = new UpdateGroupsRequest();
        request.setGroupIds(List.of(randomLong()));

        PersonService personService = createTestable();

        assertThrows(NullPointerException.class, () -> personService.updateGroups(null, request));
        assertThrows(NullPointerException.class, () -> personService.updateGroups(randomLong(), null));
    }

    @Test
    public void updateGroupsThrowsIfRequestHasInvalidAction()
    {
        Long personId = randomLong();

        UpdateGroupsRequest request1 = new UpdateGroupsRequest();
        request1.setGroupIds(List.of(randomLong()));

        PersonService personService = createTestable();
        assertThrows(BadRequestException.class, () -> personService.updateGroups(personId, request1));

        UpdateGroupsRequest request2 = new UpdateGroupsRequest();
        request2.setAction(randomString());
        request2.setGroupIds(List.of(randomLong()));

        assertThrows(BadRequestException.class, () -> personService.updateGroups(personId, request2));
    }

    @Test
    public void updateGroupsThrowsWhenPersonDoesNotExist()
    {
        given(groupRepository.existsById(any())).willReturn(true);

        Long personId = randomLong();
        given(personRepository.findById(personId)).willReturn(Optional.empty());

        UpdateGroupsRequest request = new UpdateGroupsRequest();
        request.setAction(UpdateAction.ADD.getValue());
        request.setGroupIds(List.of(randomLong()));

        PersonService personService = createTestable();

        assertThrows(NotFoundException.class, () -> personService.updateGroups(personId, request));
    }

    @Test
    public void updateGroupsThrowsWhenGroupDoesNotExist()
    {
        Long personId = randomLong();
        given(personRepository.findById(personId)).willReturn(Optional.of(mock(Person.class)));

        List<Long> groupIds = List.of(randomLong(), randomLong());
        given(groupRepository.existsById(groupIds.get(0))).willReturn(true);
        given(groupRepository.existsById(groupIds.get(1))).willReturn(false);
        UpdateGroupsRequest request = new UpdateGroupsRequest();
        request.setAction(UpdateAction.ADD.getValue());
        request.setGroupIds(groupIds);

        PersonService personService = createTestable();

        assertThrows(BadRequestException.class, () -> personService.updateGroups(personId, request));
    }

    @Test
    public void updateGroupsDoesNotThrowWhenGroupIdsIsEmpty()
    {
        Long personId = randomLong();
        given(personRepository.findById(personId)).willReturn(Optional.of(mock(Person.class)));
        given(personRepository.save(any())).willReturn(mock(Person.class));

        List<Long> groupIds = Collections.emptyList();
        UpdateGroupsRequest request = new UpdateGroupsRequest();
        request.setAction(UpdateAction.ADD.getValue());
        request.setGroupIds(groupIds);

        PersonService personService = createTestable();

        assertDoesNotThrow(() -> personService.updateGroups(personId, request));
    }

    @Test
    public void updateGroupsAddsGroups()
    {
//        Person person = createPerson();
//        Long personId = person.getId();
//        Group existingGroup = person.getGroups().stream().findAny().get();
//        given(personRepository.findById(personId)).willReturn(Optional.of(person));
//        given(personRepository.save(any())).willReturn(person);
//
//        Group newGroup = createGroup();
//        List<Long> groupIds = List.of(existingGroup.getId(), newGroup.getId(), newGroup.getId());
//        given(groupRepository.existsById(existingGroup.getId())).willReturn(true);
//        given(groupRepository.existsById(newGroup.getId())).willReturn(true);
//        given(groupRepository.findAllByIdIn(groupIds)).willReturn(Set.of(existingGroup, newGroup));
//
//        UpdateGroupsRequest request = new UpdateGroupsRequest();
//        request.setAction(UpdateAction.ADD.getValue());
//        request.setGroupIds(groupIds);
//
//        PersonService personService = createTestable();
//        personService.updateGroups(personId, request);
//
//        verify(person).setGroups(Set.of(existingGroup, newGroup));
//        verify(personRepository).save(person);
    }

    @Test
    public void updateGroupsRemovesGroups()
    {
//        List<Group> existingGroups = List.of(
//                createGroup(),
//                createGroup());
//
//        Person person = createPerson();
//        given(person.getGroups()).willReturn(Set.of(existingGroups.get(0), existingGroups.get(1)));
//        Long personId = person.getId();
//        given(personRepository.findById(personId)).willReturn(Optional.of(person));
//        given(personRepository.save(any())).willReturn(person);
//
//        Group newGroup = createGroup();
//        List<Long> groupIds = List.of(existingGroups.get(0).getId(), existingGroups.get(0).getId(),
//                newGroup.getId());
//        given(groupRepository.existsById(existingGroups.get(0).getId())).willReturn(true);
//        given(groupRepository.existsById(newGroup.getId())).willReturn(true);
//        given(groupRepository.findAllByIdIn(groupIds)).willReturn(Set.of(existingGroups.get(0),
//                newGroup));
//
//        UpdateGroupsRequest request = new UpdateGroupsRequest();
//        request.setAction(UpdateAction.REMOVE.getValue());
//        request.setGroupIds(groupIds);
//
//        PersonService personService = createTestable();
//        personService.updateGroups(personId, request);
//
//        verify(person).setGroups(Set.of(existingGroups.get(1)));
//        verify(personRepository).save(person);
    }

    private void resetRequest()
    {
        request = mock(CreateOrUpdatePersonRequest.class);
        given(request.getName()).willReturn(randomString());
        given(request.getEmailAddress()).willReturn(randomString());
        given(request.getPhone()).willReturn(randomString());
        given(request.getActive()).willReturn(randomBoolean());
    }

    private void resetPersonAndRequest()
    {
        Long permissionId = randomLong();
        String permissionName = randomString();
        Long personId = randomLong();
        String personName = randomString();
        String personEmailAddress = randomString();
        String personPhone = randomString();
        Boolean personActive = randomBoolean();

        Permission permission = mock(Permission.class);
        given(permission.getId()).willReturn(permissionId);
        given(permission.getName()).willReturn(permissionName);

        person = mock(Person.class);
        given(person.getId()).willReturn(personId);
        given(person.getName()).willReturn(personName);
        given(person.getEmailAddress()).willReturn(personEmailAddress);
        given(person.getPhone()).willReturn(personPhone);
        given(person.getPermissions()).willReturn(Set.of(permission));
        given(person.getActive()).willReturn(personActive);

        request = mock(CreateOrUpdatePersonRequest.class);
        given(request.getName()).willReturn(personName);
        given(request.getEmailAddress()).willReturn(personEmailAddress);
        given(request.getPhone()).willReturn(personPhone);
        given(request.getPermissionIds()).willReturn(List.of(permissionId));
        given(request.getActive()).willReturn(personActive);
    }

    private Person createPerson()
    {
        Permission permission = createPermission();

        Person person = mock(Person.class);
        given(person.getId()).willReturn(randomLong());
        given(person.getName()).willReturn(randomString());
        given(person.getEmailAddress()).willReturn(randomString());
        given(person.getPhone()).willReturn(randomString());
        given(person.getPermissions()).willReturn(Set.of(permission));
        given(person.getActive()).willReturn(randomBoolean());

        return person;
    }

    private Group createGroup()
    {
        Set<Permission> permissions = Set.of(createPermission());

        List<Person> people = List.of(createPerson(), createPerson(), createPerson());

        Group group = mock(Group.class);
        given(group.getId()).willReturn(randomLong());
        given(group.getName()).willReturn(randomString());
        given(group.getPermissions()).willReturn(permissions);
        given(group.getPeople()).willReturn(people);

        return group;
    }

    private Permission createPermission()
    {
        Permission permission = mock(Permission.class);
        given(permission.getId()).willReturn(randomLong());
        given(permission.getName()).willReturn(randomString());

        return permission;
    }

    private void validatePersonFromRequest(Person person, CreateOrUpdatePersonRequest request)
    {
        if (request == null) {
            assertThat(person, nullValue());
            return;
        }

        assertThat(person, notNullValue());
        assertThat(person.getName(), equalTo(request.getName()));
        assertThat(person.getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(person.getPhone(), equalTo(request.getPhone()));
        assertThat(person.getActive(), equalTo(request.getActive()));
    }

    private void validateResponseFromRequest(PersonResponse response, CreateOrUpdatePersonRequest request)
    {
        if (request == null) {
            assertThat(response, nullValue());
            return;
        }

        assertThat(response, notNullValue());
        assertThat(response.getName(), equalTo(request.getName()));
        assertThat(response.getEmailAddress(), equalTo(request.getEmailAddress()));
        assertThat(response.getPhone(), equalTo(request.getPhone()));
        assertThat(response.getActive(), equalTo(request.getActive()));
    }

    private void validateResponseFromPerson(PersonResponse response, Person person)
    {
        if (request == null) {
            assertThat(response, nullValue());
            return;
        }

        assertThat(response, notNullValue());
        assertThat(response.getName(), equalTo(person.getName()));
        assertThat(response.getEmailAddress(), equalTo(person.getEmailAddress()));
        assertThat(response.getPhone(), equalTo(person.getPhone()));
        assertThat(response.getActive(), equalTo(person.getActive()));
    }

    private PersonService createTestable()
    {
        return new PersonService(
                groupService,
                personRepository,
                permissionRepository,
                groupRepository);
    }
}