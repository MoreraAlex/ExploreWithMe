package ru.practicum.ewm.request.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @EntityGraph(attributePaths = {"event", "requester"})
    List<ParticipationRequest> findByRequesterId(Long requesterId);

    @EntityGraph(attributePaths = {"event", "requester"})
    List<ParticipationRequest> findByEventIdAndEventInitiatorId(Long eventId, Long initiatorId);

    @EntityGraph(attributePaths = {"event", "requester"})
    List<ParticipationRequest> findByIdInAndEventIdAndEventInitiatorId(Collection<Long> ids, Long eventId, Long userId);

    @EntityGraph(attributePaths = {"event", "requester"})
    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("""
            select request.event.id, count(request.id)
            from ParticipationRequest request
            where request.event.id in :eventIds and request.status = :status
            group by request.event.id
            """)
    List<Object[]> countByEventIdsAndStatus(@Param("eventIds") Collection<Long> eventIds,
                                            @Param("status") RequestStatus status);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Modifying
    @Query("""
            update ParticipationRequest request
            set request.status = :status
            where request.event.id = :eventId and request.status = :currentStatus
            """)
    void updateStatusByEventIdAndStatus(@Param("eventId") Long eventId,
                                        @Param("currentStatus") RequestStatus currentStatus,
                                        @Param("status") RequestStatus status);
}
