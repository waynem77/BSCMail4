package io.github.waynem77.bscmail4.server.database.repository;

import io.github.waynem77.bscmail4.server.database.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Note entities.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long>
{
    /**
     * Counts the number of notes for the person with the given ID.
     *
     * @param personId the ID of the person to count notes for
     * @return the number of notes for the person
     */
    long countByPersonId(Long personId);

    /**
     * Finds all notes for the person with the given ID, with pagination and sorting.
     *
     * @param personId the ID of the person to find notes for
     * @param pageable pagination and sorting information
     * @return a page of notes for the person
     */
    Page<Note> findByPersonId(Long personId, Pageable pageable);
}
