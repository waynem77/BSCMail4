package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Permission;
import org.junit.jupiter.api.Test;

import static io.github.waynem77.bscmail4.TestUtils.randomLong;
import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PermissionResponseTest
{
    @Test
    public void fromPermissionThrowsWhenPermissionIsNull()
    {
        Permission permission = null;

        assertThrows(NullPointerException.class, () -> PermissionResponse.fromPermission(permission));
    }

    @Test
    public void fromPermissionReturnsCorrectValue()
    {
        Permission permission = mock(Permission.class);
        given(permission.getId()).willReturn(randomLong());
        given(permission.getName()).willReturn(randomString());

        PermissionResponse permissionResponse = PermissionResponse.fromPermission(permission);

        assertThat(permissionResponse, notNullValue());
        assertThat(permissionResponse.getId(), equalTo(permission.getId()));
        assertThat(permissionResponse.getName(), equalTo(permission.getName()));
    }
}