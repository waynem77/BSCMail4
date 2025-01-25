package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.request.CreatePermissionRequest;
import io.github.waynem77.bscmail4.model.response.PermissionResponse;
import io.github.waynem77.bscmail4.model.response.PermissionsResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

/**
 * Provides database operations on {@link Permission}s.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService
{
    @Autowired
    private final PermissionRepository permissionRepository;

    /**
     * Returns all the saved Permissions, in ascending order by name, by page.
     *
     * @param page the page number to return; this parameter is 0-indexed
     * @param size the page size
     * @return the requested page
     */
    public PermissionsResponse getAllPermissions(int page, int size)
    {
        log.info("Getting all permissions. page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "name");

        return new PermissionsResponse(permissionRepository.findAll(pageable).map(PermissionResponse::fromPermission));
    }

    /**
     * Creates a new Permission from the given CreatePermissionRequest and stores it in the database.
     *
     * @param request the CreatePermissionRequest; may not be null, and must have a non-null, valid (unique) name
     * @return a PermissionResponse representing the new Permission
     * @throws BadRequestException if the request is invalid or the Permission name is not unique
     */
    public PermissionResponse createPermission(@NonNull CreatePermissionRequest request)
    {
        log.info("Creating permission.");
        if (request.getName() == null)
        {
            log.error("Request is invalid. request={}", request);
            throw new BadRequestException("Request is invalid");
        }

        Permission permission = request.toPermission();
        try
        {
            permission = permissionRepository.save(permission);
        } catch (JpaSystemException e)
        {
            log.error("Permission name is not unique. permission={}", permission);
            throw new BadRequestException("Permission name must be unique", e);
        }

        log.info("Created new permission. permission={}", permission);

        return PermissionResponse.fromPermission(permission);
    }

    /**
     * Finds the Permission with the given id in the database and returns a PermissionResponse representing it.
     *
     * @param id the Permission id
     * @return a PermissionResponse representing the Permission
     * @throws NotFoundException if the Permission does not exist
     */
    public PermissionResponse getPermissionById(Long id)
    {
        log.info("Getting permission with id={}", id);

        return PermissionResponse.fromPermission(
                permissionRepository
                        .findById(id)
                        .orElseThrow(() ->
                        {
                            log.error("Unable to find permission. id={}", id);
                            return new NotFoundException("Unable to find permission.");
                        }));
    }

    /**
     * Deletes the Permission with the given id in the database.
     * <p>
     * This method is idempotent. If no permission with the given id exists in the database, this method does nothing.
     *
     * @param id the Permission id
     */
    public void deletePermissionById(Long id)
    {
        log.info("Deleting permission. id={}", id);
        permissionRepository.deleteById(id);
    }
}
