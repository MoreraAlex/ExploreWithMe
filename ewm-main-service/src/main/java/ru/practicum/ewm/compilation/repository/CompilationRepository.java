package ru.practicum.ewm.compilation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.compilation.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    @Query("select compilation from Compilation compilation where compilation.id = :id")
    Optional<Compilation> findWithEventsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    List<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    @Query("select compilation from Compilation compilation")
    List<Compilation> findAllWithEvents(Pageable pageable);
}
