package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.entity.Person;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.github.waynem77.bscmail4.TestUtils.randomLong;
import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GroupResponseTest
{
    @Test
    public void fromGroupReturnsCorrectValue()
    {
        Group group = createGroup();
        List<Permission> permissions = new ArrayList<>(group.getPermissions());

        GroupResponse response = GroupResponse.fromGroup(group);

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(group.getId()));
        assertThat(response.getName(), equalTo(group.getName()));
        assertThat(response.getMemberCount(), equalTo(3L));
        assertThat(response.getActiveMemberCount(), equalTo(2L));
        assertThat(response.getMessages(), equalTo(Collections.emptyList()));
        assertThat(response.getPermissions(), notNullValue());
        assertThat(response.getPermissions().size(), equalTo(2));

        List<PermissionResponse> receivedPermissions = response.getPermissions();
        List<Permission> expectedPermissions = permissions.get(0).getId().equals(receivedPermissions.get(0).getId()) ?
                List.of(permissions.get(0), permissions.get(1)) :
                List.of(permissions.get(1), permissions.get(0));

        assertThat(receivedPermissions.get(0).getId(), equalTo(expectedPermissions.get(0).getId()));
        assertThat(receivedPermissions.get(0).getName(), equalTo(expectedPermissions.get(0).getName()));
        assertThat(receivedPermissions.get(1).getId(), equalTo(expectedPermissions.get(1).getId()));
        assertThat(receivedPermissions.get(1).getName(), equalTo(expectedPermissions.get(1).getName()));
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
        Set<Permission> permissions = Set.of(createPermission(), createPermission());
        List<Person> people = List.of(createPerson(true), createPerson(true), createPerson(false));

        Group group = mock(Group.class);
        given(group.getId()).willReturn(randomLong());
        given(group.getName()).willReturn(randomString());
        given(group.getPermissions()).willReturn(permissions);
        given(group.getPeople()).willReturn(people);

        return group;
    }

}