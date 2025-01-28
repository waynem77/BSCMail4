package io.github.waynem77.bscmail4.model.repository;

import io.github.waynem77.bscmail4.model.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * A repository for {@link Group} objects.
 */
public interface GroupRepository extends JpaRepository<Group, Long>,
        PagingAndSortingRepository<Group, Long>,
        JpaSpecificationExecutor<Group>
{
}
