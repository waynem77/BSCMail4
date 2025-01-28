package io.github.waynem77.bscmail4.model.specification;

import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Permission;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a Specification to filter Group queries.
 * <p>
 * The properties of the object determine the filtering operations.
 * <dl>
 *     <dt>nameLike</dt>
 *     <dd>If not null, the filter will only select Group objects whose <code>name</code> property contains the given value, case-insensitive.</code></dd>
 *
 *     <dt>permissionIds</dt>
 *     <dd>If not null, the filter will only select Group objects whose <code>permissions</code> property contains <em>all</em> of the permissions with the given ids.</dd>
 *
 *     <dt>personIds</dt>
 *     <dd>If not null, the filter will only select Group objects whose <code>people</code> property contains <em>all</em> of the permissions with the given ids.</dd>
 * </dl>
 * <p>
 * In the case where multiple properties have been set, the filter performs a Boolean AND operation. For example, if
 * nameLike is set to "marvin" and permissionIds is set to [ 5, 12 ], the filter will select all Group objects whose names match the string "marvin" and which contain Permission objects with the ids of 5 and 12.
 */
@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class GroupFilter
{
    private String nameLike;
    private List<Long> permissionIds;
    private List<Long> personIds;

    /**
     * Creates a Specification from the filter.
     *
     * @return a Specification equivalent to the filter
     */
    public Specification<Group> toSpecification()
    {
        List<Specification<Group>> specifications = new ArrayList<>();

        if (nameLike != null) {
            specifications.add(nameLike(nameLike));
        }
        if (permissionIds != null) {
            for (Long id : permissionIds) {
                specifications.add(permissionIdsContains(id));
            }
        }
        if (personIds != null) {
            for (Long id : personIds) {
                specifications.add(personIdsContains(id));
            }
        }

        return Specification.allOf(specifications);
    }


    /**
     * Returns a Specification that matches Groups whose name contains the given pattern, case-insensitive.
     *
     * @param pattern the pattern to match
     * @return a Specification that matches Group name to the pattern
     */
    private static Specification<Group> nameLike(String pattern)
    {
        return (Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + pattern.toLowerCase() + "%");
    }

    /**
     * Returns a specification that matches Groups containing a Permission with the given id
     *
     * @param permissionId the Permission id
     * @return a specification that matches Group permissions to the id
     */
    private static Specification<Group> permissionIdsContains(Long permissionId)
    {
        return (Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
        {
            Join<Permission, Group> groupPermissions = root.join("permissions");
            return criteriaBuilder.equal(groupPermissions.get("id"), permissionId);
        };
    }

    /**
     * Returns a specification that matches Groups containing a Person with the given id
     *
     * @param personId the Person id
     * @return a specification that matches Group people to the id
     */
    private static Specification<Group> personIdsContains(Long personId)
    {
        return (Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
        {
            Join<Permission, Group> groupPermissions = root.join("people");
            return criteriaBuilder.equal(groupPermissions.get("id"), personId);
        };
    }
}
