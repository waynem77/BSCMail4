package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Role;
import org.junit.jupiter.api.Test;

import static io.github.waynem77.bscmail4.TestUtils.randomLong;
import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RoleResponseTest
{
    @Test
    public void fromRoleThrowsWhenRoleIsNull()
    {
        Role role = null;

        assertThrows(NullPointerException.class, () -> RoleResponse.fromRole(role));
    }

    @Test
    public void fromRoleReturnsCorrectValue()
    {
        Role role = mock(Role.class);
        given(role.getId()).willReturn(randomLong());
        given(role.getName()).willReturn(randomString());

        RoleResponse roleResponse = RoleResponse.fromRole(role);

        assertThat(roleResponse, notNullValue());
        assertThat(roleResponse.getId(), equalTo(role.getId()));
        assertThat(roleResponse.getName(), equalTo(role.getName()));
    }
}