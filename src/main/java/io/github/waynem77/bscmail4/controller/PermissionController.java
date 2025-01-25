package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.request.CreatePermissionRequest;
import io.github.waynem77.bscmail4.model.response.PermissionResponse;
import io.github.waynem77.bscmail4.model.response.PermissionsResponse;
import io.github.waynem77.bscmail4.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * APIs regarding {@link Permission} object.
 */
@RestController
@RequiredArgsConstructor
public class PermissionController
{
    @Autowired
    private final PermissionService permissionService;

    @GetMapping("/api/permission")
    public PermissionsResponse getAllPermissions(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "25") int size)
    {
        return permissionService.getAllPermissions(page, size);
    }

    /**
     * Creates a new Permission and returns its information. If the request is invalid, or if the Permission name is not unique,
     * this API will return a status of BAD REQUEST.
     *
     * @param request the CreatePermissionRequest
     * @return information about the newly created permission
     */
    @PostMapping("/api/permission")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse createPermission(@RequestBody CreatePermissionRequest request)
    {
        return permissionService.createPermission(request);
    }

    /**
     * Retrieves the Permission with the given id. If the Permission is not found, this API will return a status of NOT FOUND.
     */
    @GetMapping("/api/permission/{permissionId}")
    public PermissionResponse getPermissionById(
            @PathVariable(name = "permissionId") Long permissionId)
    {
        return permissionService.getPermissionById(permissionId);
    }

    @DeleteMapping("/api/permission/{permissionId}")
    public void deletePermission(
            @PathVariable(name = "permissionId") Long permissionId)
    {
        permissionService.deletePermissionById(permissionId);
    }
}
