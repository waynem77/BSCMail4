package io.github.waynem77.bscmail4.model.specification;

import io.github.waynem77.bscmail4.model.entity.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for Person entity filtering.
 * Provides reusable criteria for building dynamic queries.
 */
public class PersonSpecification
{

    /**
     * Creates a specification to filter persons by active status.
     *
     * @param active The active status to filter by (null means no filtering)
     * @return Specification for filtering by active status
     */
    public static Specification<Person> hasActiveStatus(Boolean active)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (active == null)
            {
                return criteriaBuilder.conjunction(); // No filtering
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    /**
     * Creates a specification to filter persons by name (case-insensitive partial match).
     *
     * @param name The name to search for (null means no filtering)
     * @return Specification for filtering by name
     */
    public static Specification<Person> hasNameLike(String name)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty())
            {
                return criteriaBuilder.conjunction(); // No filtering
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.trim().toLowerCase() + "%"
            );
        };
    }

    /**
     * Creates a specification to filter persons by email address (case-insensitive partial match).
     *
     * @param email The email to search for (null means no filtering)
     * @return Specification for filtering by email
     */
    public static Specification<Person> hasEmailLike(String email)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (email == null || email.trim().isEmpty())
            {
                return criteriaBuilder.conjunction(); // No filtering
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("emailAddress")),
                    "%" + email.trim().toLowerCase() + "%"
            );
        };
    }

    /**
     * Creates a specification to filter persons by phone number (case-insensitive partial match).
     *
     * @param phone The phone to search for (null means no filtering)
     * @return Specification for filtering by phone
     */
    public static Specification<Person> hasPhoneLike(String phone)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (phone == null || phone.trim().isEmpty())
            {
                return criteriaBuilder.conjunction(); // No filtering
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("phone")),
                    "%" + phone.trim().toLowerCase() + "%"
            );
        };
    }

    /**
     * Creates a specification to filter persons by text search across name, email, and phone fields.
     *
     * @param textSearch The text to search for (null means no filtering)
     * @return Specification for filtering by text search across multiple fields
     */
    public static Specification<Person> hasTextSearch(String textSearch)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (textSearch == null || textSearch.trim().isEmpty())
            {
                return criteriaBuilder.conjunction(); // No filtering
            }

            String searchTerm = "%" + textSearch.toLowerCase() + "%";

            // Create predicates for each field
            Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    searchTerm
            );

            Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("emailAddress")),
                    searchTerm
            );

            Predicate phonePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("phone")),
                    searchTerm
            );

            // Combine with OR logic
            return criteriaBuilder.or(namePredicate, emailPredicate, phonePredicate);
        };
    }

    /**
     * Combines multiple specifications using AND logic.
     *
     * @param specifications Array of specifications to combine
     * @return Combined specification
     */
    @SafeVarargs
    public static Specification<Person> and(Specification<Person>... specifications)
    {
        return (Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Predicate[] predicates = new Predicate[specifications.length];
            for (int i = 0; i < specifications.length; i++)
            {
                predicates[i] = specifications[i].toPredicate(root, query, criteriaBuilder);
            }
            return criteriaBuilder.and(predicates);
        };
    }
}
