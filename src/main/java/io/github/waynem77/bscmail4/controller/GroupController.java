package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.request.CreateOrUpdateGroupRequest;
import io.github.waynem77.bscmail4.model.request.UpdatePermissionsRequest;
import io.github.waynem77.bscmail4.model.response.GroupResponse;
import io.github.waynem77.bscmail4.model.response.GroupsResponse;
import io.github.waynem77.bscmail4.model.specification.GroupFilter;
import io.github.waynem77.bscmail4.model.specification.SortDirection;
import io.github.waynem77.bscmail4.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * APIs regarding {@link io.github.waynem77.bscmail4.model.entity.Group} objects.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class GroupController
{
    @Autowired
    private final GroupService groupService;

    /**
     * Creates a new Group and returns its information. If the request is invalid, or if the Group name is not unique,
     * this API will return a status of BAD REQUEST.
     *
     * @param request the CreateOrUpdateGroupRequest
     * @return information about the Group
     */
    @PostMapping("/api/group")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@RequestBody CreateOrUpdateGroupRequest request)
    {
        return groupService.createGroup(request);
    }

    /**
     * Retrieves the Group with the given id. If the Group is not found, this API will return a status of NOT FOUND.
     *
     * @param groupId the Group id
     * @return information about the Group
     */
    @GetMapping("/api/group/{groupId}")
    public GroupResponse getGroupById(
            @PathVariable(name = "groupId") Long groupId)
    {
        return groupService.getGroupById(groupId);
    }

    /**
     * Deletes the Group with the given id. If the Group does not exists, the API has no effect.
     * <p>
     * This API is idempotent.
     *
     * @param groupId The Group id
     */
    @DeleteMapping("/api/group/{groupId}")
    public void deleteGroup(
            @PathVariable(name = "groupId") Long groupId)
    {
        groupService.deleteGroupById(groupId);
    }

    /**
     * Returns a Spring Page of all Groups matching the given filters, ordered by Group name.
     *
     * @param name        a string that the Group name must match; may be null
     * @param permissions a comma-delimited list of required Permission ids; may be null
     * @param people      a comma-delimited list of required Person ids; may be null
     * @param direction   sort direction; may be "ascending" (default value) or "descending"
     * @param page        the page number (0-indexed) to return; default value is 0
     * @param size        the size of the page to return; default value is 25
     * @return a page of Groups
     */
    @GetMapping("/api/group")
    public GroupsResponse getAllGroups(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "permissions", required = false) String permissions,
            @RequestParam(name = "people", required = false) String people,
            @RequestParam(name = "direction", required = false, defaultValue = "ascending") String direction,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "25") int size)
    {
        GroupFilter filter = new GroupFilter()
                .withNameLike(name)
                .withPermissionIds(getIdsFromString(permissions))
                .withPersonIds(getIdsFromString(people));

        SortDirection sortDirection = SortDirection.fromValue(direction);
        if (sortDirection == null)
        {
            sortDirection = SortDirection.ASC;
        }

        return groupService.getGroups(filter, sortDirection, page, size);
    }

    /**
     * Adds Permissions to the Group with the given id.
     *
     * @param request the list of Permission ids to add
     * @param groupId the Group id
     * @return information about the Group
     */
    @PatchMapping("/api/group/{groupId}/permission")
    public GroupResponse updatePermissions(
            @RequestBody UpdatePermissionsRequest request,
            @PathVariable(name = "groupId") Long groupId)
    {
        return groupService.updatePermissions(groupId, request);
    }

    /**
     * Splits a string of comma-delimited ids (as integers) into a list
     *
     * @param ids the comma-delimited string
     * @return a list of integers
     */
    private List<Long> getIdsFromString(String ids)
    {
        if (ids == null)
        {
            return null;
        }
        return Arrays.stream(ids.split(","))
                .map(Long::valueOf)
                .toList();
    }
}
