package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.common.Validation;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.AdminStateAction;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.UserStateAction;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.EventSpecifications;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.service.ParticipationRequestService;
import ru.practicum.ewm.stats.StatsFacade;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestService requestService;
    private final StatsFacade statsFacade;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Validation.checkPage(from, size);
        getUser(userId);
        Specification<Event> specification = EventSpecifications.fetchRelations()
                .and(EventSpecifications.initiatorIn(List.of(userId)));
        return toShortDtos(eventRepository.findAll(specification, new OffsetPageRequest(from, size)).getContent());
    }

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException("Field: eventDate. Error: must be at least two hours later");
        }
        User initiator = getUser(userId);
        Category category = getCategory(dto.getCategory());
        Event event = EventMapper.toEntity(dto, initiator, category);
        event.setCreatedOn(LocalDateTime.now());
        Event saved = eventRepository.save(event);
        return toFullDto(saved);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        getUser(userId);
        Event event = eventRepository.findWithCategoryAndInitiatorByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        return toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        getUser(userId);
        Event event = eventRepository.findWithCategoryAndInitiatorByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (event.getState() == EventState.PUBLISHED) {
            throw new ForbiddenException("Only pending or canceled events can be changed");
        }
        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Field: eventDate. Error: must be at least two hours later");
        }
        applyUserUpdate(event, dto);
        return toFullDto(event);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users,
                                             List<EventState> states,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             int from,
                                             int size) {
        Validation.checkPage(from, size);
        validateRange(rangeStart, rangeEnd);
        Specification<Event> specification = EventSpecifications.fetchRelations()
                .and(EventSpecifications.initiatorIn(users))
                .and(EventSpecifications.statesIn(states))
                .and(EventSpecifications.categoriesIn(categories))
                .and(EventSpecifications.eventDateBetween(rangeStart, rangeEnd));
        return toFullDtos(eventRepository.findAll(specification, new OffsetPageRequest(from, size)).getContent());
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findWithCategoryAndInitiatorById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        applyAdminUpdate(event, dto);
        return toFullDto(event);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               EventSort sort,
                                               int from,
                                               int size,
                                               HttpServletRequest request) {
        Validation.checkPage(from, size);
        validateRange(rangeStart, rangeEnd);
        LocalDateTime start = rangeStart == null && rangeEnd == null ? LocalDateTime.now() : rangeStart;
        Specification<Event> specification = EventSpecifications.fetchRelations()
                .and(EventSpecifications.state(EventState.PUBLISHED))
                .and(EventSpecifications.text(text))
                .and(EventSpecifications.categoriesIn(categories))
                .and(EventSpecifications.paid(paid))
                .and(EventSpecifications.eventDateBetween(start, rangeEnd));
        Sort dbSort = sort == EventSort.EVENT_DATE ? Sort.by("eventDate").ascending() : Sort.unsorted();
        List<Event> events = eventRepository.findAll(specification, new OffsetPageRequest(from, size, dbSort)).getContent();
        List<EventShortDto> dtos = toShortDtos(events);
        if (Boolean.TRUE.equals(onlyAvailable)) {
            dtos = dtos.stream()
                    .filter(dto -> dto.getConfirmedRequests() < findById(events, dto.getId()).getParticipantLimit()
                            || findById(events, dto.getId()).getParticipantLimit() == 0)
                    .toList();
        }
        if (sort == EventSort.VIEWS) {
            dtos = dtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .toList();
        }
        statsFacade.saveHit(request);
        return dtos;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findWithCategoryAndInitiatorByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        statsFacade.saveHit(request);
        return toFullDto(event);
    }

    private void applyUserUpdate(Event event, UpdateEventUserRequest dto) {
        applyCommonUpdate(event, dto.getAnnotation(), dto.getCategory(), dto.getDescription(), dto.getEventDate(),
                dto.getLocation(), dto.getPaid(), dto.getParticipantLimit(), dto.getRequestModeration(), dto.getTitle());
        if (dto.getStateAction() == UserStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        } else if (dto.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }
    }

    private void applyAdminUpdate(Event event, UpdateEventAdminRequest dto) {
        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Field: eventDate. Error: must contain a date that has not yet occurred");
        }
        applyCommonUpdate(event, dto.getAnnotation(), dto.getCategory(), dto.getDescription(), dto.getEventDate(),
                dto.getLocation(), dto.getPaid(), dto.getParticipantLimit(), dto.getRequestModeration(), dto.getTitle());
        if (dto.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ForbiddenException("Cannot publish the event because it's not in the right state: "
                        + event.getState());
            }
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ForbiddenException("Event date must be at least one hour later");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if (dto.getStateAction() == AdminStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ForbiddenException("Cannot reject the event because it's already published");
            }
            event.setState(EventState.CANCELED);
        }
    }

    private void applyCommonUpdate(Event event,
                                   String annotation,
                                   Long categoryId,
                                   String description,
                                   LocalDateTime eventDate,
                                   ru.practicum.ewm.event.dto.LocationDto location,
                                   Boolean paid,
                                   Integer participantLimit,
                                   Boolean requestModeration,
                                   String title) {
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (categoryId != null) {
            event.setCategory(getCategory(categoryId));
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (eventDate != null) {
            event.setEventDate(eventDate);
        }
        if (location != null) {
            event.setLocation(EventMapper.toLocation(location));
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (title != null) {
            event.setTitle(title);
        }
    }

    private List<EventShortDto> toShortDtos(List<Event> events) {
        Map<Long, Long> confirmed = requestService.getConfirmedCounts(ids(events));
        Map<String, Long> views = loadViews(events);
        return events.stream()
                .map(event -> EventMapper.toShortDto(
                        event,
                        confirmed.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(StatsFacade.eventUri().apply(event.getId()), 0L)
                ))
                .toList();
    }

    private List<EventFullDto> toFullDtos(List<Event> events) {
        Map<Long, Long> confirmed = requestService.getConfirmedCounts(ids(events));
        Map<String, Long> views = loadViews(events);
        return events.stream()
                .map(event -> EventMapper.toFullDto(
                        event,
                        confirmed.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(StatsFacade.eventUri().apply(event.getId()), 0L)
                ))
                .toList();
    }

    private EventFullDto toFullDto(Event event) {
        long confirmed = requestService.getConfirmedCounts(List.of(event.getId())).getOrDefault(event.getId(), 0L);
        long views = statsFacade.getViews(StatsFacade.eventUri().apply(event.getId()));
        return EventMapper.toFullDto(event, confirmed, views);
    }

    private Map<String, Long> loadViews(List<Event> events) {
        return statsFacade.getViews(ids(events).stream()
                .map(StatsFacade.eventUri())
                .toList());
    }

    private List<Long> ids(List<Event> events) {
        return events.stream()
                .map(Event::getId)
                .toList();
    }

    private Event findById(List<Event> events, Long id) {
        return events.stream()
                .filter(event -> event.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Range start must be before range end");
        }
    }
}
