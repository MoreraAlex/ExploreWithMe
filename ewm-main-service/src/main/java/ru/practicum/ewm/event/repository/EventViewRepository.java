package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.EventView;

public interface EventViewRepository extends JpaRepository<EventView, Long> {

    boolean existsByEventIdAndIp(Long eventId, String ip);

    long countByEventId(Long eventId);
}
