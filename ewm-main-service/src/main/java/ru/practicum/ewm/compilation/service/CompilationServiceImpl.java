package ru.practicum.ewm.compilation.service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.common.Validation;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventRatingDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventReactionRepository;
import ru.practicum.ewm.event.repository.EventReactionStats;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.service.ParticipationRequestService;
import ru.practicum.ewm.stats.StatsFacade;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventReactionRepository eventReactionRepository;
    private final ParticipationRequestService requestService;
    private final StatsFacade statsFacade;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.toEntity(dto, loadEvents(dto.getEvents()));
        return toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = getCompilation(compId);
        if (dto.getEvents() != null) {
            compilation.setEvents(loadEvents(dto.getEvents()));
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        return toDto(compilation);
    }

    @Override
    public List<CompilationDto> get(Boolean pinned, int from, int size) {
        Validation.checkPage(from, size);
        Pageable pageable = new OffsetPageRequest(from, size);
        List<Compilation> compilations = pinned == null
                ? compilationRepository.findAllWithEvents(pageable)
                : compilationRepository.findByPinned(pinned, pageable);
        return compilations.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CompilationDto getById(Long compId) {
        return toDto(getCompilation(compId));
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findWithEventsById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private Set<Event> loadEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(eventRepository.findAllById(eventIds));
    }

    private CompilationDto toDto(Compilation compilation) {
        List<Event> events = List.copyOf(compilation.getEvents());
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> confirmed = requestService.getConfirmedCounts(eventIds);
        Map<String, Long> views = statsFacade.getViews(eventIds.stream()
                .map(StatsFacade.eventUri())
                .toList());
        Map<Long, EventRatingDto> ratings = ratingByEventIds(eventIds);
        return CompilationMapper.toDto(compilation, events.stream()
                .map(event -> EventMapper.toShortDto(
                        event,
                        confirmed.getOrDefault(event.getId(), 0L),
                        Math.max(
                                event.getViews() == null ? 0L : event.getViews(),
                                views.getOrDefault(StatsFacade.eventUri().apply(event.getId()), 0L)
                        ),
                        ratings.get(event.getId())
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private Map<Long, EventRatingDto> ratingByEventIds(List<Long> eventIds) {
        Map<Long, EventRatingDto> ratings = new HashMap<>();
        eventIds.forEach(eventId -> ratings.put(eventId, EventRatingDto.builder()
                .eventId(eventId)
                .likes(0L)
                .dislikes(0L)
                .rating(0L)
                .build()));
        if (eventIds.isEmpty()) {
            return ratings;
        }
        for (EventReactionStats stats : eventReactionRepository.findStatsByEventIds(eventIds)) {
            long likes = stats.getLikes() == null ? 0L : stats.getLikes();
            long dislikes = stats.getDislikes() == null ? 0L : stats.getDislikes();
            ratings.put(stats.getEventId(), EventRatingDto.builder()
                    .eventId(stats.getEventId())
                    .likes(likes)
                    .dislikes(dislikes)
                    .rating(likes - dislikes)
                    .build());
        }
        return ratings;
    }
}
