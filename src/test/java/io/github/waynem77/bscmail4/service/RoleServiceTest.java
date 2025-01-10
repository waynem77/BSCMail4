package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.entity.Role;
import io.github.waynem77.bscmail4.model.repository.RoleRepository;
import io.github.waynem77.bscmail4.model.request.CreateRoleRequest;
import io.github.waynem77.bscmail4.model.response.RoleResponse;
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

class RoleServiceTest
{
    private RoleRepository roleRepository;

    @BeforeEach
    public void setup()
    {
        roleRepository = mock(RoleRepository.class);
    }

    @Test
    public void createRoleThrowsIfRequestIsNull()
    {
        CreateRoleRequest request = null;

        assertThrows(NullPointerException.class, () -> createTestable().createRole(request));

        verify(roleRepository, never()).save(any());
    }

    @Test
    public void createRoleThrowsIfRequestIsInvalid()
    {
        CreateRoleRequest request = mock(CreateRoleRequest.class);
        given(request.getName()).willReturn(null);

        assertThrows(BadRequestException.class, () -> createTestable().createRole(request));

        verify(roleRepository, never()).save(any());
    }

    @Test
    public void createRoleThrowsIfRepositoryThrows()
    {
        given(roleRepository.save(any())).willThrow(JpaSystemException.class);

        CreateRoleRequest request = mock(CreateRoleRequest.class);
        given(request.getName()).willReturn(randomString());

        assertThrows(BadRequestException.class, () -> createTestable().createRole(request));

        verify(roleRepository, times(1)).save(any());
    }

    @Test
    public void createRoleCreatesRole()
    {
        String roleName = randomString();
        Long roleId = randomLong();

        Role roleToSave = mock(Role.class);
        given(roleToSave.getName()).willReturn(roleName);

        Role roleToReturn = mock(Role.class);
        given(roleToReturn.getId()).willReturn(roleId);
        given(roleToReturn.getName()).willReturn(roleName);

        given(roleRepository.save(eq(roleToSave))).willReturn(roleToReturn);

        CreateRoleRequest request = mock(CreateRoleRequest.class);
        given(request.getName()).willReturn(randomString());
        given(request.toRole()).willReturn(roleToSave);

        RoleService roleService = createTestable();
        RoleResponse response = roleService.createRole(request);

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(roleId));
        assertThat(response.getName(), equalTo(roleName));

        verify(roleRepository).save(roleToSave);
    }

    @Test
    public void getRoleByIdThrowsIfRoleNotFound()
    {
        Long id = randomLong();

        given(roleRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> createTestable().getRoleById(id));
    }

    @Test
    public void getRoleByIdReturnsCorrectValue()
    {
        Long id = randomLong();

        Role role = mock(Role.class);
        given(role.getId()).willReturn(id);
        given(role.getName()).willReturn(randomString());

        given(roleRepository.findById(id)).willReturn(Optional.of(role));

        RoleService roleService = createTestable();
        RoleResponse response = roleService.getRoleById(id);

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(id));
        assertThat(response.getName(), equalTo(role.getName()));
    }

    @Test
    public void deleteRoleByIdDeletesRole()
    {
        Long id = randomLong();

        RoleService roleService = createTestable();
        roleService.deleteRoleById(id);

        verify(roleRepository, times(1)).deleteById(eq(id));
    }

    private RoleService createTestable()
    {
        return new RoleService(roleRepository);
    }
}