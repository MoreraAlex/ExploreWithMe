package ru.practicum.ewm.request.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        getUser(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User requester = getUser(userId);
        Event event = getEvent(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in their own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        RequestStatus status = event.getParticipantLimit() == 0 || !event.getRequestModeration()
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;
        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();
        return ParticipationRequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        getUser(userId);
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }
        request.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        getOwnerEvent(userId, eventId);
        return requestRepository.findByEventIdAndEventInitiatorId(eventId, userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        Event event = getOwnerEvent(userId, eventId);
        List<Long> requestIds = request.getRequestIds() == null ? List.of() : request.getRequestIds();
        List<ParticipationRequest> requests = requestRepository.findByIdInAndEventIdAndEventInitiatorId(
                requestIds,
                eventId,
                userId
        );

        if (requests.stream().anyMatch(participationRequest -> participationRequest.getStatus() != RequestStatus.PENDING)) {
            throw new ConflictException("Request must have status PENDING");
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            confirmRequests(event, requests);
        } else if (request.getStatus() == RequestStatus.REJECTED) {
            requests.forEach(participationRequest -> participationRequest.setStatus(RequestStatus.REJECTED));
        } else {
            throw new ConflictException("Unsupported request status: " + request.getStatus());
        }

        List<ParticipationRequestDto> confirmed = requests.stream()
                .filter(participationRequest -> participationRequest.getStatus() == RequestStatus.CONFIRMED)
                .map(ParticipationRequestMapper::toDto)
                .toList();
        List<ParticipationRequestDto> rejected = requests.stream()
                .filter(participationRequest -> participationRequest.getStatus() == RequestStatus.REJECTED)
                .map(ParticipationRequestMapper::toDto)
                .toList();
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    @Override
    public Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    private void confirmRequests(Event event, List<ParticipationRequest> requests) {
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            requests.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
            return;
        }

        long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        for (ParticipationRequest request : requests) {
            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached");
            }
            request.setStatus(RequestStatus.CONFIRMED);
            confirmed++;
        }
        if (confirmed >= event.getParticipantLimit()) {
            requestRepository.updateStatusByEventIdAndStatus(
                    event.getId(),
                    RequestStatus.PENDING,
                    RequestStatus.REJECTED
            );
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findWithCategoryAndInitiatorById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event getOwnerEvent(Long userId, Long eventId) {
        return eventRepository.findWithCategoryAndInitiatorByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
