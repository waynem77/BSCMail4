package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.entity.Role;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link PersonResponse}.
 */
class PersonResponseTest
{
    @Test
    public void fromPersonThrowsWhenPersonIsNull()
    {
        Person person = null;

        assertThrows(NullPointerException.class, () -> PersonResponse.fromPerson(person));
    }

    @Test
    public void fromRoleReturnsCorrectValue()
    {
        List<Role> roles = List.of(
                mock(Role.class),
                mock(Role.class),
                mock(Role.class));
        given(roles.get(0).getId()).willReturn(randomLong());
        given(roles.get(0).getName()).willReturn(randomStringWithPrefix("a"));
        given(roles.get(1).getId()).willReturn(randomLong());
        given(roles.get(1).getName()).willReturn(randomStringWithPrefix("b"));
        given(roles.get(2).getId()).willReturn(randomLong());
        given(roles.get(2).getName()).willReturn(randomStringWithPrefix("c"));

        Person person = mock(Person.class);
        given(person.getId()).willReturn(randomLong());
        given(person.getName()).willReturn(randomString());
        given(person.getEmailAddress()).willReturn(randomString());
        given(person.getPhone()).willReturn(randomString());
        given(person.getRoles()).willReturn(new HashSet<>(roles));
        given(person.getActive()).willReturn(randomBoolean());

        PersonResponse personResponse = PersonResponse.fromPerson(person);

        assertThat(personResponse, notNullValue());
        assertThat(personResponse.getId(), equalTo(person.getId()));
        assertThat(personResponse.getName(), equalTo(person.getName()));
        assertThat(personResponse.getEmailAddress(), equalTo(person.getEmailAddress()));
        assertThat(personResponse.getPhone(), equalTo(person.getPhone()));
        assertThat(personResponse.getActive(), equalTo(person.getActive()));

        List<RoleResponse> expectedRoleResponses = roles.stream()
                .map(role ->
                {
                    RoleResponse roleResponse = new RoleResponse();
                    roleResponse.setId(role.getId());
                    roleResponse.setName(role.getName());
                    return roleResponse;
                })
                .toList();
        assertThat(personResponse.getRoles(), equalTo(expectedRoleResponses));
    }

    @Test
    public void fromRoleReturnsCorrectValueWhenRolesIsNull()
    {
        Person person = mock(Person.class);
        given(person.getId()).willReturn(randomLong());
        given(person.getName()).willReturn(randomString());
        given(person.getEmailAddress()).willReturn(randomString());
        given(person.getPhone()).willReturn(randomString());
        given(person.getRoles()).willReturn(null);
        given(person.getActive()).willReturn(randomBoolean());

        PersonResponse personResponse = PersonResponse.fromPerson(person);

        assertThat(personResponse, notNullValue());
        assertThat(personResponse.getId(), equalTo(person.getId()));
        assertThat(personResponse.getName(), equalTo(person.getName()));
        assertThat(personResponse.getEmailAddress(), equalTo(person.getEmailAddress()));
        assertThat(personResponse.getPhone(), equalTo(person.getPhone()));
        assertThat(personResponse.getRoles(), equalTo(Collections.emptyList()));
        assertThat(personResponse.getActive(), equalTo(person.getActive()));
    }
}