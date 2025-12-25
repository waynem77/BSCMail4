package io.github.waynem77.bscmail4.server.database.specification;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 * Factory class for creating Specifications for Person entity queries.
 */
public class PersonSpecifications
{
    /**
     * Creates a Specification that filters persons by isActive status.
     *
     * @param isActive the active status to filter by (null means no filtering)
     * @return a Specification for filtering by isActive, or null if isActive is null
     */
    public static Specification<Person> isActive(Boolean isActive)
    {
        if (isActive == null)
        {
            return null;
        }

        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isActive"), isActive);
    }

    /**
     * Creates a Specification that filters persons by a search string.
     * The search string is matched against both name and emailAddress fields using case-insensitive partial matching.
     *
     * @param search the search string to filter by (null or empty means no filtering)
     * @return a Specification for filtering by search string, or null if search is null or empty
     */
    public static Specification<Person> matchesSearch(String search)
    {
        if (search == null || search.trim().isEmpty())
        {
            return null;
        }

        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
        {
            Predicate nameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    searchPattern
            );
            Predicate emailMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("emailAddress")),
                    searchPattern
            );
            return criteriaBuilder.or(nameMatch, emailMatch);
        };
    }
}

