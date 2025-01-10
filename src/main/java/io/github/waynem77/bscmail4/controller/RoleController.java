package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.request.CreateRoleRequest;
import io.github.waynem77.bscmail4.model.response.RoleResponse;
import io.github.waynem77.bscmail4.model.response.RolesResponse;
import io.github.waynem77.bscmail4.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * APIs regarding {@link io.github.waynem77.bscmail4.model.entity.Role} object.
 */
@RestController
@RequiredArgsConstructor
public class RoleController
{
    @Autowired
    private final RoleService roleService;

    @GetMapping("/api/role")
    public RolesResponse getAllRoles(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "25") int size)
    {
        return roleService.getAllRoles(page, size);
    }

    /**
     * Creates a new Role and returns its information. If the request is invalid, or if the Role name is not unique,
     * this API will return a status of BAD REQUEST.
     *
     * @param request the CreateRoleRequest
     * @return information about the newly created role
     */
    @PostMapping("/api/role")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse createRole(@RequestBody CreateRoleRequest request)
    {
        return roleService.createRole(request);
    }

    /**
     * Retrieves the Role with the given id. If the Role is not found, this API will return a status of NOT FOUND.
     */
    @GetMapping("/api/role/{roleId}")
    public RoleResponse getRoleById(
            @PathVariable(name = "roleId") Long roleId)
    {
        return roleService.getRoleById(roleId);
    }

    @DeleteMapping("/api/role/{roleId}")
    public void deleteRole(
            @PathVariable(name = "roleId") Long roleId)
    {
        roleService.deleteRoleById(roleId);
    }
}
