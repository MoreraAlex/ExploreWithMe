package ru.practicum.ewm.event.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.EventReaction;

public interface EventReactionRepository extends JpaRepository<EventReaction, Long> {

    Optional<EventReaction> findByEventIdAndUserId(Long eventId, Long userId);

    void deleteByEventIdAndUserId(Long eventId, Long userId);

    @Modifying
    @Query(value = """
            insert into event_reactions (event_id, user_id, reaction_type, created, updated)
            values (:eventId, :userId, :reactionType, :now, :now)
            on conflict (event_id, user_id)
            do update set reaction_type = excluded.reaction_type,
                          updated = case
                              when event_reactions.reaction_type = excluded.reaction_type
                              then event_reactions.updated
                              else excluded.updated
                          end
            """, nativeQuery = true)
    void upsert(@Param("eventId") Long eventId,
                @Param("userId") Long userId,
                @Param("reactionType") String reactionType,
                @Param("now") LocalDateTime now);

    @Query("""
            select reaction.event.id as eventId,
                   sum(case when reaction.reactionType = ru.practicum.ewm.event.model.ReactionType.LIKE
                            then 1 else 0 end) as likes,
                   sum(case when reaction.reactionType = ru.practicum.ewm.event.model.ReactionType.DISLIKE
                            then 1 else 0 end) as dislikes
            from EventReaction reaction
            where reaction.event.id in :eventIds
            group by reaction.event.id
            """)
    List<EventReactionStats> findStatsByEventIds(@Param("eventIds") Collection<Long> eventIds);
}
