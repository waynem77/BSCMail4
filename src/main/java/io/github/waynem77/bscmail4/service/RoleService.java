package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.entity.Role;
import io.github.waynem77.bscmail4.model.repository.RoleRepository;
import io.github.waynem77.bscmail4.model.request.CreateRoleRequest;
import io.github.waynem77.bscmail4.model.response.RoleResponse;
import io.github.waynem77.bscmail4.model.response.RolesResponse;
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
 * Provides database operations on {@link Role}s.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService
{
    @Autowired
    private final RoleRepository roleRepository;

    /**
     * Returns all the saved Roles, in ascending order by name, by page.
     *
     * @param page the page number to return; this parameter is 0-indexed
     * @param size the page size
     * @return the requested page
     */
    public RolesResponse getAllRoles(int page, int size)
    {
        log.info("Getting all roles. page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "name");

        return new RolesResponse(roleRepository.findAll(pageable).map(RoleResponse::fromRole));
    }

    /**
     * Creates a new Role from the given CreateRoleRequest and stores it in the database.
     *
     * @param request the CreateRoleRequest; may not be null, and must have a non-null, valid (unique) name
     * @return a RoleResponse representing the new Role
     * @throws BadRequestException if the request is invalid or the Role name is not unique
     */
    public RoleResponse createRole(@NonNull CreateRoleRequest request)
    {
        log.info("Creating role.");
        if (request.getName() == null)
        {
            log.error("Request is invalid. request={}", request);
            throw new BadRequestException("Request is invalid");
        }

        Role role = request.toRole();
        try
        {
            role = roleRepository.save(role);
        } catch (JpaSystemException e)
        {
            log.error("Role name is not unique. role={}", role);
            throw new BadRequestException("Role name must be unique", e);
        }

        log.info("Created new role. role={}", role);

        return RoleResponse.fromRole(role);
    }

    /**
     * Finds the Role with the given id in the database and returns a RoleResponse representing it.
     *
     * @param id the Role id
     * @return a RoleResponse representing the Role
     * @throws NotFoundException if the Role does not exist
     */
    public RoleResponse getRoleById(Long id)
    {
        log.info("Getting role with id={}", id);

        return RoleResponse.fromRole(
                roleRepository
                        .findById(id)
                        .orElseThrow(() ->
                        {
                            log.error("Unable to find role. id={}", id);
                            return new NotFoundException("Unable to find role.");
                        }));
    }

    /**
     * Deletes the Role with the given id in the database.
     * <p>
     * This method is idempotent. If no role with the given id exists in the database, this method does nothing.
     *
     * @param id the Role id
     */
    public void deleteRoleById(Long id)
    {
        log.info("Deleting role. id={}", id);
        roleRepository.deleteById(id);
    }
}
