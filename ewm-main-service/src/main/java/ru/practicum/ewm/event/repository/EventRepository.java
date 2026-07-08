package ru.practicum.ewm.event.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category", "initiator"})
    @Query("select event from Event event where event.id = :id")
    Optional<Event> findWithCategoryAndInitiatorById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "initiator"})
    @Query("select event from Event event where event.id = :id and event.initiator.id = :initiatorId")
    Optional<Event> findWithCategoryAndInitiatorByIdAndInitiatorId(@Param("id") Long id,
                                                                   @Param("initiatorId") Long initiatorId);

    @EntityGraph(attributePaths = {"category", "initiator"})
    @Query("select event from Event event where event.id = :id and event.state = :state")
    Optional<Event> findWithCategoryAndInitiatorByIdAndState(@Param("id") Long id, @Param("state") EventState state);
}
