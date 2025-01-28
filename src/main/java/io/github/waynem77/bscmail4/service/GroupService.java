package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.UpdateAction;
import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.repository.GroupRepository;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdateGroupRequest;
import io.github.waynem77.bscmail4.model.request.UpdatePermissionsRequest;
import io.github.waynem77.bscmail4.model.response.GroupResponse;
import io.github.waynem77.bscmail4.model.response.GroupsResponse;
import io.github.waynem77.bscmail4.model.specification.GroupFilter;
import io.github.waynem77.bscmail4.model.specification.SortDirection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs database operations on {@link io.github.waynem77.bscmail4.model.entity.Group}s.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService
{
    @Autowired
    private final GroupRepository groupRepository;
    @Autowired
    private final PermissionRepository permissionRepository;

    /**
     * Creates a new Group from the given CreateOrUpdateGroupRequest and stores it in the database.
     *
     * @param request the CreateOrUpdateGroupRequest; may not be null, and must have a non-null, valid (unique) name
     * @return a GroupResponse representing the new Group
     * @throws BadRequestException if the request is invalid or the Group name is not unique
     */
    public GroupResponse createGroup(@NonNull CreateOrUpdateGroupRequest request)
    {
        log.info("Creating group. request={}", request);

        validateRequest(request);

        try
        {
            Group group = new Group();
            group.setName(request.getName());
            group = groupRepository.save(group);

            return GroupResponse.fromGroup(group);
        }
        catch (JpaSystemException e)
        {
            log.error("Error saving group. Name is not unique. exception={}", e.getMessage());
            throw new BadRequestException("Name must be unique.", e);
        }
    }

    /**
     * Finds the Group with the given id in the database and returns a GroupResponse representing it.
     *
     * @param id the group id
     * @return a GroupResponse representing the Group
     * @throws NotFoundException if the Group does not exist in the database
     */
    public GroupResponse getGroupById(@NonNull Long id)
    {
        log.info("Getting group. id={}", id);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Group not found. id={}", id);
                    return new NotFoundException("Group not found.");
                });
        return GroupResponse.fromGroup(group);
    }

    /**
     * Deletes the Group with the given id in the database.
     * <p>
     * This method is idempotent. If no group with the given id exists in the database, this method does nothing.
     *
     * @param id the Group id
     */
    public void deleteGroupById(@NonNull Long id)
    {
        log.info("Deleting group. id={}", id);
        groupRepository.deleteById(id);
    }

    /**
     * Returns all Group objects matching the given filter, sorted by name in the given direction, by page
     *
     * @param filter    the filter; may not be null
     * @param direction the direction; may not be null
     * @param page      the 0-based page number; must be nonnegative
     * @param size      the page size; must be positive
     * @return the requested page
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if page or size are invalid
     */
    public GroupsResponse getGroups(GroupFilter filter, SortDirection direction, int page, int size)
    {
        log.info("Getting groups with filters. filter={}, direction={}, page={}, size={}", filter, direction, page,
                size);

        Pageable pageable = PageRequest.of(page, size, direction.getSpringDirection(), "name");
        Specification<Group> specification = filter.toSpecification();
        return new GroupsResponse(groupRepository.findAll(specification, pageable).map(GroupResponse::fromGroup));
    }

    /**
     * Adds or removes the given Permissions to the given Group and returns a corresponding GroupResponse.
     *
     * @param groupId the Group id; may not be null
     * @param request the request containing the Permission ids; may not be null
     * @return a GroupResponse representing the updated Group
     * @throws NullPointerException if either argument is null
     * @throws NotFoundException    if groupId is invalid
     * @throws BadRequestException  if any id in permissionIds is invalid
     */
    public GroupResponse updatePermissions(@NonNull Long groupId, @NonNull UpdatePermissionsRequest request)
    {
        log.info("Adding permissions to group. groupId={}, request={}", groupId, request);

        UpdateAction action = UpdateAction.fromValue(request.getAction());
        if (action == null)
        {
            log.error("Invalid request; action is invalid. action={}", request.getAction());
            throw new BadRequestException("Invalid request.");
        }

        List<Long> permissionsIds = request.getPermissionIds();
        if (permissionsIds == null)
        {
            log.error("Invalid request; permissionIds is null.");
            throw new BadRequestException("Invalid request.");
        }
        for (Long id : permissionsIds)
        {
            if (!permissionRepository.existsById(id))
            {
                log.error("Permission does not exist. id={}", id);
                throw new BadRequestException("Permission does not exist.");
            }
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    log.error("Group does not exist. groupId={}", groupId);
                    return new NotFoundException("Group does not exist.");
                });
        Set<Permission> oldPermissions = group.getPermissions() != null ?
                group.getPermissions() :
                Collections.emptySet();
        Set<Permission> requestedPermissions = permissionRepository.findAllByIdIn(permissionsIds);

        Set<Permission> combinedPermissions = new HashSet<>(oldPermissions);
        if (action == UpdateAction.ADD)
        {
            combinedPermissions.addAll(requestedPermissions);
        }
        else if (action == UpdateAction.REMOVE)
        {
            combinedPermissions.removeAll(requestedPermissions);
        }

        group.setPermissions(combinedPermissions);
        group = groupRepository.save(group);

        return GroupResponse.fromGroup(group);
    }

    /**
     * Validates a CreateOrUpdateGroupRequest.
     *
     * @param request the request
     * @throws BadRequestException if the request is invalid
     */
    private void validateRequest(CreateOrUpdateGroupRequest request)
    {
        if (request.getName() == null)
        {
            log.error("Invalid request. Name may not be null. request={}", request);
            throw new BadRequestException("Invalid request. Name may not be null.");
        }
    }
}
