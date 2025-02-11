package io.github.waynem77.bscmail4.model.repository;

import io.github.waynem77.bscmail4.model.entity.Group;
import io.github.waynem77.bscmail4.model.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * A database repository for {@link Person} objects.
 */
public interface PersonRepository extends JpaRepository<Person, Long>,
        PagingAndSortingRepository<Person, Long>,
        JpaSpecificationExecutor<Person>
{
//    @Query("select distinct p from Person p join Group g where g member of p.groups and g.id = :id")
    @Query(value = "select * from person p inner join person_group pg where pg.group_id = :id and pg.person_id = p.id", nativeQuery = true)
    List<Person> findAllByGroupId(@Param("id") Long id);
}
