package io.github.waynem77.bscmail4.model.request;

import io.github.waynem77.bscmail4.model.entity.Permission;
import org.junit.jupiter.api.Test;

import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class CreatePermissionRequestTest
{
    @Test
    public void toPermissionReturnsCorrectValue()
    {
        CreatePermissionRequest createPermissionRequest = new CreatePermissionRequest();
        createPermissionRequest.setName(randomString());

        Permission permission = createPermissionRequest.toPermission();
        assertThat(permission, notNullValue());
        assertThat(permission.getId(), nullValue());
        assertThat(permission.getName(), equalTo(createPermissionRequest.getName()));
    }
}