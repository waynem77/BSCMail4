package io.github.waynem77.bscmail4.model.request;

import io.github.waynem77.bscmail4.model.entity.Role;
import org.junit.jupiter.api.Test;

import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class CreateRoleRequestTest
{
    @Test
    public void toRoleReturnsCorrectValue()
    {
        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest.setName(randomString());

        Role role = createRoleRequest.toRole();
        assertThat(role, notNullValue());
        assertThat(role.getId(), nullValue());
        assertThat(role.getName(), equalTo(createRoleRequest.getName()));
    }
}