package io.github.waynem77.bscmail4.model.repository;

import io.github.waynem77.bscmail4.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Set;

/**
 * A database repository for {@link Permission} objects.
 */
public interface PermissionRepository extends JpaRepository<Permission, Long>, PagingAndSortingRepository<Permission, Long>
{
    Set<Permission> findAllByIdIn(Collection<Long> ids);
}
