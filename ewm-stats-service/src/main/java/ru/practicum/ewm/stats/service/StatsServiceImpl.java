package ru.practicum.ewm.stats.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.exception.BadRequestException;
import ru.practicum.ewm.stats.mapper.EndpointHitMapper;
import ru.practicum.ewm.stats.repository.EndpointHitRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final EndpointHitRepository endpointHitRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto endpointHitDto) {
        endpointHitRepository.save(EndpointHitMapper.toEntity(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start date must be before or equal to end date.");
        }

        boolean uniqueOnly = Boolean.TRUE.equals(unique);
        boolean filterByUris = uris != null && !uris.isEmpty();

        if (uniqueOnly && filterByUris) {
            return endpointHitRepository.findUniqueStatsByUris(start, end, uris);
        }
        if (uniqueOnly) {
            return endpointHitRepository.findUniqueStats(start, end);
        }
        if (filterByUris) {
            return endpointHitRepository.findStatsByUris(start, end, uris);
        }
        return endpointHitRepository.findStats(start, end);
    }
}
