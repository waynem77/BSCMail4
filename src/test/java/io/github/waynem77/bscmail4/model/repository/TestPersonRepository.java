package io.github.waynem77.bscmail4.model.repository;

import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * A database repository for {@link Person} objects.
 */
public interface TestPersonRepository extends JpaRepository<Person, Long>,
        PagingAndSortingRepository<Person, Long>,
        JpaSpecificationExecutor<Person>
{
//    @Query("select p.groups from Person p join fetch p.groups where p.id = :id")
    @Query("select p.groups from Person p where p.id = :id")
    List<Group> getGroupsByPersonId(@Param("id") Long id);

    @Query("select p from Person p join fetch p.groups join fetch p.groups where p.id = :id")
    Optional<Person> eagerFindById(@Param("id") Long id);
}
