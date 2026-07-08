package ru.practicum.ewm.request.service;

import java.util.List;
import java.util.Map;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

public interface ParticipationRequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto create(Long userId, Long eventId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long userId,
                                                       Long eventId,
                                                       EventRequestStatusUpdateRequest request);

    Map<Long, Long> getConfirmedCounts(List<Long> eventIds);
}
