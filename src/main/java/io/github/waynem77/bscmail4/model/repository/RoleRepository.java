package io.github.waynem77.bscmail4.model.repository;

import io.github.waynem77.bscmail4.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Set;

/**
 * A database repository for {@link Role} objects.
 */
public interface RoleRepository extends JpaRepository<Role, Long>, PagingAndSortingRepository<Role, Long>
{
    Set<Role> findAllByIdIn(Collection<Long> ids);
}
