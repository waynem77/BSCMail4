package io.github.waynem77.bscmail4.model.repository;

import io.github.waynem77.bscmail4.model.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * A database repository for {@link Person} objects.
 */
public interface PersonRepository extends JpaRepository<Person, Long>,
        PagingAndSortingRepository<Person, Long>,
        JpaSpecificationExecutor<Person>
{
}
