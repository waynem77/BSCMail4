package io.github.waynem77.bscmail4.model.specification;

import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.entity.Role;
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
import java.util.Collection;
import java.util.List;

/**
 * Creates a Specification to filter Person queries.
 * <p>
 * The properties of the object determine the filtering operations.
 * <dl>
 *     <dt>active</dt>
 *     <dd>If not null, the filter will only select Person objects whose <code>active</code> property matches the
 *     given value.</dd>
 *
 *     <dt>nameLike</dt>
 *     <dd>If not null, the filter will only select Person objects whose <code>name</code> property contains the
 *     given value, case-insensitive.</code></dd>
 *
 *     <dt>roleIds</dt>
 *     <dd>If not null, the filter will only select Person objects whose <code>roles</code> property contains
 *     <em>all</em> of the roles with the given ids.</dd>
 * </dl>
 * <p>
 * In the case where multiple properties have been set, the filter performs a Boolean AND operation. For example, if
 * active is set to true and roleIds is set to "marvin", the filter will select all Person objects whose active
 * property is true and whose names match the string "marvin".
 */
@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class PersonFilter
{
    private Boolean active;
    private String nameLike;
    private Collection<Long> roleIds;

    public Specification<Person> toSpecification()
    {
        List<Specification<Person>> specifications = new ArrayList<>();

        if (active != null)
        {
            specifications.add(activeEqualTo(active));
        }
        if (nameLike != null)
        {
            specifications.add(nameLike(nameLike));
        }
        if (roleIds != null)
        {
            for (Long roleId : roleIds)
            {
                specifications.add(rolesContains(roleId));
            }
        }

        return Specification.allOf(specifications);
    }

    private static Specification<Person> activeEqualTo(Boolean active)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active);
    }

    private static Specification<Person> nameLike(String pattern)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + pattern.toLowerCase() + "%");
    }

    private static Specification<Person> rolesContains(Long roleId)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
        {
            Join<Role, Person> personRoles = root.join("roles");
            return criteriaBuilder.equal(personRoles.get("id"), roleId);
        };
    }
}
