package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.request.CreatePermissionRequest;
import io.github.waynem77.bscmail4.model.response.PermissionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.JpaSystemException;

import java.util.Optional;

import static io.github.waynem77.bscmail4.TestUtils.randomLong;
import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class PermissionServiceTest
{
    private PermissionRepository permissionRepository;

    @BeforeEach
    public void setup()
    {
        permissionRepository = mock(PermissionRepository.class);
    }

    @Test
    public void createPermissionThrowsIfRequestIsNull()
    {
        CreatePermissionRequest request = null;

        assertThrows(NullPointerException.class, () -> createTestable().createPermission(request));

        verify(permissionRepository, never()).save(any());
    }

    @Test
    public void createPermissionThrowsIfRequestIsInvalid()
    {
        CreatePermissionRequest request = mock(CreatePermissionRequest.class);
        given(request.getName()).willReturn(null);

        assertThrows(BadRequestException.class, () -> createTestable().createPermission(request));

        verify(permissionRepository, never()).save(any());
    }

    @Test
    public void createPermissionThrowsIfRepositoryThrows()
    {
        given(permissionRepository.save(any())).willThrow(JpaSystemException.class);

        CreatePermissionRequest request = mock(CreatePermissionRequest.class);
        given(request.getName()).willReturn(randomString());

        assertThrows(BadRequestException.class, () -> createTestable().createPermission(request));

        verify(permissionRepository, times(1)).save(any());
    }

    @Test
    public void createPermissionCreatesPermission()
    {
        String permissionName = randomString();
        Long permissionId = randomLong();

        Permission permissionToSave = mock(Permission.class);
        given(permissionToSave.getName()).willReturn(permissionName);

        Permission permissionToReturn = mock(Permission.class);
        given(permissionToReturn.getId()).willReturn(permissionId);
        given(permissionToReturn.getName()).willReturn(permissionName);

        given(permissionRepository.save(eq(permissionToSave))).willReturn(permissionToReturn);

        CreatePermissionRequest request = mock(CreatePermissionRequest.class);
        given(request.getName()).willReturn(randomString());
        given(request.toPermission()).willReturn(permissionToSave);

        PermissionService permissionService = createTestable();
        PermissionResponse response = permissionService.createPermission(request);

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(permissionId));
        assertThat(response.getName(), equalTo(permissionName));

        verify(permissionRepository).save(permissionToSave);
    }

    @Test
    public void getPermissionByIdThrowsIfPermissionNotFound()
    {
        Long id = randomLong();

        given(permissionRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> createTestable().getPermissionById(id));
    }

    @Test
    public void getPermissionByIdReturnsCorrectValue()
    {
        Long id = randomLong();

        Permission permission = mock(Permission.class);
        given(permission.getId()).willReturn(id);
        given(permission.getName()).willReturn(randomString());

        given(permissionRepository.findById(id)).willReturn(Optional.of(permission));

        PermissionService permissionService = createTestable();
        PermissionResponse response = permissionService.getPermissionById(id);

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(id));
        assertThat(response.getName(), equalTo(permission.getName()));
    }

    @Test
    public void deletePermissionByIdDeletesPermission()
    {
        Long id = randomLong();

        PermissionService permissionService = createTestable();
        permissionService.deletePermissionById(id);

        verify(permissionRepository, times(1)).deleteById(eq(id));
    }

    private PermissionService createTestable()
    {
        return new PermissionService(permissionRepository);
    }
}